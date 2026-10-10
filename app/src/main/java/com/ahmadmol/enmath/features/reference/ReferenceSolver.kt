package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*

@Composable
fun ReferenceSolver() {
    var problem by rememberSaveable { mutableIntStateOf(0) }
    var method by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }
    var showInput by rememberSaveable { mutableStateOf(false) }
    val names =
        listOf(
            refText("نهايات وحالات عدم تعيين", "Limits and indeterminate forms"),
            refText("الاشتقاق والتابع اللوغاريتمي", "Differentiation and logarithms"),
            refText("التكامل بالتعويض", "Integration by substitution"),
            refText("المصفوفات والمحددات", "Matrices and determinants"),
        )
    ReferencePage("screen-solver") {
        Column {
            RefHeading(
                refText(
                    "الحل المفصّل مع التبريرات الرياضية",
                    "Detailed solution with mathematical reasoning",
                ),
                refText("المحلل الرياضي الذكي للبكالوريا", "Baccalaureate mathematics solver"),
                Icons.Outlined.Calculate,
            )
            RefLabel(
                refText(
                    "خطوات منهجية معتمدة وفق سلم تصحيح الشهادة الثانوية العامة السورية",
                    "Structured steps aligned with the Syrian Baccalaureate marking approach",
                )
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            RefLabel(
                refText("نماذج امتحانية نموذجية:", "Worked exam examples:"),
                RefPalette.white,
                9,
                true,
            )
            listOf(listOf(0, 1), listOf(2, 3)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { i ->
                        RefCard(
                            Modifier.weight(1f).heightIn(min = 59.dp).clickable {
                                problem = i
                                method = 0
                                showInput = false
                            },
                            if (problem == i) RefPalette.violet else RefPalette.border,
                            padding = 7.dp,
                        ) {
                            RefLabel(names[i], RefPalette.violet, 7, true)
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                NativeFormula(i, 11)
                            }
                        }
                    }
                }
            }
        }
        RefCard(accent = Color(0xFF3E3489), background = SolidColor(Color(0xFF13132E))) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RefLabel(names[problem], RefPalette.violet, 9, true)
                RefLabel(
                    refText("علمي", "Science"),
                    RefPalette.violet,
                    7,
                    modifier =
                        Modifier.border(.6.dp, Color(0xFF3F3484), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
            EquationPanel(96) { NativeFormula(problem, 19) }
            HorizontalDivider(
                Modifier.padding(vertical = 6.dp),
                thickness = .6.dp,
                color = RefPalette.border,
            )
            RefLabel(refText("طريقة الحل المختارة:", "Selected solution method:"))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RefButton(
                    refText(
                        "طريقة التحليل الرياضي\n(المعتمدة وزارياً)",
                        "Algebraic method\n(standard approach)",
                    ),
                    { method = 0 },
                    Modifier.weight(1f).testTag("method-algebra"),
                    method == 0,
                )
                RefButton(
                    refText(
                        "طريقة قاعدة لوبيتال\n(L'Hôpital)",
                        "L'Hôpital's rule\n(limit example)",
                    ),
                    {
                        method = 1
                        problem = 0
                    },
                    Modifier.weight(1f).testTag("method-hopital"),
                    method == 1,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                RefIcon(Icons.AutoMirrored.Outlined.FactCheck, RefPalette.green, 11)
                RefLabel(
                    refText("خطوات الحل الرياضي المتسلسلة", "Step-by-step mathematical solution"),
                    RefPalette.white,
                    9,
                    true,
                )
            }
            RefLabel(
                refText(
                    if (problem == 0 && method == 0) "4 خطوات" else "3 خطوات",
                    if (problem == 0 && method == 0) "4 steps" else "3 steps",
                ),
                size = 7,
            )
        }
        if (problem == 0 && method == 0) {
            StepPanel(
                1,
                refText(
                    "التعويض العددي المباشر يؤدي إلى الصفر في كل من البسط والمقام، وهي حالة عدم تعيين تستوجب التحليل الرياضي لإزالة العامل الصفري.",
                    "Direct substitution gives zero in numerator and denominator. Factor before cancelling the common factor.",
                ),
                true,
            ) {
                LimitFormula(1, 12)
            }
            StepPanel(
                2,
                refText(
                    "تحليل البسط باستخدام متطابقة فرق مربعين حيث a² − b² = (a − b)(a + b).",
                    "Factor the numerator using the difference of squares: a² − b² = (a − b)(a + b).",
                ),
            ) {
                LimitFormula(2, 12)
            }
            StepPanel(
                3,
                refText(
                    "اختزال العامل المشترك (x − 2) لأن x تسعى إلى 2 ولا تساويها تماماً (x ≠ 2).",
                    "Cancel (x − 2), because x approaches 2 without equalling 2 (x ≠ 2).",
                ),
            ) {
                LimitFormula(3, 11)
            }
            StepPanel(
                4,
                refText(
                    "التعويض المباشر بـ x = 2 بعد زوال حالة عدم التعيين لنحصل على النهاية المطلوبة: 4.",
                    "Substitute x = 2 after removing the indeterminate form. The limit is 4.",
                ),
            ) {
                LimitFormula(4, 12)
            }
        } else if (problem == 0) {
            StepPanel(
                1,
                refText(
                    "نتحقق أولاً من حالة عدم التعيين 0/0 ومن قابلية الاشتقاق قرب 2.",
                    "Verify the 0/0 form and differentiability near 2.",
                ),
                true,
            ) {
                LimitFormula(1, 12)
            }
            StepPanel(
                2,
                refText(
                    "نشتق البسط والمقام كلّاً على حدة: مشتق x² − 4 هو 2x ومشتق x − 2 هو 1.",
                    "Differentiate numerator and denominator separately: 2x and 1.",
                ),
            ) {
                LimitFormula(5, 14)
            }
            StepPanel(
                3,
                refText(
                    "نحسب نهاية نسبة المشتقات عند x = 2 لنحصل على 4.",
                    "Evaluate the derivative ratio at x = 2 to obtain 4.",
                ),
            ) {
                MathRun("2 × 2 = 4", 16)
            }
        } else {
            val equations =
                when (problem) {
                    1 -> listOf("u = x² + 1,  u′ = 2x", "(ln u)′ = u′ / u", "f′(x) = 2x / (x² + 1)")
                    2 -> listOf("u = x²,  du = 2x dx", "∫ xeˣ² dx = ½ ∫ eᵘ du", "½ eˣ² + C")
                    else ->
                        listOf(
                            "det(A) = 3 × 4 − 1 × 2 = 10",
                            "A⁻¹ = ¹⁄₁₀ (4  −1; −2  3)",
                            "A · A⁻¹ = I",
                        )
                }
            val explanations =
                when (problem) {
                    1 ->
                        listOf(
                            refText(
                                "نعرّف التابع الداخلي ونحسب مشتقه.",
                                "Identify and differentiate the inner function.",
                            ),
                            refText(
                                "نطبّق قاعدة اشتقاق التابع المركب اللوغاريتمي.",
                                "Apply the logarithmic chain rule.",
                            ),
                            refText(
                                "نعوّض ونبسط الكسر؛ x² + 1 موجب لكل x.",
                                "Substitute and simplify; x² + 1 is always positive.",
                            ),
                        )
                    2 ->
                        listOf(
                            refText(
                                "نختار التعويض ونحسب التفاضل.",
                                "Choose the substitution and its differential.",
                            ),
                            refText("نستبدل x dx بنصف du.", "Replace x dx with half of du."),
                            refText(
                                "نتكامل ثم نعود إلى المتغير الأصلي ونضيف ثابت التكامل.",
                                "Integrate, substitute back and add the integration constant.",
                            ),
                        )
                    else ->
                        listOf(
                            refText(
                                "نحسب المحدد ونلاحظ أنه غير صفري.",
                                "Compute the nonzero determinant.",
                            ),
                            refText(
                                "نبدّل عنصري القطر ونغيّر إشارتي العنصرين الآخرين، ثم نقسم على المحدد.",
                                "Swap diagonal entries, negate off-diagonal entries and divide by the determinant.",
                            ),
                            refText(
                                "نتحقق من النتيجة بالضرب للحصول على مصفوفة الوحدة.",
                                "Verify by multiplying to obtain the identity matrix.",
                            ),
                        )
                }
            equations.forEachIndexed { i, e ->
                StepPanel(i + 1, explanations[i]) { MathRun(e, 13) }
            }
        }
        RefCard(
            accent = RefPalette.green.copy(alpha = .65f),
            background = Brush.horizontalGradient(listOf(Color(0xFF05201F), Color(0xFF0C242A))),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    RefLabel(
                        refText("النتيجة النهائية المحسوبة بدقة:", "Final calculated result:"),
                        RefPalette.green,
                        8,
                        true,
                    )
                    MathRun(
                        when (problem) {
                            0 -> "4"
                            1 -> "2x / (x² + 1)"
                            2 -> "½ eˣ² + C"
                            else -> "A⁻¹ = ¹⁄₁₀ (4  −1; −2  3)"
                        },
                        16,
                        RefPalette.white,
                    )
                }
                RefIcon(Icons.Outlined.CheckCircleOutline, RefPalette.green, 21)
            }
        }
        RefCard {
            RefLabel(
                refText("لوحة الرموز الرياضية التفاعلية:", "Interactive mathematical symbols:")
            )
            val keys =
                listOf(
                    "lim\nx→0",
                    "f′(x)",
                    "∫",
                    "x²",
                    "ln(x)",
                    "⁰⁄₀",
                    "x²",
                    "√x",
                    "π",
                    "∞",
                    "(a c; b d)",
                    "sin(x)",
                )
            listOf(keys.take(6), keys.drop(6)).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    row.forEach { key ->
                        Box(
                            Modifier.weight(1f)
                                .height(if (key.contains(";")) 53.dp else 32.dp)
                                .background(RefPalette.equation, RoundedCornerShape(5.dp))
                                .border(.6.dp, RefPalette.border, RoundedCornerShape(5.dp))
                                .clickable {
                                    input += key
                                    showInput = true
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            MathRun(key, 9)
                        }
                    }
                }
            }
            if (showInput) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        input,
                        { input = it },
                        Modifier.fillMaxWidth().testTag("reference-math-input"),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                }
                RefLabel(
                    refText(
                        "أمثلة تجريبية منسقة؛ اختر نموذجاً أعلاه لعرض الحل الصحيح.",
                        "Curated demo examples; select an example above for its verified solution.",
                    )
                )
                RefButton(
                    refText("مسح الإدخال", "Clear input"),
                    {
                        input = ""
                        showInput = false
                    },
                )
            }
        }
    }
}

@Composable
private fun EquationPanel(height: Int, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxWidth()
            .height(height.dp)
            .background(RefPalette.equation, RoundedCornerShape(6.dp))
            .border(.6.dp, RefPalette.border, RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun StepPanel(
    number: Int,
    explanation: String,
    hint: Boolean = false,
    formula: @Composable () -> Unit,
) {
    RefCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RefLabel(refText("الخطوة $number:", "Step $number:"), RefPalette.violet, 8, true)
            if (hint) RefLabel(refText("♧ انتبه للحل!", "Check the form!"), Color(0xFFFBB500), 8)
        }
        EquationPanel(74, formula)
        RefLabel(explanation, RefPalette.white, 9)
    }
}
