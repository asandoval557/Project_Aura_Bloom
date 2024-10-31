package com.example.project_aura_bloom.models

data class UserProfile(
    val fullName: String = "",
    val date_joined: String = "",
    val email: String = "",
    val user_id: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhoneNumber: String = "",
    val profileImageUrl: String = ""
)