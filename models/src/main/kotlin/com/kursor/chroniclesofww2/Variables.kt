package com.kursor.chroniclesofww2

object Variables {
    val JWT_SECRET = System.getenv("JWT_SECRET")
    val POSTGRES_DB_PASSWORD = System.getenv("POSTGRES_DB_PASSWORD")
}

const val AUTH_JWT = "auth-jwt"