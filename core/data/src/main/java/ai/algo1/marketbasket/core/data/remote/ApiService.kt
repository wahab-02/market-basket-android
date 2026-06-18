package ai.algo1.marketbasket.core.data.remote

import ai.algo1.marketbasket.core.data.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiService @Inject constructor(private val client: HttpClient) {
    private val base = BuildConfig.BACKEND_BASE_URL.trimEnd('/')

    suspend fun ensureListCode(publicId: String): String =
        client.post("$base/api/list-code/ensure") {
            contentType(ContentType.Application.Json)
            setBody(EnsureListCodeRequest(publicId))
        }.body<EnsureListCodeResponse>().listCode

    suspend fun importMarketBasket(request: ImportRequest): ImportResponse =
        client.post("$base/api/market-basket/import") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun googleSync(publicId: String): Int =
        client.get("$base/api/google/sync") {
            parameter("publicId", publicId)
        }.body<GoogleSyncResponse>().synced
}
