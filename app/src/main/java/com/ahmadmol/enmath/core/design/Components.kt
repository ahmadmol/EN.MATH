package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.LayoutDirection
import com.ahmadmol.enmath.core.model.*

@Composable
fun ScreenPage(
    modifier: Modifier = Modifier,
    narrow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier
                .widthIn(max = if (narrow) Space.formMax else Space.contentMax)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
            content = content,
        )
    }
}

@Composable
fun PageHeading(eyebrow: String, title: String, subtitle: String = "") {
    Column(verticalArrangement = Arrangement.spacedBy(Space.sm)) {
        Text(
            eyebrow,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle.isNotEmpty())
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BrandMark() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.md),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(Space.symbol),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "∑",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Column {
            Text("EN.MATH", style = MaterialTheme.typography.titleLarge)
            Text(
                label(TextKey.t_7a74be9631),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = Space.touch),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(Space.lg),
    ) {
        Text(text)
    }
}

@Composable
fun AppTextField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 5,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions =
            KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        visualTransformation =
            if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon =
            if (password)
                ({
                    TextButton(onClick = { visible = !visible }) {
                        Text(
                            if (visible) label(TextKey.t_a47bb75a14)
                            else label(TextKey.t_996be0fcf0)
                        )
                    }
                })
            else null,
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tonal: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors =
        CardDefaults.cardColors(
            containerColor =
                if (tonal) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
        )
    if (onClick != null)
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = colors,
        ) {
            Column(
                Modifier.padding(Space.xl),
                verticalArrangement = Arrangement.spacedBy(Space.md),
                content = content,
            )
        }
    else
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = colors,
        ) {
            Column(
                Modifier.padding(Space.xl),
                verticalArrangement = Arrangement.spacedBy(Space.md),
                content = content,
            )
        }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun ProgressCard(title: String, subtitle: String, fraction: Float, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun CourseCard(book: Book, completed: Set<String>, onClick: () -> Unit) {
    val count = book.lessons.count { it.id in completed }
    AppCard(onClick = onClick, modifier = Modifier.testTag("book-${book.id}")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SymbolBadge(if (book.id == "book-1") "∫" else "π")
            Column(Modifier.weight(1f).padding(horizontal = Space.lg)) {
                Text(book.title.text(), style = MaterialTheme.typography.titleLarge)
                Text(book.subtitle.text(), style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null)
        }
        Text(
            label(TextKey.t_d2b9e51dce, count, book.lessons.size),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LinearProgressIndicator(
            progress = { count.toFloat() / book.lessons.size },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun TopicCard(topic: Topic, completed: Set<String>, onClick: () -> Unit) {
    AppCard(onClick = onClick, modifier = Modifier.testTag("topic-${topic.id}")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            SymbolBadge(topic.symbol)
            Column(Modifier.weight(1f)) {
                Text(topic.title.text(), style = MaterialTheme.typography.titleMedium)
                Text(
                    label(
                        TextKey.t_4a5c00c5b2,
                        topic.lessons.count { it.id in completed },
                        topic.lessons.size,
                    )
                )
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
        }
    }
}

@Composable
fun SymbolBadge(symbol: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.size(Space.symbol),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun MathText(
    value: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        MathRender(value, style)
    }
}

@Composable
fun DemoNotice(text: String = label(TextKey.t_57cf7f7bed)) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text,
            Modifier.fillMaxWidth().padding(Space.md),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
fun EmptyState(title: String, detail: String) {
    AppCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(detail)
    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onNotifications: (() -> Unit)? = null,
    unread: Int = 0,
) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            if (onBack != null)
                IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, label(TextKey.t_794ab43a2c))
                }
        },
        actions = {
            if (onSearch != null)
                IconButton(onSearch, Modifier.testTag("global-search")) {
                    Icon(Icons.Outlined.Search, label(TextKey.t_67b32d1cb9))
                }
            if (onNotifications != null)
                IconButton(onNotifications, Modifier.testTag("global-notifications")) {
                    BadgedBox(badge = { if (unread > 0) Badge { Text("$unread") } }) {
                        Icon(Icons.Outlined.Notifications, label(TextKey.t_bda3e17468))
                    }
                }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    AppCard(modifier) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CompletedLabel() {
    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.secondary)
        Text(label(TextKey.t_ac2e05afaf))
    }
}
