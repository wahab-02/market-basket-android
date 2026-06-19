package ai.algo1.marketbasket.feature.list

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.remote.ListChange
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
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

class ListViewModelTest {
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

    private suspend fun loadedRepo(remote: RemoteListDataSource): ListRepository =
        ListRepository(remote).apply { load("pub1") }

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())   // viewModelScope uses Dispatchers.Main
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test fun addByName_resolvesCategory_andInserts() = runTest(UnconfinedTestDispatcher()) {
        val remote = FakeRemote()
        val vm = ListViewModel(loadedRepo(remote))
        backgroundScope.launch { vm.uiState.collect {} }   // activate WhileSubscribed before reading .value
        vm.addByName("Fresh Whole Milk")   // CategoryResolver keyword -> "dairy"
        assertEquals(1, remote.inserts.size)
        assertEquals("dairy", remote.inserts.single().categoryId)
        assertEquals("search", remote.inserts.single().source)
        assertTrue(vm.uiState.value.categories.flatMap { it.items }.any { it.name == "Fresh Whole Milk" })
    }

    @Test fun blankName_doesNothing() = runTest(UnconfinedTestDispatcher()) {
        val remote = FakeRemote()
        val vm = ListViewModel(loadedRepo(remote))
        vm.addByName("   ")
        assertEquals(0, remote.inserts.size)
    }
}
