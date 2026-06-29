package ai.algo1.marketbasket.feature.ideas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class IdeasUiState(
    val currentPageIndex: Int = 0,
    val videoActions: Map<String, VideoActionsState> = emptyMap(),
    val selectedRecipeForList: Recipe? = null,
    val isAddingToList: Boolean = false,
)

@HiltViewModel
class IdeasViewModel @Inject constructor(
    @ApplicationContext context: Context,
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
        _uiState.update { it.copy(selectedRecipeForList = null, isAddingToList = false) }
    }

    fun onAddIngredientsConfirmed(videoId: String) {
        _uiState.update { state ->
            val current = state.videoActions[videoId] ?: VideoActionsState()
            state.copy(
                videoActions = state.videoActions + (videoId to current.copy(addedToList = true)),
                selectedRecipeForList = null,
                isAddingToList = false,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
