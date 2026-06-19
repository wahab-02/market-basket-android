package ai.algo1.marketbasket.feature.deals

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.remote.ListChange
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.deals.Deal
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import kotlinx.coroutines.Dispatchers
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

class DealsViewModelTest {
    private class FakeRemote : RemoteListDataSource {
        val inserts = mutableListOf<GroceryItem>()
        override suspend fun loadUser(publicId: String) = AppUserDto(id = "owner", publicId = publicId)
        override suspend fun loadItems(userId: String) = emptyList<GroceryItem>()
        override suspend fun insertItem(userId: String, item: GroceryItem) { inserts += item }
        override suspend fun updateItem(id: String, item: GroceryItem) {}
        override suspend fun setChecked(id: String, checked: Boolean) {}
        override suspend fun deleteItem(id: String) {}
        override fun changes(userId: String): Flow<ListChange> = emptyFlow()
    }

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun sectionsArePopulated() = runTest(UnconfinedTestDispatcher()) {
        val repo = ListRepository(FakeRemote()).apply { load("p") }
        val vm = DealsViewModel(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        assertTrue(vm.uiState.value.sections.isNotEmpty())
    }

    @Test fun addToList_resolvesCategory_andAddsAsPromo() = runTest(UnconfinedTestDispatcher()) {
        val remote = FakeRemote()
        val vm = DealsViewModel(ListRepository(remote).apply { load("p") })
        vm.addToList(Deal(itemName = "Whole Milk", price = "$3", description = "", savings = "", category = "For You"))
        assertEquals(1, remote.inserts.size)
        assertEquals("dairy", remote.inserts.single().categoryId)   // generic -> keyword "milk"
        assertEquals("promo", remote.inserts.single().source)
    }
}
