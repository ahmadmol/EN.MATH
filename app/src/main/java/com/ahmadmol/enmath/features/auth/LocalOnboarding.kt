package com.ahmadmol.enmath.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.design.*
import kotlinx.coroutines.launch

@Composable fun LocalOnboarding(initialPage: Int = 0, onDone: () -> Unit) {
    val pager = rememberPagerState(initialPage = initialPage, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf(false) }
    val finish = { error = runCatching(onDone).isFailure }
    BackHandler(pager.currentPage > 0) { scope.launch { pager.scrollToPage(pager.currentPage - 1) } }
    EntryBackdrop(when(pager.currentPage) { 0 -> R.drawable.entry_bg_onboarding_01; 1 -> R.drawable.entry_bg_onboarding_02; else -> R.drawable.entry_bg_onboarding_03 }) {
        Column(Modifier.widthIn(max=520.dp).fillMaxSize().testTag("screen-onboarding").align(Alignment.TopCenter).padding(horizontal=20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
                EntryBrand()
                TextButton(finish, Modifier.testTag("onboarding-skip")) { Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_6)) }
            }
            HorizontalPager(pager, Modifier.weight(1f).testTag("onboarding-pager")) { page ->
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment=Alignment.CenterHorizontally) {
                    Image(painterResource(when(page) { 0 -> R.drawable.entry_hero_onboarding_01_learn; 1 -> R.drawable.entry_hero_onboarding_02_study; else -> R.drawable.entry_hero_onboarding_03_progress }), null,
                        Modifier.fillMaxWidth().heightIn(max=400.dp).aspectRatio(.95f).clip(RoundedCornerShape(24.dp)), contentScale=ContentScale.Fit)
                    Surface(shape=RoundedCornerShape(topStart=28.dp, topEnd=28.dp), color=MaterialTheme.colorScheme.surface) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
                            EntryTitle(when(page) { 0 -> entryText(com.ahmadmol.enmath.R.string.entry_copy_7); 1 -> entryText(com.ahmadmol.enmath.R.string.entry_copy_8); else -> entryText(com.ahmadmol.enmath.R.string.entry_copy_9) })
                            Text(when(page) { 0 -> entryText(com.ahmadmol.enmath.R.string.entry_copy_10); 1 -> entryText(com.ahmadmol.enmath.R.string.entry_copy_11); else -> entryText(com.ahmadmol.enmath.R.string.entry_copy_12) }, Modifier.fillMaxWidth(), textAlign=TextAlign.Center, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement=Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                repeat(3) { index -> Box(Modifier.size(if(index==pager.currentPage) 12.dp else 9.dp).background(if(index==pager.currentPage) EntryIndigo else EntryIndigo.copy(alpha=.2f), CircleShape)) }
            }
            Button(onClick={ if(pager.currentPage==2) finish() else scope.launch { pager.scrollToPage(pager.currentPage+1) } },
                modifier=Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("onboarding-continue"), shape=RoundedCornerShape(28.dp)) {
                Text(if(pager.currentPage==2) entryText(com.ahmadmol.enmath.R.string.entry_copy_13) else entryText(com.ahmadmol.enmath.R.string.entry_copy_14))
            }
            if(error) Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_15), color=MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
        }
    }
}
