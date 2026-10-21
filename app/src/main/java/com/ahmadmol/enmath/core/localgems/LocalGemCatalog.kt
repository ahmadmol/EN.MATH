package com.ahmadmol.enmath.core.localgems

import com.ahmadmol.enmath.core.model.Copy

data class LocalGemItem(val id: String, val title: Copy, val summary: Copy, val price: Int, val asset: String)
object LocalGemCatalog {
    val items = listOf(
        LocalGemItem("limits-guide-v1", Copy("A guide to indeterminate limits", "دليل معالجة عدم التعيين"),
            Copy("An extra study sheet with worked examples and checks.", "ملف مساعد إضافي بأمثلة محلولة وخطوات تحقق."), 40, "limits"),
        LocalGemItem("enrichment-review-v1", Copy("A short enrichment review", "مراجعة إثرائية قصيرة"),
            Copy("A compact course with extra limit, derivative and integral examples.", "كورس قصير بأمثلة إضافية في النهايات والاشتقاق والتكامل."), 120, "review"),
    )
    fun item(id: String) = items.find { it.id == id }
}
