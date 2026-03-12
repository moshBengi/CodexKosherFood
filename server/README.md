# Kosher Food AI Review Server

FastAPI backend for reviewing uncertain ingredients with OpenAI.

## Required environment variables

- `OPENAI_API_KEY`

## Optional environment variables

- `OPENAI_MODEL` default: `gpt-4.1-nano`
- `APP_ENV` default: `development`
- `CORS_ALLOW_ORIGINS` default: `*`

## Run locally

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload
```

## Render

The repository root contains `render.yaml`. Create a new Blueprint deployment in Render and set `OPENAI_API_KEY` in the dashboard.
