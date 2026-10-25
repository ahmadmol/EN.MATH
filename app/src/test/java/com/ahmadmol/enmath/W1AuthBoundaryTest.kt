package com.ahmadmol.enmath

import androidx.lifecycle.SavedStateHandle
import com.ahmadmol.enmath.core.data.LocalDemoRepository
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.auth.AuthViewModel
import org.junit.Assert.*
import org.junit.Test

class W1AuthBoundaryTest {
    @Test fun productionRejectsDemoValidationVerificationAndRecovery() {
        val vm = AuthViewModel(SavedStateHandle())
        vm.set("email", "fixture@example.test")
        vm.set("password", "fixture-pass")
        vm.set("confirmation", "fixture-pass")
        vm.set("code", "123456")
        assertFalse(vm.validate(false))
        assertFalse(vm.validate(true))
        assertFalse(vm.verify())
        assertFalse(vm.beginReset())
        assertFalse(vm.newPassword())
        assertEquals("demo_disabled", vm.error.value)
    }
    @Test fun explicitlyEnabledDebugDemoRemainsLocalAndClearsSecrets() {
        val vm = AuthViewModel(SavedStateHandle(), true)
        vm.set("email", "fixture@example.test")
        vm.set("password", "fixture-pass")
        assertTrue(vm.validate(false))
        vm.set("code", "123456")
        assertTrue(vm.verify())
        vm.clearSecrets()
        assertEquals("", vm.password.value)
        assertEquals("", vm.code.value)
    }
    @Test fun productionDoesNotRestoreOrAcceptDemoAccountAndPreservesItsStoredData() {
        val files = mutableMapOf<String, FixturePreferences>()
        val prefs = { name: String -> files.getOrPut(name) { FixturePreferences() }.prefs }
        val debug = LocalDemoRepository(true, prefs)
        val user = User("Fixture", "fixture@example.test", AccountType.Student)
        debug.signIn(user)
        debug.completeLesson("limits-practice")
        val before = files.mapValues { it.value.values.toMap() }
        val production = LocalDemoRepository(false, prefs)
        assertNull(production.state.value.user)
        assertThrows(IllegalStateException::class.java) { production.signIn(user) }
        assertEquals(before, files.mapValues { it.value.values.toMap() })
        production.signIn(User("Guest", "", AccountType.Guest))
        assertEquals(AccountType.Guest, production.state.value.user?.type)
        assertTrue(production.state.value.completed.isEmpty())
        val restored = LocalDemoRepository(true, prefs)
        restored.signIn(user)
        assertEquals(setOf("limits-practice"), restored.state.value.completed)
    }
}
