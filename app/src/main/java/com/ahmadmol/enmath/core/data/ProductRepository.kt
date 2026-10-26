package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.*
import java.util.Locale
import kotlin.math.roundToInt
import org.json.JSONArray
import org.json.JSONObject

fun ProductState.question(id: String) = questions.find { it.id == id } ?: ProductSeed.question(id)

val LearningState.activeStudentId: String
    get() = LearningRules.activeStudentId(this)

object LearningRules {
    /** Provenance for learning only; Guest has no Gems owner. Legacy student IDs stay intact. */
    fun commandOwner(state: LearningState): String? = when (state.user?.type) {
        AccountType.Student -> state.user.localId ?: state.user.email.trim().lowercase(Locale.ROOT).takeIf { it.isNotEmpty() }
        AccountType.Guest -> state.user.localId ?: state.localGuestId
        else -> null
    }

    fun acceptsCommand(state: LearningState, startedBy: String?): Boolean =
        startedBy != null && startedBy == commandOwner(state)

    val historyTopicIds = listOf("all", "limits", "derivatives", "integrals", "matrices", "probability")

    fun solutionHistory(history: List<HistoryItem>, topicId: String, query: String): List<HistoryItem> {
        val topic = topicId.takeIf { it in historyTopicIds } ?: "all"
        val term = search(query)
        return history.filter { search(it.expression).contains(term) && (topic == "all" || it.topicId == topic) }
    }

    fun activeStudentId(state: LearningState): String {
        val u = state.user ?: return "anonymous"
        u.localId?.let { return it }
        if (u.type == AccountType.Guest) return "guest"
        val email = u.email.trim().lowercase(Locale.ROOT)
        val canonical = "${u.type.name.lowercase(Locale.ROOT)}:$email"
        val account = if (u.type == AccountType.Student && email == "ahmed@example.com") "ahmed" else canonical
        if (u.type == AccountType.Student && email.isNotBlank()) {
            state.studentRecordId?.takeIf {
                it.isNotBlank() && state.studentRecordOwners[it] == account
            }?.let { return it }
        }
        // Only the explicit seeded account maps to demo classroom data.
        if (u.type == AccountType.Student && email == "ahmed@example.com" &&
            state.studentRecordOwners["ahmed"].let { it == null || it == account }) return "ahmed"
        if (email.isNotBlank()) return canonical
        return "unidentified:${u.type.name.lowercase(Locale.ROOT)}"
    }

    fun correct(question: Question, answer: String): Boolean {
        fun normalized(s: String) =
            s.trim().lowercase(Locale.ROOT).replace(" ", "").replace("−", "-").replace("²", "^2")
        fun number(s: String): Double? {
            val text = s.trim()
            val parts = text.split("/")
            return if (parts.size == 2) {
                val a = parts[0].toDoubleOrNull()
                val b = parts[1].toDoubleOrNull()
                if (a != null && b != null && b != 0.0) a / b else null
            } else text.toDoubleOrNull()
        }
        val actual = number(answer)
        val expected = number(question.answer)
        return if (actual != null && expected != null) kotlin.math.abs(actual - expected) < 1e-6
        else normalized(answer) == normalized(question.answer)
    }

    fun mastery(topic: Topic, completed: Set<String>, accuracy: Float, quiz: Float): TopicMastery {
        val done = topic.lessons.count { it.id in completed }
        val score =
            (40f * done / topic.lessons.size +
                    40f * accuracy.coerceIn(0f, 1f) +
                    20f * quiz.coerceIn(0f, 1f))
                .roundToInt()
        val stage =
            when {
                score >= 90 -> Mastery.Mastered
                score >= 70 -> Mastery.Proficient
                score >= 45 -> Mastery.Practicing
                score >= 20 -> Mastery.Learning
                score > 0 -> Mastery.Started
                else -> Mastery.NotStarted
            }
        return TopicMastery(score, stage, done, accuracy, quiz)
    }

    fun topic(state: LearningState, id: String, studentId: String = state.activeStudentId): TopicMastery {
        val t = state.topic(id) ?: return TopicMastery(0, Mastery.NotStarted, 0, 0f, 0f)
        val targetStudentId = if (studentId.isBlank()) state.activeStudentId else studentId
        val s = ProductSeed.students.find { it.id == targetStudentId }
        val isCurrentStudent = targetStudentId == state.activeStudentId
        val record = state.studentRecords[targetStudentId]
        val completed = if (isCurrentStudent) state.completed else record?.completed ?: s?.completed.orEmpty()
        val attempts =
            if (isCurrentStudent)
                state.product.attempts.filter {
                    ProductSeed.sets.find { set -> set.id == it.setId }?.topicId == id
                }
            else record?.product?.attempts.orEmpty().filter {
                ProductSeed.sets.find { set -> set.id == it.setId }?.topicId == id
            }
        val accuracy =
            attempts.lastOrNull { !it.setId.endsWith("quiz") }?.accuracy
                ?: if (completed.any { it.startsWith("$id-") }) (s?.practiceAccuracy ?: 0f) else 0f
        val quiz =
            attempts.filter { it.setId.endsWith("quiz") }.maxOfOrNull { it.accuracy }
                ?: if (completed.any { it.startsWith("$id-") }) (s?.quizAccuracy ?: 0f) else 0f
        return mastery(t, completed, accuracy, quiz)
    }

    fun stats(state: LearningState, now: Long = System.currentTimeMillis()): ProductStats {
        val day = 86_400_000L
        val today = now / day
        val days = state.product.activity.map { it.timestamp / day }.toSet()
        var streak = 0
        var cursor = if (today in days) today else today - 1
        while (cursor in days) {
            streak++
            cursor--
        }
        val attempts = state.product.attempts
        val total = attempts.sumOf { it.total }
        val accuracy =
            if (total == 0) { if (state.user?.localId != null) 0f else ProductSeed.students.first().practiceAccuracy }
            else attempts.sumOf { it.correct }.toFloat() / total
        return ProductStats(
            state.lessons.count { it.id in state.completed }.toFloat() / state.lessons.size.coerceAtLeast(1),
            total,
            accuracy,
            state.product.activity.sumOf { it.minutes },
            streak,
            (6 downTo 0).map { offset ->
                state.product.activity
                    .filter { it.timestamp / day == today - offset }
                    .sumOf { it.minutes }
            },
        )
    }

    fun search(value: String) =
        value
            .lowercase(Locale.ROOT)
            .replace(Regex("[ًٌٍَُِّْـ]"), "")
            .replace(Regex("[إأآ]"), "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .trim()
}

/**
 * Versioned local JSON store, distinct from UI. A backend adapter may implement the same actions.
 */
class CorruptLocalData(cause: Throwable) : IllegalStateException("Local data needs recovery; original product_v1 preserved", cause)
class LocalDataWriteFailed : IllegalStateException("Local data save was not acknowledged")

internal object ProductCodec {
    private fun copy(v: Copy) = JSONObject().put("en", v.en).put("ar", v.ar)

    private fun c(o: JSONObject, key: String): Copy =
        o.getJSONObject(key).let { Copy(it.getString("en"), it.getString("ar")) }

    private fun strings(o: JSONObject, key: String) =
        o.getJSONArray(key).let { a -> (0 until a.length()).map { a.getString(it) } }

    private fun maps(o: JSONObject, key: String): Map<String, String> =
        o.getJSONObject(key)
            .let { m -> m.keys().asSequence().associateWith { m.getString(it) } }

    private fun objects(o: JSONObject, key: String) =
        o.getJSONArray(key).let { a -> (0 until a.length()).map { a.getJSONObject(it) } }

    private fun array(values: Iterable<JSONObject>) =
        JSONArray().apply { values.forEach { put(it) } }

    fun encode(p: ProductState): String {
        val o = JSONObject().put("version", 1)
        o.put(
            "questions",
            array(
                p.questions.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("topic", it.topicId)
                        .put("prompt", copy(it.prompt))
                        .put("expression", it.expression)
                        .put("kind", it.kind.name)
                        .put("options", JSONArray(it.options))
                        .put("answer", it.answer)
                        .put("hint", copy(it.hint))
                        .put("explanation", copy(it.explanation))
                }
            ),
        )
        o.put(
            "attempts",
            array(
                p.attempts.map {
                    JSONObject()
                        .put("set", it.setId)
                        .put("answers", JSONObject(it.answers))
                        .put("correct", it.correct)
                        .put("total", it.total)
                        .put("seconds", it.seconds)
                        .put("time", it.timestamp)
                }
            ),
        )
        o.put(
            "classes",
            array(
                p.classes.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("name", copy(it.name))
                        .put("students", JSONArray(it.studentIds))
                        .put("code", it.code)
                        .put("topic", it.topicId)
                }
            ),
        )
        o.put(
            "assignments",
            array(
                p.assignments.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("title", copy(it.title))
                        .put("description", copy(it.description))
                        .put("class", it.classId)
                        .put("questions", JSONArray(it.questionIds))
                        .put("due", it.due)
                        .put("points", it.points)
                        .put("attempts", it.attempts)
                        .put("hints", it.hints)
                        .put("late", it.late)
                }
            ),
        )
        o.put(
            "submissions",
            array(
                p.submissions.map {
                    JSONObject()
                        .put("assignment", it.assignmentId)
                        .put("student", it.studentId)
                        .put("answers", JSONObject(it.answers))
                        .put("score", it.score)
                        .put("seconds", it.seconds)
                        .put("time", it.timestamp)
                        .put("feedback", copy(it.feedback))
                        .put("reviewed", it.reviewed)
                }
            ),
        )
        o.put(
            "resources",
            array(
                p.resources.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("title", copy(it.title))
                        .put("topic", it.topicId)
                        .put("kind", it.kind.name)
                        .put("body", copy(it.body))
                        .put("classes", JSONArray(it.classIds.toList()))
                        .put("time", it.updated)
                }
            ),
        )
        o.put(
            "notes",
            array(
                p.notifications.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("title", copy(it.title))
                        .put("body", copy(it.body))
                        .put("target", it.target)
                        .put("teacher", it.teacher)
                        .put("time", it.timestamp)
                }
            ),
        )
        o.put(
            "history",
            array(
                p.history.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("expression", it.expression)
                        .put("topic", it.topicId)
                        .put("time", it.timestamp)
                }
            ),
        )
        o.put(
            "activity",
            array(
                p.activity.map {
                    JSONObject()
                        .put("id", it.id)
                        .put("title", copy(it.title))
                        .put("time", it.timestamp)
                        .put("minutes", it.minutes)
                }
            ),
        )
        o.put("read", JSONArray(p.readNotifications.toList()))
            .put("lessons", JSONArray(p.savedLessons.toList()))
            .put("problems", JSONArray(p.savedProblems.toList()))
            .put("searches", JSONArray(p.searches))
            .put("enabled", p.notificationsEnabled)
            .put("reminder", p.reminder)
            .put("learning_commands", JSONArray(p.learningCommands.toList()))
            .put("verified_lessons", JSONObject(p.verifiedLessons))
            .put("pending_gems", array(p.pendingGemEvents.map {
                JSONObject().put("id", it.id).put("owner", it.owner).put("kind", it.kind)
                    .put("content", it.contentId).put("answers", JSONObject(it.answers)).put("at", it.at)
            }))
        return o.toString()
    }

    fun save(prefs: SharedPreferences, p: ProductState) {
        load(prefs)
        val editor = prefs.edit()
        // Additive recovery snapshot. Never replace the first preserved version.
        prefs.getString("product_v1", null)?.let { original ->
            if (!prefs.contains("product_v1_recovery")) editor.putString("product_v1_recovery", original)
        }
        if (!editor.putString("product_v1", encode(p)).commit()) throw LocalDataWriteFailed()
    }

    fun load(prefs: SharedPreferences): ProductState {
        val raw = prefs.getString("product_v1", null) ?: return ProductSeed.seed()
        return decode(raw)
    }

    fun decode(raw: String): ProductState {
        return runCatching {
                val o = JSONObject(raw)
                require(o.getInt("version") == 1) { "Unsupported product schema" }
                listOf("questions", "attempts", "classes", "assignments", "submissions", "resources",
                    "notes", "history", "activity", "read", "lessons", "problems", "searches")
                    .forEach { o.getJSONArray(it) }
                ProductState(
                    questions =
                        objects(o, "questions").map {
                            Question(
                                it.getString("id"),
                                it.getString("topic"),
                                c(it, "prompt"),
                                it.getString("expression"),
                                QuestionKind.valueOf(it.getString("kind")),
                                strings(it, "options"),
                                it.getString("answer"),
                                c(it, "hint"),
                                c(it, "explanation"),
                            )
                        },
                    attempts =
                        objects(o, "attempts").map {
                            Attempt(
                                it.getString("set"),
                                maps(it, "answers"),
                                it.getInt("correct"),
                                it.getInt("total"),
                                it.getInt("seconds"),
                                it.getLong("time"),
                            )
                        },
                    classes =
                        objects(o, "classes").map {
                            Classroom(
                                it.getString("id"),
                                c(it, "name"),
                                strings(it, "students"),
                                it.getString("code"),
                                it.getString("topic"),
                            )
                        },
                    assignments =
                        objects(o, "assignments").map {
                            Assignment(
                                it.getString("id"),
                                c(it, "title"),
                                c(it, "description"),
                                it.getString("class"),
                                strings(it, "questions"),
                                it.getLong("due"),
                                it.getInt("points"),
                                it.getInt("attempts"),
                                it.getBoolean("hints"),
                                it.getBoolean("late"),
                            )
                        },
                    submissions =
                        objects(o, "submissions").map {
                            Submission(
                                it.getString("assignment"),
                                it.getString("student"),
                                maps(it, "answers"),
                                it.getInt("score"),
                                it.getInt("seconds"),
                                it.getLong("time"),
                                c(it, "feedback"),
                                it.optBoolean("reviewed", false),
                            )
                        },
                    resources =
                        objects(o, "resources").map {
                            TeacherResource(
                                it.getString("id"),
                                c(it, "title"),
                                it.getString("topic"),
                                ResourceKind.valueOf(it.getString("kind")),
                                c(it, "body"),
                                strings(it, "classes").toSet(),
                                it.getLong("time"),
                            )
                        },
                    notifications =
                        objects(o, "notes").map {
                            AppNotification(
                                it.getString("id"),
                                c(it, "title"),
                                c(it, "body"),
                                it.getString("target"),
                                it.getBoolean("teacher"),
                                it.getLong("time"),
                            )
                        },
                    history =
                        objects(o, "history").map {
                            HistoryItem(
                                it.getString("id"),
                                it.getString("expression"),
                                it.getString("topic"),
                                it.getLong("time"),
                            )
                        },
                    activity =
                        objects(o, "activity").map {
                            ActivityItem(
                                it.getString("id"),
                                c(it, "title"),
                                it.getLong("time"),
                                it.getInt("minutes"),
                            )
                        },
                    readNotifications = strings(o, "read").toSet(),
                    savedLessons = strings(o, "lessons").toSet(),
                    savedProblems = strings(o, "problems").toSet(),
                    searches = strings(o, "searches"),
                    notificationsEnabled = o.optBoolean("enabled", true),
                    reminder = o.optString("reminder", "18:00"),
                    learningCommands = if (o.has("learning_commands")) strings(o, "learning_commands").toSet() else emptySet(),
                    verifiedLessons = if (o.has("verified_lessons")) maps(o, "verified_lessons") else emptyMap(),
                    pendingGemEvents = if (o.has("pending_gems")) objects(o, "pending_gems").map {
                        PendingGemEvent(it.getString("id"), it.getString("owner"), it.getString("kind"),
                            it.getString("content"), maps(it, "answers"), it.getLong("at"))
                    } else emptyList(),
                )
            }
            .getOrElse { throw CorruptLocalData(it) }
    }
}
