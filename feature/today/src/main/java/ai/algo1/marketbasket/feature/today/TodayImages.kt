package ai.algo1.marketbasket.feature.today

import androidx.annotation.DrawableRes

internal object TodayImages {
    @DrawableRes fun smartPick(key: String): Int = when (key) {
        "creamy-garlic-pasta" -> R.drawable.smartpick_creamy_garlic_pasta
        "kraft-cabot-cheddar" -> R.drawable.smartpick_kraft_cabot_cheddar
        "fresh-strawberries" -> R.drawable.smartpick_fresh_strawberries
        "chicken-broccoli-rice" -> R.drawable.smartpick_chicken_broccoli_rice
        else -> R.drawable.smartpick_cheddar_cheese
    }

    @DrawableRes fun community(key: String): Int = when (key) {
        "protein-community" -> R.drawable.community_protein
        "vegetable-community" -> R.drawable.community_vegetable
        "vegan-community" -> R.drawable.community_vegan
        "dinner-community" -> R.drawable.community_dinner
        "paleo-community" -> R.drawable.community_paleo
        else -> R.drawable.community_storemgr
    }
}
