package ai.algo1.marketbasket.core.domain.connection

/** App-gate state. Loading is the initial value before bootstrap resolves. */
enum class ConnectionState { Loading, Unconnected, Connected }

object Connection {
    /**
     * The Android gate. Unlike the web (which keys on a `?u=` URL param that arrives from the
     * webhook), Android mints the publicId locally, so it is "connected" once an app_users row
     * exists for it — or once we have cached that it connected before (survives offline launches).
     */
    fun resolve(cachedConnected: Boolean, rowExists: Boolean): ConnectionState =
        if (cachedConnected || rowExists) ConnectionState.Connected else ConnectionState.Unconnected
}
