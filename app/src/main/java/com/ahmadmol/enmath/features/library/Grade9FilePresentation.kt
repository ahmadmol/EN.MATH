package com.ahmadmol.enmath.features.library

import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.data.StudentFile
import java.io.FileNotFoundException
import java.io.IOException

enum class G9FileFilter { ALL, PDF, WORD }
enum class G9FileFailure { PERMISSION, MISSING, NO_READER, PROVIDER, INVALID, UNKNOWN }

fun grade9Files(files: List<StudentFile>, query: String, filter: G9FileFilter): List<StudentFile> {
    val terms = LearningRules.search(query).split(Regex("\\s+")).filter(String::isNotBlank)
    return files.filter { file ->
        (filter == G9FileFilter.ALL || when (filter) {
            G9FileFilter.PDF -> file.mime == "application/pdf"
            G9FileFilter.WORD -> file.mime in setOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            else -> true
        }) &&
            terms.all { LearningRules.search(file.name).contains(it) }
    }.sortedWith(compareBy<StudentFile> { LearningRules.search(it.name) }.thenBy { it.id })
}

fun grade9FileFailure(error: Throwable): G9FileFailure = when (error) {
    is SecurityException -> G9FileFailure.PERMISSION
    is FileNotFoundException -> G9FileFailure.MISSING
    is IOException -> G9FileFailure.PROVIDER
    is IllegalArgumentException -> G9FileFailure.INVALID
    else -> if (error.javaClass.simpleName == "ActivityNotFoundException") G9FileFailure.NO_READER else G9FileFailure.UNKNOWN
}

fun grade9FileSize(bytes: Long?): String = when {
    bytes == null -> "—"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(java.util.Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024))
}
