package ai.algo1.marketbasket.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddItemNotificationStateTest {
    @Test
    fun showItemsAdded_replacesVisibleNotificationWithNewId() {
        val state = AddItemNotificationState()

        val first = state.showItemsAdded("Item Added")
        val second = state.showItemsAdded("Items added from recipe")

        assertEquals(AddItemNotification(id = 2L, message = "Items added from recipe"), second)
        assertEquals(second, state.current)
        assertEquals(1L, first.id)
    }

    @Test
    fun dismissOnlyClearsMatchingVisibleNotification() {
        val state = AddItemNotificationState()

        val first = state.showItemsAdded("Item Added")
        val second = state.showItemsAdded("Items added from recipe")

        state.dismiss(first.id)
        assertEquals(second, state.current)

        state.dismiss(second.id)
        assertNull(state.current)
    }
}
