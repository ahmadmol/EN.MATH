package com.ahmadmol.enmath.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
private fun AuthError(vm: AuthViewModel) {
    val error by vm.error.collectAsStateWithLifecycle()
    val message =
        when (error) {
            "name" -> label(TextKey.t_2d6ed82df3)
            "email" -> label(TextKey.t_c0513f0aef)
            "password" -> label(TextKey.t_8e69f4f858)
            "match" -> label(TextKey.t_44bfd23c2d)
            "teacher" -> label(TextKey.t_4064dc24ff)
            "terms" -> label(TextKey.t_af31379f6f)
            "code" -> label(TextKey.t_7c1f10df3f)
            else -> ""
        }
    if (message.isNotEmpty())
        Text(
            message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag("auth-error"),
        )
}

@Composable
fun RoleSelector(role: AccountType, onRole: (AccountType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
        listOf(AccountType.Student, AccountType.Teacher).forEach { type ->
            FilterChip(
                selected = role == type,
                onClick = { onRole(type) },
                label = {
                    Text(
                        if (type == AccountType.Student) label(TextKey.t_3b846b6647)
                        else label(TextKey.t_41df12babb)
                    )
                },
                modifier = Modifier.testTag("select-${type.name}"),
            )
        }
    }
}

@Composable
fun LoginScreen(
    vm: AuthViewModel,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onForgot: () -> Unit,
) {
    val email by vm.email.collectAsStateWithLifecycle()
    val password by vm.password.collectAsStateWithLifecycle()
    val role by vm.role.collectAsStateWithLifecycle()
    ScreenPage(Modifier.testTag("screen-login"), true) {
        PageHeading("EN.MATH", label(TextKey.t_4bd9b3fa20), label(TextKey.t_9440b55d06))
        DemoNotice(label(TextKey.t_bfdd842d70))
        RoleSelector(AccountType.valueOf(role), vm::chooseRole)
        AppTextField(
            email,
            { vm.set("email", it) },
            label(TextKey.t_711e4aa84e),
            Modifier.testTag("auth-email"),
            keyboardType = KeyboardType.Email,
        )
        AppTextField(
            password,
            { vm.set("password", it) },
            label(TextKey.t_a98160205a),
            Modifier.testTag("auth-password"),
            password = true,
        )
        AuthError(vm)
        PrimaryButton(
            label(TextKey.t_bd9e360130),
            { if (vm.validate(false)) onLogin() },
            Modifier.testTag("login-submit"),
        )
        TextButton(onClick = onForgot, modifier = Modifier.testTag("forgot")) {
            Text(label(TextKey.t_f1c9af17c3))
        }
        OutlinedButton(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth().testTag("register"),
        ) {
            Text(label(TextKey.t_344e8ddb55))
        }
    }
}

@Composable
fun AccountChoiceScreen(onSelect: (AccountType) -> Unit) {
    ScreenPage(Modifier.testTag("screen-account-choice"), true) {
        PageHeading(label(TextKey.t_8738fe67a6), label(TextKey.t_c2e2baa31f))
        listOf(AccountType.Student, AccountType.Teacher).forEach { role ->
            AppCard(
                onClick = { onSelect(role) },
                modifier = Modifier.testTag("register-${role.name}"),
            ) {
                Text(
                    if (role == AccountType.Student) label(TextKey.t_96ee0d02e3)
                    else label(TextKey.t_8f236c7a72),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    if (role == AccountType.Student) label(TextKey.t_c7e19e5294)
                    else label(TextKey.t_2594e8a647)
                )
            }
        }
        DemoNotice()
    }
}

@Composable
fun RegistrationScreen(vm: AuthViewModel, teacher: Boolean, onContinue: () -> Unit) {
    val name by vm.name.collectAsStateWithLifecycle()
    val email by vm.email.collectAsStateWithLifecycle()
    val password by vm.password.collectAsStateWithLifecycle()
    val confirmation by vm.confirmation.collectAsStateWithLifecycle()
    val subject by vm.subject.collectAsStateWithLifecycle()
    val experience by vm.experience.collectAsStateWithLifecycle()
    val institution by vm.institution.collectAsStateWithLifecycle()
    val accepted by vm.accepted.collectAsStateWithLifecycle()
    var showTerms by remember { mutableStateOf(false) }
    ScreenPage(Modifier.testTag("screen-registration"), true) {
        PageHeading(
            label(TextKey.t_db640f5b9c),
            if (teacher) label(TextKey.t_6f119ee864) else label(TextKey.t_e61534a23d),
        )
        DemoNotice(label(TextKey.t_dcf57c409e))
        AppTextField(
            name,
            { vm.set("name", it) },
            label(TextKey.t_85c73acada),
            Modifier.testTag("auth-name"),
        )
        AppTextField(
            email,
            { vm.set("email", it) },
            label(TextKey.t_711e4aa84e),
            Modifier.testTag("auth-email"),
            keyboardType = KeyboardType.Email,
        )
        if (teacher) {
            AppTextField(
                subject,
                { vm.set("subject", it) },
                label(TextKey.t_31eced6d57),
                Modifier.testTag("teacher-subject"),
            )
            AppTextField(
                experience,
                { vm.set("experience", it.filter(Char::isDigit).take(2)) },
                label(TextKey.t_1554f4db6b),
                Modifier.testTag("teacher-experience"),
                keyboardType = KeyboardType.Number,
            )
            AppTextField(institution, { vm.set("institution", it) }, label(TextKey.t_9f33bb467e))
        }
        AppTextField(
            password,
            { vm.set("password", it) },
            label(TextKey.t_ad0311fff4),
            Modifier.testTag("auth-password"),
            password = true,
        )
        AppTextField(
            confirmation,
            { vm.set("confirmation", it) },
            label(TextKey.t_5b5743805e),
            Modifier.testTag("auth-confirm"),
            password = true,
        )
        Row {
            Checkbox(
                checked = accepted,
                onCheckedChange = vm::accept,
                modifier = Modifier.testTag("auth-terms"),
            )
            TextButton(onClick = { showTerms = true }) { Text(label(TextKey.t_63c7e31c42)) }
        }
        AuthError(vm)
        PrimaryButton(
            label(TextKey.t_9db065e98a),
            { if (vm.validate(true)) onContinue() },
            Modifier.testTag("registration-submit"),
        )
    }
    if (showTerms)
        AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text(label(TextKey.t_cdc7551be2)) },
            text = { Text(label(TextKey.t_4d0f4ebd77)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.accept(true)
                        showTerms = false
                    }
                ) {
                    Text(label(TextKey.t_de281d583c))
                }
            },
        )
}

@Composable
fun VerificationScreen(vm: AuthViewModel, onVerify: () -> Unit) {
    val code by vm.code.collectAsStateWithLifecycle()
    val email by vm.email.collectAsStateWithLifecycle()
    var resent by remember { mutableStateOf(false) }
    ScreenPage(Modifier.testTag("screen-verification"), true) {
        PageHeading(label(TextKey.t_91067f9a97), label(TextKey.t_db7aaa76e0), email)
        DemoNotice(label(TextKey.t_006ebee366))
        AppTextField(
            code,
            { vm.set("code", it.filter(Char::isDigit).take(6)) },
            label(TextKey.t_df1ef156f3),
            Modifier.testTag("auth-code"),
            keyboardType = KeyboardType.Number,
        )
        AuthError(vm)
        PrimaryButton(
            label(TextKey.t_a2e41f05a0),
            { if (vm.verify()) onVerify() },
            Modifier.testTag("verify-submit"),
        )
        TextButton(onClick = { resent = true }) { Text(label(TextKey.t_082155007d)) }
        if (resent) Text(label(TextKey.t_fd2727cf9d))
    }
}

@Composable
fun ForgotPasswordScreen(vm: AuthViewModel, onContinue: () -> Unit) {
    val email by vm.email.collectAsStateWithLifecycle()
    ScreenPage(Modifier.testTag("screen-forgot"), true) {
        PageHeading(label(TextKey.t_1da6c63fb2), label(TextKey.t_8804247fd9))
        DemoNotice(label(TextKey.t_c3427ebcc7))
        AppTextField(
            email,
            { vm.set("email", it) },
            label(TextKey.t_711e4aa84e),
            Modifier.testTag("auth-email"),
            keyboardType = KeyboardType.Email,
        )
        AuthError(vm)
        PrimaryButton(
            label(TextKey.t_29c1be7a43),
            { if (vm.beginReset()) onContinue() },
            Modifier.testTag("forgot-submit"),
        )
    }
}

@Composable
fun NewPasswordScreen(vm: AuthViewModel, onDone: () -> Unit) {
    val password by vm.password.collectAsStateWithLifecycle()
    val confirmation by vm.confirmation.collectAsStateWithLifecycle()
    ScreenPage(Modifier.testTag("screen-new-password"), true) {
        PageHeading(label(TextKey.t_354d5e989e), label(TextKey.t_55f61c37c5))
        DemoNotice(label(TextKey.t_bb202e55bd))
        AppTextField(
            password,
            { vm.set("password", it) },
            label(TextKey.t_9111cb18d5),
            Modifier.testTag("auth-password"),
            password = true,
        )
        AppTextField(
            confirmation,
            { vm.set("confirmation", it) },
            label(TextKey.t_5b5743805e),
            Modifier.testTag("auth-confirm"),
            password = true,
        )
        AuthError(vm)
        PrimaryButton(
            label(TextKey.t_89c0b50eea),
            { if (vm.newPassword()) onDone() },
            Modifier.testTag("new-password-submit"),
        )
    }
}
