package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun ReferenceStudy(open: (String) -> Unit) {
    var book by rememberSaveable { mutableIntStateOf(0) }
    ReferencePage("screen-study") {
        RefHeading(
            refText("منهاج الرياضيات - البكالوريا العلمي", "Mathematics · Science Baccalaureate"),
            refText(
                "وفق الخطة المعتمدة لوزارة التربية السورية",
                "According to the Syrian Ministry of Education curriculum",
            ),
        )
        RefCard(
            Modifier,
            padding = 3.dp,
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    RefButton(
                        refText("الجزء الأول (تحليل)", "Part I (Analysis)"),
                        { book = 0 },
                        Modifier.weight(1f).testTag("reference-book-1"),
                        book == 0,
                    )
                    RefButton(
                        refText("الجزء الثاني (جبر وفضاء)", "Part II (Algebra & geometry)"),
                        { book = 1 },
                        Modifier.weight(1f).testTag("reference-book-2"),
                        book == 1,
                    )
                }
            },
        )
        RefCard {
            RefLabel(
                if (book == 0)
                    refText("الجزء الأول: التحليل والتوابع", "Part I: Analysis and functions")
                else
                    refText(
                        "الجزء الثاني: الجبر والهندسة الفضائية والاحتمالات",
                        "Part II: Algebra, spatial geometry and probability",
                    ),
                RefPalette.white,
                10,
                true,
            )
            RefLabel(
                if (book == 0)
                    refText(
                        "النهايات والاستمرار، الاشتقاق وتطبيقاته، الهندسة، التابع اللوغاريتمي والتابع الأسي، التكامل والتوابع الأصلية وحساب المساحات.",
                        "Limits, continuity, derivatives, logarithmic and exponential functions, integrals and areas.",
                    )
                else
                    refText(
                        "المصفوفات والمحددات، الأشعة في الفراغ، الجداء السلمي والمستقيم، الأعداد العقدية، والتحليل التوافقي والاحتمال الشرطي.",
                        "Matrices, determinants, vectors, complex numbers, combinatorics and conditional probability.",
                    )
            )
        }
        if (book == 0) {
            RefLabel(
                refText(
                    "الوحدة الأولى: نهايات التوابع والاستمرار",
                    "Unit I: Limits and continuity",
                ),
                RefPalette.violet,
                9,
                true,
            )
            TopicGroup(
                refText("مفهوم النهاية وحالات عدم التعيين", "Limits and indeterminate forms"),
                88,
                listOf(
                    refText("إزالة حالات عدم التعيين 0/0 و ∞/∞", "Removing indeterminate forms") to
                        "limits-practice",
                    refText(
                        "المقارب الأفقي والشاقولي للتابع والواقع النسبي",
                        "Horizontal and vertical asymptotes",
                    ) to "limits-foundation",
                ),
                open,
            )
            TopicGroup(
                refText(
                    "الوحدة الثانية: الاشتقاق وتطبيقاته وجدول التغيرات",
                    "Unit II: Derivatives and variation tables",
                ),
                75,
                emptyList(),
                open,
            )
            RefLabel(
                refText(
                    "الوحدة الثالثة: التابع اللوغاريتمي النيبيري والتابع الأسي",
                    "Unit III: Logarithmic and exponential functions",
                ),
                RefPalette.violet,
                9,
                true,
            )
            TopicGroup(
                refText("خواص ln(x) و eˣ وطلباتهما الجبرية", "Properties of ln(x) and eˣ"),
                82,
                emptyList(),
                open,
            )
        } else {
            RefLabel(
                refText(
                    "الوحدة الأولى: المصفوفات وحل المعادلات الخطية",
                    "Unit I: Matrices and linear equations",
                ),
                RefPalette.violet,
                9,
                true,
            )
            TopicGroup(
                refText("العمليات على المصفوفات وحساب المقلوب", "Matrix operations and inverses"),
                92,
                listOf(
                    refText(
                        "حساب محدد ومقلوب مصفوفة من المرتبة الثانية والثالثة",
                        "Second and third order determinants and inverses",
                    ) to "matrices-practice"
                ),
                open,
            )
            RefLabel(
                refText(
                    "الوحدة الثانية: الاحتمال والتحليل التوافقي",
                    "Unit II: Probability and combinatorics",
                ),
                RefPalette.violet,
                9,
                true,
            )
            TopicGroup(
                refText(
                    "الاحتمال الشرطي والاستقلال الاحتمالي وقانون بايز",
                    "Conditional probability, independence and Bayes' rule",
                ),
                78,
                emptyList(),
                open,
            )
        }
    }
}

@Composable
private fun TopicGroup(
    title: String,
    percent: Int,
    lessons: List<Pair<String, String>>,
    open: (String) -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(lessons.isNotEmpty()) }
    RefCard(
        Modifier,
        content = {
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    RefLabel(title, RefPalette.white, 9, true)
                    RefLabel(
                        refText("نسبة الإنجاز: $percent%", "Completion: $percent%"),
                        RefPalette.green,
                        7,
                    )
                }
                RefIcon(
                    if (expanded) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.ChevronLeft,
                    RefPalette.muted,
                    13,
                )
            }
            if (expanded) {
                val entries =
                    if (lessons.isEmpty())
                        listOf(
                            refText("شرح المفهوم وتطبيقاته", "Concepts and applications") to
                                "derivatives-practice"
                        )
                    else lessons
                entries.forEachIndexed { i, entry ->
                    androidx.compose.material3.HorizontalDivider(
                        thickness = .6.dp,
                        color = RefPalette.border,
                    )
                    Row(
                        Modifier.fillMaxWidth()
                            .clickable { open("lesson/${entry.second}") }
                            .padding(vertical = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        RefIcon(Icons.Outlined.CheckCircleOutline, RefPalette.green, 12)
                        Column(Modifier.weight(1f)) {
                            RefLabel(entry.first, RefPalette.white, 8, true)
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                RefIcon(Icons.Outlined.Schedule, RefPalette.muted, 9)
                                RefLabel(
                                    refText(
                                        if (i == 0 && lessons.size > 1) "25 دقيقة" else "30 دقيقة",
                                        if (i == 0 && lessons.size > 1) "25 minutes"
                                        else "30 minutes",
                                    ),
                                    size = 7,
                                )
                            }
                        }
                        RefIcon(Icons.Outlined.ChevronLeft, RefPalette.muted, 12)
                    }
                }
            }
        },
    )
}
