package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.*

object DemoContent {
    private val rules =
        listOf(
            Copy(
                "A limit describes the value approached near a point. The function need not be defined at the point. If both one-sided limits exist and agree, the two-sided limit exists. Direct substitution in a polynomial is valid. For 0/0, factor before evaluating.",
                "تصف النهاية القيمة التي تقترب منها الدالة قرب نقطة، ولا يشترط تعريفها عند النقطة. إذا وجدت النهايتان من الجهتين وتساوتا وجدت النهاية. في كثيرات الحدود يصح التعويض المباشر؛ وفي 0/0 حلّل قبل الحساب.",
            ),
            Copy(
                "f′(x)=lim h→0 [f(x+h)−f(x)]/h. For x² expand the square, cancel x², divide by h≠0 and let h→0: f′(x)=2x. This is the instantaneous rate of change and tangent slope.",
                "f′(x)=lim h→0 [f(x+h)−f(x)]/h. في x² انشر المربع واختصر x² ثم اقسم على h≠0 واجعل h تؤول إلى صفر: المشتقة 2x وهي معدل التغير اللحظي وميل المماس.",
            ),
            Copy(
                "An antiderivative F satisfies F′=f. For substitution choose u=x²+1, du=2x dx. Then ∫2x(x²+1)dx=∫u du=u²/2+C=(x²+1)²/2+C. Differentiate to check. A definite integral is F(b)−F(a).",
                "الدالة الأصلية F تحقق F′=f. بالتعويض اختر u=x²+1 وdu=2x dx. فيصبح ∫2x(x²+1)dx=∫u du=u²/2+C=(x²+1)²/2+C. اشتق للتحقق. التكامل المحدد هو F(b)−F(a).",
            ),
            Copy(
                "For A of size m×n and B of size n×p, AB is m×p. Each entry is a row-column dot product. For [[1,2],[3,4]] times the identity: first row yields 1×1+2×0=1 and 1×0+2×1=2; the second yields 3 and 4. Multiplication is generally not commutative.",
                "إذا كانت A بأبعاد m×n وB بأبعاد n×p فإن AB بأبعاد m×p. كل عنصر جداء صف بعمود. لضرب [[1,2],[3,4]] بمصفوفة الوحدة يعطي الصف الأول 1×1+2×0=1 و1×0+2×1=2، والثاني 3 و4. الضرب عمومًا غير تبديلي.",
            ),
            Copy(
                "For equally likely outcomes P(A)=favorable/total. Conditioning on B restricts the sample space: P(A|B)=P(A∩B)/P(B), with P(B)>0. Given an even die result {2,4,6}, P(6|even)=1/3. Independent events satisfy P(A∩B)=P(A)P(B).",
                "للنتائج المتساوية الاحتمال P(A)=الموافق/الكلي. يقيّد الشرط B فضاء النتائج: P(A|B)=P(A∩B)/P(B) مع P(B)>0. إذا كانت نتيجة النرد زوجية {2,4,6} فاحتمال 6 هو 1/3. للأحداث المستقلة P(A∩B)=P(A)P(B).",
            ),
            Copy(
                "A spatial point has three coordinates. Distance is √[(x₂−x₁)²+(y₂−y₁)²+(z₂−z₁)²]. From (0,0,0) to (1,2,2), distance is √9=3. Volume uses cubic units; a cube of edge 3 has volume 27.",
                "للنقطة الفراغية ثلاثة إحداثيات. المسافة √[(x₂−x₁)²+(y₂−y₁)²+(z₂−z₁)²]. من (0,0,0) إلى (1,2,2) المسافة √9=3. يقاس الحجم بوحدات مكعبة؛ حجم مكعب حرفه 3 هو 27.",
            ),
            Copy(
                "The real domain of ln is x>0. ln is the inverse of eˣ: ln(e²)=2. Products become sums only for positive arguments: ln(ab)=ln(a)+ln(b). ln(a+b) cannot be split. Solve ln(x)=2 by exponentiating both sides to get x=e².",
                "مجال ln الحقيقي هو x>0 وهي عكس eˣ: ln(e²)=2. يتحول الجداء إلى مجموع لوسائط موجبة فقط: ln(ab)=ln(a)+ln(b). لا يمكن تفكيك ln(a+b). لحل ln(x)=2 طبّق الأسية على الطرفين لتحصل على x=e².",
            ),
            Copy(
                "A sequence maps integer indices to values. Starting at 1, an arithmetic sequence has uₙ=u₁+(n−1)r; a geometric sequence has uₙ=u₁qⁿ⁻¹. For u₁=2,q=3: u₄=54. Check the starting index before substitution.",
                "تربط المتتالية الأدلة الصحيحة بقيم. عند البداية من 1 للحسابية uₙ=u₁+(n−1)r وللهندسية uₙ=u₁qⁿ⁻¹. إذا u₁=2 وq=3 فإن u₄=54. تحقّق من دليل البداية قبل التعويض.",
            ),
        )
    private val extraTitles =
        listOf(
            Copy("Continuity and one-sided limits", "الاستمرار والنهايات الجانبية"),
            Copy("Tangent and derivative definition", "المماس وتعريف المشتقة"),
            Copy("Integration by substitution", "التكامل بالتعويض"),
            Copy("Matrix multiplication", "ضرب المصفوفات"),
            Copy("Conditional probability", "الاحتمال الشرطي"),
            Copy("Distance in space", "المسافة في الفراغ"),
            Copy("Logarithmic equations", "المعادلات اللوغارتمية"),
            Copy("Arithmetic and geometric sequences", "المتتاليات الحسابية والهندسية"),
        )
    private val checkpoints =
        listOf(
            "lim x→3 (x+2)" to "5",
            "d/dx(x²) at x=3" to "6",
            "∫₀¹2x dx" to "1",
            "det [[1,0],[0,1]]" to "1",
            "P(head on fair coin)" to "0.5",
            "V(cube), a=2" to "8",
            "ln(1)" to "0",
            "uₙ=2n+1; u₂" to "5",
        )
    private val allTopics =
        BaseDemoContent.books
            .flatMap { it.topics }
            .mapIndexed { i, topic ->
                val first = topic.lessons.first()
                val rich =
                    topic.lessons.map {
                        it.copy(
                            introduction = rules[i],
                            rule = first.explanation,
                            mistake =
                                Copy(
                                    "Check the domain and verify each algebraic step. Never divide by zero.",
                                    "تحقّق من المجال ومن كل خطوة جبرية. لا تقسم على صفر.",
                                ),
                            checkpoint = checkpoints[i].first,
                            checkpointAnswer = checkpoints[i].second,
                        )
                    }
                topic.copy(
                    lessons =
                        rich +
                            first.copy(
                                id = "${topic.id}-applications",
                                title = extraTitles[i],
                                minutes = 15,
                                introduction = rules[i],
                                explanation = rules[i],
                                rule = first.explanation,
                                checkpoint = checkpoints[i].first,
                                checkpointAnswer = checkpoints[i].second,
                            )
                )
            }
    val books =
        listOf(
            Book(
                "book-1",
                Copy("Book one", "الكتاب الأول"),
                Copy("Calculus and sequences", "التحليل والمتتاليات"),
                listOf("limits", "derivatives", "integrals", "sequences").map { id ->
                    allTopics.first { it.id == id }
                },
            ),
            Book(
                "book-2",
                Copy("Book two", "الكتاب الثاني"),
                Copy("Algebra, probability and geometry", "الجبر والاحتمالات والهندسة"),
                listOf("matrices", "probability", "geometry", "logarithms").map { id ->
                    allTopics.first { it.id == id }
                },
            ),
        )
    val initiallyCompleted = BaseDemoContent.initiallyCompleted
    val weeklyActivity = BaseDemoContent.weeklyActivity
    const val initialExercises = 9
    const val streak = 3
    const val dailyGoal = 3
    val examples =
        BaseDemoContent.examples +
            listOf(
                ProblemExample(Copy("Limits", "نهايات"), "lim x→1 (x^2-1)/(x-1)"),
                ProblemExample(Copy("Matrices", "مصفوفات"), "det [[1,2],[3,4]]"),
                ProblemExample(Copy("Probability", "احتمال"), "P(even on fair die)"),
                ProblemExample(Copy("Matrices", "مصفوفات"), "[[1,2],[3,4]]*[[1,0],[0,1]]"),
            )

    fun lesson(id: String) = books.flatMap { it.lessons }.find { it.id == id }

    fun topic(id: String) = allTopics.find { it.id == id }

    fun topicOf(id: String) = allTopics.find { it.lessons.any { lesson -> lesson.id == id } }
}

object ProductSeed {
    private fun q(
        topic: String,
        index: Int,
        prompt: Copy,
        expression: String,
        answer: String,
        kind: QuestionKind,
        options: List<String>,
        explain: Copy,
    ) =
        Question(
            "$topic-q$index",
            topic,
            prompt,
            expression,
            kind,
            options,
            answer,
            Copy(
                "Identify the applicable rule, then substitute carefully.",
                "حدّد القاعدة المناسبة ثم عوّض بدقة.",
            ),
            explain,
        )

    private val basic =
        listOf(
            Triple("limits", "lim x→3 (x+2)", "5"),
            Triple("derivatives", "d/dx(x²) at x=3", "6"),
            Triple("integrals", "∫₀¹ 2x dx", "1"),
            Triple("matrices", "det [[1,0],[0,1]]", "1"),
            Triple("probability", "P(head on fair coin)", "0.5"),
            Triple("geometry", "V(cube), a=2", "8"),
            Triple("logarithms", "ln(1)", "0"),
            Triple("sequences", "uₙ=2n+1; u₂", "5"),
        )
    val questions =
        basic.flatMap { (id, expr, answer) ->
            val topic = DemoContent.topic(id)!!
            val secondary =
                when (id) {
                    "limits" -> "lim x→2 (x²)" to "4"
                    "derivatives" -> "d/dx(x³) at x=2" to "12"
                    "integrals" -> "∫₀¹ 3x² dx" to "1"
                    "matrices" -> "det [[1,2],[0,3]]" to "3"
                    "probability" -> "P(6 on fair die)" to "1/6"
                    "geometry" -> "distance((0,0,0),(1,2,2))" to "3"
                    "logarithms" -> "ln(e)" to "1"
                    else -> "uₙ=2n+1; u₃" to "7"
                }
            val symbolic =
                when (id) {
                    "derivatives" -> "d/dx(x²)" to "2x"
                    "integrals" -> "∫2x dx" to "x^2+C"
                    "sequences" -> "uₙ=2n+1; uₙ₊₁−uₙ" to "2"
                    else -> expr to answer
                }
            listOf(
                q(
                    id,
                    1,
                    Copy("Choose the correct result", "اختر النتيجة الصحيحة"),
                    expr,
                    answer,
                    QuestionKind.Choice,
                    listOf(answer, "2", "10", "−1").distinct(),
                    topic.lessons.first().explanation,
                ),
                q(
                    id,
                    2,
                    Copy("Enter the numerical result", "أدخل النتيجة العددية"),
                    expr,
                    answer,
                    QuestionKind.Numeric,
                    emptyList(),
                    topic.lessons.first().explanation,
                ),
                q(
                    id,
                    3,
                    Copy("Is the equality correct?", "هل المساواة صحيحة؟"),
                    "$expr = $answer",
                    "true",
                    QuestionKind.TrueFalse,
                    listOf("true", "false"),
                    topic.lessons.first().explanation,
                ),
                q(
                    id,
                    4,
                    Copy("Complete the expression", "أكمل التعبير"),
                    symbolic.first,
                    symbolic.second,
                    QuestionKind.Expression,
                    emptyList(),
                    topic.lessons.first().explanation,
                ),
                q(
                    id,
                    5,
                    Copy("Check the proposed answer", "تحقّق من الإجابة المقترحة"),
                    "$expr = 42",
                    "false",
                    QuestionKind.TrueFalse,
                    listOf("true", "false"),
                    topic.lessons.first().explanation,
                ),
                q(
                    id,
                    6,
                    Copy("Apply the rule to a new example", "طبّق القاعدة على مثال جديد"),
                    secondary.first,
                    secondary.second,
                    QuestionKind.Numeric,
                    emptyList(),
                    topic.lessons.first().explanation,
                ),
            )
        }
    val sets =
        basic.flatMap { (id, _, _) ->
            listOf(
                PracticeSet(
                    "$id-practice-set",
                    id,
                    Copy("Practice", "تدريب"),
                    questions.filter { it.topicId == id }.map { it.id },
                ),
                PracticeSet(
                    "$id-quiz",
                    id,
                    Copy("Mini quiz", "اختبار قصير"),
                    questions.filter { it.topicId == id }.take(4).map { it.id },
                    true,
                ),
            )
        }

    fun question(id: String) = questions.find { it.id == id }

    val students =
        (0..24).map { i ->
            Student(
                if (i == 0) "ahmed" else "student-$i",
                if (i == 0) Copy("Ahmed Mohammad", "أحمد محمد")
                else Copy("Student $i", "الطالب $i"),
                if (i == 0) DemoContent.initiallyCompleted
                else DemoContent.books.flatMap { it.lessons }.take(i % 18).map { it.id }.toSet(),
                if (i == 0) .65f else .35f + (i % 6) * .1f,
                if (i == 0) .75f else .3f + (i % 6) * .1f,
            )
        }

    fun seed(now: Long = System.currentTimeMillis()): ProductState {
        val day = 86_400_000L
        val classes =
            listOf(
                Classroom(
                    "class-a",
                    Copy("Science Baccalaureate — A", "الثالث الثانوي العلمي — أ"),
                    students.take(13).map { it.id },
                    "EN-A-2401",
                ),
                Classroom(
                    "class-b",
                    Copy("Science Baccalaureate — B", "الثالث الثانوي العلمي — ب"),
                    students.drop(13).map { it.id },
                    "EN-B-2402",
                ),
            )
        val assignments =
            listOf(
                Assignment(
                    "assignment-1",
                    Copy("Derivative review", "مراجعة الاشتقاق"),
                    Copy(
                        "Practice derivative rules and verify your answers.",
                        "تدرّب على قواعد الاشتقاق وتحقّق من إجاباتك.",
                    ),
                    "class-a",
                    questions.filter { it.topicId == "derivatives" }.take(4).map { it.id },
                    now + 2 * day,
                ),
                Assignment(
                    "assignment-2",
                    Copy("Limits checkpoint", "تقييم النهايات"),
                    Copy("Review algebraic limits.", "راجع النهايات الجبرية."),
                    "class-a",
                    questions.filter { it.topicId == "limits" }.take(4).map { it.id },
                    now - day,
                ),
                Assignment(
                    "assignment-3",
                    Copy("Probability fundamentals", "أساسيات الاحتمالات"),
                    Copy("Count the favorable outcomes.", "احسب النتائج الموافقة."),
                    "class-a",
                    questions.filter { it.topicId == "probability" }.take(4).map { it.id },
                    now - 3 * day,
                ),
            )
        val submissions =
            assignments
                .flatMapIndexed { index, a ->
                    students.take(if (index == 2) 9 else 5).map { s ->
                        val answers =
                            a.questionIds.associateWith { id ->
                                if (s.id == "ahmed" || s.id.hashCode() % 3 != 0)
                                    question(id)!!.answer
                                else "42"
                            }
                        Submission(
                            a.id,
                            s.id,
                            answers,
                            answers.count { question(it.key)?.answer == it.value } * 100 /
                                answers.size,
                            180,
                            now - day,
                            Copy(
                                "Good effort. Review the worked rules.",
                                "جهد جيد. راجع القواعد المحلولة.",
                            ),
                        )
                    }
                }
                .filter { it.studentId != "ahmed" || it.assignmentId == "assignment-3" }
        val resources =
            listOf(
                TeacherResource(
                    "resource-1",
                    Copy("Derivative formula sheet", "ورقة قوانين الاشتقاق"),
                    "derivatives",
                    ResourceKind.FormulaSheet,
                    Copy(
                        "d/dx(xⁿ)=nxⁿ⁻¹\nDerivative of a constant = 0",
                        "d/dx(xⁿ)=nxⁿ⁻¹\nمشتقة الثابت = 0",
                    ),
                    setOf("class-a", "class-b"),
                    now - day,
                )
            )
        val notes =
            listOf(
                AppNotification(
                    "note-1",
                    Copy("New assignment", "واجب جديد"),
                    assignments[0].title,
                    "assignments/assignment-1",
                    false,
                    now,
                ),
                AppNotification(
                    "note-2",
                    Copy("Teacher feedback", "ملاحظات المعلّمة"),
                    Copy("Your probability result is ready.", "نتيجة واجب الاحتمالات جاهزة."),
                    "assignments/assignment-3/result",
                    false,
                    now - day,
                ),
                AppNotification(
                    "note-3",
                    Copy("Study reminder", "تذكير بالدراسة"),
                    Copy("Take one focused learning step today.", "ابدأ خطوة تعلّم مركّزة اليوم."),
                    "study",
                    false,
                    now,
                ),
                AppNotification(
                    "note-4",
                    Copy("Submissions ready", "تسليمات جاهزة"),
                    Copy("Review your class responses.", "راجع إجابات الصف."),
                    "teacher/assignments/assignment-1",
                    true,
                    now,
                ),
            )
        val activity =
            (0..27)
                .filter { it % 4 != 3 }
                .map { i ->
                    ActivityItem(
                        "seed-$i",
                        Copy("Study session", "جلسة دراسة"),
                        now - i * day,
                        10 + i % 3 * 5,
                    )
                }
        return ProductState(
            classes = classes,
            assignments = assignments,
            submissions = submissions,
            resources = resources,
            notifications = notes,
            activity = activity,
        )
    }
}
