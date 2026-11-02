package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.remember
import com.ahmadmol.enmath.core.model.Language
import java.text.NumberFormat
import java.util.Locale

@Composable
fun learningNumber(value: Int): String {
    val language = LocalLanguage.current
    return remember(value, language) {
        NumberFormat.getIntegerInstance(if (language == Language.Arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH).format(value)
    }
}

@Composable
fun learningPercent(value: Int): String {
    val language = LocalLanguage.current
    return remember(value, language) {
        NumberFormat.getPercentInstance(if (language == Language.Arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH).format(value / 100.0)
    }
}

/** Scoped palette: preserves the app's selected theme and Arabic typography. */
@Composable
fun PremiumLearningTheme(content: @Composable () -> Unit) {
    if (LocalStudentExperience.current) {
        content()
        return
    }
    val dark = MaterialTheme.colorScheme.background.luminance() < .5f
    val base = MaterialTheme.colorScheme
    MaterialTheme(colorScheme = base.copy(
        primary = if (dark) Color(0xFF23C6F5) else Color(0xFF0063BF),
        secondary = if (dark) Color(0xFF1AD5B3) else Color(0xFF007D69),
        tertiary = if (dark) Color(0xFFB29AFF) else Color(0xFF7142C2),
        background = if (dark) Color(0xFF081426) else Color(0xFFF3F7FD),
        surface = if (dark) Color(0xFF10243C) else Color.White,
        surfaceVariant = if (dark) Color(0xFF152D49) else Color(0xFFE8F1FC),
        outlineVariant = if (dark) Color(0xFF285D98) else Color(0xFFB8D1EB),
        onSurface = if (dark) Color(0xFFF5F8FF) else Color(0xFF10243C),
        onSurfaceVariant = if (dark) Color(0xFFA6B7D2) else Color(0xFF455D78),
    ), content = content)
}

@Composable
fun PremiumPage(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    PremiumLearningTheme {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ScreenPage(modifier, narrow = true, content = content)
        }
    }
}

@Composable
fun PremiumSubScreenTopBar(title: String, onBack: () -> Unit, onSearch: (() -> Unit)?,
    onNotifications: () -> Unit, unread: Int, circular: Boolean = false) {
    PremiumLearningTheme {
        val fontScale = LocalDensity.current.fontScale
        val buttonShape = if (circular) androidx.compose.foundation.shape.CircleShape else RoundedCornerShape(16.dp)
        val actions: @Composable RowScope.() -> Unit = {
            if (onSearch != null) OutlinedIconButton(onSearch, Modifier.size(48.dp).testTag("global-search"), shape = buttonShape) {
                Icon(Icons.Outlined.Search, tr("Search", "بحث"), tint = MaterialTheme.colorScheme.primary)
            }
            OutlinedIconButton(onNotifications, Modifier.size(48.dp).testTag("global-notifications"), shape = buttonShape) {
                BadgedBox(badge = { if (unread > 0) Badge { Text(learningNumber(unread)) } }) {
                    Icon(Icons.Outlined.Notifications, tr("Notifications: $unread unread", "الإشعارات: $unread غير مقروءة"), tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.background) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                val wrapActions = maxWidth < 340.dp || fontScale > 1.3f
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedIconButton(onBack, Modifier.size(48.dp).testTag("back"), shape = buttonShape) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, tr("Back", "رجوع"), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                        if (!wrapActions) actions()
                    }
                    if (wrapActions) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), content = actions)
                }
            }
        }
    }
}


@Composable
fun PremiumHeroCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null,
    accent: Color = MaterialTheme.colorScheme.primary, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val body: @Composable () -> Unit = {
        Column(Modifier.background(Brush.linearGradient(listOf(
            accent.copy(alpha = .10f), Color.Transparent, accent.copy(alpha = .04f)
        ))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
    if (onClick == null) Surface(modifier.fillMaxWidth(), shape = shape,
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, accent.copy(alpha = .48f)), content = body)
    else Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = shape,
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, accent.copy(alpha = .48f)), content = body)
}

/** Stacks at narrow widths or large accessibility font sizes. */
@Composable
fun PremiumHeroLayout(artwork: @Composable () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints {
        if (maxWidth < 300.dp || fontScale > 1.3f) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
                Box(Modifier.fillMaxWidth().clearAndSetSemantics {}) { artwork() }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(.62f), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
                Box(Modifier.weight(.38f).clearAndSetSemantics {}) { artwork() }
            }
        }
    }
}

@Composable
fun LearningStatisticsCards(items: List<Pair<String, String>>) {
    val fontScale = LocalDensity.current.fontScale
    val item: @Composable (Pair<String, String>, Modifier) -> Unit = { (value, title), modifier ->
        Surface(modifier, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
    BoxWithConstraints {
        if (maxWidth < 280.dp || fontScale > 1.3f) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { items.forEach { item(it, Modifier.fillMaxWidth()) } }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item(it, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun PremiumBookArtwork(symbol: String) {
    Box(Modifier.fillMaxWidth().clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.widthIn(max = 180.dp).fillMaxWidth()) {
            LearningArtwork(symbol, Modifier.height(maxWidth))
        }
    }
}

@Composable
fun RewardChestArtwork(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val edge = minOf(size.width, size.height)
            val w = edge; val h = edge
            translate((size.width - edge) / 2, (size.height - edge) / 2) {
                drawCircle(Brush.radialGradient(listOf(Color(0xFF23C6F5).copy(alpha = .32f), Color.Transparent)), h*.55f, Offset(w*.5f,h*.5f))
                val lid = Path().apply {
                    moveTo(w*.32f,h*.40f); lineTo(w*.28f,h*.15f); lineTo(w*.63f,h*.20f); lineTo(w*.70f,h*.44f); close()
                }
                drawPath(lid, Brush.linearGradient(listOf(Color(0xFF168BFF),Color(0xFF13286D))))
                drawPath(lid, Color(0xFF74DFFF), style=Stroke(2.dp.toPx()))
                val body = Path().apply {
                    moveTo(w*.30f,h*.47f); lineTo(w*.69f,h*.47f); lineTo(w*.66f,h*.85f); lineTo(w*.33f,h*.85f); close()
                }
                drawPath(body, Brush.linearGradient(listOf(Color(0xFF338BFF), Color(0xFF25309A))))
                drawPath(body, Color(0xFF75D9FF), style=Stroke(2.dp.toPx()))
                drawLine(Color(0xFF9EF4FF), Offset(w*.32f,h*.49f),Offset(w*.67f,h*.49f),3.dp.toPx())
                drawRoundRect(Color(0xFF89BFFF),Offset(w*.47f,h*.47f),androidx.compose.ui.geometry.Size(w*.06f,h*.13f),androidx.compose.ui.geometry.CornerRadius(4f))
                listOf(.25f to .36f,.74f to .3f,.59f to .08f).forEach { (x,y) ->
                    drawLine(Color(0xFF53D9FF),Offset(w*x-4,h*y),Offset(w*x+4,h*y),2.dp.toPx())
                    drawLine(Color(0xFF53D9FF),Offset(w*x,h*y-6),Offset(w*x,h*y+6),2.dp.toPx())
                }
            }
        }
    }
}

@Composable
fun LibraryBooksArtwork() {
    Canvas(Modifier.fillMaxWidth().height(144.dp)) {
        val edge = minOf(size.width, size.height)
        val w = edge; val h = edge
        translate((size.width - edge) / 2, (size.height - edge) / 2) {
            drawCircle(Brush.radialGradient(listOf(Color(0xFF168BFF).copy(alpha=.30f),Color.Transparent)),w*.55f,Offset(w*.5f,h*.5f))
            for (i in 0..2) {
                val y = .62f-i*.19f
                fun face(vararg points: Pair<Float,Float>) = Path().apply {
                    points.forEachIndexed { index,(x,v) -> if(index==0) moveTo(w*x,h*v) else lineTo(w*x,h*v) }; close()
                }
                val cover = face(.12f to y,.64f to y-.15f,.91f to y-.03f,.38f to y+.15f)
                val pages = face(.38f to y+.15f,.91f to y-.03f,.91f to y+.10f,.38f to y+.28f)
                val spine = face(.12f to y,.38f to y+.15f,.38f to y+.28f,.12f to y+.13f)
                drawPath(pages,Color(0xFF9BD7FF))
                drawPath(spine,Brush.linearGradient(listOf(Color(0xFF007FFF),Color(0xFF102D88))))
                drawPath(cover,Brush.linearGradient(listOf(Color(0xFF168BFF),Color(0xFF0043BD))))
                drawPath(cover,Color(0xFF23C6F5).copy(alpha=.7f),style=Stroke(1.dp.toPx()))
                for (line in 1..3) drawLine(Color(0xFF387AB6),Offset(w*.41f,h*(y+.15f+line*.025f)),Offset(w*.88f,h*(y-.01f+line*.025f)),.6.dp.toPx())
            }
        }
    }
}

@Composable
fun LearningProgressBar(done: Int, total: Int, accent: Color = MaterialTheme.colorScheme.primary) {
    val progress = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else 0f
    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp),
        color = accent, trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .45f))
}

@Composable
fun EducationalInfoBanner(text: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PremiumActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.AutoStories, enabled: Boolean = true) {
    Surface(onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = Color(0xFF006AE8)) {
        Row(Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF0062F5), Color(0xFF007BA8))))
            .heightIn(min = 60.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = Color.White)
            Text(text, Modifier.weight(1f), color = Color.White, style = MaterialTheme.typography.titleMedium)
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = Color.White)
        }
    }
}

@Composable
fun PremiumSymbol(symbol: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(modifier.width(56.dp).heightIn(min = 56.dp).clearAndSetSemantics {}, shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = .16f),
        border = BorderStroke(1.dp, accent.copy(alpha = .5f))) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, Modifier.padding(6.dp), color = accent, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ChapterProgressCard(title: String, subtitle: String, symbol: String, done: Int, total: Int,
    accent: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PremiumHeroCard(modifier, onClick, accent) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PremiumSymbol(symbol, accent)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = accent)
        }
        LearningProgressBar(done, total, accent)
    }
}

/** Decorative faceted diamond. No UI text or financial data baked into the illustration. */
@Composable
fun GemArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier.height(150.dp).fillMaxWidth()) {
        val edge = minOf(size.width, size.height)
        val w = edge; val h = edge
        translate((size.width - edge) / 2, (size.height - edge) / 2) {
            drawCircle(Brush.radialGradient(listOf(Color(0xFF168BFF).copy(alpha = .3f), Color.Transparent)), h * .50f, Offset(w*.5f,h*.5f))
            fun face(vararg p: Pair<Float,Float>) = Path().apply {
                p.forEachIndexed { i, (x,y) -> if(i==0) moveTo(w*x,h*y) else lineTo(w*x,h*y) }; close()
            }
            val points = listOf(.08f to .34f, .25f to .10f, .75f to .10f, .92f to .34f, .5f to .90f)
            val colors = listOf(Color(0xFF6673FF),Color(0xFF52EAFF),Color(0xFF168BFF),Color(0xFF5344EB),Color(0xFF24C7FF))
            points.indices.forEach { i ->
                val path = face(points[i], points[(i+1)%points.size], .5f to .36f)
                drawPath(path, Brush.linearGradient(listOf(colors[i], Color(0xFF0059CB))))
                drawPath(path, Color(0xFFB2F5FF), style = Stroke(1.dp.toPx()))
            }
            drawOval(Brush.radialGradient(listOf(Color(0xFF168BFF).copy(alpha=.5f),Color.Transparent)),
                Offset(w*.22f,h*.9f), androidx.compose.ui.geometry.Size(w*.56f,h*.08f))
        }
    }
}
