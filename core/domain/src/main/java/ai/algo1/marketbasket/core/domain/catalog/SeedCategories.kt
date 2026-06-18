package ai.algo1.marketbasket.core.domain.catalog

import ai.algo1.marketbasket.core.domain.model.Category

/** 1:1 transcription of the seed list in src/data/index.ts (items start empty). */
object SeedCategories {
    val all: List<Category> = listOf(
        Category(id = "produce", name = "Produce", color = "#4ecb71", colorDark = "#3ba85c"),
        Category(id = "snacks", name = "Snacks", color = "#e74c3c", colorDark = "#c0392b"),
        Category(id = "dairy", name = "Dairy", color = "#3498db", colorDark = "#2980b9"),
        Category(id = "bakery", name = "Bakery", color = "#e67e22", colorDark = "#d35400"),
        Category(id = "beverages", name = "Beverages", color = "#9b59b6", colorDark = "#8e44ad"),
        Category(id = "frozen", name = "Frozen", color = "#1abc9c", colorDark = "#16a085"),
    )
}
