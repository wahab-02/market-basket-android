package ai.algo1.marketbasket.feature.onboarding

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.remote.ListChange
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModelTest {
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class FakeRemote(private val user: AppUserDto?) : RemoteListDataSource {
        override suspend fun loadUser(publicId: String): AppUserDto? = user?.copy(publicId = publicId)
        override suspend fun loadItems(userId: String): List<GroceryItem> = emptyList()
        override suspend fun insertItem(userId: String, item: GroceryItem) {}
        override suspend fun updateItem(id: String, item: GroceryItem) {}
        override suspend fun setChecked(id: String, checked: Boolean) {}
        override suspend fun deleteItem(id: String) {}
        override fun changes(userId: String): Flow<ListChange> = emptyFlow()
    }

    @Test fun uiState_reflectsLoadedProfileAndBuildsQrUrl() = runTest {
        val repo = ListRepository(
            FakeRemote(AppUserDto(id = "o", displayName = "Sam", phoneNumber = "1555", listCode = "ABC123"))
        )
        repo.load("pid123")
        val vm = ConnectionViewModel(repo)
        vm.uiState.test {
            // Drop the initial default (if seen before the upstream computes), then read the populated state.
            var state = awaitItem()
            if (state.qrUrl == null) state = awaitItem()
            assertEquals("Hi Sam", state.greeting)
            assertTrue(state.phoneConnected)
            assertEquals("ABC123", state.listCode)
            assertTrue(state.qrUrl!!.endsWith("/?u=pid123"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun greeting_fallsBackToShopper_whenNoDisplayName() = runTest {
        val repo = ListRepository(FakeRemote(AppUserDto(id = "o")))
        repo.load("pidZ")
        val vm = ConnectionViewModel(repo)
        vm.uiState.test {
            var state = awaitItem()
            if (state.qrUrl == null) state = awaitItem()
            assertEquals("Hi Shopper!", state.greeting)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
