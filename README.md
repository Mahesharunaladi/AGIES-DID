# AGIES-DID

The Java backend gateway now lives in [`backend/`](backend/).

Run it from the backend directory:

```bash
cd backend
cp .env.example .env
docker compose --env-file .env -f compose.yml up --build
```

Open:

- Gateway: `http://localhost:8000`
- API docs: `http://localhost:8000/docs`
- Parseable: `http://localhost:8081`
- Grafana: `http://localhost:3000`
