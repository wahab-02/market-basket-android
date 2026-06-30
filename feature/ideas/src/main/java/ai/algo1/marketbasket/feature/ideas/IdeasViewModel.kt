package ai.algo1.marketbasket.feature.ideas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.catalog.CategoryResolver
import ai.algo1.marketbasket.core.domain.model.Category
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IdeasUiState(
    val currentPageIndex: Int = 0,
    val videoActions: Map<String, VideoActionsState> = emptyMap(),
    val shoppingCategories: List<Category> = emptyList(),
    val selectedRecipeForList: Recipe? = null,
    val isAddingToList: Boolean = false,
)

@HiltViewModel
class IdeasViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val listRepository: ListRepository,
) : ViewModel() {

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        repeatMode = ExoPlayer.REPEAT_MODE_ONE
        volume = 0f
        playWhenReady = true
        val firstVideo = recipeVideos.firstOrNull()
        if (firstVideo != null) {
            setMediaItem(MediaItem.fromUri(firstVideo.uri))
            prepare()
            play()
        }
    }

    private val _uiState = MutableStateFlow(IdeasUiState())
    val uiState: StateFlow<IdeasUiState> = _uiState.asStateFlow()

    init {
        listRepository.categories
            .onEach { categories ->
                _uiState.update { it.copy(shoppingCategories = categories) }
            }
            .launchIn(viewModelScope)
    }

    fun onPageSettled(index: Int) {
        val video = recipeVideos.getOrNull(index) ?: return
        _uiState.update { it.copy(currentPageIndex = index) }
        player.setMediaItem(MediaItem.fromUri(video.uri))
        player.prepare()
        player.play()
    }

    fun onFavoriteToggle(videoId: String) {
        _uiState.update { state ->
            val current = state.videoActions[videoId] ?: VideoActionsState()
            state.copy(
                videoActions = state.videoActions + (videoId to current.copy(favorited = !current.favorited)),
            )
        }
    }

    fun onBookmarkToggle(videoId: String) {
        _uiState.update { state ->
            val current = state.videoActions[videoId] ?: VideoActionsState()
            state.copy(
                videoActions = state.videoActions + (videoId to current.copy(bookmarked = !current.bookmarked)),
            )
        }
    }

    fun onAddToListPress(recipe: Recipe) {
        _uiState.update { it.copy(selectedRecipeForList = recipe) }
    }

    fun onAddToListDismiss() {
        _uiState.update { state ->
            val recipeId = state.selectedRecipeForList?.id
            val current = recipeId?.let { state.videoActions[it] }
            state.copy(
                videoActions = if (recipeId != null && current != null) {
                    state.videoActions + (recipeId to current.copy(addedToList = false))
                } else {
                    state.videoActions
                },
                selectedRecipeForList = null,
                isAddingToList = false,
            )
        }
    }

    fun onAddIngredientsConfirmed(videoId: String) {
        val recipe = _uiState.value.selectedRecipeForList
            ?: recipeVideos.firstOrNull { it.id == videoId }?.recipe
            ?: return

        _uiState.update { it.copy(isAddingToList = true) }

        viewModelScope.launch {
            val ingredients = prepareVideoIngredientsForList(recipe.ingredients)
            var addedSuccessfully = false

            try {
                ingredients.forEach { ingredient ->
                    listRepository.addOrIncrement(
                        name = ingredient,
                        categoryId = CategoryResolver.resolveCategoryFromText(
                            rawCategory = null,
                            productName = ingredient,
                        ),
                        source = "video",
                    )
                }
                addedSuccessfully = true
            } catch (_: Exception) {
                addedSuccessfully = false
            } finally {
                _uiState.update { state ->
                    val current = state.videoActions[videoId] ?: VideoActionsState()
                    state.copy(
                        videoActions = if (addedSuccessfully) {
                            state.videoActions + (videoId to current.copy(addedToList = false))
                        } else {
                            state.videoActions
                        },
                        selectedRecipeForList = if (addedSuccessfully) null else state.selectedRecipeForList,
                        isAddingToList = false,
                    )
                }
            }
        }
    }

    fun onAddRecipeIngredient(recipeId: String, ingredient: RecipeIngredient) {
        val recipe = recipeCards.firstOrNull { it.id == recipeId } ?: return
        val isMissing = recipe.copy(ingredients = listOf(ingredient))
            .missingIngredientsFor(_uiState.value.shoppingCategories)
            .isNotEmpty()
        if (!isMissing) return

        viewModelScope.launch {
            addRecipeIngredientToList(ingredient)
        }
    }

    fun onAddMissingRecipeIngredients(recipeId: String) {
        val recipe = recipeCards.firstOrNull { it.id == recipeId } ?: return
        val missingIngredients = recipe.missingIngredientsFor(_uiState.value.shoppingCategories)
        if (missingIngredients.isEmpty()) return

        viewModelScope.launch {
            missingIngredients.forEach { ingredient ->
                addRecipeIngredientToList(ingredient)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    private suspend fun addRecipeIngredientToList(ingredient: RecipeIngredient) {
        listRepository.addOrIncrement(
            name = ingredient.name,
            categoryId = CategoryResolver.resolveCategoryFromText(
                rawCategory = null,
                productName = ingredient.name,
            ),
            source = "recipe",
        )
    }
}
