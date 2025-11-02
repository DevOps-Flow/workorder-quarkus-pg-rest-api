#!/usr/bin/env bash
set -euo pipefail

# generate_workorders.sh
#
# Gera workorders usando a API deste projeto.
# Requisitos: curl, jq
# Uso (exemplos):
#   CUSTOMER_BASE_URL=http://customer.lab.int API_BASE_URL=http://localhost:8080 ./generate_workorders.sh
#   TOTAL_USERS=50 WORKORDERS_PER_USER=5 ./generate_workorders.sh

API_BASE_URL=${API_BASE_URL:-http://workorder.lab.int:80}
CUSTOMER_BASE_URL=${CUSTOMER_BASE_URL:-http://customer.lab.int}
TOTAL_USERS=${TOTAL_USERS:-100}
WORKORDERS_PER_USER=${WORKORDERS_PER_USER:-10}
TIMEOUT=${TIMEOUT:-10}

command -v curl >/dev/null 2>&1 || { echo "curl is required" >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "jq is required" >&2; exit 1; }

echo "API base: $API_BASE_URL"
echo "Customer base: $CUSTOMER_BASE_URL"
echo "Will fetch $TOTAL_USERS customers and create $WORKORDERS_PER_USER workorders each (total=$(($TOTAL_USERS*$WORKORDERS_PER_USER)))"

fetch_customers() {
  local try endpoints resp ids
  endpoints=( 
    "$CUSTOMER_BASE_URL/api/v1/customers?size=$TOTAL_USERS"
    "$CUSTOMER_BASE_URL/api/v1/customers?page=0&size=$TOTAL_USERS"
    "$CUSTOMER_BASE_URL/api/v1/customers"
  )

  for try in "${endpoints[@]}"; do
    echo "Trying to fetch customers from: $try"
    resp=$(curl -sS -m $TIMEOUT "$try" || true)
    if [[ -z "$resp" ]]; then
      echo "No response from $try"
      continue
    fi

    # Try multiple JSON shapes: array, {content: [...]}, {data: [...]}, {items: [...]}
    ids=$(echo "$resp" | jq -r '
      if type=="array" then .[].id
      elif .content then .content[].id
      elif .data then .data[].id
      elif .items then .items[].id
      elif .customers then .customers[].id
      else empty end' 2>/dev/null || true)

    if [[ -n "$ids" ]]; then
      echo "Found $(echo "$ids" | wc -l) customer ids"
      echo "$ids" | head -n $TOTAL_USERS
      return 0
    fi
  done

  return 1
}

create_workorder() {
  local customer_id=$1
  local title=$2
  local description=$3

  # JSON must match WorkOrderRequest { customerId: UUID, title, description }
  local payload
  payload=$(jq -n --arg cid "$customer_id" --arg title "$title" --arg desc "$description" '{customerId: $cid, title: $title, description: $desc}')

  http_code=$(curl -s -o /tmp/gw_last_body -w "%{http_code}" -X POST "$API_BASE_URL/api/v1/work-orders" \
    -H "Content-Type: application/json" \
    -d "$payload" || true)

  echo "$http_code" >/dev/stderr
  if [[ "$http_code" == "201" || "$http_code" == "200" ]]; then
    return 0
  else
    echo "Failed to create workorder for customer $customer_id (http $http_code) body:" >&2
    cat /tmp/gw_last_body >&2
    return 1
  fi
}

main() {
  mapfile -t customers < <(fetch_customers)
  if [[ ${#customers[@]} -eq 0 ]]; then
    echo "No customers found; aborting." >&2
    exit 2
  fi

  # Trim to TOTAL_USERS
  if [[ ${#customers[@]} -gt $TOTAL_USERS ]]; then
    customers=("${customers[@]:0:$TOTAL_USERS}")
  fi

  total_success=0
  total_fail=0

  i=0
  for cid in "${customers[@]}"; do
    i=$((i+1))
    echo "[${i}/${#customers[@]}] Customer: $cid"
    for n in $(seq 1 $WORKORDERS_PER_USER); do
      # random title
      title="Auto-generated workorder $(date +%s)-$RANDOM"
      description="Created by script generate_workorders.sh"
      if create_workorder "$cid" "$title" "$description"; then
        total_success=$((total_success+1))
      else
        total_fail=$((total_fail+1))
      fi
      # small sleep to avoid hammering
      sleep 0.05
    done
  done

  echo "Done. success=$total_success fail=$total_fail"
}

main
