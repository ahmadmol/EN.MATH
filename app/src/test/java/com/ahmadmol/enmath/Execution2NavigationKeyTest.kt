package com.ahmadmol.enmath
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.navigationSessionKey
import org.junit.Assert.*
import org.junit.Test

class Execution2NavigationKeyTest {
    private fun student(id:String, generation:Long)=LearningState(user=User("Same name","",AccountType.Student,id,StudyStage.BAC),generation=generation)
    @Test fun ownerGenerationAndStageCannotReuseAnotherNavStateNamespace() {
        val a=student("00000000-0000-0000-0000-000000000001",1)
        val b=student("00000000-0000-0000-0000-000000000002",1)
        assertNotEquals(navigationSessionKey(a),navigationSessionKey(b))
        assertNotEquals(navigationSessionKey(a),navigationSessionKey(a.copy(generation=2)))
        assertNotEquals(navigationSessionKey(a),navigationSessionKey(a.copy(user=a.user!!.copy(studyStage=StudyStage.G9))))
    }
    @Test fun studentStateCannotBeRestoredAsTeacherOrAnonymousState() {
        val a=student("00000000-0000-0000-0000-000000000001",1)
        assertNotEquals(navigationSessionKey(a),navigationSessionKey(LearningState()))
        assertNotEquals(navigationSessionKey(a),navigationSessionKey(LearningState(user=User("Teacher","teacher@example.test",AccountType.Teacher))))
    }
}
