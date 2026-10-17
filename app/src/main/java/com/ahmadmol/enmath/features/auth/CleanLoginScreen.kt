package com.ahmadmol.enmath.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
            verticalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            BookLogo(Modifier.size(Space.hero + Space.xl))
            Text("EN.MATH", style = MaterialTheme.typography.headlineSmall)
        }
        PageHeading(
            tr("Welcome back", "أهلاً بعودتك"),
            if (teacher) tr("Teacher sign in", "تسجيل دخول المعلم")
            else tr("Student sign in", "تسجيل دخول الطالب"),
            if (teacher)
                tr(
                    "Manage your classes, assignments and resources.",
                    "أدر شعبك وواجباتك ومواردك التعليمية.",
                )
            else
                tr(
                    "Continue learning, practicing and tracking your progress.",
                    "تابع دروسك وتمارينك وتقدّمك الدراسي.",
                ),
        )
        RoleSelector(AccountType.valueOf(role), vm::chooseRole)
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
                Text(label(TextKey.t_f1c9af17c3))
            }
            PrimaryButton(
                tr("Sign in", "تسجيل الدخول"),
                { if (vm.validate(false)) login() },
                Modifier.testTag("login-submit"),
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(tr("New here?", "ليس لديك حساب؟"), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = register, modifier = Modifier.testTag("register")) {
                Text(tr("Create account", "إنشاء حساب"))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Icon(
                Icons.Outlined.Info,
                null,
                Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                tr(
                    "Demo only: use a valid email and at least 8 password characters. No server sign-in occurs.",
                    "دخول تجريبي: استخدم بريداً صحيحاً وكلمة مرور من 8 أحرف على الأقل. لا يتم الاتصال بخادم حسابات.",
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
