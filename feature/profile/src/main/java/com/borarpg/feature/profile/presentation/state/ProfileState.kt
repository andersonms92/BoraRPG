package com.borarpg.feature.profile.presentation.state

import com.borarpg.feature.profile.presentation.form.ProfileForm

data class ProfileState(
    val draft: ProfileForm = ProfileForm(),
    val errors: Map<String, String> = emptyMap(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null,
)
