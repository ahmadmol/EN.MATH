package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.Copy

/**
 * Pedagogical content for Syrian Grade 9 Algebra: Unit 1 (Numbers and Fractions), Lesson 1 (Nature of Numbers).
 * Source: Syrian Curriculum, Grade 9 Mathematics (Algebra), 2025–2026 Edition, Pages 6–7.
 *
 * Original educational draft structured for Grade 9 students (ages 14–15).
 * Independent educator approval has not been recorded; the source index is not lesson approval.
 * Strictly labeled as educational draft for learning and formative practice; not an official ministry endorsement.
 */
object Grade9NatureLesson {
    const val LESSON_ID = "G9-ALG-U01-L01"
    const val REVISION = "enmath-nature-numbers-v1"
    val ref get() = requireNotNull(G9SourceCatalog.ref(LESSON_ID))
    val title = Copy("Nature of numbers", "طبيعة الأعداد")
    val bookTitle = Copy("Grade 9 Algebra", "رياضيات التاسع — الجبر")
    val unitTitle = Copy("Numbers and fractions", "الأعداد والكسور")
    val sourceEdition = Copy("2025–2026 Edition · Pages 6–7", "طبعة 2025–2026 · الصفحتان 6–7")

    val blocks = listOf(
        G9LessonBlock(
            title = Copy("What will I learn in this lesson?", "ماذا سأتعلم في هذا الدرس؟"),
            body = Copy(
                "Classify numbers into natural, integer, decimal, rational, and irrational sets, understand their nested relationships, and identify the smallest set for any given number.",
                "أتعرف تصنيفات الأعداد (طبيعية، صحيحة، عشرية، عادية، غير عادية)، وأفهم العلاقات المتداخلة بينها، وأحدد أصغر مجموعة ينتمي إليها كل عدد، وأميز بين الكتابة العشرية المنتهية والدورية."
            )
        ),
        G9LessonBlock(
            title = Copy("Natural numbers and integers", "الأعداد الطبيعية والأعداد الصحيحة"),
            body = Copy(
                "Natural numbers N are whole counting numbers starting from 0, 1, 2, 3, ... Integers Z include all natural numbers along with their negative counterparts: ... -3, -2, -1, 0, 1, 2, 3, ... Every natural number is necessarily an integer.",
                "مجموعة الأعداد الطبيعية N هي أعداد العد الموجبة مع الصفر: 0، 1، 2، 3، ...\n" +
                "مجموعة الأعداد الصحيحة Z تضم جميع الأعداد الطبيعية مع معاكساتها السالبة: ...، -3، -2، -1، 0، 1، 2، 3، ...\n" +
                "قاعدة: كل عدد طبيعي هو عدد صحيح بالضرورة."
            ),
            formula = "N ⊂ Z"
        ),
        G9LessonBlock(
            title = Copy("Rational numbers", "الأعداد العادية (النسبية)"),
            body = Copy(
                "A rational number Q is any number that can be written as a fraction a/b where a and b are integers and b is not 0. Every integer n is rational because n = n/1. Rational numbers have decimal representations that are either finite or infinite repeating (periodic).",
                "العدد العادي Q هو كل عدد يمكن كتابته على شكل كسر a/b حيث a و b عددان صحيحان و b ≠ 0.\n" +
                "كل عدد صحيح n هو عدد عادي لأن n = n/1.\n" +
                "الكتابة العشرية للعدد العادي تكون إما منتهية (مثل 0.4 أو 0.375) أو غير منتهية ولكن دورية تتكرر بانتظام (مثل 5/6 = 0.8333...)."
            ),
            formula = "Q = { a/b : a ∈ Z, b ∈ Z, b ≠ 0 }"
        ),
        G9LessonBlock(
            title = Copy("Decimal numbers", "الأعداد العشرية"),
            body = Copy(
                "A decimal number D is a rational number whose decimal representation terminates (finite digits after the decimal point), or can be written as a fraction with denominator as a power of 10. For example, 0.25 = 25/100 is decimal and rational. In contrast, 5/6 = 0.8333... is rational but NOT decimal.",
                "العدد العشري D هو عدد عادي كتابته العشرية منتهية (أرقامه بعد الفاصلة تقف عند حد)، أو يمكن كتابته على شكل كسر مقامه قوة للعدد 10 (10 أو 100 أو 1000 ...).\n" +
                "مثال: 0.25 = 25/100 عدد عشري وعادي.\n" +
                "أما العدد 5/6 = 0.8333... فهو عدد عادي لكنه ليس عشرياً لأن كتابته العشرية غير منتهية."
            ),
            formula = "D = { a / 10^n : a ∈ Z, n ∈ N } ⊂ Q"
        ),
        G9LessonBlock(
            title = Copy("Irrational numbers", "الأعداد غير العادية (غير النسبية)"),
            body = Copy(
                "An irrational number cannot be expressed as a fraction of two integers. Its decimal representation is infinite and non-repeating. Common examples include pi and square roots of non-perfect squares such as √2, √3, and √5.",
                "العدد غير العادي هو عدد لا يمكن كتابته على شكل كسر a/b لعددين صحيحين.\n" +
                "كتابته العشرية غير منتهية وغير دورية، مثل: π، وجذور الأعداد التي ليست مربعات كاملة مثل √2، √3، √5."
            ),
            formula = "π ≈ 3.14159... ; √2 ≈ 1.41421... ; √3 ≈ 1.73205..."
        ),
        G9LessonBlock(
            title = Copy("Golden Rule: Value over appearance", "قاعدة ذهبية: العبرة بالقيمة لا بالشكل الظاهري"),
            body = Copy(
                "Always simplify the number before classifying it! The square root symbol alone does not mean irrational: for instance, √25 = 5 is a natural, integer, decimal, and rational number, NOT irrational. Similarly, √49 = 7 and 10/2 = 5.",
                "احذر المغالطة الشائعة: لا تحكم على طبيعة العدد من شكله الظاهري أو وجود رمز الجذر!\n" +
                "مثال: العدد √25 قيمته الحقيقية هي 5، إذن هو عدد طبيعي وصحيح وعادي وعشري وليس غير عادي.\n" +
                "وكذلك: √49 = 7، و 10/2 = 5."
            ),
            formula = "√25 = 5 ∈ N ⊂ Z ⊂ D ⊂ Q"
        )
    )

    val workedExample = G9WorkedExample(
        prompt = Copy(
            "Classify each of the following numbers into the smallest set it belongs to: -3, 2/5, π, √49, √2.",
            "صنّف كلاً من الأعداد الآتية موضحاً أصغر مجموعة ينتمي إليها: -3 ، 2/5 ، π ، √49 ، √2."
        ),
        steps = listOf(
            G9LessonBlock(
                title = Copy("Step 1: Simplify numbers to find their true value", "الخطوة 1: تبسيط الأعداد لمعرفة قيمتها الفعلية"),
                body = Copy(
                    "Calculate roots and fractions: √49 = 7 (because 7 × 7 = 49), and 2/5 = 0.4.",
                    "نحسب الجذور والكسور القابلة للتبسيط أولاً:\n" +
                    "√49 = 7 (لأن 7 × 7 = 49)\n" +
                    "2/5 = 0.4 (كتابة عشرية منتهية)"
                ),
                formula = "√49 = 7 ; 2/5 = 0.4"
            ),
            G9LessonBlock(
                title = Copy("Step 2: Classify integers", "الخطوة 2: تصنيف الأعداد الصحيحة"),
                body = Copy(
                    "-3 is a negative integer (smallest set: Z). √49 = 7 is a positive counting number (smallest set: natural numbers N).",
                    "-3 عدد صحيح سالب، وأصغر مجموعة ينتمي إليها هي مجموعة الأعداد الصحيحة Z.\n" +
                    "√49 = 7 عدد صحيح موجب، وأصغر مجموعة ينتمي إليها هي مجموعة الأعداد الطبيعية N (والصحيحة Z)."
                ),
                formula = "-3 ∈ Z ; √49 = 7 ∈ N"
            ),
            G9LessonBlock(
                title = Copy("Step 3: Classify decimal numbers", "الخطوة 3: تصنيف الأعداد العشرية"),
                body = Copy(
                    "2/5 = 0.4 has a terminating decimal representation (or 4/10), so its smallest set is decimal numbers D.",
                    "العدد 2/5 = 0.4 كتابته العشرية منتهية (يمكن كتابته 4/10)، وأصغر مجموعة ينتمي إليها هي مجموعة الأعداد العشرية D."
                ),
                formula = "2/5 = 4/10 = 0.4 ∈ D"
            ),
            G9LessonBlock(
                title = Copy("Step 4: Classify irrational numbers", "الخطوة 4: تصنيف الأعداد غير العادية"),
                body = Copy(
                    "π and √2 cannot be written as a/b of two integers; their decimals are infinite and non-repeating. They belong to irrational numbers.",
                    "العددان π و √2 جذور ومقادير غير منتهية وغير دورية ولا يمكن كتابتها بكسر عددين صحيحين؛ إذن هما عددان غير عاديين."
                ),
                formula = "π ∉ Q ; √2 ∉ Q"
            )
        )
    )

    data class SelfCheck(
        val prompt: Copy,
        val options: List<Copy>,
        val correctIndex: Int,
        val explanation: Copy
    )

    val selfCheck = SelfCheck(
        prompt = Copy(
            "Which of the following is an irrational (non-rational) number?",
            "أي الأعداد الآتية يُعد عدداً غير عادي (غير نسبي)؟"
        ),
        options = listOf(
            Copy("0.75", "0.75"),
            Copy("√16", "√16"),
            Copy("√5", "√5"),
            Copy("-8", "-8")
        ),
        correctIndex = 2,
        explanation = Copy(
            "√5 is irrational because 5 is not a perfect square. Meanwhile, √16 = 4 (integer), 0.75 = 3/4 (decimal), and -8 is an integer.",
            "العدد √5 غير عادي لأن 5 ليس مربعاً كاملاً وجذره غير نسبي. أما √16 = 4 (صحيح)، و 0.75 = 3/4 (عشري)، و -8 (صحيح)."
        )
    )

    val summary = Copy(
        "N ⊂ Z ⊂ D ⊂ Q. Every natural number is an integer, and every integer is rational. Decimals have terminating expansions. Square roots of non-perfect squares and π are irrational. Always judge by mathematical value, not outward appearance.",
        "تذكّر دائماً تسلسل المجموعات: N ⊂ Z ⊂ D ⊂ Q.\n" +
        "كل عدد طبيعي هو صحيح، وكل عدد صحيح هو عادي.\n" +
        "الأعداد العشرية فاصلتها منتهية، والأعداد غير العادية مثل √2 و π لا يمكن كتابتها ككسر عددين صحيحين.\n" +
        "العبرة دائماً بالقيمة الحقيقية للعدد بعد التبسيط لا برمز الجذر."
    )
}
