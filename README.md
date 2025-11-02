# Workorder Quarkus + PostgreSQL REST API

Projeto de exemplo implementando uma API REST para gerenciar Work Orders.

## Visão geral da arquitetura

- Aplicação: Java (Quarkus)
- Padrão: API REST (JAX-RS / Jakarta RESTful Web Services)
- Persistência: Hibernate ORM com PostgreSQL
- Migrations: Liquibase (scripts em `src/main/resources/db/changelog`)
- Cliente REST para serviço de Customer: MicroProfile Rest Client (`CustomerClient`)
- Containerização: Docker (há `Dockerfile.native` para imagem nativa)
- Orquestração: manifests Kubernetes em `k8s/`

Componentes principais
- `src/main/java/com/labsafer/workorder/web/WorkOrderResource.java` — controller REST para Work Orders
- `src/main/java/com/labsafer/workorder/application/services/WorkOrderServiceImpl.java` — lógica de negócio
- `src/main/java/com/labsafer/workorder/infrastructure/client/CustomerClient.java` — cliente REST para obter dados de customers
- `src/main/resources/application.properties` — configurações (porta, datasource, cliente REST)

## Frameworks e práticas utilizadas

- Quarkus (runtime e devtools)
- Jakarta EE / JAX-RS para endpoints
- MicroProfile Rest Client para comunicação com serviço de Customer
- Hibernate ORM (JPA) para mapeamento objeto-relacional
- Liquibase para versionamento de schema
- Jackson para serialização/deserialização JSON
- Validação com Jakarta Validation (Bean Validation)
- Testes: JUnit (tests sob `src/test/java`)
- Boas práticas: configuração via variáveis de ambiente, separação de camadas (web/application/infrastructure/domain), uso de DTOs, tratamento centralizado de exceções

## Endpoints principais

- POST /api/v1/work-orders
	- Cria uma Work Order
	- Payload: { "customerId": "<uuid>", "title": "...", "description": "..." }
	- Resposta: 201 Created com o objeto criado
- GET /api/v1/work-orders
	- Lista work orders (paginação via query params `page` e `size`)
- GET /api/v1/work-orders/{id}
	- Busca por id
- PUT /api/v1/work-orders/{id}
	- Atualiza title/description e opcionalmente o status (query param `status`)
- DELETE /api/v1/work-orders/{id}
	- Remove a work order

O cliente de customer espera o endpoint `/api/v1/customers/{id}` no serviço de customers.

## Configuração (variáveis relevantes)

As propriedades estão em `src/main/resources/application.properties` e podem ser sobrescritas por variáveis de ambiente.

- `quarkus.http.port` — porta HTTP (padrão 8080)
- `quarkus.datasource.jdbc.url`, `quarkus.datasource.username`, `quarkus.datasource.password` — configuração do PostgreSQL
- `customer.base-url` — URL base do serviço de Customer (usado pelo MicroProfile Rest Client)

Exemplo mínimo de env vars para desenvolvimento:

```bash
export HTTP_PORT=8080
export DB_JDBC_URL=jdbc:postgresql://localhost:5432/workorder_db
export DB_USERNAME=workorder
export DB_PASSWORD=workorder
export CUSTOMER_BASE_URL=http://localhost:8081
```

## Build e execução

Pré-requisitos: JDK 17+, Maven 3.8+, Docker (opcional), PostgreSQL.

1) Build com Maven (jar/runner):

```bash
mvn clean package -DskipTests
```

2) Rodar em modo dev (Quarkus dev):

```bash
mvn quarkus:dev
```

3) Rodar o artefato empacotado (ex.: runner no target):

```bash
java -jar target/workorder-quarkus-pg-rest-api-1.0.0-runner.jar
```

4) Build e executar imagem nativa com Docker (apenas se quiser imagem nativa):

```bash
# gera a imagem nativa via Dockerfile.native (depende de GraalVM / build native)
docker build -f Dockerfile.native -t workorder-native:latest .
docker run --rm -p 8080:8080 \ 
	-e DB_JDBC_URL="jdbc:postgresql://postgres:5432/workorder_db" \ 
	workorder-native:latest
```

## Banco de dados e migrations

O projeto usa Liquibase. Os scripts estão em `src/main/resources/db/changelog`.
Ao iniciar, se `quarkus.liquibase.migrate-at-start=true`, as migrations serão aplicadas automaticamente.

## Testes

Executar testes com Maven:

```bash
mvn test
```

## Deploy (Kubernetes)

Existem manifests Kubernetes em `k8s/`:

- `deployment.yaml` — deployment do aplicativo
- `service-account.yaml`, `service.yaml`, `ingress.yaml`, `namespace.yaml`, `secret.yaml`, `configmap.yaml`

Para aplicar em cluster:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
# e ingress se necessário
kubectl apply -f k8s/ingress.yaml
```

A configuração do cliente de Customer é feita via ConfigMap/variáveis de ambiente (`customer.base-url`).

## Geração de dados de exemplo

Um script utilitário `generate_workorders.sh` foi adicionado na raiz para gerar workorders automaticamente utilizando a própria API:

- Ele busca até `TOTAL_USERS` customers do serviço de customers e cria `WORKORDERS_PER_USER` workorders por customer.
- Requisitos: `curl`, `jq`.
- Exemplo de uso:

```bash
chmod +x generate_workorders.sh
CUSTOMER_BASE_URL=http://localhost:8081 API_BASE_URL=http://localhost:8080 ./generate_workorders.sh
```

Parâmetros configuráveis via variáveis de ambiente: `API_BASE_URL`, `CUSTOMER_BASE_URL`, `TOTAL_USERS`, `WORKORDERS_PER_USER`, `TIMEOUT`.

## Observações e boas práticas

- Mantenha as credenciais (DB_PASSWORD, tokens) em Secrets no cluster.
- Use health checks e readiness probes no Kubernetes para controlar deploys.
- Ajuste pool de conexões do Hibernate conforme carga esperada.
- Se precisar de autenticação, adicione um filtro/JWT e propague tokens nas chamadas para o serviço de Customer.

### Secret de Kubernetes (não versionar)

Por segurança, o arquivo que contém credenciais sensíveis para o cluster Kubernetes (por exemplo `k8s/secret.yaml`) não deve ser versionado com credenciais reais. Em repositórios públicos ou compartilhados, nunca comite senhas, tokens ou certificados.

Crie o secret localmente antes de aplicar os manifests. Exemplo de conteúdo (arquivo de exemplo, NÃO use credenciais reais no repositório):

```yaml
apiVersion: v1
kind: Secret
metadata:
	name: workorder-api-secret
	namespace: workorder-api
type: Opaque
stringData:
	DB_USERNAME: "workorder"
	DB_PASSWORD: "workorder"
```

Você pode aplicar esse arquivo com:

```bash
# aplica o secret (arquivo local com valores de teste/placeholder)
kubectl apply -f k8s/secret.yaml
```

Ou criar o secret diretamente a partir de literais (útil em CI/CD ou scripts de deploy):

```bash
kubectl create secret generic workorder-api-secret \
	--namespace workorder-api \
	--from-literal=DB_USERNAME=workorder \
	--from-literal=DB_PASSWORD=workorder
```

Se usa-se pipelines (CI/CD), armazene credenciais em secrets do provedor (GitHub Secrets, GitLab CI variables, Vault, etc.) e injete no cluster usando ferramentas seguras.

## Contribuindo

1. Abra uma branch para sua feature/fix
2. Adicione/rode testes
3. Abra um Pull Request descrevendo a mudança

---

Se quiser, posso:
- adicionar exemplos de requests curl para cada endpoint,
- incluir instruções detalhadas para CI/CD (GitHub Actions),
- adicionar suporte ao script `generate_workorders.sh` para autenticação (Bearer token).
Basta dizer qual opção prefere.
