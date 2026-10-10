package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.*

/** Curated demo catalog; replace with a catalog API without changing screens. */
internal object BaseDemoContent {
    private fun topic(
        id: String,
        en: String,
        ar: String,
        symbol: String,
        example: String,
        answer: Copy,
    ): Topic {
        return Topic(
            id,
            Copy(en, ar),
            symbol,
            listOf(
                Lesson(
                    "$id-foundations",
                    Copy("$en · Foundations", "$ar · الأساسيات"),
                    8,
                    Copy(
                        "Begin with a worked example, then explain each transformation in your own words.",
                        "ابدأ بمثال محلول، ثم اشرح كل تحويل بكلماتك.",
                    ),
                    example,
                    answer,
                ),
                Lesson(
                    "$id-practice",
                    Copy("$en · Guided practice", "$ar · تدريب موجّه"),
                    12,
                    Copy(
                        "Revisit the example. Identify the rule and check the result before completing this lesson.",
                        "راجع المثال، وحدّد القاعدة وتحقّق من النتيجة قبل إكمال الدرس.",
                    ),
                    example,
                    answer,
                ),
            ),
        )
    }

    val books =
        listOf(
            Book(
                "book-1",
                Copy("Book one", "الكتاب الأول"),
                Copy("Calculus & linear algebra", "التحليل والجبر الخطي"),
                listOf(
                    topic(
                        "limits",
                        "Limits",
                        "النهايات",
                        "lim",
                        "lim x→1 (x² − 1)/(x − 1) = 2",
                        Copy(
                            "Factor into (x − 1)(x + 1). For x ≠ 1 cancel x − 1, then take the limit of x + 1.",
                            "حلّل إلى (x − 1)(x + 1). عندما x ≠ 1 اختصر x − 1، ثم احسب نهاية x + 1.",
                        ),
                    ),
                    topic(
                        "derivatives",
                        "Derivatives",
                        "الاشتقاق",
                        "f′",
                        "d/dx (x²) = 2x",
                        Copy(
                            "The power rule is d/dx(xⁿ) = nxⁿ⁻¹. With n = 2 the derivative is 2x.",
                            "قاعدة القوى: مشتقة xⁿ هي nxⁿ⁻¹. عند n = 2 تكون المشتقة 2x.",
                        ),
                    ),
                    topic(
                        "integrals",
                        "Integrals",
                        "التكامل",
                        "∫",
                        "∫ 2x dx = x² + C",
                        Copy(
                            "Increase the exponent by one and divide by the new exponent. Include C for an indefinite integral.",
                            "زد الأس واحدًا واقسم على الأس الجديد. أضف ثابت التكامل C في التكامل غير المحدد.",
                        ),
                    ),
                    topic(
                        "matrices",
                        "Matrices",
                        "المصفوفات",
                        "[A]",
                        "det [[1, 2], [3, 4]] = −2",
                        Copy(
                            "A 2 × 2 determinant is ad − bc: 1 × 4 − 2 × 3 = −2.",
                            "محدد مصفوفة 2 × 2 يساوي ad − bc: أي 1 × 4 − 2 × 3 = −2.",
                        ),
                    ),
                ),
            ),
            Book(
                "book-2",
                Copy("Book two", "الكتاب الثاني"),
                Copy("Probability & mathematical thinking", "الاحتمالات والتفكير الرياضي"),
                listOf(
                    topic(
                        "probability",
                        "Probability",
                        "الاحتمالات",
                        "P",
                        "P(even on a fair die) = 3/6 = 1/2",
                        Copy(
                            "2, 4 and 6 are favorable, out of six equally likely outcomes.",
                            "النتائج الموافقة هي 2 و4 و6 من ست نتائج متساوية الاحتمال.",
                        ),
                    ),
                    topic(
                        "geometry",
                        "Spatial geometry",
                        "الهندسة الفضائية",
                        "△",
                        "V(cube) = a³; a = 3 → V = 27",
                        Copy(
                            "Volume is length × width × height. A cube has equal edges, so V = a³.",
                            "الحجم يساوي الطول × العرض × الارتفاع. أبعاد المكعب متساوية، لذلك V = a³.",
                        ),
                    ),
                    topic(
                        "logarithms",
                        "Logarithms",
                        "اللوغارتمات",
                        "ln",
                        "ln(e²) = 2",
                        Copy(
                            "The natural logarithm is the inverse exponential function: ln(eˣ) = x.",
                            "اللوغارتم الطبيعي عكس الدالة الأسية: ln(eˣ) = x.",
                        ),
                    ),
                    topic(
                        "sequences",
                        "Sequences",
                        "المتتاليات",
                        "uₙ",
                        "uₙ = 2n + 1; u₃ = 7",
                        Copy("Substitute n = 3: 2 × 3 + 1 = 7.", "عوّض n = 3: أي 2 × 3 + 1 = 7."),
                    ),
                ),
            ),
        )
    val initiallyCompleted =
        setOf("limits-foundations", "derivatives-foundations", "probability-foundations")
    val weeklyActivity = listOf(1, 2, 0, 3, 1, 2, 1)
    const val initialExercises = 9
    const val streak = 3
    const val dailyGoal = 3
    val examples =
        listOf(
            ProblemExample(Copy("Equations", "معادلات"), "2x + 4 = 10"),
            ProblemExample(Copy("Derivatives", "اشتقاق"), "d/dx(x^2)"),
            ProblemExample(Copy("Integrals", "تكامل"), "∫ 2x dx"),
        )

    fun lesson(id: String) = books.flatMap { it.lessons }.find { it.id == id }

    fun topic(id: String) = books.flatMap { it.topics }.find { it.id == id }
}
