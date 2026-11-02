package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.Copy

/** Explanations for the existing demonstration questions, not official curriculum coverage. */
internal object ExerciseExplanations {
    private val primary = mapOf(
        "limits" to Copy("Substitute x=3: 3+2=5.", "بالتعويض x=3: 3+2=5."),
        "derivatives" to Copy("d(x²)/dx=2x. At x=3: 2×3=6.", "d(x²)/dx=2x؛ عند x=3: 2×3=6."),
        "integrals" to Copy("An antiderivative is x². From 0 to 1: 1²−0²=1.", "دالة أصلية هي x²؛ من 0 إلى 1: 1²−0²=1."),
        "matrices" to Copy("The determinant is ad−bc: 1×1−0×0=1.", "المحدد ad−bc: 1×1−0×0=1."),
        "probability" to Copy("One of two equally likely coin outcomes: probability 1/2.", "نتيجة واحدة من نتيجتين متساويتي الاحتمال: الاحتمال 1/2."),
        "geometry" to Copy("Cube volume is a³. For a=2: 2³=8 cubic units.", "حجم المكعب a³؛ عندما a=2: 2³=8 وحدات مكعبة."),
        "logarithms" to Copy("Since e⁰=1, ln(1)=0.", "بما أن e⁰=1، فإن ln(1)=0."),
        "sequences" to Copy("Substitute n=2: 2×2+1=5.", "بالتعويض n=2: 2×2+1=5.")
    )
    private val secondary = mapOf(
        "limits" to Copy("Substitute x=2: 2²=4.", "بالتعويض x=2: 2²=4."),
        "derivatives" to Copy("d(x³)/dx=3x². At x=2: 3×4=12.", "d(x³)/dx=3x²؛ عند x=2: 3×4=12."),
        "integrals" to Copy("An antiderivative is x³. From 0 to 1: 1³−0³=1.", "دالة أصلية هي x³؛ من 0 إلى 1: 1³−0³=1."),
        "matrices" to Copy("The determinant is 1×3−2×0=3.", "المحدد 1×3−2×0=3."),
        "probability" to Copy("One of six equally likely die outcomes: probability 1/6.", "نتيجة واحدة من ست نتائج متساوية الاحتمال: الاحتمال 1/6."),
        "geometry" to Copy("Distance is √(1²+2²+2²)=√9=3.", "المسافة √(1²+2²+2²)=√9=3."),
        "logarithms" to Copy("Since e¹=e, ln(e)=1.", "بما أن e¹=e، فإن ln(e)=1."),
        "sequences" to Copy("Substitute n=3: 2×3+1=7.", "بالتعويض n=3: 2×3+1=7.")
    )
    fun forQuestion(topic: String, index: Int, fallback: Copy): Copy {
        val base=primary[topic] ?: return fallback
        if(index==6) return secondary[topic] ?: fallback
        if(index==4) return when(topic) {
            "derivatives" -> Copy("The power rule gives d(x²)/dx=2x.", "تعطي قاعدة القوة d(x²)/dx=2x.")
            "integrals" -> Copy("∫2x dx=x²+C. Include the arbitrary integration constant.", "∫2x dx=x²+C؛ نضيف ثابت التكامل الاختياري.")
            "sequences" -> Copy("[2(n+1)+1]−(2n+1)=2.", "[2(n+1)+1]−(2n+1)=2.")
            else -> base
        }
        return if(index==5) Copy("The proposed value 42 is incorrect. "+base.en, "القيمة المقترحة 42 غير صحيحة. "+base.ar) else base
    }
}
