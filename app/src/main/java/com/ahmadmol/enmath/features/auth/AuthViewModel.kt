package com.ahmadmol.enmath.features.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DemoAuthRules {
    const val verificationCode = "123456"

    fun validEmail(value: String) = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value.trim())

    fun validPassword(value: String) = value.length >= 8

    fun validTeacher(subject: String, experience: String): Boolean =
        subject.isNotBlank() && experience.toIntOrNull()?.let { it in 0..60 } == true
}

/** UI demo validation only: no backend, tokens or real account creation. */
class AuthViewModel(private val saved: SavedStateHandle) : ViewModel() {
    val role = saved.getStateFlow("role", AccountType.Student.name)
    val name = saved.getStateFlow("name", "")
    val email = saved.getStateFlow("email", "")
    private val _password = MutableStateFlow("")
    private val _confirmation = MutableStateFlow("")
    val password = _password.asStateFlow()
    val confirmation = _confirmation.asStateFlow()
    val subject = saved.getStateFlow("subject", "")
    val experience = saved.getStateFlow("experience", "")
    val institution = saved.getStateFlow("institution", "")
    val code = saved.getStateFlow("code", "")
    val accepted = saved.getStateFlow("accepted", false)
    val resetFlow = saved.getStateFlow("reset", false)
    val error = saved.getStateFlow("error", "")

    fun set(field: String, value: String) {
        when (field) {
            "password" -> _password.value = value
            "confirmation" -> _confirmation.value = value
            else -> saved[field] = value
        }
        saved["error"] = ""
    }

    fun accept(value: Boolean) {
        saved["accepted"] = value
        saved["error"] = ""
    }

    fun chooseRole(value: AccountType) {
        saved["role"] = value.name
    }

    fun validate(register: Boolean): Boolean {
        val failure =
            when {
                register && name.value.trim().length < 2 -> "name"
                !DemoAuthRules.validEmail(email.value) -> "email"
                !DemoAuthRules.validPassword(password.value) -> "password"
                register && password.value != confirmation.value -> "match"
                register &&
                    role.value == AccountType.Teacher.name &&
                    !DemoAuthRules.validTeacher(subject.value, experience.value) -> "teacher"
                register && !accepted.value -> "terms"
                else -> ""
            }
        saved["error"] = failure
        if (failure.isEmpty()) {
            saved["reset"] = false
            saved["code"] = ""
        }
        return failure.isEmpty()
    }

    fun beginReset(): Boolean {
        if (!DemoAuthRules.validEmail(email.value)) {
            saved["error"] = "email"
            return false
        }
        saved["reset"] = true
        saved["code"] = ""
        saved["error"] = ""
        return true
    }

    fun verify(): Boolean {
        val valid = code.value == DemoAuthRules.verificationCode
        saved["error"] = if (valid) "" else "code"
        return valid
    }

    fun newPassword(): Boolean {
        val valid =
            DemoAuthRules.validPassword(password.value) && password.value == confirmation.value
        saved["error"] = if (valid) "" else "match"
        if (valid) clearSecrets()
        return valid
    }

    fun user() =
        User(
            name.value.trim().ifBlank { email.value.substringBefore('@') },
            email.value.trim(),
            AccountType.valueOf(role.value),
        )

    fun clearSecrets() {
        _password.value = ""
        _confirmation.value = ""
        saved["code"] = ""
        saved["error"] = ""
    }

    fun reset() {
        clearSecrets()
        listOf("name", "email", "subject", "experience", "institution").forEach { saved[it] = "" }
        saved["accepted"] = false
        saved["reset"] = false
    }
}
