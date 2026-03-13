from typing import Literal

from pydantic import BaseModel, Field


Literal["רכיב תקין", "רכיב לא כשר", "אין זיהוי ודאי"]


class UncertainIngredientInput(BaseModel):
    original_name: str = Field(min_length=1)
    normalized_name: str = Field(min_length=1)
    current_reason: str = Field(min_length=1)


class AiReviewRequestPayload(BaseModel):
    ingredients_text: str = Field(min_length=1)
    uncertain_ingredients: list[UncertainIngredientInput] = Field(min_length=1)


class AiIngredientReviewPayload(BaseModel):
    original_name: str
    normalized_name: str
    hebrew_translation: str
    recommendation: IngredientStatusLiteral
    reason: str
    confidence: float | None = Field(default=None, ge=0.0, le=1.0)


class AiReviewResponsePayload(BaseModel):
    reviews: list[AiIngredientReviewPayload]
    message: str | None = None


class OpenAiReviewCollection(BaseModel):
    reviews: list[AiIngredientReviewPayload]
    message: str | None = None
