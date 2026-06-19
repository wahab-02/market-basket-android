package ai.algo1.marketbasket.core.data.remote

import ai.algo1.marketbasket.core.data.dto.AppUserDto
import ai.algo1.marketbasket.core.data.dto.ShoppingListItemDto
import ai.algo1.marketbasket.core.data.dto.toDomain
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

private const val TABLE = "shopping_list_items"
private const val ITEM_COLUMNS = "id, user_id, name, quantity, category, checked, source, image_url, created_at"
private const val USER_COLUMNS =
    "id, public_id, display_name, slack_id, phone_number, list_code, alexa_connected, chatgpt_connected, claude_connected, google_connected, siri_connected"

@Singleton
class SupabaseListDataSource @Inject constructor(
    private val supabase: SupabaseClient,
    private val json: Json,
) : RemoteListDataSource {

    override suspend fun loadUser(publicId: String): AppUserDto? =
        supabase.from("app_users")
            .select(Columns.raw(USER_COLUMNS)) { filter { eq("public_id", publicId) } }
            .decodeSingleOrNull<AppUserDto>()

    override suspend fun loadItems(userId: String): List<GroceryItem> =
        supabase.from(TABLE)
            .select(Columns.raw(ITEM_COLUMNS)) {
                filter { eq("user_id", userId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<ShoppingListItemDto>()
            .map { it.toDomain() }

    override suspend fun insertItem(userId: String, item: GroceryItem) {
        supabase.from(TABLE).insert(
            ShoppingListItemDto(
                id = item.id, userId = userId, name = item.name, quantity = item.quantity,
                category = item.categoryId, checked = item.checked, source = item.source, imageUrl = item.imageUrl,
            )
        )
    }

    override suspend fun updateItem(id: String, item: GroceryItem) {
        supabase.from(TABLE).update({
            set("checked", item.checked)
            set("quantity", item.quantity)
            set("category", item.categoryId)
            set("source", item.source)
            set("image_url", item.imageUrl)
        }) { filter { eq("id", id) } }
    }

    override suspend fun setChecked(id: String, checked: Boolean) {
        supabase.from(TABLE).update({ set("checked", checked) }) { filter { eq("id", id) } }
    }

    override suspend fun deleteItem(id: String) {
        supabase.from(TABLE).delete { filter { eq("id", id) } }
    }

    override fun changes(userId: String): Flow<ListChange> = flow {
        val channel = supabase.channel("shopping_list_realtime")
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = TABLE
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        emitAll(
            changeFlow.map { action ->
                when (action) {
                    is PostgresAction.Insert ->
                        ListChange.Upserted(json.decodeFromJsonElement(ShoppingListItemDto.serializer(), action.record).toDomain(), isUpdate = false)
                    is PostgresAction.Update ->
                        ListChange.Upserted(json.decodeFromJsonElement(ShoppingListItemDto.serializer(), action.record).toDomain(), isUpdate = true)
                    is PostgresAction.Delete ->
                        ListChange.Deleted(action.oldRecord["id"]?.jsonPrimitive?.content ?: "")
                    else -> ListChange.Deleted("")
                }
            }
        )
    }
}
