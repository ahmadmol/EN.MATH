package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.model.StudyStage

val EntryTeal = Color(0xFF009F9D)
val EntryIndigo = Color(0xFF414BC6)
@Composable fun entryInk() = if (MaterialTheme.colorScheme.surface.luminance() > .5f) Color(0xFF0D205E) else MaterialTheme.colorScheme.onSurface

@Composable fun EntryBackdrop(background: Int = R.drawable.entry_bg_stage_picker, content: @Composable BoxScope.() -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            if (MaterialTheme.colorScheme.surface.luminance() > .5f)
                Image(painterResource(background), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), content = content)
        }
    }
}

@Composable fun EntryBrand(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(R.drawable.ic_enmath), null, Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)))
        Text("EN.MATH", color = entryInk(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
    }
}

@Composable fun EntryTitle(text: String) {
    Text(text, Modifier.fillMaxWidth(), color = entryInk(), style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
}

@Composable fun StagePicker(onStage: (StudyStage) -> Unit) {
    EntryBrand(Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start))
    Spacer(Modifier.height(20.dp))
    EntryTitle(entryText(com.ahmadmol.enmath.R.string.entry_copy_0))
    Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_1), Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = entryInk())
    StudyStage.entries.forEach { stage ->
        val g9 = stage == StudyStage.G9
        val accent = if (g9) EntryTeal else EntryIndigo
        Card(onClick = { onStage(stage) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("local-stage-${stage.name}"),
            shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(3.dp)) {
            Box(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(accent.copy(alpha=.24f), accent.copy(alpha=.08f))))) {
                Image(painterResource(if(g9) R.drawable.entry_student_grade9_cutout else R.drawable.entry_student_baccalaureate_cutout), null,
                    Modifier.fillMaxWidth().height(164.dp), contentScale = ContentScale.Fit)
            }
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if(g9) entryText(com.ahmadmol.enmath.R.string.entry_copy_2) else entryText(com.ahmadmol.enmath.R.string.entry_copy_3), color = entryInk(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if(g9) entryText(com.ahmadmol.enmath.R.string.entry_copy_4) else entryText(com.ahmadmol.enmath.R.string.entry_copy_5), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(50), color = accent, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("‹", color=Color.White, fontSize=32.sp) }
                }
            }
        }
    }
}

@Composable fun EntryHero(stage: StudyStage) {
    Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)).background(
        Brush.verticalGradient(listOf((if(stage==StudyStage.G9) EntryTeal else EntryIndigo).copy(alpha=.12f), MaterialTheme.colorScheme.surface)))) {
        if (!LocalIsDarkTheme.current) Image(painterResource(if(stage==StudyStage.G9) R.drawable.entry_bg_onboarding_01 else R.drawable.entry_bg_onboarding_02), null,
            Modifier.matchParentSize(), contentScale=ContentScale.Crop)
        Image(painterResource(if(stage==StudyStage.G9) R.drawable.entry_student_grade9_cutout else R.drawable.entry_student_baccalaureate_cutout), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    }
}

@Composable fun EntryAction(title: String, detail: String, icon: Int, modifier: Modifier = Modifier,
    primary: Boolean = false, stage: StudyStage = StudyStage.G9, enabled: Boolean = true, click: () -> Unit) {
    val accent = if(stage == StudyStage.G9) EntryTeal else EntryIndigo
    Card(onClick=click, enabled=enabled, modifier=modifier.fillMaxWidth(), shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.cardColors(containerColor=if(primary) accent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.7f))) {
        Row(Modifier.padding(16.dp).heightIn(min=56.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, color=if(primary) Color.White else entryInk(), style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.Bold)
                Text(detail, color=if(primary) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style=MaterialTheme.typography.bodySmall)
            }
            Surface(color=if(primary) Color.White else accent.copy(alpha=.1f), shape=RoundedCornerShape(50), modifier=Modifier.size(48.dp)) {
                Image(painterResource(icon), null, Modifier.padding(10.dp), contentScale=ContentScale.Fit)
            }
        }
    }
}

@Composable fun CurriculumTile(title: String, detail: String, modifier: Modifier = Modifier, indigo: Boolean = false, click: () -> Unit) {
    val accent = if(indigo) EntryIndigo else EntryTeal
    Card(onClick=click, modifier=modifier.fillMaxWidth(), shape=RoundedCornerShape(22.dp),
        colors=CardDefaults.cardColors(containerColor=accent.copy(alpha=.10f))) {
        Row(Modifier.padding(20.dp), horizontalArrangement=Arrangement.spacedBy(16.dp), verticalAlignment=Alignment.CenterVertically) {
            Image(painterResource(R.drawable.entry_ic_book_open), null, Modifier.size(52.dp))
            Column(Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(title, color=entryInk(), style=MaterialTheme.typography.titleLarge, fontWeight=FontWeight.Bold)
                Text(detail, color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}




@Composable fun entryText(@androidx.annotation.StringRes id: Int): String {
    val configuration = android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current)
    configuration.setLocale(java.util.Locale(if(LocalLanguage.current == com.ahmadmol.enmath.core.model.Language.Arabic) "ar" else "en"))
    return androidx.compose.ui.platform.LocalContext.current.createConfigurationContext(configuration).resources.getString(id)
}

@Composable fun StageEntryContent(stage: StudyStage, busy: Boolean = false, back: () -> Unit,
    guest: () -> Unit, create: () -> Unit, saved: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
        EntryBrand()
        TextButton(back) { Text(entryText(R.string.entry_copy_19)) }
    }
    EntryHero(stage)
    EntryTitle(entryText(if(stage==StudyStage.G9) R.string.entry_copy_20 else R.string.entry_copy_21))
    Text(entryText(R.string.entry_copy_22), Modifier.fillMaxWidth(), textAlign=TextAlign.Center)
    EntryAction(entryText(R.string.entry_copy_23), entryText(R.string.entry_copy_24), R.drawable.entry_ic_play,
        Modifier.testTag("local-open-guest"), primary=true, stage=stage, enabled=!busy, click=guest)
    EntryAction(entryText(R.string.entry_copy_25), entryText(R.string.entry_copy_26), R.drawable.entry_ic_user,
        stage=stage, enabled=!busy, click=create)
    EntryAction(entryText(R.string.entry_copy_27), entryText(R.string.entry_copy_28), R.drawable.entry_ic_folder,
        stage=stage, enabled=!busy, click=saved)
    EducationalInfoBanner(entryText(R.string.entry_copy_29))
}
