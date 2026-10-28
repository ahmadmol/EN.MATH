# EN.MATH — BAC Source Index Corrections (Verified Errata V1.1)

Date: 2026-10-27

## Purpose
Source-only correction of the BAC curriculum index generated from user-supplied PDFs. It does **not** modify EN.MATH application code or the user's local worktree and does **not** approve any curriculum edition for 2026–2027.

## Verified original PDFs
- Part I: `Sci-Math-1.pdf` (253 PDF pages, cover 2023–2024). SHA-256: `68ac74c38efc97abe037df0cf185038fba28162de1f30c94ba12a61844b8867e`.
- Part II: `زميلك - الرياضيات الجزء الثاني.pdf` (217 PDF pages, cover 2021–2022). SHA-256: `c43f76f7b8e9cda0a6d615b84dc40500825edc96dfbfb65cb845a037edb3745a`.
Both values were recalculated from the PDFs and matched the original catalog. No textbook PDF is included in this ZIP.

## Corrections grounded in Part I
1. Unit `bac-1-u07` (التكامل والتوابع الأصلية): printed pages **217–250**, corresponding PDF pages **218–251**. Previous erroneous range 217–216 (PDF 218–217). The unit title page is PDF 218; the last unit exercise page is PDF 251; scientific glossary begins PDF 252 (printed 251).
2. Exercises `bac-1-u07-exercise`: printed **244–250**, PDF **245–251**, rather than printed 244–216 and PDF 245–217. Activity pages printed 242–243 / PDF 243–244 were already correct and unchanged.
3. In unit `bac-1-u05`, lesson 4 `bac-1-u05-l04` (مشتق التابع المركب ln∘u) starts and ends on **printed page 163** / PDF page **164**; lesson 5 `bac-1-u05-l05` (نهايات مهمة...) **also starts partway through that same printed page**, continuing in the following pages. This is a *shared start page*, not a reason to merge their IDs. Lesson 5's original end remains pending any more detailed review; its shared-start metadata is retained.
4. Inventory: the **34 PDF pages 218–251** that were incorrectly classed as `FRONT_MATTER` are now linked to unit 7 using TOC-based section categories. `manual_page_content_reviewed` remains `False` for every row; this fix is not a page-by-page pedagogical review.

## Files updated
- `BAC_CURRICULUM_CATALOG.json`
- `BAC_UNITS.csv`
- `BAC_LESSONS.csv`
- `BAC_ACTIVITY_EXERCISE_SECTIONS.csv`
- `BAC_PAGE_INVENTORY.csv`
- `BAC_SOURCE_ANALYSIS.md` (added correction notice)

Remaining files from the original package are included unchanged. Lesson titles, IDs, edition labels, source PDF hashes, and rewards/progress semantics are unchanged. Source content is still indexed only / blocked for edition and educational review. The previous importer may need to regenerate its coverage tables and review queue from these corrected source files; don't alter checked-in or historic identity/progress records.

## Integration guardrails for Codex
- Import corrections into the **isolated BAC integration worktree**, not the old 8ba76a Git tree. Back up local uncommitted BAC changes first.
- Diff source inputs, verify original source SHA and IDs, regenerate derived output, and run importer `--check` plus all regression tests.
- Preserve the existing 24-demo-lesson progress and all G9/teacher data. G9 integration resides in a different worktree and must be reconciled separately against the current clean HEAD.
- No commit/push, emulator, ADB, clear data or APK installation without further instruction.
