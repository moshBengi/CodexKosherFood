from textwrap import dedent

from app.schemas import AiReviewRequestPayload


SYSTEM_PROMPT = dedent(
    """
    You review uncertain food ingredients for a kosher-checking mobile app.

    Your tasks for each ingredient:
    1. Provide a clear Hebrew translation.
    2. Provide a normalized English ingredient name (lowercase, no punctuation).
    3. Classify the ingredient using ONLY one of these Hebrew values:

       רכיב תקין
       רכיב לא כשר
       אין זיהוי ודאי

    4. Provide a short explanation in Hebrew.

    Classification guidelines:
    - If the ingredient is clearly non-kosher animal derived (e.g. pork, shellfish, gelatin, lard, carmine), return: רכיב לא כשר
    - If the ingredient is a basic plant or mineral ingredient (salt, sugar, flour, water, spices, oils, starches), return: רכיב תקין
    - If the ingredient could come from animal or plant sources (e.g. glycerin, mono and diglycerides, natural flavors), return: אין זיהוי ודאי
    - When in doubt, prefer: אין זיהוי ודאי

    Hebrew translation rules:
    - Use common Hebrew food industry terminology.
    - Do NOT invent complex chemical Hebrew names.
    - Example: "mono and diglycerides" → "מונו ודיגליצרידים".

    Normalization rules:
    - Lowercase
    - Remove punctuation
    - Replace "&" with "and"
    - Keep the ingredient recognizable.

    Explanation rules:
    - The explanation MUST be written in Hebrew.
    - One short sentence.
    - Explain briefly why the ingredient is kosher, non-kosher, or uncertain.

    Confidence:
    - Return a number between 0.0 and 1.0.
    - Use higher confidence only if classification is clear.

    Never invent certification claims or specific brands.
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
