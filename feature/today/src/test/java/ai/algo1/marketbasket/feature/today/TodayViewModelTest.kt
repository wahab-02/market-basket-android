package ai.algo1.marketbasket.feature.today

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.remote.ListChange
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
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
class TodayViewModelTest {
    private class FakeRemote(private val initial: List<GroceryItem> = emptyList()) : RemoteListDataSource {
        val inserts = mutableListOf<GroceryItem>()
        override suspend fun loadUser(publicId: String) = AppUserDto(id = "owner", publicId = publicId)
        override suspend fun loadItems(userId: String) = initial
        override suspend fun insertItem(userId: String, item: GroceryItem) { inserts += item }
        override suspend fun updateItem(id: String, item: GroceryItem) {}
        override suspend fun setChecked(id: String, checked: Boolean) {}
        override suspend fun deleteItem(id: String) {}
        override fun changes(userId: String): Flow<ListChange> = emptyFlow()
    }

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun savingsAndSaleCount_fromForYouSubset() = runTest(UnconfinedTestDispatcher()) {
        val vm = TodayViewModel(ListRepository(FakeRemote()).apply { load("p") })
        backgroundScope.launch { vm.uiState.collect {} }
        assertEquals("\$7.26", vm.uiState.value.savingsTotal)
        assertEquals(3, vm.uiState.value.saleCount)
        assertEquals("3 items on sale", vm.uiState.value.saleCountLabel)
    }

    @Test fun metrics_reflectSeededList() = runTest(UnconfinedTestDispatcher()) {
        val items = listOf(
            GroceryItem(id = "a", name = "Milk", categoryId = "dairy"),
            GroceryItem(id = "b", name = "Bread", categoryId = "bakery"),
            GroceryItem(id = "c", name = "Eggs", categoryId = "dairy"),
        )
        val vm = TodayViewModel(ListRepository(FakeRemote(items)).apply { load("p") })
        backgroundScope.launch { vm.uiState.collect {} }
        assertEquals(3, vm.uiState.value.itemCount)
        assertEquals(2, vm.uiState.value.categoryCount) // dairy + bakery
        // 3 items: 2.25 + (2-1)*2 = 4.25 -> ceil = 5
        assertEquals(5, vm.uiState.value.tripMinutes)
        assertTrue("milk" in vm.uiState.value.addedItemNames)
    }

    @Test fun staticData_isExposed() = runTest(UnconfinedTestDispatcher()) {
        val vm = TodayViewModel(ListRepository(FakeRemote()).apply { load("p") })
        backgroundScope.launch { vm.uiState.collect {} }
        assertEquals(5, vm.uiState.value.smartPicks.size)
        assertEquals(6, vm.uiState.value.communityLists.size)
        assertEquals("Johnny the protein guy", vm.uiState.value.communityLists.first().title)
    }

    @Test fun addCommunityItems_resolvesCategory_andAddsAsCommunity() = runTest(UnconfinedTestDispatcher()) {
        val remote = FakeRemote()
        val vm = TodayViewModel(ListRepository(remote).apply { load("p") })
        vm.addCommunityItems(listOf("Whole Milk", "Zzzqxx Widget"))
        assertEquals(2, remote.inserts.size)
        assertTrue(remote.inserts.all { it.source == "community" })
        assertEquals("dairy", remote.inserts.first { it.name == "Whole Milk" }.categoryId) // "milk" keyword
        assertEquals("produce", remote.inserts.first { it.name == "Zzzqxx Widget" }.categoryId) // fallback
    }
}
