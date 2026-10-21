package com.ahmadmol.enmath.core.model

/** Local preview switch only, never a production authorization mechanism. */
object DemoRolePolicy {
    fun target(user: User?, debuggable: Boolean): AccountType? {
        if (!debuggable || user == null || user.type == AccountType.Guest || user.email.isBlank()) return null
        return if (user.type == AccountType.Teacher) AccountType.Student else AccountType.Teacher
    }
}
