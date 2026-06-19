package ai.algo1.marketbasket.feature.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.algo1.marketbasket.core.designsystem.MarketBasketColors

@Composable
fun ListRoute(viewModel: ListViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ListScreen(state = state, onAdd = viewModel::addByName, onToggle = viewModel::toggle, onDelete = viewModel::delete)
}

@Composable
fun ListScreen(
    state: ListUiState,
    onAdd: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var expandedId by remember { mutableStateOf<String?>(null) }
    var draft by remember { mutableStateOf("") }

    // Reconcile expanded state with external (realtime) changes: clear a stale id if its item is gone.
    LaunchedEffect(state.categories) {
        if (expandedId != null && state.categories.none { c -> c.items.any { it.id == expandedId } }) {
            expandedId = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MarketBasketTopBar(greeting = "Your list", onSearchToggle = {})

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Add an item") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd(draft); draft = "" }),
            )
            Button(onClick = { onAdd(draft); draft = "" }) { Text("Add") }
        }

        if (state.categories.isEmpty()) {
            Text(
                "Your list is empty",
                modifier = Modifier.fillMaxSize().wrapContentSize(),
                color = MarketBasketColors.TextSecondary,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                state.categories.forEach { category ->
                    item(key = "h_${category.id}") {
                        Text(
                            text = category.name.uppercase(),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MarketBasketColors.Primary,
                        )
                    }
                    items(category.items, key = { it.id }) { groceryItem ->
                        ListItem(
                            item = groceryItem,
                            expanded = expandedId == groceryItem.id,
                            onClick = { expandedId = if (expandedId == groceryItem.id) null else groceryItem.id },
                            onToggle = { onToggle(groceryItem.id) },
                            onDelete = { onDelete(groceryItem.id); expandedId = null },
                        )
                    }
                }
            }
        }
    }
}
