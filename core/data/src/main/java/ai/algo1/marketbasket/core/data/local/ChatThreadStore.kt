package ai.algo1.marketbasket.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ChatThreadRepository {
    suspend fun getOrCreateThreadId(publicId: String): String
}

@Singleton
class ChatThreadStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ChatThreadRepository {
    override suspend fun getOrCreateThreadId(publicId: String): String {
        val key = stringPreferencesKey("chat_thread_$publicId")
        return dataStore.data.map { it[key] }.first() ?: run {
            val newId = UUID.randomUUID().toString()
            dataStore.edit { it[key] = newId }
            newId
        }
    }
}
