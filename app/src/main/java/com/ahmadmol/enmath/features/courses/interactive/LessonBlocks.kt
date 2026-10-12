package com.ahmadmol.enmath.features.courses.interactive

import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.model.Copy
import com.ahmadmol.enmath.core.model.Question

/** Presentation-only sequences reference existing catalog/questions; no second curriculum store. */
sealed interface LessonBlock {
    val id: String

    data class Explanation(override val id: String, val title: Copy, val body: Copy) : LessonBlock

    data class Equation(override val id: String, val expression: String, val reason: Copy) :
        LessonBlock

    data class Visual(override val id: String, val caption: Copy) : LessonBlock

    data class WorkedExample(
        override val id: String,
        val expression: String,
        val steps: List<Pair<String, Copy>>,
    ) : LessonBlock

    data class MultipleChoice(override val id: String, val question: Question) : LessonBlock

    data class NumericQuestion(override val id: String, val question: Question) : LessonBlock

    data class MathExpressionQuestion(override val id: String, val question: Question) :
        LessonBlock

    data class TrueFalse(override val id: String, val question: Question) : LessonBlock

    data class Hint(override val id: String, val body: Copy) : LessonBlock

    data class Checkpoint(override val id: String, val question: Question) : LessonBlock

    data class Completion(
        override val id: String,
        val title: Copy = Copy("You connected the idea to the rule", "ربطت الفكرة بالقاعدة"),
        val body: Copy =
            Copy(
                "Now apply the idea independently in practice.",
                "حان الوقت لتطبيق الفكرة بنفسك في التدريب.",
            ),
    ) : LessonBlock
}

fun LessonBlock.question(): Question? =
    when (this) {
        is LessonBlock.MultipleChoice -> question
        is LessonBlock.NumericQuestion -> question
        is LessonBlock.MathExpressionQuestion -> question
        is LessonBlock.TrueFalse -> question
        is LessonBlock.Checkpoint -> question
        else -> null
    }

object LimitsLessonSequence {
    const val lessonId = "limits-practice"
    const val practiceId = "limits-practice-set"

    fun blocks(state: LearningState): List<LessonBlock> {
        val lesson = state.lesson(lessonId) ?: return emptyList()
        fun q(index: Int) = state.questions.first { it.id == "limits-q$index" }
        return listOf(
            LessonBlock.Explanation(
                "concept",
                Copy("Approach, rather than arrive", "نقترب من القيمة"),
                Copy(
                    "A limit describes where the values approach as x gets close to a point. The function need not be defined at that point.",
                    "تصف النهاية القيمة التي نقترب منها عندما تقترب x من نقطة. لا يشترط أن تكون الدالة معرّفة عند النقطة نفسها.",
                ),
            ),
            LessonBlock.Visual(
                "visual",
                Copy(
                    "Move x from either side of 3. Where does x + 2 approach?",
                    "حرّك x من جانبي 3. إلى أي قيمة تقترب x + 2؟",
                ),
            ),
            LessonBlock.MultipleChoice("prediction", q(1)),
            LessonBlock.Explanation(
                "understand",
                Copy("The two sides agree", "الجهتان تتفقان"),
                Copy(
                    "From the left and from the right, x + 2 approaches 5. A missing value at x = 3 would not change this limit.",
                    "من اليسار ومن اليمين تقترب x + 2 من 5. وجود ثقب عند x = 3 لا يغيّر هذه النهاية.",
                ),
            ),
            LessonBlock.Equation(
                "rule",
                "lim x→a p(x) = p(a)",
                Copy(
                    "For a polynomial, direct substitution gives the limit. A 0/0 expression needs further work; it is not a numerical answer.",
                    "في كثيرات الحدود تعطيك قيمة التعويض المباشر النهاية. أما 0/0 فهي حالة عدم تعيين تحتاج معالجة، وليست إجابة عددية.",
                ),
            ),
            LessonBlock.WorkedExample(
                "example",
                lesson.example,
                listOf(
                    "x² − 1 = (x − 1)(x + 1)" to
                        Copy("Factor the difference of squares.", "حلّل فرق المربعين."),
                    "(x² − 1)/(x − 1) = x + 1, x ≠ 1" to
                        Copy(
                            "Cancel only away from 1. The original fraction is still undefined at 1.",
                            "اختصر خارج 1 فقط؛ الكسر الأصلي يبقى غير معرّف عند 1.",
                        ),
                    "lim x→1 (x + 1) = 2" to
                        Copy(
                            "Take the limit of the simplified expression.",
                            "احسب نهاية التعبير المبسّط.",
                        ),
                ),
            ),
            LessonBlock.MathExpressionQuestion("challenge", q(4)),
            LessonBlock.TrueFalse("verify", q(5)),
            LessonBlock.Hint("remember", lesson.mistake),
            LessonBlock.Checkpoint("checkpoint", q(6)),
            LessonBlock.Completion("completion"),
        )
    }
}

fun limitsExplanation(question: Question): Copy =
    when (question.id) {
        "limits-q5" ->
            Copy(
                "Direct substitution gives 3 + 2 = 5, not 42. The proposed equality is false.",
                "يعطي التعويض المباشر 3 + 2 = 5 وليس 42؛ لذلك المساواة المقترحة غير صحيحة.",
            )
        "limits-q6" ->
            Copy(
                "x² is a polynomial, so substitute x = 2: 2² = 4.",
                "x² كثير حدود، لذا عوّض x = 2 لتحصل على 2² = 4.",
            )
        else ->
            Copy(
                "x + 2 is a polynomial. Substitute x = 3: 3 + 2 = 5. The values from both sides agree.",
                "x + 2 كثير حدود؛ بالتعويض x = 3 نحصل على 3 + 2 = 5، وتتفق القيم من الجهتين.",
            )
    }
