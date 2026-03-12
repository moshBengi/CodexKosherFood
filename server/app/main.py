from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.config import Settings, get_settings
from app.openai_client import OpenAiReviewService
from app.schemas import AiReviewRequestPayload, AiReviewResponsePayload


@asynccontextmanager
async def lifespan(app: FastAPI):
    settings = get_settings()
    app.state.settings = settings
    app.state.review_service = OpenAiReviewService(settings) if settings.openai_enabled else None
    yield


app = FastAPI(
    title="Kosher Food AI Review API",
    version="1.0.0",
    lifespan=lifespan,
)


def get_app_settings() -> Settings:
    return get_settings()


def get_review_service() -> OpenAiReviewService:
    service = app.state.review_service
    if service is None:
        raise HTTPException(status_code=503, detail="OPENAI_API_KEY is not configured.")
    return service


app.add_middleware(
    CORSMiddleware,
    allow_origins=get_settings().cors_origins,
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/health")
async def health(settings: Settings = Depends(get_app_settings)) -> dict[str, object]:
    return {
        "status": "ok",
        "environment": settings.app_env,
        "openai_enabled": settings.openai_enabled,
        "model": settings.openai_model,
    }


@app.post("/api/ai/review-ingredients", response_model=AiReviewResponsePayload)
async def review_ingredients(
    payload: AiReviewRequestPayload,
    review_service: OpenAiReviewService = Depends(get_review_service),
) -> AiReviewResponsePayload:
    try:
        result = await review_service.review_ingredients(payload)
    except Exception as exc:  # pragma: no cover
        raise HTTPException(status_code=502, detail=f"OpenAI review failed: {exc}") from exc

    return AiReviewResponsePayload(
        reviews=result.reviews,
        message=result.message,
    )
