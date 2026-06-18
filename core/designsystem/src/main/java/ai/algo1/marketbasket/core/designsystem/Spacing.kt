package ai.algo1.marketbasket.core.designsystem

import androidx.compose.ui.unit.dp

/**
 * Spacing scale. The web app uses Tailwind px values mapped 1:1 to dp (per the migration spec's
 * fidelity rules). These are the common steps; exact per-component px values are applied inline
 * in each screen during its feature migration.
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}
