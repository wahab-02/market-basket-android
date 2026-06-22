package ai.algo1.marketbasket.core.data.dto

import ai.algo1.marketbasket.core.domain.connection.UserProfile
import ai.algo1.marketbasket.core.domain.model.GroceryItem
import ai.algo1.marketbasket.core.domain.model.Promotion

/** shopping_list_items row -> domain. The DB column `category` maps to domain `categoryId`. */
fun ShoppingListItemDto.toDomain(): GroceryItem = GroceryItem(
    id = id,
    name = name,
    categoryId = category,
    checked = checked,
    quantity = quantity,
    source = source,
    imageUrl = imageUrl,
)

fun PromotionDto.toDomain(): Promotion = Promotion(
    id = id,
    productName = productName,
    productImage = productImage,
    promotionText = promotionText,
    saveAmount = saveAmount,
    originalPrice = originalPrice,
    currentPrice = currentPrice,
    category = category,
    barcode = barcode,
    badgeText = badgeText,
    isActive = isActive,
    createdAt = createdAt ?: "",
)

/** app_users row -> domain connection profile. */
fun AppUserDto.toUserProfile(): UserProfile = UserProfile(
    displayName = displayName,
    phoneNumber = phoneNumber,
    listCode = listCode,
)
