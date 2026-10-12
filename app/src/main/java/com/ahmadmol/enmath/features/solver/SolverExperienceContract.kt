package com.ahmadmol.enmath.features.solver

/**
 * UI intent boundary: QuickSolve uses existing routes/engine. LearnSolution is reserved, not
 * advertised as an implemented guided engine. Future step UI may consume the same Solution.
 */
enum class SolverExperienceMode {
    QuickSolve,
    LearnSolution,
}

data class SolverExperienceRequest(val expression: String, val mode: SolverExperienceMode)
