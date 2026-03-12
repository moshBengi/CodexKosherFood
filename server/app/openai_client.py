from openai import AsyncOpenAI

from app.config import Settings
from app.prompting import SYSTEM_PROMPT, build_user_prompt
from app.schemas import AiReviewRequestPayload, OpenAiReviewCollection


class OpenAiReviewService:
    def __init__(self, settings: Settings) -> None:
        self._settings = settings
        self._client = AsyncOpenAI(api_key=settings.openai_api_key)

    async def review_ingredients(
        self,
        payload: AiReviewRequestPayload,
    ) -> OpenAiReviewCollection:
        response = await self._client.responses.parse(
            model=self._settings.openai_model,
            input=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": build_user_prompt(payload)},
            ],
            text_format=OpenAiReviewCollection,
        )

        parsed = response.output_parsed
        if parsed is None:
            raise RuntimeError("OpenAI returned no structured review payload.")
        return parsed
