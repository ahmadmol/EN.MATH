package com.ahmadmol.enmath.features.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.design.*
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
    LaunchedEffect(Unit) {
        delay(900)
        callback()
    }
    Box(Modifier.fillMaxSize().testTag("screen-splash"), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.xl),
        ) {
            BrandMark()
            Text(label(TextKey.t_2e3ea6aca1), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().testTag("screen-onboarding")) {
        Row(
            Modifier.fillMaxWidth().padding(Space.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "EN.MATH",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDone, modifier = Modifier.testTag("intro-skip")) {
                Text(label(TextKey.t_485c964fc7))
            }
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            ScreenPage(narrow = true) {
                AppCard(tonal = true) {
                    Spacer(Modifier.height(Space.xl))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            page.symbol,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = .15f))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        MathText(page.formula)
                    }
                    Spacer(Modifier.height(Space.xl))
                }
                Text(page.title.text(), style = MaterialTheme.typography.headlineLarge)
                Text(page.body.text(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(
            Modifier.widthIn(max = Space.formMax)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(Space.xl),
            verticalArrangement = Arrangement.spacedBy(Space.lg),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                pages.indices.forEach { i ->
                    Surface(
                        modifier =
                            Modifier.padding(Space.xs)
                                .size(
                                    width = if (i == pager.currentPage) Space.xl else Space.sm,
                                    height = Space.sm,
                                ),
                        shape = MaterialTheme.shapes.small,
                        color =
                            if (i == pager.currentPage) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                    ) {}
                }
            }
            PrimaryButton(
                if (pager.currentPage == pages.lastIndex) label(TextKey.t_df60f8d791)
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
        BrandMark()
        Spacer(Modifier.height(Space.lg))
        PageHeading(
            label(TextKey.t_dbce05439f),
            label(TextKey.t_c9c7136316),
            label(TextKey.t_479e114db0),
        )
        AppCard(
            onClick = { onRole(AccountType.Student) },
            tonal = true,
            modifier = Modifier.testTag("role-student"),
        ) {
            Text(label(TextKey.t_4f567e1c20), style = MaterialTheme.typography.titleLarge)
            Text(label(TextKey.t_51d2fe6071))
        }
        AppCard(
            onClick = { onRole(AccountType.Teacher) },
            modifier = Modifier.testTag("role-teacher"),
        ) {
            Text(label(TextKey.t_0f82bcf371), style = MaterialTheme.typography.titleLarge)
            Text(label(TextKey.t_44c7ecd060))
        }
        OutlinedButton(
            onClick = onGuest,
            modifier = Modifier.fillMaxWidth().heightIn(min = Space.touch).testTag("guest"),
        ) {
            Text(label(TextKey.t_bb13a814b0))
        }
        DemoNotice()
    }
}
