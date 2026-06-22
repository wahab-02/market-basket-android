package ai.algo1.marketbasket.core.domain.today

data class CommunityListItem(val name: String, val imageUrl: String? = null)

data class CommunityList(
    val id: String,
    val icon: String,
    val title: String,
    val description: String,
    val itemCount: Int,
    val cardImageKey: String,
    val items: List<CommunityListItem>,
)

private fun items(vararg names: String) = names.map { CommunityListItem(it) }

/** Port of communityListsData.ts. all-order matches TodayPage render order. */
object CommunityLists {
    val PROTEIN = CommunityList(
        id = "protein-guy", icon = "💪", title = "Johnny the protein guy",
        description = "High-protein, under ${'$'}90/week", itemCount = 20, cardImageKey = "protein-community",
        items = items(
            "Chicken thighs, bone-in", "Greek yogurt, plain", "Eggs, large", "Ground turkey",
            "Cottage cheese", "Salmon fillets", "Tuna, canned", "Protein shake, vanilla",
            "Sweet potatoes", "Brown rice", "Broccoli", "Almonds", "Mozzarella cheese",
            "Black beans, canned", "Olive oil", "Garlic", "Spinach", "Oats, rolled",
            "Milk, whole", "Cheddar cheese",
        ),
    )
    val SARAH = CommunityList(
        id = "sarah-vegetarian", icon = "🥦", title = "Sarah the vegetarian",
        description = "Plant-forward, family of 4", itemCount = 20, cardImageKey = "vegetable-community",
        items = items(
            "Black beans, canned", "Corn tortillas", "Mango", "Spinach", "Avocado", "Cheddar cheese",
            "Sour cream", "Bell peppers", "Red onion", "Lime", "Cilantro", "Eggs, large", "Mozzarella",
            "Broccoli", "Crushed tomatoes, canned", "Lentils", "Greek yogurt, plain", "Pasta, penne",
            "Parmesan", "Butter",
        ),
    )
    val REBECCA = CommunityList(
        id = "rebecca-vegan", icon = "🌱", title = "Maya the vegan",
        description = "Whole-food, plant-based meals", itemCount = 20, cardImageKey = "vegan-community",
        items = items(
            "Oats, rolled", "Chickpeas, canned", "Kale", "Lemon", "Tahini", "Quinoa", "Hummus",
            "Brown rice", "Frozen peas", "Tofu, firm", "Almond milk", "Coconut oil", "Nutritional yeast",
            "Sweet potatoes", "Black beans, canned", "Bananas", "Mixed frozen berries", "Peanut butter",
            "Flaxseeds", "Garlic",
        ),
    )
    val DANA = CommunityList(
        id = "dana-quick-dinner", icon = "🍳", title = "Aisha the home cook",
        description = "Quick weeknight dinners", itemCount = 20, cardImageKey = "dinner-community",
        items = items(
            "Italian sausage links", "Bag salad mix", "Penne pasta", "Jarred marinara sauce",
            "Rotisserie chicken", "Zucchini", "Cherry tomatoes", "Garlic bread",
            "Frozen stir-fry vegetables", "Soy sauce", "Instant rice", "Canned tomato soup",
            "Shredded cheese", "Flour tortillas", "Ground beef", "Onion", "Olive oil", "Bell peppers",
            "Butter", "Pre-washed spinach",
        ),
    )
    val PETE = CommunityList(
        id = "pete-paleo", icon = "🔥", title = "Marcus the paleo guy",
        description = "Clean eating, under ${'$'}100/week", itemCount = 20, cardImageKey = "paleo-community",
        items = items(
            "Grass-fed ground beef", "Sweet potatoes", "Almonds", "Bacon, uncured", "Chicken breast",
            "Avocado", "Coconut oil", "Salmon fillets", "Eggs, large", "Kale", "Broccoli", "Walnuts",
            "Blueberries", "Garlic", "Olive oil", "Bison burger patties", "Butternut squash", "Arugula",
            "Pork chops", "Macadamia nuts",
        ),
    )
    val MIKE = CommunityList(
        id = "mike-store-manager", icon = "🏪", title = "Kevin the store manager",
        description = "Best picks from Market Basket", itemCount = 20, cardImageKey = "storemgr-community",
        items = items(
            "Whole chicken", "Fresh strawberries", "Sourdough loaf", "Salmon fillets", "Avocado",
            "Broccoli crowns", "Cheddar cheese block", "Butter", "Greek yogurt, plain", "Milk, whole",
            "Eggs, large", "Spaghetti pasta", "Crushed tomatoes, canned", "Fresh herb mix", "Blueberries",
            "Romaine lettuce", "Chicken thighs", "Orange juice", "Croissants", "Baby carrots",
        ),
    )

    val all: List<CommunityList> = listOf(PROTEIN, SARAH, REBECCA, DANA, PETE, MIKE)
}
