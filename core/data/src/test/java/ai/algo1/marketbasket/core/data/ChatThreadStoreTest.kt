package ai.algo1.marketbasket.core.data

import ai.algo1.marketbasket.core.data.local.ChatThreadStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ChatThreadStoreTest {
    @get:Rule val tempFolder = TemporaryFolder()

    private fun store(): ChatThreadStore {
        val ds = PreferenceDataStoreFactory.create {
            tempFolder.newFile("chat_thread_test.preferences_pb")
        }
        return ChatThreadStore(ds)
    }

    @Test fun `creates UUID for new publicId`() = runTest {
        val s = store()
        val id = s.getOrCreateThreadId("user-abc")
        assertNotNull(id)
        assert(id.isNotBlank())
    }

    @Test fun `returns same UUID on second call`() = runTest {
        val s = store()
        val first = s.getOrCreateThreadId("user-abc")
        val second = s.getOrCreateThreadId("user-abc")
        assertEquals(first, second)
    }

    @Test fun `different publicIds get different threadIds`() = runTest {
        val s = store()
        val a = s.getOrCreateThreadId("user-aaa")
        val b = s.getOrCreateThreadId("user-bbb")
        assert(a != b)
    }
}
