package com.ahmadmol.enmath.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun AuthError(vm: AuthViewModel) {
    val error by vm.error.collectAsStateWithLifecycle()
    val message =
        when (error) {
            "demo_disabled" -> tr("Demo sign-in and recovery are unavailable in production. Continue as a local guest; cloud accounts are not connected.", "الدخول والاستعادة التجريبيان غير متاحين في الإنتاج. تابع كزائر محلي؛ الحسابات السحابية غير متصلة.")
            "name" -> label(TextKey.t_2d6ed82df3)
            "email" -> label(TextKey.t_c0513f0aef)
            "password" -> label(TextKey.t_8e69f4f858)
            "match" -> label(TextKey.t_44bfd23c2d)
            "teacher" -> label(TextKey.t_4064dc24ff)
            "terms" -> label(TextKey.t_af31379f6f)
            "code" -> label(TextKey.t_7c1f10df3f)
            else -> ""
        }
    if (message.isNotEmpty()) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth-error")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun RoleSelector(role: AccountType, onRole: (AccountType) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Space.xs),
            modifier = Modifier.padding(4.dp)
        ) {
            listOf(AccountType.Student, AccountType.Teacher).forEach { type ->
                val selected = role == type
                Surface(
                    selected = selected,
                    onClick = { onRole(type) },
                    shape = MaterialTheme.shapes.small,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("select-${type.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = Space.sm)
                    ) {
                        Icon(
                            if (type == AccountType.Student) Icons.Outlined.School else Icons.Outlined.CoPresent,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(Space.xs))
                        Text(
                            if (type == AccountType.Student) label(TextKey.t_3b846b6647)
                            else label(TextKey.t_41df12babb),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
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
    CleanLoginScreen(vm, onLogin, onRegister, onForgot)
}

@Composable
fun AccountChoiceScreen(onSelect: (AccountType) -> Unit) {
    ScreenPage(Modifier.testTag("screen-account-choice"), true) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            MnLogoMark(Modifier.size(72.dp))
            Text(
                "EN.MATH",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.15.em
                )
            )
        }

        PageHeading(label(TextKey.t_8738fe67a6), label(TextKey.t_c2e2baa31f))

        listOf(AccountType.Student, AccountType.Teacher).forEach { role ->
            AppCard(
                onClick = { onSelect(role) },
                tonal = role == AccountType.Student,
                modifier = Modifier.testTag("register-${role.name}"),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.lg),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (role == AccountType.Student) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (role == AccountType.Student) Icons.Outlined.School
                                else Icons.Outlined.CoPresent,
                                null,
                                Modifier.size(28.dp),
                                tint = if (role == AccountType.Student) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                        Text(
                            if (role == AccountType.Student) label(TextKey.t_96ee0d02e3)
                            else label(TextKey.t_8f236c7a72),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            if (role == AccountType.Student) label(TextKey.t_c7e19e5294)
                            else label(TextKey.t_2594e8a647),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowForward,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            MnLogoMark(Modifier.size(64.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (teacher) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    if (teacher) tr("Teacher Account", "حساب معلم") else tr("Student Account", "حساب طالب"),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (teacher) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = Space.md, vertical = Space.xs)
                )
            }
        }

        PageHeading(
            label(TextKey.t_db640f5b9c),
            if (teacher) label(TextKey.t_6f119ee864) else label(TextKey.t_e61534a23d),
        )

        AppCard(tonal = true) {
            Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
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
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            Checkbox(
                checked = accepted,
                onCheckedChange = vm::accept,
                modifier = Modifier.testTag("auth-terms"),
            )
            Text(
                tr("I agree to the", "أوافق على"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = { showTerms = true },
                contentPadding = PaddingValues(horizontal = Space.xs)
            ) {
                Text(
                    label(TextKey.t_63c7e31c42),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        AuthError(vm)

        PrimaryButton(
            label(TextKey.t_9db065e98a),
            { if (vm.validate(true)) onContinue() },
            Modifier.testTag("registration-submit"),
        )

        DemoNotice(label(TextKey.t_dcf57c409e))
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
        FeatureEmblem(Icons.Outlined.MarkEmailRead)
        PageHeading(label(TextKey.t_91067f9a97), label(TextKey.t_db7aaa76e0), email)
        DemoNotice(label(TextKey.t_006ebee366))

        AppCard(tonal = true) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Space.sm),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    label(TextKey.t_df1ef156f3),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { vm.set("code", it.filter(Char::isDigit).take(6)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = Space.symbol)
                            .testTag("auth-code"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            textDirection = TextDirection.Ltr,
                            letterSpacing = com.ahmadmol.enmath.core.design.tokens.ResourceDimensions.verificationTracking,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }
        }

        AuthError(vm)

        PrimaryButton(
            label(TextKey.t_a2e41f05a0),
            { if (vm.verify()) onVerify() },
            Modifier.testTag("verify-submit"),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { resent = true }) {
                Text(
                    label(TextKey.t_082155007d),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        if (resent) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(Space.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.sm)
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        label(TextKey.t_fd2727cf9d),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(vm: AuthViewModel, onContinue: () -> Unit) {
    val email by vm.email.collectAsStateWithLifecycle()

    ScreenPage(Modifier.testTag("screen-forgot"), true) {
        FeatureEmblem(Icons.Outlined.LockReset)
        PageHeading(label(TextKey.t_1da6c63fb2), label(TextKey.t_8804247fd9))
        DemoNotice(label(TextKey.t_c3427ebcc7))

        AppCard(tonal = true) {
            AppTextField(
                email,
                { vm.set("email", it) },
                label(TextKey.t_711e4aa84e),
                Modifier.testTag("auth-email"),
                keyboardType = KeyboardType.Email,
            )
        }

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
        FeatureEmblem(Icons.Outlined.Lock)
        PageHeading(label(TextKey.t_354d5e989e), label(TextKey.t_55f61c37c5))
        DemoNotice(label(TextKey.t_bb202e55bd))

        AppCard(tonal = true) {
            Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
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
            }
        }

        AuthError(vm)

        PrimaryButton(
            label(TextKey.t_89c0b50eea),
            { if (vm.newPassword()) onDone() },
            Modifier.testTag("new-password-submit"),
        )
    }
}

