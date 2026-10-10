package com.ahmadmol.enmath.features.teacher

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*

internal fun classAverage(state: LearningState, c: Classroom) =
    if (c.studentIds.isEmpty()) 0
    else c.studentIds.map { LearningRules.topic(state, c.topicId, it).score }.average().toInt()
