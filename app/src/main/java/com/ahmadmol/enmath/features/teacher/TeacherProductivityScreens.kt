package com.ahmadmol.enmath.features.teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.design.components.teacher.*
import com.ahmadmol.enmath.core.model.Classroom
import com.ahmadmol.enmath.core.navigation.Routes
import java.text.DateFormat
import java.util.Date

/** Read-only presentation aggregation; every score delegates to the existing LearningRules. */
private fun needsSupport(state: LearningState, c: Classroom, topicId: String = c.topicId) =
    c.studentIds.count { LearningRules.topic(state, topicId, it).score < 45 }

private fun assignedTopicCount(state: LearningState, c: Classroom, topicId: String = c.topicId) =
    state.product.assignments.count { a ->
        a.classId == c.id &&
            a.questionIds.any { q -> state.questions.any { it.id == q && it.topicId == topicId } }
    }

@Composable
fun TeacherHome(state: LearningState, open: (String) -> Unit, switchRole: (() -> Unit)?) {
    val classes = state.product.classes
    val members = classes.flatMap { it.studentIds }.distinct()
    val scores =
        classes.flatMap { c ->
            c.studentIds.map { LearningRules.topic(state, c.topicId, it).score }
        }
    val average = if (scores.isEmpty()) 0 else scores.average().toInt()
    val support =
        classes
            .flatMap { c ->
                c.studentIds.filter { LearningRules.topic(state, c.topicId, it).score < 45 }
            }
            .distinct()
            .size
    val target = classes.maxByOrNull { needsSupport(state, it) }
    val active = state.product.assignments.filter { it.due >= System.currentTimeMillis() }
    ExperiencePage("screen-teacher-home", teacher = true) {
        TeacherSummary(state.user?.name.orEmpty(), members.size, classes.size, average)
        TeacherInsightPanel(
            tr("Needs attention", "بحاجة إلى متابعة"),
            tr(
                "$support students have less than 45% mastery in their class topic. Open the class to review the context before acting.",
                "$support طلاب إتقانهم أقل من 45% في موضوع شعبتهم. افتح الشعبة لفهم التفاصيل قبل اتخاذ إجراء.",
            ),
            if (target != null) tr("Review ${target.name.text()}", "مراجعة ${target.name.text()}")
            else tr("Manage classes", "إدارة الشعب"),
            { open(if (target != null) "teacher/classes/${target.id}" else Routes.TeacherClasses) },
        )
        ExperienceSection(
            tr("Classes", "الشعب الدراسية"),
            tr("Mastery of each class's current topic", "إتقان الموضوع الحالي لكل شعبة"),
        )
        if (classes.isEmpty())
            Text(tr("Create a class using the action below.", "أنشئ شعبة من الإجراءات أدناه."))
        AdaptiveGrid(classes) { c ->
            ClassRow(
                c.name.text(),
                tr(
                    "${c.studentIds.size} students · ${needsSupport(state, c)} need support",
                    "${c.studentIds.size} طالباً · ${needsSupport(state, c)} بحاجة لدعم",
                ),
                classAverage(state, c),
                { open("teacher/classes/${c.id}") },
                "teacher-class-${c.id}",
            )
        }
        ExperienceSection(tr("Active assignments", "الواجبات النشطة"))
        if (active.isEmpty())
            Text(
                tr(
                    "No upcoming assignments in this local demo.",
                    "لا توجد واجبات قادمة في التجربة المحلية.",
                )
            )
        active.take(3).forEach { a ->
            val received = state.product.submissions.count { it.assignmentId == a.id }
            AssignmentSummary(
                a.title.text(),
                tr(
                    "$received submissions · ${DateFormat.getDateInstance(DateFormat.SHORT).format(Date(a.due))}",
                    "$received تسليمات · ${DateFormat.getDateInstance(DateFormat.SHORT).format(Date(a.due))}",
                ),
                { open("teacher/assignments/${a.id}") },
                "teacher-active-${a.id}",
            )
        }
        ExperienceSection(tr("Recent activity", "النشاط الأخير"))
        state.product.submissions
            .sortedByDescending { it.timestamp }
            .take(3)
            .forEach { s ->
                val name = state.students.find { it.id == s.studentId }?.name?.text().orEmpty()
                val title =
                    state.product.assignments
                        .find { it.id == s.assignmentId }
                        ?.title
                        ?.text()
                        .orEmpty()
                DetailRow("$name · $title", tr("${s.score} points", "${s.score} نقطة"))
            }
        ExperienceSection(tr("Quick actions", "إجراءات سريعة"))
        TeacherActionBar(
            listOf(
                tr("Create assignment", "إنشاء واجب") to { open("teacher/assignments/new") },
                tr("Manage classes", "إدارة الشعب") to { open(Routes.TeacherClasses) },
                tr("Curriculum & tools", "المنهج والأدوات") to { open(Routes.TeacherResources) },
            ) + if (switchRole != null) listOf(tr("Switch to student · demo", "الانتقال للطالب · تجريبي") to switchRole) else emptyList()
        )
        Text(
            tr(
                "Local demo data · no remote class administration",
                "بيانات تجربة محلية · دون إدارة شعب عن بُعد",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun TeacherClassDetails(
    id: String,
    state: LearningState,
    onStudent: (String) -> Unit,
    onAssignment: (String) -> Unit,
) {
    val c = state.product.classes.find { it.id == id }
    var tab by rememberSaveable(id) { mutableIntStateOf(0) }
    var filter by rememberSaveable(id) { mutableIntStateOf(0) }
    var query by rememberSaveable(id) { mutableStateOf("") }
    var copied by rememberSaveable(id) { mutableStateOf(false) }
    val context = LocalContext.current
    ExperiencePage("screen-class-details", teacher = true) {
        if (c == null) {
            EmptyState(
                tr("Class unavailable", "الشعبة غير متاحة"),
                tr("Return to your classes.", "عد إلى شعبك."),
            )
            return@ExperiencePage
        }
        PageHeading(
            tr("Class overview", "متابعة الشعبة"),
            c.name.text(),
            tr(
                "${c.studentIds.size} students · ${state.topic(c.topicId)?.title?.text().orEmpty()}",
                "${c.studentIds.size} طالباً · ${state.topic(c.topicId)?.title?.text().orEmpty()}",
            ),
        )
        ClassPerformance(
            state.topic(c.topicId)?.title?.text().orEmpty(),
            classAverage(state, c),
            needsSupport(state, c),
            assignedTopicCount(state, c),
        )
        TeacherActionBar(
            listOf(
                (if (copied) tr("Invite code copied", "تم نسخ رمز الدعوة")
                else tr("Copy invite code", "نسخ رمز الدعوة")) to
                    {
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("Class invite", c.code))
                        copied = true
                    }
            )
        )
        TeacherFilterBar(
            listOf(
                tr("Overview", "نظرة عامة"),
                tr("Students", "الطلاب"),
                tr("Curriculum", "المنهج"),
                tr("Assignments", "الواجبات"),
            ),
            tab,
            { tab = it },
            "class-tab-",
        )
        when (tab) {
            0 -> {
                ExperienceSection(
                    tr("Students to follow up", "طلاب يحتاجون متابعة"),
                    tr("Prioritized by current-topic mastery", "مرتّبون حسب إتقان الموضوع الحالي"),
                )
                val students =
                    state.students
                        .filter { it.id in c.studentIds }
                        .sortedBy { LearningRules.topic(state, c.topicId, it.id).score }
                        .take(3)
                students.forEach { s ->
                    StudentPerformanceRow(
                        s.name.text(),
                        LearningRules.topic(state, c.topicId, s.id).score,
                        tr("Open learning evidence", "عرض أدلة التعلم"),
                        { onStudent(s.id) },
                        "teacher-student-${s.id}",
                    )
                }
                if (students.isEmpty())
                    Text(
                        tr(
                            "No enrolled students in this local class.",
                            "لا يوجد طلاب في هذه الشعبة المحلية.",
                        )
                    )
                TextButton({ tab = 1 }, Modifier.testTag("class-all-students")) {
                    Text(tr("View all students", "عرض جميع الطلاب"))
                }
                ExperienceSection(tr("Recent submissions", "التسليمات الأخيرة"))
                state.product.submissions
                    .filter { s ->
                        state.product.assignments.any {
                            it.id == s.assignmentId && it.classId == id
                        }
                    }
                    .sortedByDescending { it.timestamp }
                    .take(3)
                    .forEach { s ->
                        DetailRow(
                            state.students.find { it.id == s.studentId }?.name?.text().orEmpty(),
                            "${s.score}",
                        )
                    }
            }
            1 -> {
                TeacherFilterBar(
                    listOf(tr("All students", "كل الطلاب"), tr("Needs support", "بحاجة لدعم")),
                    filter,
                    { filter = it },
                    "student-filter-",
                )
                AppTextField(
                    query,
                    { query = it },
                    tr("Search students", "بحث عن طالب"),
                    Modifier.testTag("teacher-student-search"),
                )
                val visible =
                    state.students.filter {
                        it.id in c.studentIds &&
                            (filter == 0 ||
                                LearningRules.topic(state, c.topicId, it.id).score < 45) &&
                            (LearningRules.search(it.name.en)
                                .contains(LearningRules.search(query)) ||
                                LearningRules.search(it.name.ar)
                                    .contains(LearningRules.search(query)))
                    }
                visible
                    .sortedBy { LearningRules.topic(state, c.topicId, it.id).score }
                    .forEach { s ->
                        StudentPerformanceRow(
                            s.name.text(),
                            LearningRules.topic(state, c.topicId, s.id).score,
                            state.topic(c.topicId)?.title?.text().orEmpty(),
                            { onStudent(s.id) },
                            "teacher-student-${s.id}",
                        )
                    }
                if (visible.isEmpty())
                    Text(tr("No students match these filters.", "لا يوجد طلاب يطابقون التصفية."))
            }
            2 -> {
                ExperienceSection(
                    tr("Class view of the curriculum", "المنهج من منظور الشعبة"),
                    tr(
                        "The same topics, with class mastery and assigned practice",
                        "الموضوعات نفسها، مع إتقان الشعبة والتدريبات المسندة",
                    ),
                )
                state.topics.forEach { t ->
                    val average =
                        if (c.studentIds.isEmpty()) 0
                        else
                            c.studentIds
                                .map { LearningRules.topic(state, t.id, it).score }
                                .average()
                                .toInt()
                    val assigned =
                        state.product.assignments.count { a ->
                            a.classId == id &&
                                a.questionIds.any { q ->
                                    state.questions.any { it.id == q && it.topicId == t.id }
                                }
                        }
                    ExperiencePanel {
                        ClassPerformance(
                            t.title.text(),
                            average,
                            needsSupport(state, c, t.id),
                            assigned,
                            "teacher-curriculum-${t.id}",
                        )
                    }
                }
            }
            else -> {
                val assignments = state.product.assignments.filter { it.classId == id }
                if (assignments.isEmpty())
                    Text(
                        tr(
                            "No assigned work for this class yet.",
                            "لا توجد واجبات لهذه الشعبة بعد.",
                        )
                    )
                assignments.forEach { a ->
                    AssignmentSummary(
                        a.title.text(),
                        tr(
                            "${a.questionIds.size} questions · ${state.product.submissions.count { it.assignmentId == a.id }} submissions",
                            "${a.questionIds.size} أسئلة · ${state.product.submissions.count { it.assignmentId == a.id }} تسليمات",
                        ),
                        { onAssignment(a.id) },
                        "class-assignment-${a.id}",
                    )
                }
            }
        }
    }
}

@Composable
fun TeacherStudentDetails(
    classId: String,
    id: String,
    state: LearningState,
    onAssignment: (String) -> Unit,
) {
    val student = state.students.find { it.id == id }
    val c = state.product.classes.find { it.id == classId }
    ExperiencePage("screen-student-details", teacher = true) {
        if (student == null || c == null || id !in c.studentIds) {
            EmptyState(
                tr("Student unavailable", "الطالب غير متاح"),
                tr("Return to the class.", "عد إلى الشعبة."),
            )
            return@ExperiencePage
        }
        PageHeading(
            tr("Student learning evidence", "أدلة تعلم الطالب"),
            student.name.text(),
            c.name.text(),
        )
        val isCurrentStudent = id == "ahmed" || id == state.activeStudentId
        val completed = if (isCurrentStudent) state.completed.size else student.completed.size
        val latestSubmission =
            state.product.submissions
                .filter {
                    it.studentId == id &&
                        state.product.assignments.any { a ->
                            a.id == it.assignmentId && a.classId == classId
                        }
                }
                .maxByOrNull { it.timestamp }
        DetailRow(tr("Completed lessons", "الدروس المكتملة"), "$completed")
        TeacherInsightPanel(
            tr("Next teaching action", "الإجراء التعليمي التالي"),
            if (LearningRules.topic(state, c.topicId, id).score < 45)
                tr(
                    "Review the current topic's explanation and the submitted answers before assigning more practice.",
                    "راجع شرح الموضوع الحالي والإجابات المسلّمة قبل إسناد تدريب إضافي.",
                )
            else
                tr(
                    "Review the student's evidence, then decide whether to consolidate or extend the current topic.",
                    "راجع أدلة الطالب، ثم قرر تثبيت الموضوع الحالي أو التوسع فيه.",
                ),
            tr("Review latest submitted work", "مراجعة آخر تسليم"),
            { latestSubmission?.let { onAssignment(it.assignmentId) } },
            "student-teaching-insight",
            actionEnabled = latestSubmission != null,
        )
        // Explicitly show why a review action might have no submitted work to open.
        if (
            state.product.submissions.none {
                it.studentId == id &&
                    state.product.assignments.any { a ->
                        a.id == it.assignmentId && a.classId == classId
                    }
            }
        )
            Text(
                tr(
                    "No submitted work for this class yet. Topic mastery below comes from the local learning record.",
                    "لا يوجد تسليم لهذا الطالب في الشعبة بعد. الإتقان أدناه يأتي من سجل التعلم المحلي.",
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        ExperienceSection(
            tr("Topic mastery", "إتقان الموضوعات"),
            tr("Same calculation as student progress", "الحساب نفسه المستخدم في تقدم الطالب"),
        )
        AdaptiveGrid(state.topics) { t ->
            val mastery = LearningRules.topic(state, t.id, id)
            ExperiencePanel {
                DetailRow(
                    t.title.text(),
                    "${mastery.score}%",
                    Modifier.testTag("teacher-mastery-${t.id}"),
                )
                LinearProgressIndicator(
                    progress = { mastery.score / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    tr(
                        "${mastery.lessons} lessons · ${(mastery.accuracy * 100).toInt()}% practice accuracy",
                        "${mastery.lessons} دروس · ${(mastery.accuracy * 100).toInt()}% دقة تدريب",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        ExperienceSection(tr("Assigned work", "العمل المسند"))
        state.product.assignments
            .filter { it.classId == classId }
            .forEach { a ->
                val submission =
                    state.product.submissions.lastOrNull {
                        it.assignmentId == a.id && it.studentId == id
                    }
                if (submission != null)
                    AssignmentSummary(
                        a.title.text(),
                        tr(
                            "${submission.score} points · ${if (submission.reviewed) "reviewed" else "awaiting review"}",
                            "${submission.score} نقطة · ${if (submission.reviewed) "تمت المراجعة" else "بانتظار المراجعة"}",
                        ),
                        { onAssignment(a.id) },
                        "student-assignment-${a.id}",
                    )
                else
                    ExperiencePanel {
                        Text(a.title.text())
                        Text(
                            tr("Not submitted yet", "لم يُسلّم بعد"),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
        if (isCurrentStudent) {
            ExperienceSection(tr("Recent learning activity", "نشاط التعلم الأخير"))
            state.product.activity.takeLast(3).reversed().forEach {
                DetailRow(it.title.text(), tr("${it.minutes} min", "${it.minutes} دقيقة"))
            }
        }
        Text(
            tr(
                "Local shared demo persona; no remote monitoring or messages.",
                "شخصية تجريبية محلية مشتركة؛ لا توجد متابعة أو رسائل عن بُعد.",
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
