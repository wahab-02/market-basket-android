package ai.algo1.marketbasket.feature.chat

enum class Role { User, Assistant }

enum class StepStatus { Pending, Running, Done, Error }

data class LiveStep(
    val tool: String,
    val label: String,
    val status: StepStatus,
    val inputSummary: String? = null,
    val outputSummary: String? = null,
)

sealed class Artifact {
    data class Recipe(val data: RecipeData) : Artifact()
}

data class RecipeData(
    val title: String,
    val cuisine: String,
    val servings: Int,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val tags: List<String>,
)

data class RecipeIngredient(val name: String, val quantity: String)

data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val artifacts: List<Artifact>,
    val steps: List<LiveStep>,
    val suggestions: List<String>,
)
