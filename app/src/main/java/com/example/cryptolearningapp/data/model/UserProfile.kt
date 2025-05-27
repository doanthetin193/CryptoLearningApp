package com.example.cryptolearningapp.data.model

data class UserProfile(
    val name: String,
    val gender: Gender,
    val birthYear: Int
) {
    val age: Int
        get() = java.time.Year.now().value - birthYear
}

enum class Gender {
    MALE, FEMALE, OTHER
} 