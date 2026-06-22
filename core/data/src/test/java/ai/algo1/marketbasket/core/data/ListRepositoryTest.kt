package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.remote.ListChange
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import app.cash.turbine.test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ListRepositoryTest {
    private class FakeRemote(
        private val initial: List<GroceryItem>,
        private val user: AppUserDto? = AppUserDto(id = "owner"),
    ) : RemoteListDataSource {
        val realtime = Channel<ListChange>(Channel.UNLIMITED)
        var inserted = 0; var updated = 0; var checkedCalls = 0; var deleted = 0
        override suspend fun loadUser(publicId: String): AppUserDto? = user?.copy(publicId = publicId)
        override suspend fun loadItems(userId: String) = initial
        override suspend fun insertItem(userId: String, item: GroceryItem) { inserted++ }
        override suspend fun updateItem(id: String, item: GroceryItem) { updated++ }
        override suspend fun setChecked(id: String, checked: Boolean) { checkedCalls++ }
        override suspend fun deleteItem(id: String) { deleted++ }
        override fun changes(userId: String): Flow<ListChange> = realtime.receiveAsFlow()
    }

    @Test fun load_thenAdd_emitsOptimisticStateAndPersists() = runTest {
        val remote = FakeRemote(emptyList())
        val repo = ListRepository(remote)
        repo.load("pub1")
        repo.categories.test {
            assertEquals(0, awaitItem().flatMap { it.items }.size)   // loaded empty
            repo.addOrIncrement(name = "Milk", categoryId = "dairy", source = "search")
            assertEquals("Milk", awaitItem().flatMap { it.items }.single().name)  // optimistic
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, remote.inserted)
    }

    @Test fun realtimeInsert_fromAnotherClient_appliesToState() = runTest(UnconfinedTestDispatcher()) {
        // Seed one category bucket (the real app is always seeded; an empty list has nowhere to append).
        val remote = FakeRemote(listOf(GroceryItem(id = "seed", name = "Bread", categoryId = "dairy")))
        val repo = ListRepository(remote)
        repo.load("pub1")
        repo.observeRealtime(backgroundScope)   // long-running collector off the test body
        remote.realtime.send(ListChange.Upserted(GroceryItem(id = "r1", name = "Eggs", categoryId = "dairy"), isUpdate = false))
        advanceUntilIdle()                       // deterministically let the collector apply the change
        val names = repo.categories.value.flatMap { it.items }.map { it.name }
        assertTrue(names.contains("Eggs"))
        assertEquals(2, names.size)              // seed + realtime-inserted
        assertEquals(0, remote.inserted)         // realtime application must not re-persist
    }

    @Test fun toggle_flipsStateAndPersists() = runTest {
        val remote = FakeRemote(listOf(GroceryItem(id = "i1", name = "Milk", categoryId = "dairy", checked = false)))
        val repo = ListRepository(remote)
        repo.load("pub1")
        repo.toggle("i1")
        assertTrue(repo.categories.value.flatMap { it.items }.single().checked)
        assertEquals(1, remote.checkedCalls)
    }

    @Test fun delete_removesStateAndPersists() = runTest {
        val remote = FakeRemote(listOf(GroceryItem(id = "i1", name = "Milk", categoryId = "dairy")))
        val repo = ListRepository(remote)
        repo.load("pub1")
        repo.delete("i1")
        assertTrue(repo.categories.value.flatMap { it.items }.isEmpty())
        assertEquals(1, remote.deleted)
    }

    @Test fun load_returnsFalse_whenNoUserRow() = runTest {
        val repo = ListRepository(FakeRemote(emptyList(), user = null))
        val found = repo.load("pubX")
        assertEquals(false, found)
        assertEquals(null, repo.userProfile.value)
        assertEquals("pubX", repo.publicId.value)  // publicId is recorded even when unconnected
    }

    @Test fun load_returnsTrue_andExposesProfile_whenUserRowExists() = runTest {
        val user = AppUserDto(id = "owner", displayName = "Sam", phoneNumber = "1555", listCode = "ABC123")
        val repo = ListRepository(FakeRemote(emptyList(), user = user))
        val found = repo.load("pubY")
        assertTrue(found)
        val profile = repo.userProfile.value!!
        assertEquals("Sam", profile.displayName)
        assertTrue(profile.phoneConnected)
        assertEquals("ABC123", profile.listCode)
        assertEquals("pubY", repo.publicId.value)
    }
}
