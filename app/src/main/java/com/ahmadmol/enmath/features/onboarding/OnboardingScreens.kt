package com.ahmadmol.enmath.features.onboarding

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.tokens.ResourceDimensions
import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class IntroPage(
    val symbol: String,
    val title: TextKey,
    val body: TextKey,
    val formula: String,
)

private val pages =
    listOf(
        IntroPage("∫", TextKey.c_11927202fd, TextKey.c_eced6e5c06, "∫ 2x dx = x² + C"),
        IntroPage("↗", TextKey.c_0247df0668, TextKey.c_a3958a373d, "Today → Tomorrow → Your goal"),
        IntroPage("x", TextKey.c_3597612c71, TextKey.c_6eaf2a3728, "2x + 4 = 10 → x = 3"),
    )

@Composable
fun SplashScreen(onReady: () -> Unit) {
    val callback by rememberUpdatedState(onReady)
    var alpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450)
        ) { value, _ -> alpha = value }
        delay(600)
        callback()
    }
    Surface(
        modifier = Modifier.fillMaxSize().testTag("screen-splash"),
        color = Color(0xFF070C1E)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0052D4).copy(alpha = 0.40f),
                            Color(0xFF00D2FF).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.minDimension * 0.60f
                    ),
                    radius = size.minDimension * 0.60f,
                    center = center
                )
            }
            Column(
                modifier = Modifier.graphicsLayer { this.alpha = alpha },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Space.xl)
            ) {
                MnLogoMark(Modifier.size(140.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "E N . M A T H",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.28.em
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(Space.xs))
                    Text(
                        text = tr("Interactive Mathematics Platform", "منصة الرياضيات المتكاملة"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8A99AD)
                    )
                }
                Spacer(Modifier.height(Space.sm))
                LinearProgressIndicator(
                    modifier = Modifier.width(130.dp).height(3.dp),
                    color = Color(0xFF00E5FF),
                    trackColor = Color(0xFF0F1A3A)
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    Column(
        Modifier
            .fillMaxSize()
            .testTag("screen-onboarding")
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.xl, vertical = Space.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm)
            ) {
                MnLogoMark(Modifier.size(36.dp))
                Text(
                    "EN.MATH",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                TextButton(
                    onClick = onDone,
                    modifier = Modifier.testTag("intro-skip"),
                    contentPadding = PaddingValues(horizontal = Space.md, vertical = Space.xs)
                ) {
                    Text(
                        label(TextKey.t_485c964fc7),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            ScreenPage(narrow = true) {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    tonal = true
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.md),
                        modifier = Modifier.padding(vertical = Space.sm)
                    ) {
                        LearningArtwork(page.symbol, Modifier.height(ResourceDimensions.entryArtwork))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = Space.xs)
                        ) {
                            Text(
                                text = page.formula,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = Space.lg, vertical = Space.sm)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Space.sm))

                Text(
                    page.title.text(),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Text(
                    page.body.text(),
                    Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            Modifier
                .widthIn(max = Space.formMax)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { i ->
                    val isSelected = i == pager.currentPage
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = Space.xs)
                            .size(
                                width = if (isSelected) 28.dp else 10.dp,
                                height = 10.dp,
                            ),
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ) {}
                }
            }

            PrimaryButton(
                text = if (pager.currentPage == pages.lastIndex) label(TextKey.t_df60f8d791)
                else label(TextKey.t_704198b924),
                onClick = {
                    if (pager.currentPage == pages.lastIndex) onDone()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                modifier = Modifier.testTag("intro-next"),
            )
        }
    }
}

@Composable
fun GetStartedScreen(onRole: (AccountType) -> Unit, onGuest: () -> Unit) {
    ScreenPage(Modifier.testTag("screen-started"), narrow = true) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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

        PageHeading(
            eyebrow = label(TextKey.t_dbce05439f),
            title = label(TextKey.t_c9c7136316),
            subtitle = label(TextKey.t_479e114db0),
        )

        AppCard(
            onClick = { onRole(AccountType.Student) },
            tonal = true,
            modifier = Modifier.testTag("role-student"),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.lg),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.School,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            label(TextKey.t_4f567e1c20),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                tr("Student", "طالب"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = Space.sm, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        label(TextKey.t_51d2fe6071),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        AppCard(
            onClick = { onRole(AccountType.Teacher) },
            modifier = Modifier.testTag("role-teacher"),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.lg),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.CoPresent,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            label(TextKey.t_0f82bcf371),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                tr("Teacher", "معلم"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = Space.sm, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        label(TextKey.t_44c7ecd060),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onGuest,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Space.touch)
                .testTag("guest"),
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    label(TextKey.t_bb13a814b0),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        DemoNotice()
    }
}

