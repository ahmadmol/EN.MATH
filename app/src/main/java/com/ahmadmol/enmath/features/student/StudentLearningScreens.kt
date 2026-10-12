package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.design.components.student.*
import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence

@Composable
fun StudentLearningHome(state: LearningState, open: (String) -> Unit) {
    val next = state.lesson(state.lastLessonId) ?: state.lesson(LimitsLessonSequence.lessonId)
    val assignment =
        state.product.assignments
            .filter { a ->
                state.product.classes.any { it.id == a.classId && "ahmed" in it.studentIds } &&
                    state.product.submissions.none {
                        it.assignmentId == a.id && it.studentId == "ahmed"
                    } &&
                    a.due >= System.currentTimeMillis()
            }
            .minByOrNull { it.due }
    val progress = state.progress
    ExperiencePage("screen-home") {
        PageHeading(
            tr("A little understanding, every day", "فهم أعمق، كل يوم"),
            tr("Hello, ${state.user?.name.orEmpty()}", "أهلاً، ${state.user?.name.orEmpty()}"),
        )
        ExperiencePanel(emphasized = true) {
            Text(tr("Your next idea", "فكرتك التالية"), style = MaterialTheme.typography.labelLarge)
            Text(next?.title?.text().orEmpty(), style = MaterialTheme.typography.headlineMedium)
            Text(
                if (next?.id == LimitsLessonSequence.lessonId)
                    tr(
                        "Explore a limit, make a prediction, then test your reasoning.",
                        "استكشف نهاية، توقّع النتيجة، ثم اختبر فهمك.",
                    )
                else
                    tr(
                        "Continue your last lesson and apply its rule.",
                        "تابع آخر درس وطبّق قاعدته.",
                    )
            )
            PrimaryButton(
                tr("Continue learning", "متابعة التعلم"),
                { open("lesson/${next?.id ?: LimitsLessonSequence.lessonId}") },
                Modifier.testTag("student-continue"),
            )
        }
        ProgressFeedback(
            tr("Today's learning goal", "هدف التعلم اليوم"),
            tr(
                "${progress.todayCompleted} of ${state.dailyGoal} learning activities",
                "${progress.todayCompleted} من ${state.dailyGoal} أنشطة تعلم",
            ),
            progress.todayCompleted.toFloat() / state.dailyGoal.coerceAtLeast(1),
            "daily-learning-goal",
        )
        ExperiencePanel {
            ExperienceSection(
                tr("Make the idea stick", "ثبّت الفكرة"),
                tr(
                    "Six short questions on limits, with feedback after each answer.",
                    "ستة أسئلة قصيرة عن النهايات، مع تفسير بعد كل إجابة.",
                ),
            )
            SecondaryButton(
                tr("Practice limits", "تدرّب على النهايات"),
                { open("practice/${LimitsLessonSequence.practiceId}") },
                Modifier.testTag("home-practice"),
            )
        }
        DetailRow(tr("Need a quick solution?", "تحتاج حلاً سريعاً؟"), tr("Solver", "المحلّل"))
        SecondaryButton(
            tr("Open quick solver", "فتح الحل السريع"),
            { open("solver") },
            Modifier.testTag("home-quick-solver"),
        )
        if (assignment != null) {
            ExperienceSection(tr("Upcoming assignment", "واجبك القادم"))
            AppCard(
                Modifier.testTag("home-upcoming-assignment"),
                onClick = { open("assignments/${assignment.id}") },
            ) {
                Text(assignment.title.text(), style = MaterialTheme.typography.titleMedium)
                Text(assignment.description.text(), style = MaterialTheme.typography.bodyMedium)
            }
        }
        ExperienceSection(tr("Your understanding", "تقدم فهمك"))
        ExperiencePanel {
            val mastery = LearningRules.topic(state, "limits")
            ProgressFeedback(
                tr("Limits mastery", "إتقان النهايات"),
                "${mastery.score}%",
                mastery.score / 100f,
                "student-mastery-limits",
            )
            TextButton({ open("progress") }) { Text(tr("View all progress", "عرض التقدم الكامل")) }
        }
        ExperienceSection(tr("Recent learning", "تعلمك الأخير"))
        if (state.product.activity.isEmpty())
            Text(
                tr(
                    "Your completed lessons and practice will appear here.",
                    "ستظهر هنا الدروس والتدريبات التي تكملها.",
                )
            )
        state.product.activity.takeLast(3).reversed().forEach {
            DetailRow(it.title.text(), tr("${it.minutes} min", "${it.minutes} دقيقة"))
        }
    }
}

@Composable
fun StudentStudy(state: LearningState, open: (String) -> Unit) {
    val limits = state.topic("limits")
    val mastery = LearningRules.topic(state, "limits")
    ExperiencePage("screen-study") {
        PageHeading(
            tr("Learn through a sequence of ideas", "تعلّم عبر أفكار مترابطة"),
            tr("Your curriculum", "منهجك"),
        )
        ExperiencePanel(emphasized = true) {
            ExperienceSection(
                limits?.title?.text().orEmpty(),
                tr(
                    "Understand approaching a value before memorizing the rules.",
                    "افهم الاقتراب من قيمة قبل حفظ القواعد.",
                ),
            )
            ProgressFeedback(tr("My progress", "تقدمي"), "${mastery.score}%", mastery.score / 100f)
            PrimaryButton(
                tr("Explore limits", "استكشاف النهايات"),
                { open("topic/limits") },
                Modifier.testTag("study-limits"),
            )
        }
        ExperienceSection(
            tr("Books and other topics", "الكتب والموضوعات الأخرى"),
            tr(
                "The rest of the curriculum keeps its existing experience during this trial.",
                "تحتفظ بقية موضوعات المنهج بتجربتها الحالية خلال هذه المراجعة.",
            ),
        )
        AdaptiveGrid(state.books) { book ->
            CourseCard(book, state.completed) { open("book/${book.id}") }
        }
        SecondaryButton(
            tr("Browse all lessons", "استعراض جميع الدروس"),
            { open("study/catalog") },
            Modifier.testTag("browse-all-lessons"),
        )
    }
}

@Composable
fun StudentLimitsTopic(state: LearningState, lesson: (String) -> Unit, practice: () -> Unit) {
    val topic = state.topic("limits")
    val mastery = LearningRules.topic(state, "limits")
    ExperiencePage("screen-topic") {
        PageHeading(
            tr("Learn · Interact · Understand", "تعلّم · تفاعل · افهم"),
            topic?.title?.text().orEmpty(),
        )
        ProgressFeedback(
            tr("My understanding", "تقدّم فهمي"),
            "${mastery.score}%",
            mastery.score / 100f,
        )
        ExperiencePanel(emphasized = true) {
            ExperienceSection(
                tr("What is a limit telling us?", "ماذا تخبرنا النهاية؟"),
                tr(
                    "Move a point, predict a value and explain your answer.",
                    "حرّك نقطة، توقّع قيمة، ثم فسّر إجابتك.",
                ),
            )
            PrimaryButton(
                tr("Start the interactive lesson", "ابدأ الدرس التفاعلي"),
                { lesson(LimitsLessonSequence.lessonId) },
                Modifier.testTag("start-interactive-limits"),
            )
        }
        ExperienceSection(tr("More in this topic", "المزيد في هذا الموضوع"))
        topic
            ?.lessons
            ?.filterNot { it.id == LimitsLessonSequence.lessonId }
            ?.forEach { l -> LessonCard(l, l.id in state.completed) { lesson(l.id) } }
        SecondaryButton(
            tr("Practice this topic", "تدرّب على الموضوع"),
            practice,
            Modifier.testTag("topic-practice"),
        )
    }
}
