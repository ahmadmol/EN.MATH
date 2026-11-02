package com.ahmadmol.enmath

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import java.lang.reflect.Proxy
import java.nio.file.Files
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Isolated host file fixture; never opens Android app preferences or user databases. */
internal class FixturePreferences(val file: java.io.File = Files.createTempFile("w1-", ".fixture").toFile()) {
    val values = mutableMapOf<String, Any>()
    var fail = false
    var commitCount = 0
    var failCommitNumber: Int? = null
    var afterCommit: (() -> Unit)? = null
    init {
        file.deleteOnExit()
        if (file.length() > 0) java.io.ObjectInputStream(file.inputStream()).use {
            @Suppress("UNCHECKED_CAST")
            values.putAll(it.readObject() as Map<String, Any>)
        }
    }
    val prefs: SharedPreferences = Proxy.newProxyInstance(SharedPreferences::class.java.classLoader,
        arrayOf(SharedPreferences::class.java)) { _, method, args ->
        when (method.name) {
            "contains" -> values.containsKey(args[0])
            "getString", "getStringSet", "getBoolean", "getInt", "getLong", "getFloat" -> values[args[0]] ?: args[1]
            "getAll" -> values.toMap()
            "edit" -> editor()
            else -> error("Unsupported fixture method: ${method.name}")
        }
    } as SharedPreferences
    private fun editor(): SharedPreferences.Editor {
        val pending = mutableMapOf<String, Any?>()
        return Proxy.newProxyInstance(SharedPreferences.Editor::class.java.classLoader,
            arrayOf(SharedPreferences.Editor::class.java)) { proxy, method, args ->
            when {
                method.name.startsWith("put") -> { pending[args[0] as String] = args[1]; proxy }
                method.name == "commit" || method.name == "apply" -> {
                    commitCount++
                    val accepted = !fail && commitCount != failCommitNumber
                    if (accepted) {
                        pending.forEach { (k, v) -> if (v == null) values.remove(k) else values[k] = v }
                        java.io.ObjectOutputStream(file.outputStream()).use { it.writeObject(HashMap(values)) }
                        afterCommit?.invoke()
                    }
                    if (method.name == "commit") accepted else null
                }
                else -> error("Unsupported fixture editor: ${method.name}")
            }
        } as SharedPreferences.Editor
    }
}

class W1ProductRecoveryTest {
    @Test fun absentAndValidStoresRemainDistinctAndReloadFromDisk() {
        val f = FixturePreferences()
        assertEquals(ProductSeed.seed().classes, ProductCodec.load(f.prefs).classes)
        assertFalse(f.prefs.contains("product_v1"))
        val p = ProductState(savedLessons = setOf("limits-practice"), searches = listOf("fixture"))
        ProductCodec.save(f.prefs, p)
        assertEquals(p, ProductCodec.load(FixturePreferences(f.file).prefs))
    }
    @Test fun corruptTruncatedAndNewerDataCannotBeOverwritten() {
        for (raw in listOf("{", "null", "{}", "{\"version\":99}", "{\"version\":1,\"attempts\":[{}]}")) {
            val f = FixturePreferences()
            f.prefs.edit().putString("product_v1", raw).commit()
            assertThrows(CorruptLocalData::class.java) { ProductCodec.load(f.prefs) }
            assertThrows(CorruptLocalData::class.java) { ProductCodec.save(f.prefs, ProductState()) }
            assertEquals(raw, FixturePreferences(f.file).prefs.getString("product_v1", null))
        }
    }
    @Test fun partialArraysAndWrongArrayTypesAreRejectedWithoutLosingOriginal() {
        for (replacement in listOf("wrong type", org.json.JSONArray().put(JSONObject()))) {
            val f = FixturePreferences()
            ProductCodec.save(f.prefs, ProductState())
            val raw = JSONObject(f.prefs.getString("product_v1", null)!!).put("attempts", replacement).toString()
            f.prefs.edit().putString("product_v1", raw).commit()
            assertThrows(CorruptLocalData::class.java) { ProductCodec.save(f.prefs, ProductState()) }
            assertEquals(raw, f.prefs.getString("product_v1", null))
        }
    }
    @Test fun firstRecoverySnapshotIsPreservedAcrossSubsequentSavesAndRestart() {
        val f = FixturePreferences()
        val original = ProductState(searches = listOf("original"))
        ProductCodec.save(f.prefs, original)
        val raw = f.prefs.getString("product_v1", null)
        ProductCodec.save(f.prefs, original.copy(searches = listOf("second")))
        ProductCodec.save(f.prefs, original.copy(searches = listOf("third")))
        val restored = FixturePreferences(f.file)
        assertEquals(raw, restored.prefs.getString("product_v1_recovery", null))
        // Explicit fixture recovery verifies compatibility; production does not silently restore.
        restored.prefs.edit().putString("product_v1", raw).commit()
        assertEquals(original, ProductCodec.load(restored.prefs))
    }
    @Test fun unacknowledgedSaveCannotReturnSuccessAndDiskOriginalSurvives() {
        val f = FixturePreferences()
        ProductCodec.save(f.prefs, ProductState(searches = listOf("before")))
        val raw = f.prefs.getString("product_v1", null)
        f.fail = true
        assertThrows(LocalDataWriteFailed::class.java) { ProductCodec.save(f.prefs, ProductState()) }
        assertEquals(raw, FixturePreferences(f.file).prefs.getString("product_v1", null))
    }
    @Test fun corruptionAfterOpenBlocksRepositoryBeforeAnyPersonalWrite() {
        val stores = mutableMapOf<String, FixturePreferences>()
        val repo = LocalDemoRepository { name -> stores.getOrPut(name) { FixturePreferences() }.prefs }
        repo.signIn(User("Fixture", "fixture@example.test", AccountType.Student))
        val before = repo.state.value
        val personal = stores.entries.single { it.key.startsWith("enmath_personal_") && it.value.prefs.contains("product_v1") }.value
        val personalBytes = personal.file.readBytes()
        val global = stores.getValue("enmath_demo")
        global.prefs.edit().putString("product_v1", "{broken").commit()
        assertThrows(CorruptLocalData::class.java) { repo.completeLesson("limits-practice") }
        assertEquals(before, repo.state.value)
        assertArrayEquals(personalBytes, personal.file.readBytes())
        assertEquals("{broken", FixturePreferences(global.file).prefs.getString("product_v1", null))
    }
}
