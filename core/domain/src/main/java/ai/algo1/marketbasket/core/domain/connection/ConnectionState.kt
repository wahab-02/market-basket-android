package ai.algo1.marketbasket.core.domain.connection

/** App-gate state. Loading is the initial value before bootstrap resolves. */
enum class ConnectionState { Loading, Unconnected, Connected }

object Connection {
    /**
     * The Android gate is connected when an app_users row exists. A cached connection only keeps
     * the app open for transient row-check failures; it must not override a confirmed missing row.
     */
    fun resolve(cachedConnected: Boolean, rowExists: Boolean, rowCheckFailed: Boolean = false): ConnectionState =
        if (rowExists || (cachedConnected && rowCheckFailed)) ConnectionState.Connected else ConnectionState.Unconnected
}
