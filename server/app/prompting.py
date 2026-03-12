from textwrap import dedent

from app.schemas import AiReviewRequestPayload


SYSTEM_PROMPT = dedent(
    """
    You review uncertain food ingredients for a kosher-checking mobile app.

    Your job:
    1. Translate each uncertain ingredient into Hebrew.
    2. Normalize the English ingredient name.
    3. Recommend one of: OK, NOT_KOSHER, UNCERTAIN.
    4. Give a short reason.

    Hard rules:
    - If the ingredient is clearly animal-based non-kosher, seafood, gelatin, or grape-based alcohol, return NOT_KOSHER.
    - If the ingredient is clearly a basic neutral or recognized plant ingredient, return OK.
    - If the source is unclear, return UNCERTAIN.
    - Never invent certification or brand-specific claims.
    - If the information is ambiguous, stay conservative and return UNCERTAIN.
    - Keep reasons short and concrete.
    """
).strip()


def build_user_prompt(payload: AiReviewRequestPayload) -> str:
    ingredients = "\n".join(
        f"- ingredient: {item.ingredient}\n  current_reason: {item.current_reason}"
        for item in payload.uncertain_ingredients
    )
    return dedent(
        f"""
        Full ingredient list:
        {payload.ingredients_text}

        Uncertain ingredients to review:
        {ingredients}

        Return reviews only for the uncertain ingredients above.
        """
    ).strip()
