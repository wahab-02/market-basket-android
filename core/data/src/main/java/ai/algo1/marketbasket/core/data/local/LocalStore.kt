package ai.algo1.marketbasket.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "market_basket")

@Singleton
class LocalStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val PUBLIC_ID = stringPreferencesKey("public_id")
        val LIST_CODE = stringPreferencesKey("list_code")
        val CONNECTED = booleanPreferencesKey("connected")
        val HOME_INTRO_SEEN = booleanPreferencesKey("home_intro_seen")
    }

    val publicId: Flow<String?> = context.dataStore.data.map { it[Keys.PUBLIC_ID] }
    val listCode: Flow<String?> = context.dataStore.data.map { it[Keys.LIST_CODE] }
    val connected: Flow<Boolean> = context.dataStore.data.map { it[Keys.CONNECTED] ?: false }
    val homeIntroSeen: Flow<Boolean> = context.dataStore.data.map { it[Keys.HOME_INTRO_SEEN] ?: false }

    suspend fun setPublicId(value: String) {
        context.dataStore.edit { it[Keys.PUBLIC_ID] = value }
    }

    suspend fun setListCode(value: String) {
        context.dataStore.edit { it[Keys.LIST_CODE] = value }
    }

    suspend fun setConnected(value: Boolean) {
        context.dataStore.edit { it[Keys.CONNECTED] = value }
    }

    suspend fun setHomeIntroSeen() {
        context.dataStore.edit { it[Keys.HOME_INTRO_SEEN] = true }
    }
}
