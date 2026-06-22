package ai.algo1.marketbasket.core.domain.connection

/** Connection-relevant view of an app_users row (domain-side; the data layer maps its DTO to this). */
data class UserProfile(
    val displayName: String?,
    val phoneNumber: String?,
    val listCode: String?,
) {
    /** Web parity: whatsappConnected = !!user.phone_number. */
    val phoneConnected: Boolean get() = !phoneNumber.isNullOrBlank()
}
