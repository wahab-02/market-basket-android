package ai.algo1.marketbasket.core.data.di

import ai.algo1.marketbasket.core.data.BuildConfig
import ai.algo1.marketbasket.core.data.local.ChatThreadRepository
import ai.algo1.marketbasket.core.data.local.ChatThreadStore
import ai.algo1.marketbasket.core.data.remote.AgentChatRepository
import ai.algo1.marketbasket.core.data.remote.AgentChatService
import ai.algo1.marketbasket.core.data.remote.RemoteListDataSource
import ai.algo1.marketbasket.core.data.remote.SupabaseListDataSource
import ai.algo1.marketbasket.core.data.repository.CatalogRepository
import ai.algo1.marketbasket.core.data.repository.CatalogRepositoryImpl
import ai.algo1.marketbasket.core.data.repository.ListRepository
import ai.algo1.marketbasket.core.data.repository.PublicIdProvider
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("market_basket") }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Postgrest)
        install(Realtime)
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
            }
        }
    }

    @Provides
    @Singleton
    @StreamingHttpClient
    fun provideStreamingHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(0, TimeUnit.SECONDS)   // 0 = no read timeout — required for SSE
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
    }

    @Provides
    @Singleton
    fun provideAgentChatRepository(impl: AgentChatService): AgentChatRepository = impl

    @Provides
    @Singleton
    fun provideRemoteListDataSource(impl: SupabaseListDataSource): RemoteListDataSource = impl

    @Provides
    @Singleton
    fun provideCatalogRepository(impl: CatalogRepositoryImpl): CatalogRepository = impl

    @Provides
    @Singleton
    fun provideChatThreadRepository(impl: ChatThreadStore): ChatThreadRepository = impl

    @Provides
    @Singleton
    fun providePublicIdProvider(impl: ListRepository): PublicIdProvider = impl
}
