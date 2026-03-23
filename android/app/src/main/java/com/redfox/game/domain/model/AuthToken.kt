package com.redfox.game.domain.model

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
