package com.ahmadmol.enmath.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun CleanLoginScreen(
    vm: AuthViewModel,
    login: () -> Unit,
    register: () -> Unit,
    forgot: () -> Unit,
) {
    val email by vm.email.collectAsStateWithLifecycle()
    val password by vm.password.collectAsStateWithLifecycle()
    val role by vm.role.collectAsStateWithLifecycle()
    val teacher = role == AccountType.Teacher.name
    ScreenPage(Modifier.testTag("screen-login"), narrow = true) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.xs),
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
        PageHeading(
            tr("Welcome back", "أهلاً بعودتك"),
            if (teacher) tr("Teacher sign in", "تسجيل دخول المعلم")
            else tr("Student sign in", "تسجيل دخول الطالب"),
            if (teacher)
                tr(
                    "Manage your classes, assignments and resources.",
                    "أدر شعبك وواجباتك ومواردك التعليمية بكل سهولة.",
                )
            else
                tr(
                    "Continue learning, practicing and tracking your progress.",
                    "تابع دروسك وتمارينك وتقدّمك الدراسي بخطوات واثقة.",
                ),
        )
        RoleSelector(AccountType.valueOf(role), vm::chooseRole)
        AppCard(tonal = true) {
            Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
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
                TextButton(
                    onClick = forgot,
                    modifier = Modifier.align(Alignment.End).testTag("forgot"),
                ) {
                    Text(
                        label(TextKey.t_f1c9af17c3),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                PrimaryButton(
                    tr("Sign in", "تسجيل الدخول"),
                    { if (vm.validate(false)) login() },
                    Modifier.testTag("login-submit"),
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(horizontal = Space.md, vertical = Space.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    tr("New here?", "ليس لديك حساب؟"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = register, modifier = Modifier.testTag("register")) {
                    Text(
                        tr("Create account", "إنشاء حساب"),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        DemoNotice(
            tr(
                if (vm.demoAllowed) "Debug demo only. No server sign-in or cloud recovery occurs."
                else "Demo sign-in is disabled in production. Continue as a local guest. Cloud sign-in and recovery are not connected.",
                if (vm.demoAllowed) "دخول تجريبي للتطوير فقط، دون تسجيل دخول أو استعادة سحابية."
                else "الدخول التجريبي معطّل في الإنتاج. تابع كزائر محلي. تسجيل الدخول والاستعادة السحابيان غير متصلين.",
            )
        )
    }
}

