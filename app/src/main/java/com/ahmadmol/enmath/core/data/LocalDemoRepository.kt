package com.ahmadmol.enmath.core.data

import android.content.Context
import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Local demo session/preferences only. Never stores passwords or sends network requests. */
class LocalDemoRepository internal constructor(private val allowDemoAccounts: Boolean = true, private val preferences: (String) -> SharedPreferences) : LearningRepository {
    constructor(context: Context) : this(
        context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0,
        { name -> context.getSharedPreferences(name, Context.MODE_PRIVATE) })
    private val prefs = preferences("enmath_demo")
    // New isolated guest scope; unscoped legacy progress is never assigned to it.
    private val guestId = prefs.getString("w1_guest_uuid", null) ?: java.util.UUID.randomUUID().toString().also {
        if (!prefs.edit().putString("w1_guest_uuid", it).commit()) throw LocalDataWriteFailed()
    }

    private fun <T : Enum<T>> enumValue(value: String?, values: Array<T>, fallback: T): T =
        values.find { it.name == value } ?: fallback

    private val _state =
        MutableStateFlow(
            LearningState(
                localGuestId = guestId,
                user =
                    prefs.getString("name", null)?.let {
                        User(
                            it,
                            prefs.getString("email", "").orEmpty(),
                            enumValue(
                                prefs.getString("role", null),
                                AccountType.entries.toTypedArray(),
                                AccountType.Guest,
                            ),
                        ).takeIf { user -> allowDemoAccounts || user.type == AccountType.Guest }
                    },
                completed =
                    prefs.getStringSet("completed", null)?.toSet()
                        ?: DemoContent.initiallyCompleted,
                lastLessonId = prefs.getString("last", "limits-practice").orEmpty(),
                theme =
                    enumValue(
                        prefs.getString("theme", null),
                        ThemeMode.entries.toTypedArray(),
                        ThemeMode.System,
                    ),
                language =
                    enumValue(
                        prefs.getString("language", null),
                        Language.entries.toTypedArray(),
                        Language.Arabic,
                    ),
                onboarded = prefs.getBoolean("onboarded", false),
                product = ProductCodec.load(prefs),
            )
        )
    override val state: StateFlow<LearningState> = _state.asStateFlow()

    // Additive account snapshots. Legacy keys remain available and are never deleted.
    // Historic markers are retained; they do not authorize new imports while D03 is open.
    private val legacyOwner = prefs.getString("personal_legacy_owner", null)
    // A single known pre-upgrade owner retains its existing submission/classroom ID.
    // This alias is scoped to that owner and is never inferred for subsequent accounts.
    private val legacyRecordId = prefs.getString("personal_legacy_record_id", null)
    // Retain an already recorded compatibility claim. Never infer one from a name or email prefix.
    private val recordOwners = legacyRecordId?.takeIf { it.isNotBlank() }
        ?.let { id -> legacyOwner?.let { mapOf(id to it) } }.orEmpty()
    private fun personalPrefs(user: User?) = user?.takeIf {
        it.type == AccountType.Guest || it.email.isNotBlank()
    }?.let {
        val owner = if (it.type == AccountType.Guest) "guest:$guestId" else LearningRules.activeStudentId(LearningState(user = it))
        val key = java.security.MessageDigest.getInstance("SHA-256")
            .digest(owner.toByteArray(Charsets.UTF_8)).joinToString("") { byte -> "%02x".format(byte) }
        preferences("enmath_personal_$key")
    }

    private fun studentRecords(): Map<String, StudentLearningRecord> =
        prefs.getStringSet("student_snapshot_files", emptySet()).orEmpty().mapNotNull { file ->
            val stored = preferences(file)
            val id = stored.getString("record_owner", null) ?: return@mapNotNull null
            id to StudentLearningRecord(stored.getStringSet("completed", emptySet()).orEmpty().toSet(), ProductCodec.load(stored))
        }.toMap()

    private fun shared(personal: ProductState, global: ProductState) = personal.copy(
        classes = global.classes, assignments = global.assignments, submissions = global.submissions,
        resources = global.resources, questions = global.questions, notifications = global.notifications,
    )

    init {
        val initial = _state.value
        // Unknown legacy ownership is retained in place for approved recovery, never inferred.
        if (initial.user?.type != AccountType.Teacher) {
            _state.value = initial.copy(product = shared(ProductState(), initial.product), completed = emptySet(), lastLessonId = "limits-practice")
        }
        personalPrefs(initial.user)?.takeIf { it.contains("product_v1") }?.let { scoped ->
            _state.value = _state.value.copy(
                product = shared(ProductCodec.load(scoped), initial.product),
                studentRecordId = scoped.getString("student_record_id", null),
                completed = scoped.getStringSet("completed", null)?.toSet() ?: emptySet(),
                lastLessonId = scoped.getString("last", "limits-practice").orEmpty(),
            )
        }
        if (_state.value.studentRecordId == null && initial.user?.type == AccountType.Student &&
            LearningRules.activeStudentId(initial) == legacyOwner) {
            _state.value = _state.value.copy(studentRecordId = legacyRecordId)
        }
        _state.value = _state.value.copy(studentRecords = studentRecords(), studentRecordOwners = recordOwners)
    }

    private fun update(transform: (LearningState) -> LearningState) {
        val state = transform(_state.value)
        val scoped = personalPrefs(state.user)
        // Check both stores before the first write; a corrupt global store must not partially
        // publish a successful personal update. Cross-store crash atomicity is DATA-01 work.
        ProductCodec.load(prefs)
        scoped?.let { ProductCodec.load(it) }
        if (scoped != null) {
            ProductCodec.save(scoped, state.product)
            if (!scoped.edit().putStringSet("completed", state.completed).putString("last", state.lastLessonId).putString("student_record_id", state.studentRecordId).commit()) throw LocalDataWriteFailed()
            if (state.user?.type == AccountType.Student) {
                val id = state.activeStudentId
                if (!scoped.edit().putString("record_owner", id).commit()) throw LocalDataWriteFailed()
                val owner = LearningRules.activeStudentId(LearningState(user = state.user))
                val hash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(owner.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
                if (!prefs.edit().putStringSet("student_snapshot_files",
                    prefs.getStringSet("student_snapshot_files", emptySet()).orEmpty() + "enmath_personal_$hash").commit()) throw LocalDataWriteFailed()
            }
            // Shared classroom records retain their existing storage and IDs.
            ProductCodec.save(prefs, shared(ProductCodec.load(prefs), state.product))
        } else if (state.user?.type == AccountType.Teacher) {
            ProductCodec.save(prefs, state.product)
        }
        val sessionSaved = prefs.edit().apply {
            putString("name", state.user?.name)
            putString("email", state.user?.email)
            putString("role", state.user?.type?.name)
            putString("theme", state.theme.name)
            putString("language", state.language.name)
            putBoolean("onboarded", state.onboarded)
        }.commit()
        if (!sessionSaved) throw LocalDataWriteFailed()
        _state.value = state.copy(studentRecords = studentRecords())
    }

    override fun signIn(user: User) {
        check(allowDemoAccounts || user.type == AccountType.Guest) { "Demo accounts are unavailable in production; Guest remains local" }
        // Flush the departing account before switching; never copy its personal state.
        update { it }
        val current = _state.value
        val global = ProductCodec.load(prefs)
        val scoped = personalPrefs(user)
        val id = LearningRules.activeStudentId(current.copy(user = user, studentRecordId = null))
        // D03 is open: even a historic inferred owner marker is not approval to import.
        val personal = if (scoped?.contains("product_v1") == true) ProductCodec.load(scoped) else ProductState()
        val completed = when {
            scoped?.contains("completed") == true -> scoped.getStringSet("completed", null).orEmpty().toSet()
            else -> emptySet()
        }
        update { current.copy(user = user, studentRecordId = scoped?.getString("student_record_id", null) ?: if (id == legacyOwner) legacyRecordId else null, product = shared(personal, global), completed = completed,
            lastLessonId = scoped?.getString("last", "limits-practice") ?: "limits-practice") }
    }

    override fun logout() {
        update { it }
        update { it.copy(user = null, studentRecordId = null, completed = emptySet(), product = ProductState()) }
    }

    override fun finishOnboarding() = update { it.copy(onboarded = true) }

    override fun completeLesson(id: String) {
        if (DemoContent.lesson(id) != null)
            update { it.copy(completed = it.completed + id, lastLessonId = id) }
    }

    override fun openLesson(id: String) {
        if (DemoContent.lesson(id) != null) update { it.copy(lastLessonId = id) }
    }

    override fun setTheme(mode: ThemeMode) = update { it.copy(theme = mode) }

    override fun setLanguage(language: Language) = update { it.copy(language = language) }

    override fun changeProduct(transform: (ProductState) -> ProductState) = update {
        it.copy(product = transform(it.product))
    }
}
