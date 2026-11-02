package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.Copy
import java.math.BigInteger

data class G9LessonBlock(val title: Copy, val body: Copy, val formula: String = "")
data class G9WorkedExample(val prompt: Copy, val steps: List<G9LessonBlock>)
data class G9FractionQuestion(val id: String, val numerator: Int, val denominator: Int,
    val answer: String, val hint: Copy, val correction: Copy)

/** Original EN.MATH pilot; source pages visually checked, never redistributed as textbook scans.
 * This is a bounded 2025–2026 learning pilot, not official 2026–2027 curriculum certification.
 * No external human/Ministry approval and no rewards/mastery claim are implied.
 */
object Grade9Pilot {
    const val lessonId = "G9-ALG-U01-L03"
    const val revision = "enmath-reduced-fractions-v1"
    const val practiceId = "g9-reduced-fractions-v1"
    val ref get() = requireNotNull(G9SourceCatalog.ref(lessonId))
    val title = Copy("Reduced fractions", "كسور مختزلة")
    val blocks = listOf(
        G9LessonBlock(Copy("What will I learn?", "ماذا سأتعلم؟"), Copy(
            "Reduce a fraction of positive integers, keep its value, and explain why the result cannot be reduced further.",
            "أختزل كسرًا من عددين صحيحين موجبين، وأحافظ على قيمته، وأفسّر لماذا لا يمكن اختزاله أكثر.")),
        G9LessonBlock(Copy("The idea", "الفكرة"), Copy(
            "Different fractions can describe the same quantity. Dividing the numerator and denominator by the same nonzero common divisor keeps their quotient unchanged. Here both integers are positive.",
            "قد تعبّر كسور مختلفة عن المقدار نفسه. قسمة البسط والمقام على القاسم المشترك نفسه، غير الصفر، تبقي ناتج القسمة ثابتًا. نعمل هنا بعددين صحيحين موجبين.")),
        G9LessonBlock(Copy("Definition", "التعريف"), Copy(
            "A positive fraction is reduced when the greatest common divisor of its numerator and denominator is 1. A reduced fraction may be greater than 1.",
            "يكون الكسر الموجب مختزلًا عندما يكون القاسم المشترك الأكبر لبسطه ومقامه هو 1. وقد يكون الكسر المختزل أكبر من 1."), "gcd(a, b) = 1; a > 0, b > 0"),
        G9LessonBlock(Copy("The rule and its conditions", "القاعدة وشروطها"), Copy(
            "Find d, the GCD of a and b, then divide both by d. The resulting numerator and denominator have no common divisor greater than 1. Never divide by zero or change only one part.",
            "أوجد d، القاسم المشترك الأكبر للعددين a وb، ثم اقسم كليهما عليه. لا يبقى للبسط والمقام الناتجين قاسم مشترك أكبر من 1. لا تقسم على صفر ولا تغيّر طرفًا واحدًا فقط."),
            "d = gcd(a, b); a/b = (a ÷ d)/(b ÷ d)"),
        G9LessonBlock(Copy("Check your reasoning", "تحقّق من تفكيرك"), Copy(
            "Check equality by cross multiplication, then check that the new GCD is 1. Equal value alone is not enough when the question asks for a reduced fraction.",
            "تحقّق من تساوي القيمتين بالضرب التبادلي، ثم تحقّق أن القاسم المشترك الأكبر الجديد هو 1. تساوي القيمة وحده لا يكفي عندما يُطلب كسر مختزل.")),
    )
    val examples = listOf(
        G9WorkedExample(Copy("Reduce 42/70.", "اختزل الكسر 42/70."), listOf(
            G9LessonBlock(Copy("Find the GCD", "أوجد القاسم المشترك الأكبر"), Copy("42 = 14 × 3 and 70 = 14 × 5; 3 and 5 are coprime, so the GCD is 14.", "14 عامل مشترك للعددين. بعد القسمة عليه نحصل على 3 و5، وهما أوليان فيما بينهما؛ إذن القاسم المشترك الأكبر هو 14."), "gcd(42, 70) = 14"),
            G9LessonBlock(Copy("Divide both parts", "اقسم الطرفين"), Copy("Divide numerator and denominator by 14.", "نقسم البسط والمقام على 14."), "42/70 = (42 ÷ 14)/(70 ÷ 14) = 3/5"),
            G9LessonBlock(Copy("Verify", "تحقّق"), Copy("The value is unchanged and gcd(3, 5) = 1; the result is reduced.", "القيمة لم تتغيّر وgcd(3, 5) = 1؛ فالنتيجة مختزلة."), "42 × 5 = 70 × 3 = 210"))),
        G9WorkedExample(Copy("Reduce 84/126 in one step.", "اختزل 84/126 بخطوة واحدة."), listOf(
            G9LessonBlock(Copy("Find the divisor", "حدّد القاسم"), Copy("126 = 84 + 42 and 84 = 2 × 42, so the GCD is 42.", "بطرح 84 من 126 نحصل على 42، وهو نصف 84؛ إذن القاسم المشترك الأكبر هو 42."), "gcd(84, 126) = 42"),
            G9LessonBlock(Copy("Reduce and verify", "اختزل وتحقّق"), Copy("Divide both by 42. Since 2 and 3 are coprime, stop here.", "اقسم كليهما على 42. بما أن 2 و3 أوليان فيما بينهما نتوقف هنا."), "84/126 = 2/3; 84 × 3 = 126 × 2")))
    )
    val questions = listOf(
        G9FractionQuestion("g9-rf-q1", 42, 56, "3/4", Copy("Both numbers are multiples of 14.", "العددان من مضاعفات 14."), Copy("The GCD is 14. Divide 42 and 56 by 14 to get 3/4; gcd(3,4)=1.", "القاسم المشترك الأكبر هو 14. بقسمة 42 و56 عليه نحصل على 3/4؛ وgcd(3,4)=1.")),
        G9FractionQuestion("g9-rf-q2", 48, 180, "4/15", Copy("Find the greatest common divisor, not just 2.", "أوجد القاسم المشترك الأكبر، ولا تكتفِ بالقسمة على 2."), Copy("The GCD is 12. 48÷12=4 and 180÷12=15. The result is 4/15.", "القاسم المشترك الأكبر هو 12. 48÷12=4 و180÷12=15. النتيجة 4/15.")),
        G9FractionQuestion("g9-rf-q3", 91, 143, "7/11", Copy("Try the common factor 13.", "جرّب العامل المشترك 13."), Copy("91=13×7 and 143=13×11. Divide both by 13; the reduced fraction is 7/11.", "91=13×7 و143=13×11. اقسم كليهما على 13؛ الكسر المختزل هو 7/11.")),
        G9FractionQuestion("g9-rf-q4", 31, 42, "31/42", Copy("A fraction may already be reduced.", "قد يكون الكسر مختزلًا أصلًا."), Copy("31 is prime and does not divide 42, so gcd(31,42)=1. Keep 31/42.", "31 عدد أولي ولا يقسم 42، إذن gcd(31,42)=1. يبقى الكسر 31/42.")),
    )
    val checkpoint = G9FractionQuestion("g9-rf-check", 54, 90, "3/5", Copy("Use the GCD 18.", "استعمل القاسم المشترك الأكبر 18."), Copy("Divide both by 18 to get 3/5; its GCD is 1.", "اقسم كليهما على 18 لتحصل على 3/5؛ والقاسم المشترك الأكبر الجديد هو 1."))
    val content by lazy { G9LessonTemplate(ref, revision, title, blocks, examples,
        Copy("Find the GCD → divide both parts → verify equal value and GCD 1. Subtracting the same number from both parts is not a simplification rule.",
            "أوجد القاسم المشترك الأكبر ← اقسم الطرفين ← تحقّق من تساوي القيمة ومن أن القاسم الجديد 1. طرح العدد نفسه من الطرفين ليس قاعدة للاختزال.")) }
    val questionTemplates by lazy { questions.map { q -> G9QuestionTemplate(q.id,
        Copy("Write an equal reduced positive fraction.", "اكتب كسرًا موجبًا مختزلًا يساوي الكسر المعطى."),
        "${q.numerator}/${q.denominator}", q.hint, q.correction, G9AnswerContract.ReducedPositiveFraction(q.numerator, q.denominator)) } }
    /** Exact integer grading: decimals and unreduced equivalents are deliberately not sufficient. */
    fun correct(question: G9FractionQuestion, input: String): Boolean {
        val normalized = MathAnswer.normalize(input).replace('⁄', '/').replace('∕', '/')
        if (normalized.length > 64 || !normalized.matches(Regex("[0-9]+/[0-9]+"))) return false
        val (a, b) = normalized.split('/').map { it.toBigInteger() }
        return a.signum() > 0 && b.signum() > 0 && a.gcd(b) == BigInteger.ONE &&
            a * question.denominator.toBigInteger() == b * question.numerator.toBigInteger()
    }
}
