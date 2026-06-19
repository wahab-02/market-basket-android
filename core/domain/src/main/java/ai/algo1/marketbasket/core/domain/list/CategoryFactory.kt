package ai.algo1.marketbasket.core.domain.list

import ai.algo1.marketbasket.core.domain.catalog.SeedCategories
import ai.algo1.marketbasket.core.domain.model.Category

/** Builds a Category bucket for an id, mirroring App.tsx's categoryColorForId + name title-casing. */
object CategoryFactory {
    // Default bucket color for ids not in the seed set (App.tsx falls back to a neutral swatch).
    private const val DEFAULT_COLOR = "#9AA0A6"
    private const val DEFAULT_COLOR_DARK = "#80868B"

    fun categoryColorForId(id: String): Pair<String, String> =
        SeedCategories.all.firstOrNull { it.id == id }?.let { it.color to it.colorDark }
            ?: (DEFAULT_COLOR to DEFAULT_COLOR_DARK)

    /** "personal-care" -> "Personal Care" */
    fun titleCase(id: String): String =
        id.replace('-', ' ').split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    fun create(id: String): Category {
        val (color, colorDark) = categoryColorForId(id)
        return Category(id = id, name = titleCase(id), color = color, colorDark = colorDark, items = emptyList())
    }
}
