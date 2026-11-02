package com.ahmadmol.enmath.core.localgems

import com.ahmadmol.enmath.core.model.AccountType
import com.ahmadmol.enmath.core.model.User
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.Locale

class InsufficientLocalGems : IllegalStateException("INSUFFICIENT_GEMS")

enum class GemReward(val units: Int) { LESSON(10), MASTERY(20), PRACTICE(5), ASSIGNMENT(5), WEEKLY(15) }
data class GemRewardEntry(val key: String, val kind: GemReward, val units: Int, val at: Long, val day: String, val week: String)
data class GemUnlock(val itemId: String, val price: Int, val at: Long)
data class GemAssessment(val id: String, val topic: String, val quiz: Boolean, val accuracy: Float)
data class GemAccount(
    val owner: String, val activatedAt: Long, val balance: Int = 0,
    val excluded: Set<String> = emptySet(), val rewards: List<GemRewardEntry> = emptyList(),
    val unlocks: List<GemUnlock> = emptyList(), val lessons: Set<String> = emptySet(),
    val assessments: List<GemAssessment> = emptyList(),
)

/** Local pilot policy only. No money, networking or changes to learning scores. */
object LocalGemsPolicy {
    const val DAILY_CAP = 40
    const val WEEKLY_CAP = 120
    private fun calendar(now: Long) = GregorianCalendar(TimeZone.getTimeZone("Asia/Damascus"), Locale.ROOT).apply { timeInMillis=now }
    private fun date(calendar: Calendar): String = String.format(Locale.ROOT, "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH))
    fun owner(user: User?): String? = user?.takeIf { it.type == AccountType.Student }
        ?.let { it.legacyWalletKey ?: it.localId ?: it.email.trim().lowercase(Locale.ROOT).takeIf(String::isNotEmpty) }
    fun attributable(user: User?, startedBy: String?): Boolean = startedBy != null && owner(user) == startedBy
    fun day(now: Long): String = date(calendar(now))
    fun week(now: Long): String = date(calendar(now).apply {
        add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK)+5)%7))
    })
    fun qualified(required: List<String>, answers: Map<String, String>, correct: Int): Boolean =
        required.isNotEmpty() && required.distinct().size == required.size &&
            required.all { !answers[it].isNullOrBlank() } && correct in 0..required.size &&
            correct * 100L >= required.size * 80L
    fun award(account: GemAccount, kind: GemReward, id: String, at: Long): GemAccount {
        // D06: retain these historical entries, but never mint new weekly/mastery grants.
        if (kind == GemReward.WEEKLY || kind == GemReward.MASTERY) return account
        val key = "${kind.name}:$id"
        if (at < account.activatedAt || key in account.excluded || account.rewards.any { it.key == key }) return account
        val day = day(at); val week = week(at)
        val units = minOf(kind.units,
            (DAILY_CAP - account.rewards.filter { it.day == day }.sumOf { it.units }).coerceAtLeast(0),
            (WEEKLY_CAP - account.rewards.filter { it.week == week }.sumOf { it.units }).coerceAtLeast(0))
        // A capped achievement is recorded too; replaying tomorrow cannot mint its remainder.
        return account.copy(balance = account.balance + units,
            rewards = account.rewards + GemRewardEntry(key, kind, units, at, day, week))
    }
    fun unlock(account: GemAccount, item: LocalGemItem, at: Long): GemAccount {
        if (account.unlocks.any { it.itemId == item.id }) return account
        if (account.balance < item.price) throw InsufficientLocalGems()
        return account.copy(balance = account.balance - item.price,
            unlocks = account.unlocks + GemUnlock(item.id, item.price, at))
    }
    fun validate(account: GemAccount) {
        check(account.balance >= 0 && account.balance == account.rewards.sumOf { it.units } - account.unlocks.sumOf { it.price })
        check(account.rewards.map { it.key }.distinct().size == account.rewards.size)
        check(account.unlocks.map { it.itemId }.distinct().size == account.unlocks.size)
        check(account.rewards.all { it.units in 0..it.kind.units })
        check(account.rewards.groupBy { it.day }.values.all { list -> list.sumOf { it.units } <= DAILY_CAP })
        check(account.rewards.groupBy { it.week }.values.all { list -> list.sumOf { it.units } <= WEEKLY_CAP })
    }
}

interface LocalGemsStorage {
    fun activate(owner: String, excluded: Set<String>, at: Long): GemAccount
    fun change(owner: String, transform: (GemAccount) -> GemAccount): GemAccount
    fun read(owner: String): GemAccount
}
