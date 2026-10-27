package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

data class LocalLearner(val id: String, val name: String, val stage: StudyStage,
    val guest: Boolean, val legacyWalletKey: String? = null, val legacySource: String? = null) {
    fun user() = User(name, "", if (guest) AccountType.Guest else AccountType.Student,
        id, stage, legacyWalletKey)
}
data class LearnerSnapshot(val completed: Set<String> = emptySet(), val last: String = "limits-practice",
    val product: ProductState = ProductState())
data class LegacyCandidate(val source: String, val canConfirm: Boolean, val reason: String)

/** Additive, local-only registry. No automatic legacy claim, contact lookup or SQLite rekeying. */
internal class LocalLearnerStore(private val preferences: (String) -> SharedPreferences) {
    private val registry = preferences("enmath_local_identities_v1")
    private var poisoned = false
    private fun array(o: JSONObject, key: String) = o.getJSONArray(key).let { a ->
        (0 until a.length()).map { a.getJSONObject(it) }
    }
    private fun emptyRegistry() = JSONObject().put("version", 1).put("profiles", JSONArray())
        .put("journals", JSONArray()).put("active", "")
    private fun read(): JSONObject {
        if (poisoned) throw LocalDataWriteFailed()
        val raw = registry.getString("registry", null) ?: return emptyRegistry()
        return try {
            JSONObject(raw).also { o ->
                require(o.getInt("version") == 1)
                if (o.has("guestNotices")) {
                    val notices = o.getJSONObject("guestNotices")
                    StudyStage.entries.forEach { stage -> if (notices.has(stage.name)) require(notices.getInt(stage.name) >= 1) }
                }
                val profiles = array(o, "profiles").map(::profile)
                require(profiles.map { it.id }.distinct().size == profiles.size)
                val wallets = profiles.filterNot { it.guest }.map { it.legacyWalletKey ?: it.id }
                require(wallets.distinct().size == wallets.size)
                require(profiles.filter { it.guest }.map { it.stage }.distinct().size == profiles.count { it.guest })
                require(o.getString("active").isEmpty() || profiles.any { it.id == o.getString("active") })
                array(o, "journals").forEach { j ->
                    profile(j.getJSONObject("profile")); j.getString("source"); j.getString("hash")
                    require(j.getString("status") in setOf("PREPARED", "DONE", "SUSPENDED"))
                }
            }
        } catch (e: Exception) { throw CorruptLocalData(e) }
    }
    private fun commit(prefs: SharedPreferences, key: String, raw: String) {
        if (poisoned) throw LocalDataWriteFailed()
        if (!prefs.edit().putString(key, raw).commit()) {
            poisoned = true
            throw LocalDataWriteFailed()
        }
    }
    private fun write(o: JSONObject) = commit(registry, "registry", o.toString())
    private fun profile(o: JSONObject): LocalLearner {
        val id = o.getString("id"); UUID.fromString(id)
        val guest = o.getBoolean("guest")
        val wallet = o.optString("wallet").takeIf { it.isNotEmpty() }
        require(!guest || wallet == null)
        return LocalLearner(id, o.getString("name"), StudyStage.valueOf(o.getString("stage")), guest,
            wallet, o.optString("source").takeIf { it.isNotEmpty() })
    }
    private fun json(p: LocalLearner) = JSONObject().put("id", p.id).put("name", p.name)
        .put("stage", p.stage.name).put("guest", p.guest).put("wallet", p.legacyWalletKey.orEmpty())
        .put("source", p.legacySource.orEmpty())
    fun guestNotices(): Set<StudyStage> {
        val a = read().optJSONObject("guestNotices") ?: return emptySet()
        return StudyStage.entries.filter { a.optInt(it.name, 0) >= 1 }.toSet()
    }
    fun acknowledgeGuestNotice(stage: StudyStage) {
        val o = read(); val a = o.optJSONObject("guestNotices") ?: JSONObject()
        a.put(stage.name, 1); write(o.put("guestNotices", a))
    }
    fun profiles(): List<LocalLearner> = array(read(), "profiles").map(::profile)
    fun active(): LocalLearner? = read().getString("active").let { id -> profiles().find { it.id == id } }
    fun select(id: String?) {
        val o = read()
        require(id == null || array(o, "profiles").any { it.getString("id") == id })
        if (id != null) load(id)
        write(o.put("active", id.orEmpty()))
    }
    fun create(name: String, stage: StudyStage, guest: Boolean): LocalLearner {
        val o = read()
        if (guest) array(o, "profiles").map(::profile).find { it.guest && it.stage == stage }?.let {
            select(it.id); return it
        }
        val p = LocalLearner(UUID.randomUUID().toString(), name.trim().ifBlank { if (guest) "Guest" else "Local student" }, stage, guest)
        save(p.id, LearnerSnapshot(last = if (stage == StudyStage.BAC) "limits-practice" else ""))
        o.getJSONArray("profiles").put(json(p)); write(o.put("active", p.id))
        return p
    }
    private fun encode(id: String, s: LearnerSnapshot) = JSONObject().put("version", 1).put("owner", id)
        .put("completed", JSONArray(s.completed.toList())).put("last", s.last)
        .put("product", JSONObject(ProductCodec.encode(s.product))).toString()
    fun load(id: String): LearnerSnapshot {
        UUID.fromString(id)
        val raw = preferences("enmath_learner_$id").getString("snapshot", null)
            ?: throw CorruptLocalData(IllegalStateException("Missing learner snapshot"))
        return try {
            val o = JSONObject(raw); require(o.getInt("version") == 1)
            require(o.getString("owner") == id)
            val completed = o.getJSONArray("completed")
            val product = ProductCodec.decode(o.getJSONObject("product").toString())
            require(product.pendingGemEvents.all { it.owner == id && it.id in product.learningCommands &&
                it.kind in setOf("LESSON", "PRACTICE", "ASSIGNMENT") })
            require(product.pendingGemEvents.map { it.id }.distinct().size == product.pendingGemEvents.size)
            LearnerSnapshot((0 until completed.length()).map { completed.getString(it) }.toSet(),
                o.getString("last"), product)
        } catch (e: Exception) { throw CorruptLocalData(e) }
    }
    fun save(id: String, snapshot: LearnerSnapshot) {
        UUID.fromString(id)
        val prefs = preferences("enmath_learner_$id")
        if (prefs.contains("snapshot")) load(id) // never overwrite a corrupt/newer snapshot
        commit(prefs, "snapshot", encode(id, snapshot))
    }
    private fun digest(value: String) = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    private fun fingerprint(source: String): String {
        val prefs = preferences(source)
        return digest(JSONObject().put("product", prefs.getString("product_v1", null))
            .put("owner", prefs.getString("record_owner", null))
            .put("completed", JSONArray(prefs.getStringSet("completed", emptySet()).orEmpty().sorted()))
            .put("last", prefs.getString("last", null)).toString())
    }
    /** Existing scoped record + matching storage key is evidence; names/global markers are not. */
    private fun confirmedWallet(source: String): String? {
        if (source !in preferences("enmath_demo").getStringSet("student_snapshot_files", emptySet()).orEmpty()) return null
        val owner = preferences(source).getString("record_owner", null) ?: return null
        if (!owner.startsWith("student:")) return null
        val email = owner.removePrefix("student:")
        if (email.isBlank() || email != email.trim().lowercase(java.util.Locale.ROOT)) return null
        if (source != "enmath_personal_${digest(owner)}") return null
        return email
    }
    fun legacyCandidates(): List<LegacyCandidate> {
        val global = preferences("enmath_demo")
        val sources = global.getStringSet("student_snapshot_files", emptySet()).orEmpty() +
            setOf("enmath_demo").filter { global.contains("product_v1") || global.contains("completed") } +
            listOfNotNull(global.getString("w1_guest_uuid", null)?.let { "enmath_personal_${digest("guest:$it")}" })
                .filter { preferences(it).contains("product_v1") }
        val claimed = profiles().mapNotNull { it.legacySource }.toSet()
        val suspended = array(read(), "journals").filter { it.getString("status") == "SUSPENDED" }
            .map { it.getString("source") }.toSet()
        return (sources - claimed).sorted().map { source ->
            val valid = source !in suspended && runCatching { ProductCodec.load(preferences(source)); confirmedWallet(source) != null }.getOrDefault(false)
            LegacyCandidate(source, valid, if (valid) "CONFIRMED_LOCAL_SCOPE_STAGE_REQUIRED" else "OWNERSHIP_UNRESOLVED_PRESERVED")
        }
    }
    fun migrate(source: String, stage: StudyStage, name: String): LocalLearner {
        resumeMigrations()
        profiles().find { it.legacySource == source }?.let { require(it.stage == stage); select(it.id); return it }
        val wallet = requireNotNull(confirmedWallet(source)) { "Ownership unresolved; source preserved" }
        require(profiles().none { !it.guest && (it.legacyWalletKey ?: it.id) == wallet })
        val o = read()
        require(array(o, "journals").none { it.getString("source") == source })
        ProductCodec.load(preferences(source))
        val p = LocalLearner(UUID.randomUUID().toString(), name.trim().ifBlank { "Imported local student" }, stage, false, wallet, source)
        o.getJSONArray("journals").put(JSONObject().put("source", source).put("hash", fingerprint(source))
            .put("profile", json(p)).put("status", "PREPARED"))
        write(o)
        resumeMigrations()
        return profiles().first { it.id == p.id }.also { select(it.id) }
    }
    fun resumeMigrations() {
        val o = read()
        for (j in array(o, "journals")) {
            if (j.getString("status") != "PREPARED") continue
            val source = j.getString("source")
            if (fingerprint(source) != j.getString("hash") || confirmedWallet(source) == null) {
                j.put("status", "SUSPENDED"); write(o); continue
            }
            val p = profile(j.getJSONObject("profile")); val sourcePrefs = preferences(source)
            val old = ProductCodec.load(sourcePrefs)
            // Shared classroom rows have no new UUID provenance. Retain them only in the source.
            val personal = old.copy(classes = emptyList(), assignments = emptyList(), submissions = emptyList(),
                resources = emptyList(), questions = emptyList(), notifications = emptyList(),
                pendingGemEvents = emptyList())
            val snapshot = LearnerSnapshot(sourcePrefs.getStringSet("completed", emptySet()).orEmpty().toSet(),
                sourcePrefs.getString("last", "limits-practice").orEmpty(), personal)
            val target = preferences("enmath_learner_${p.id}")
            if (target.contains("snapshot")) require(load(p.id) == snapshot) { "Migration target differs; preserved for review" }
            else save(p.id, snapshot)
            o.getJSONArray("profiles").put(json(p)); j.put("status", "DONE"); write(o)
        }
    }
}
