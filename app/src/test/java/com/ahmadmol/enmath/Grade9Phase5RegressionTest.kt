package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.library.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class Grade9Phase5RegressionTest {
    private val pdf = StudentFile("same", "content://test/document/1", "ملخص الجبر.pdf", "application/pdf", page = 3)
    @Test fun wordFilterDoesNotTreatEveryNonPdfAsWord() {
        val files = listOf(pdf, pdf.copy(id="word",mime="application/msword"),pdf.copy(id="bad",mime="text/plain"))
        assertEquals(listOf("word"),grade9Files(files,"",G9FileFilter.WORD).map { it.id })
    }
    @Test fun combinedWordsAndTypeMustAllMatchWithoutMutatingMetadata() {
        assertEquals(listOf(pdf),grade9Files(listOf(pdf),"الجبر ملخص",G9FileFilter.PDF))
        assertTrue(grade9Files(listOf(pdf),"الجبر هندسة",G9FileFilter.ALL).isEmpty())
        assertEquals(3,pdf.page)
    }
    @Test fun offlineProviderFailureIsNotReportedAsMissingPermission() {
        assertEquals(G9FileFailure.PROVIDER,grade9FileFailure(IOException("offline")))
        assertEquals(G9FileFailure.PERMISSION,grade9FileFailure(SecurityException()))
    }
    @Test fun sameDocumentIdCannotLeakPageOrFileAcrossOwnersAndRestarts() {
        var state = LearningState(user=User("A","",AccountType.Guest,"00000000-0000-0000-0000-000000000001",StudyStage.G9))
        val stores=mutableMapOf<String,FixturePreferences>()
        fun prefs(name:String)=stores.getOrPut(name){FixturePreferences()}.prefs
        var repo=Grade9StudyRepository(::prefs){state}
        val a=StudentCatalog.scope(state)!!
        val before=state
        assertEquals(PlanWrite.SAVED,repo.addFile(a,pdf))
        assertEquals(PlanWrite.SAVED,repo.filePage(a,pdf.id,7))
        state=state.copy(user=state.user!!.copy(localId="00000000-0000-0000-0000-000000000002"),generation=1)
        val b=StudentCatalog.scope(state)!!
        assertTrue((repo.read(b) as G9StudyRead.Ready).data.files.isEmpty())
        assertEquals(PlanWrite.OWNER_CHANGED,repo.removeFile(a,pdf.id))
        repo.addFile(b,pdf.copy(uri="content://test/other",page=0))
        state=before.copy(generation=2)
        repo=Grade9StudyRepository(::prefs){state}
        val restored=(repo.read(StudentCatalog.scope(state)!!) as G9StudyRead.Ready).data
        assertEquals(7,restored.files.single().page)
        assertEquals(pdf.uri,restored.files.single().uri)
        assertEquals(before.completed,state.completed)
        assertEquals(before.product,state.product)
        assertEquals(PlanWrite.SAVED,repo.removeFile(StudentCatalog.scope(state)!!,pdf.id))
        assertTrue((repo.read(StudentCatalog.scope(state)!!) as G9StudyRead.Ready).data.files.isEmpty())
    }
}
