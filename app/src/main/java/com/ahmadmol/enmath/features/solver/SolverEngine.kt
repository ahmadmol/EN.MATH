package com.ahmadmol.enmath.features.solver

import com.ahmadmol.enmath.core.model.*

/** Contract for a future local engine, backend or AI adapter. */
interface SolverEngine {
    fun solve(expression: String): Solution
}

/** Only returns curated worked examples for exact supported inputs. Never guesses. */
class DemoSolverEngine : SolverEngine {
    private fun key(value: String) =
        value.replace(" ", "").replace("−", "-").replace("×", "*").replace("²", "^2").lowercase()

    override fun solve(expression: String): Solution {
        val steps: List<SolutionStep>
        val answer: String
        when (key(expression)) {
            "limx→1(x^2-1)/(x-1)" -> {
                val factoring =
                    listOf(
                        SolutionStep(
                            Copy("Factor", "حلّل"),
                            "(x²−1)/(x−1)=(x−1)(x+1)/(x−1)",
                            Copy("Difference of squares.", "فرق مربعين."),
                        ),
                        SolutionStep(
                            Copy("Cancel away from 1", "اختصر خارج 1"),
                            "x+1, x≠1",
                            Copy(
                                "The expressions agree in a punctured neighborhood.",
                                "يتفق التعبيران في جوار مثقوب.",
                            ),
                        ),
                        SolutionStep(
                            Copy("Take the limit", "احسب النهاية"),
                            "1+1=2",
                            Copy("Continuity of a polynomial.", "استمرار كثير الحدود."),
                        ),
                    )
                val hopital =
                    listOf(
                        SolutionStep(
                            Copy("Check the hypotheses", "تحقّق من الشروط"),
                            "0/0; d/dx(x−1)=1≠0",
                            Copy(
                                "Both functions are differentiable near 1; the derivative ratio has a limit.",
                                "الدالتان قابلتان للاشتقاق قرب 1 ونهاية نسبة المشتقتين موجودة.",
                            ),
                        ),
                        SolutionStep(
                            Copy("L'Hôpital's rule", "قاعدة لوبيتال"),
                            "lim x→1 2x/1=2",
                            Copy(
                                "Use the derivative ratio only after verifying the hypotheses.",
                                "استخدم نسبة المشتقتين بعد التحقق من الشروط.",
                            ),
                        ),
                    )
                return Solution(
                    expression,
                    "2",
                    factoring,
                    topicId = "limits",
                    methods =
                        listOf(
                            SolutionMethod(Copy("Factoring", "التحليل"), factoring),
                            SolutionMethod(Copy("L'Hôpital", "لوبيتال"), hopital),
                        ),
                )
            }
            "det[[1,2],[3,4]]" ->
                return Solution(
                    expression,
                    "−2",
                    listOf(
                        SolutionStep(
                            Copy("Determinant rule", "قاعدة المحدد"),
                            "ad−bc",
                            Copy("For a 2×2 matrix.", "لمصفوفة 2×2."),
                        ),
                        SolutionStep(
                            Copy("Substitute", "عوّض"),
                            "1×4−2×3=−2",
                            Copy("Subtract the cross products.", "اطرح جداء القطرين."),
                        ),
                    ),
                    topicId = "matrices",
                )
            "[[1,2],[3,4]]*[[1,0],[0,1]]" ->
                return Solution(
                    expression,
                    "[[1,2],[3,4]]",
                    listOf(
                        SolutionStep(
                            Copy("Row-column products", "جداء الصف والعمود"),
                            "[1×1+2×0, 1×0+2×1]=[1,2]",
                            Copy("Compute the first row.", "احسب الصف الأول."),
                        ),
                        SolutionStep(
                            Copy("Second row", "الصف الثاني"),
                            "[3×1+4×0, 3×0+4×1]=[3,4]",
                            Copy(
                                "The identity matrix preserves the original matrix.",
                                "تحافظ مصفوفة الوحدة على المصفوفة الأصلية.",
                            ),
                        ),
                    ),
                    topicId = "matrices",
                )
            "p(evenonfairdie)" ->
                return Solution(
                    expression,
                    "1/2",
                    listOf(
                        SolutionStep(
                            Copy("Sample space", "فضاء النتائج"),
                            "Ω={1,2,3,4,5,6}",
                            Copy(
                                "A fair die has six equally likely outcomes.",
                                "للنرد العادل ست نتائج متساوية الاحتمال.",
                            ),
                        ),
                        SolutionStep(
                            Copy("Favorable outcomes", "النتائج الموافقة"),
                            "A={2,4,6}; P(A)=3/6=1/2",
                            Copy(
                                "Divide favorable by total outcomes.",
                                "اقسم عدد النتائج الموافقة على الكلي.",
                            ),
                        ),
                    ),
                    topicId = "probability",
                )
            "2x+4=10" -> {
                answer = "x = 3"
                steps =
                    listOf(
                        SolutionStep(
                            Copy("Subtract 4 on both sides", "اطرح 4 من الطرفين"),
                            "2x + 4 − 4 = 10 − 4",
                            Copy(
                                "Keep the equation balanced by doing the same operation on both sides.",
                                "حافظ على توازن المعادلة بإجراء العملية نفسها على الطرفين.",
                            ),
                        ),
                        SolutionStep(
                            Copy("Simplify", "بسّط"),
                            "2x = 6",
                            Copy(
                                "The constant terms cancel on the left.",
                                "تختصر الحدود الثابتة في الطرف الأيسر.",
                            ),
                        ),
                        SolutionStep(
                            Copy("Divide by 2", "اقسم على 2"),
                            "x = 3",
                            Copy("Check: 2 × 3 + 4 = 10.", "تحقّق: 2 × 3 + 4 = 10."),
                        ),
                    )
            }
            "d/dx(x^2)" -> {
                answer = "2x"
                steps =
                    listOf(
                        SolutionStep(
                            Copy("Identify the power", "حدّد الأس"),
                            "f(x) = x², n = 2",
                            Copy("This is a power function.", "هذه دالة قوة."),
                        ),
                        SolutionStep(
                            Copy("Apply the power rule", "طبّق قاعدة القوى"),
                            "d/dx(xⁿ) = nxⁿ⁻¹",
                            Copy(
                                "Multiply by the exponent and reduce it by one.",
                                "اضرب بالأس وأنقصه واحدًا.",
                            ),
                        ),
                        SolutionStep(
                            Copy("Simplify", "بسّط"),
                            "f′(x) = 2x",
                            Copy(
                                "For n = 2, the remaining exponent is 1.",
                                "عند n = 2 يكون الأس المتبقي 1.",
                            ),
                        ),
                    )
            }
            "∫2xdx" -> {
                answer = "x² + C"
                steps =
                    listOf(
                        SolutionStep(
                            Copy("Take out the constant", "أخرج الثابت"),
                            "∫ 2x dx = 2 ∫ x dx",
                            Copy("Integration is linear.", "التكامل عملية خطية."),
                        ),
                        SolutionStep(
                            Copy("Integrate the power", "كامل القوة"),
                            "2(x² / 2) + C",
                            Copy(
                                "Use ∫ xⁿ dx = xⁿ⁺¹/(n+1) + C for n ≠ −1.",
                                "استخدم قاعدة تكامل القوى عندما n ≠ −1.",
                            ),
                        ),
                        SolutionStep(
                            Copy("Simplify and check", "بسّط وتحقّق"),
                            "x² + C",
                            Copy(
                                "Differentiate the result to obtain 2x.",
                                "اشتق النتيجة لتحصل على 2x.",
                            ),
                        ),
                    )
            }
            else -> return Solution(expression, null, emptyList())
        }
        return Solution(
            expression,
            answer,
            steps,
            topicId =
                when (key(expression)) {
                    "2x+4=10" -> "equations"
                    "∫2xdx" -> "integrals"
                    else -> "derivatives"
                },
        )
    }
}
