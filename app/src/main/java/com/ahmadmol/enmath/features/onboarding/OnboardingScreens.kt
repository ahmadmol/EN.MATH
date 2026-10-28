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
    LaunchedEffect(Unit) { withFrameNanos { }; callback() }
    EntryBackdrop(com.ahmadmol.enmath.R.drawable.entry_bg_splash) {
        Column(Modifier.fillMaxSize().testTag("screen-splash").padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.SpaceEvenly) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(com.ahmadmol.enmath.R.drawable.ic_enmath), null, Modifier.size(110.dp))
            Text("EN.MATH", color=entryInk(), fontWeight=FontWeight.ExtraBold, style=MaterialTheme.typography.displaySmall)
            Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_44), color=entryInk(), textAlign=TextAlign.Center)
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(com.ahmadmol.enmath.R.drawable.entry_student_grade9_cutout), null,
                Modifier.fillMaxWidth().weight(1f).padding(top=24.dp), contentScale=androidx.compose.ui.layout.ContentScale.Fit)
            Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_45), Modifier.padding(vertical=20.dp), color=entryInk(), textAlign=TextAlign.Center)
        }
    }
}
@Composable
fun OnboardingScreen(onDone: () -> Unit) { com.ahmadmol.enmath.features.auth.LocalOnboarding(onDone = onDone) }

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
                                entryText(com.ahmadmol.enmath.R.string.entry_copy_46),
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
                                entryText(com.ahmadmol.enmath.R.string.entry_copy_47),
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
