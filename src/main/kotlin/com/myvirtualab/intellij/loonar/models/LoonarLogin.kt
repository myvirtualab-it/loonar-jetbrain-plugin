package com.myvirtualab.intellij.loonar.models

public data class LoginParams(
        val username: String,
        val password: String,
        // val pixelRatio: Int
)

public data class LoonarLogin(
        val token: String,
)
