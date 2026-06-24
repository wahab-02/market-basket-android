package ai.algo1.marketbasket.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val PUBLIC_ID = stringPreferencesKey("public_id")
        val LIST_CODE = stringPreferencesKey("list_code")
        val CONNECTED = booleanPreferencesKey("connected")
        val HOME_INTRO_SEEN = booleanPreferencesKey("home_intro_seen")
    }

    val publicId: Flow<String?> = dataStore.data.map { it[Keys.PUBLIC_ID] }
    val listCode: Flow<String?> = dataStore.data.map { it[Keys.LIST_CODE] }
    val connected: Flow<Boolean> = dataStore.data.map { it[Keys.CONNECTED] ?: false }
    val homeIntroSeen: Flow<Boolean> = dataStore.data.map { it[Keys.HOME_INTRO_SEEN] ?: false }

    suspend fun setPublicId(value: String) {
        dataStore.edit { it[Keys.PUBLIC_ID] = value }
    }

    suspend fun setListCode(value: String) {
        dataStore.edit { it[Keys.LIST_CODE] = value }
    }

    suspend fun setConnected(value: Boolean) {
        dataStore.edit { it[Keys.CONNECTED] = value }
    }

    suspend fun setHomeIntroSeen() {
        dataStore.edit { it[Keys.HOME_INTRO_SEEN] = true }
    }
}
