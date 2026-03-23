package com.redfox.game.domain.model

data class User(
    val id: Long,
    val email: String,
    val nickname: String,
    val country: String,
    val balanceReal: Double,
    val balanceDemo: Double,
    val kycStatus: KycStatus,
    val emailVerified: Boolean
)

enum class KycStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED
}
