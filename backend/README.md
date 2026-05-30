# AGIES-DID

Java backend API gateway for the AGIES DID trust plane.

## What This Builds

- **Gateway:** Spring Boot runs on port `8000` and exposes OpenAPI at `/docs`.
- **CORS:** accepted origins come from `FRONTEND_ORIGINS`.
- **Identity plane:** SPIRE server/agent configs are included under the `identity` Compose profile.
- **Telemetry:** Fluent Bit ships logs into Parseable on host port `8081`; Grafana runs on host port `3000`.
- **AI analytics:** Java service interfaces cover prompt embeddings, trust scoring, and Neo4j MetaGraph state. The current embedding/trust implementation is deterministic for dev, so the gateway boots before a model artifact is available.
- **Policy:** OPA applies ABAC-style restrictions as trust drops.
- **Runtime:** Docker Compose is the local orchestrator. Kubernetes NetworkPolicies are provided in `infra/k8s` for cluster containment.

## Required Environment

Copy the example env file and fill in real secrets:

```bash
cp .env.example .env
```

Required variables:

- `SPIRE_JOIN_TOKEN`
- `PARSEABLE_USERNAME`
- `PARSEABLE_PASSWORD`
- `PARSEABLE_BASIC_AUTH`
- `GRAFANA_ADMIN_PASSWORD`

Optional:

- `FRONTEND_ORIGINS`
- `NEO4J_PASSWORD`

## Run

```bash
docker compose --env-file .env -f compose.yml up --build
```

Open:

- Gateway: `http://localhost:8000`
- API docs: `http://localhost:8000/docs`
- Parseable: `http://localhost:8081`
- Grafana: `http://localhost:3000`

Demo workload:

```bash
docker compose --env-file .env -f compose.yml --profile demo up --build
```

SPIRE identity services:

```bash
docker compose --env-file .env -f compose.yml --profile identity up --build
```

Kernel telemetry via Tetragon needs host privileges:

```bash
docker compose --env-file .env -f compose.yml --profile kernel-telemetry up --build
```

## Gateway Endpoints

- `POST /api/v1/trust/evaluate`
- `POST /api/v1/policy/decision`
- `GET /api/v1/agents/{agentId}/state`
- `POST /api/v1/revocations`
- `GET /actuator/health`

Example:

```bash
curl -X POST http://localhost:8000/api/v1/trust/evaluate \
  -H 'Content-Type: application/json' \
  -d '{
    "agentId": "dev-ai-agent",
    "prompt": "summarize audit records",
    "requestedTools": ["audit-read", "ledger-read"],
    "context": {"mode": "demo"}
  }'
```

## Java AI Path

The Python components from the original architecture map to Java this way:

- Sentence Transformers `all-MiniLM-L6-v2`: plug in DJL or ONNX Runtime behind `EmbeddingService`.
- PyTorch Neural Granger Causality: replace `TrustScoringService` with a Java model adapter or a sidecar model endpoint.
- Neo4j StateGraph: already wired through the official Neo4j Java driver.

The deterministic implementation is intentionally small and boot-friendly; it gives the gateway stable behavior while the trained model and embedding runtime are still being integrated.
