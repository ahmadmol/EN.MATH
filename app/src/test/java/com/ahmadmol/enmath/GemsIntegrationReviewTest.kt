package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import com.ahmadmol.enmath.core.design.*
import androidx.compose.ui.text.style.BaselineShift
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test

class GemsIntegrationReviewTest {
    private val student=User("Student","s@example.test",AccountType.Student)
    @Test fun productionCannotEscalateStudentTeacherOrGuestThroughDemoSwitch() {
        listOf(student,student.copy(type=AccountType.Teacher),student.copy(type=AccountType.Guest)).forEach {
            assertNull(DemoRolePolicy.target(it,false))
        }
        assertNull(DemoRolePolicy.target(null,false))
    }
    @Test fun debugSwitchNeedsAnExistingBoundLocalAccount() {
        assertEquals(AccountType.Teacher,DemoRolePolicy.target(student,true))
        assertEquals(AccountType.Student,DemoRolePolicy.target(student.copy(type=AccountType.Teacher),true))
        assertNull(DemoRolePolicy.target(student.copy(type=AccountType.Guest),true))
        assertNull(DemoRolePolicy.target(student.copy(email=""),true))
        assertNull(DemoRolePolicy.target(student.copy(type=AccountType.Teacher,email=""),true))
    }
    @Test fun teacherOrBlankEmailExplainsMissingStudentWalletWithoutSeeding() {
        assertEquals("s@example.test",LocalGemsPolicy.owner(student))
        assertNull(LocalGemsPolicy.owner(student.copy(type=AccountType.Teacher)))
        assertNull(LocalGemsPolicy.owner(student.copy(email="")))
        assertNull(LocalGemsPolicy.owner(student.copy(type=AccountType.Guest)))
    }
    @Test fun forcedViewModelSwitchIsDeniedWithoutDebugContextAndKeepsProgress() {
        val initial=LearningState(user=student,completed=setOf("limits-foundations"))
        val repo=object:LearningRepository {
            override val state=MutableStateFlow(initial)
            override fun signIn(user:User) {state.value=state.value.copy(user=user)}
            override fun logout() {state.value=state.value.copy(user=null)}
            override fun finishOnboarding() {}
            override fun completeLesson(id:String) {}
            override fun openLesson(id:String) {}
            override fun setTheme(mode:ThemeMode) {}
            override fun setLanguage(language:Language) {}
            override fun changeProduct(transform:(ProductState)->ProductState) {}
        }
        val vm=AppViewModel(repo)
        assertFalse(vm.canSwitchDemoRole());assertNull(vm.switchDemoRole());assertEquals(initial,repo.state.value)
    }
    @Test fun caretPowersAreRaisedWithoutEvaluatingOrChangingTheirSigns() {
        val value=nativeMathAnnotated("x^2 + x^(-1) + y^(n+1)")
        assertEquals("x2 + x-1 + yn+1",value.text)
        assertEquals(3,value.spanStyles.count {it.item.baselineShift==BaselineShift.Superscript})
    }
    @Test fun complexOrChainedPowersRemainLiteral() {
        listOf("x^2^3","x^(1/(n+1))","x^sin(x)").forEach {assertEquals(it,nativeMathAnnotated(it).text)}
    }
    @Test fun existingUnicodePowersAndFractionOperandsArePreserved() {
        assertEquals("x² + eˣ",nativeMathAnnotated("x² + eˣ").text)
        val p=nativeEquationParts("(x^2-4)/(x-2)")
        assertEquals("(x^2-4)",p.numerator);assertEquals("(x-2)",p.denominator)
        assertEquals("(x2-4)",nativeMathAnnotated(p.numerator).text)
    }
    @Test fun intervalsAndInequalitiesAreIsolatedInArabicProse() {
        val raw="المجال x ∈ [0, +∞[ فقط؛ والشرط x>0 ضروري"
        val display=isolateMathInArabic(raw)
        assertTrue(display.contains("\u2066x ∈ [0, +∞[\u2069"))
        assertTrue(display.contains("\u2066x>0\u2069"))
        assertEquals(raw,display.replace("\u2066","").replace("\u2069",""))
    }
    @Test fun plainEnglishMathNeedsNoBidiControls() {
        val raw="Domain x ∈ R, x != 0; x^2"
        assertEquals(raw,isolateMathInArabic(raw))
    }
}
