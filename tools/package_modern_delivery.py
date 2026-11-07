"""Package verified local outputs and write an honest, reproducible campaign handoff."""
import argparse
import hashlib
import json
from pathlib import Path
import shutil
import subprocess

ROOT=Path(__file__).resolve().parents[1]

def git(*args):
    return subprocess.check_output(['git',*args],cwd=ROOT,text=True,encoding='utf-8').strip()

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--delivery',type=Path,required=True)
    parser.add_argument('--qa',type=Path,required=True)
    args=parser.parse_args();out=args.delivery;out.mkdir(parents=True,exist_ok=True)
    evidence=json.loads((out/'evidence/EVIDENCE.json').read_text(encoding='utf-8'))
    render=json.loads((args.qa/'render-status.json').read_text(encoding='utf-8'))
    debug=evidence['tests']['Debug']['totals']['tests']; release=evidence['tests']['Release']['totals']['tests']
    total=debug+release+34
    assert all(evidence['tests'][v]['status']=='PASS' for v in ['Debug','Release'])
    assert all(evidence['lint'][v]['status']=='PASS' for v in ['debug','release'])
    head=git('rev-parse','HEAD');branch=git('branch','--show-current')
    files=[]
    for apk in evidence['apks']:
        name='ENMATH-modern-v7-debug.apk' if apk['variant']=='debug' else 'ENMATH-modern-v7-release-unsigned.apk'
        target=out/name;shutil.copyfile(apk['file'],target)
        digest=hashlib.sha256(target.read_bytes()).hexdigest();assert digest==apk['sha256']
        files.append(dict(name=name,sha256=digest,bytes=target.stat().st_size))
    (out/'SHA256SUMS.txt').write_text(''.join(f"{f['sha256']}  {f['name']}\n" for f in files),encoding='utf-8')
    artifact_table='\n'.join(f"| {f['name']} | {f['bytes']:,} | `{f['sha256']}` |" for f in files)
    warnings={v:evidence['lint'][v]['counts'].get('Warning',0) for v in ['debug','release']}
    reports={}
    reports['FINAL_REPORT.md']=f'''# EN.MATH modern student experience - 2026-11-02

Version **1.1.0 / 7**, package `com.ahmadmol.enmath`. Local engineering delivery is implemented and tested. Production acceptance remains conditional on approved character artwork, independent content review, cloud configuration and actual visual/device verification. These conditions are not reported as completed features.

## Implemented behavior

1. Both stages use Curriculum / Tools / Home / Progress / Profile. Home is a 64dp clipped circle centered exactly on the bottom surface's top edge, with 32dp above it. Labels grow with type; tablets at 768dp and above use a responsive rail.
2. Grade 9 has one lazy journey containing all 6 algebra units / 19 lessons and 4 geometry units / 13 lessons. Fixed S-wave coordinates, stable keys, lesson sheets, book/unit jump sheets and UUID-owned saved viewport anchors replace the former separate subject paths. The weekly plan is last; tools have their own destination.
3. Onboarding and Profile offer three stable concept identities, `muadh`, `waseem`, `lama`, saved per local UUID. The supplied collages are inconsistent preview art. Neutral labeled placeholders are used, with the same identity for all nine pose events. No final portraits or sprite animation are claimed.
4. All **192 complete original draft questions** were imported and structurally checked against 32 explicit lesson mappings. **Six already existed; 186 are newly integrated.** All six interaction types and levels 1/2/3 are retained. Debug alone packages the bank; Release cannot load it.
5. **192/192 Grade 9 questions still need independent mathematical, edition and rights approval. Zero were scientifically approved by this campaign.** The 28 existing BAC original proposals remain authoring-only drafts and were not turned into textbook questions.
6. Streak uses persisted original-pilot/BAC practice evidence, one calendar day per timezone, stable event IDs and replay protection. Source browsing, opening the app, plan edits and internal QA drafts do not earn days. Energy is Unlimited. Existing SQLite Gems rules and ledger are retained; no new reward path or retroactive award was introduced.
7. **No live Firebase function is connected.** No EN.MATH Firebase project configuration, SDK, verified Auth provider, deployed rules or cloud connector was available. UID/local-owner/project/session contracts, immutable-event merging, queue interfaces, retries and conflict handling were tested with fakes. A production durable queue and Firebase adapter are still activation work.
8. Profiles, avatars, saved QA answers/hints/wrong responses, original progress, days, plans and references remain local. The UI does not show cloud login or Connected status.
9. BAC uses the same Home/path, navigation, tools, profile and avatar components with its existing stage palette. Two books / 14 units / 63 researches remain intact. All 63 reference routes still pass the inherited production-graph test; the 24 demo lessons retain separate provenance and progress.
10. Repairs include large-font Arabic/English wrapping, full-height sheet visibility, per-owner viewport isolation, preference-write failure handling, stale-answer validation, duplicate response submissions and multiline worked-step preservation. Wrong QA responses are reachable in a dedicated notebook.
11. Final verification: **{debug} Debug + {release} Release + 34 Python = {total} successful executions**, zero failures/errors/skips. Both lints and APK builds pass. There are 72 distinct host configurations, including the actual responsive rail at 800dp. Android instrumentation compiles; device execution was not run.
12. Raster status: **{render['status']}**. Current app images approved: **0**. Bottom sheets use visible semantics actions in Robolectric because native modal touch dispatch did not navigate reliably; this is not physical gesture verification. Check sheets, focus/ripple/shadows, insets, IME, TalkBack, scrolling performance and rotation on an authorized device later.
13. Existing student IDs, edition scopes, learner snapshots, source catalogs, planner and Gems schemas were not migrated or reset. Isolated persistence/restart/failure/account-switch regressions pass. Physical installed-app upgrade retention was not tested.
14. Debug is signed with the existing local debug key; Release is **unsigned**. No production signing material was supplied. See signature evidence and hashes below.
15. Branch: `{branch}`. Tested implementation commit: `{head}`. Documentation-only final commit is recorded in the delivery's GIT_STATUS.md after packaging. No push or main merge was performed.

| Artifact | Bytes | SHA-256 |
|---|---:|---|
{artifact_table}

## Recovery and evidence

`backups/before-modern.bundle` preserves the clean 1.0.5 baseline at 3f22440. Its app tree matches 54ce759; the later baseline adds publication/security documentation. The bundle was verified, cloned without checkout and fsck-checked successfully. Final source patch/change bundle, raw test XML, lint reports, signature/badging records, input inventory and the question review ledger accompany this delivery.

The user-pasted request controlled execution. Instructions inside the reference pack and historical documents were treated as reference material, not as new commands. No emulator, ADB, phone install, data deletion, security-policy bypass, cloud project creation or production rule deployment occurred.
'''
    reports['DESIGN_SYSTEM_REPORT.md']='''# Shared student design system

Existing Material 3, Indigo/Teal stage schemes, IBM Plex Arabic / Inter, native LTR mathematics and stable Compose BOM are retained. No build-tool or UI dependency version was upgraded.

StudentExperienceTokens centralizes 28dp panels, 18dp compact shapes, 64dp Home circle, 32dp overlap, 48dp touch minimum and 640dp reading width. StudentPanel, StudentAvatar, ModernStudentBottomBar and ModernStudentRail are shared by both stages. Navigation/tools use one Material Rounded family, with directional symbols mirrored explicitly.

Opaque readable surfaces with restrained tonal gradients provide the soft-glass effect without an expensive backdrop blur. Existing Material dialogs, sheets, snackbars and error components are reused. Failed storage is shown as unavailable instead of invented zero progress. Pose identity fallback is neutral and stable.

The journey motion preference suppresses animated jumps; system animation scaling remains respected by Compose transitions. It is not a promise that every legacy animation has a separate new preference. Shared legacy tools inherit the current theme; this campaign does not claim every historical teacher/demo screen was redesigned.

Text bounds and navigation geometry are exercised in 72 host configurations. Pixel contrast, shadow/focus artifacts, screen-reader traversal and actual frame timing still need visual/device review.

Technical references read: https://developer.android.com/develop/ui/compose/designsystems/material3 and https://developer.android.com/develop/ui/compose/components/navigation-bar .
'''
    reports['UI_UX_BEFORE_AFTER.md']='''# Student experience before / after

| Area | 1.0.5 behavior | Current implementation |
|---|---|---|
| Navigation | Separate subject/training tabs; shallow Home overlap | Curriculum, Tools, central circular Home, Progress, Profile; adaptive rail |
| Grade 9 Home | Required subject choice and separate subject journeys | One continuous 32-lesson journey; book/unit sheets jump within it |
| Home density | Tools mixed into the lower Home | Status, greeting, measured learning, selector, journey, weekly plan |
| Books / units | Source metadata and plan actions before lessons | Counts and lessons first; source/plan information secondary |
| Identity | Stage-specific generic student illustrations | UUID-owned companion identity; explicit temporary neutral artwork |
| Exercises | Six-question Nature draft plus original fractions pilot | 192 distinct QA records, six types, persisted answers/hints/wrong responses, teacher-deferred marking |
| Progress | Separate original practice evidence | Retained evidence plus internal-QA history/notebook; no whole-curriculum mastery inference |
| BAC | Its separate previous source path | Same shared modern journey/navigation/profile design; 63 source references retained |

This table describes code and host evidence. No historical screenshot or concept reference is presented as a current app screenshot. Current raster approval is blocked; see QA_RESULTS.md.
'''
    reports['NAVIGATION_REPORT.md']='''# Navigation and journey

Logical tab order is Curriculum, Tools, Home, Progress, Profile; Compose mirrors it in RTL and Home stays central. Phones measure the complete circular hit target. Tablets use the shared rail, whose width grows with font scale. The five tabs retain their saved state in both stages.

Legacy g9/algebra, g9/geometry, g9/exercises, source, lesson, book, topic, solver, planner, wallet and result destinations remain registered. Existing lesson IDs and stable content routes were not rekeyed. Source/book/unit/result tags remain. New root tags are nav-study, nav-tools, nav-home, nav-progress and nav-profile. Former subject-picker/switch controls are intentionally superseded by journey-select-* and journey-jump-* controls; corresponding tests were updated rather than retaining fake hidden buttons.

Each research retains station-<id>, station-label-<id> and home-research-<id> tags. Tapping opens a lesson sheet with actual availability and a real lesson/practice action. All ten G9 units and all 32 stations are in the same lazy list. Unit positions derive from the same catalog and four header items; saved anchors store existing research IDs. No lock or source visit grants mastery.

Host tests traverse all G9 stations, both BAC books / all 14 units / all 63 references, select algebra/geometry units, check reverse-scroll geometry and per-owner restoration. Modal button visibility and semantics are verified; native modal touch dispatch in Robolectric is not accepted as device gesture evidence.
'''
    reports['AVATAR_SYSTEM_REPORT.md']='''# Avatar identity

Three stable original concept IDs are offered without a gender restriction: muadh / معاذ, waseem / وسيم, lama / لمى. `selectedAvatarId` is stored in the additive UUID-owned student-experience document. Existing profiles begin neutral and may choose later. New profile creation offers the picker before Home; name/stage fields and choice state are saveable.

All nine pose intents retain that same ID: idle, welcome, thinking, correct, try again, completed, celebrating, encouraging and streak. Production Home/path, source hero, original lesson/practice/results, progress, profile and internal QA use shared avatar presentation. Old stage art is no longer used as the current student identity in the source hero.

The six supplied 1536x1536 RGB sheets (Adam, Karim, Lina, Noor, Sara, Yousef) are opaque preview collages. They differ from the concept-board identities, contain embedded labels and are not transparent animation frames. They were inspected and kept outside the repository/application. No collage slicing, invented replacement child or false sprite animation was introduced.

Current portraits are explicitly labeled temporary neutral symbols, with distinct initials/tonal treatments. Missing final artwork remains a release-design limitation. Required artist delivery: three consistent individual transparent base portraits plus matching optional pose assets and approved usage rights. Tests cover two students, guest isolation, restart, invalid IDs, failed/corrupt writes, stale generations and all pose-owner fallbacks.
'''
    reports['EXERCISE_IMPORT_REPORT.md']='''# Exercise import

All 42 PDF pages were extracted for inspection and all JSON records were read, including prompts, choices, answers, grading modes, explanations, hints and provenance. The complete import contains 192 unique IDs / 32 lessons / 10 units, 32 records of each interaction type and the level sequence 1,1,2,2,3,3 per lesson.

Thirty-two explicit mapping records bind draft IDs to existing source IDs and unit/book boundaries. Four editorial title aliases were documented and checked. Existing source/progress IDs remain unchanged. Six questions match the already integrated Nature pilot verbatim; they use that existing activity entry. The newly integrated count is 186, not 192 additional questions.

The imported Debug asset preserves every source question field. Runtime loading requires a debuggable application and a Debug-only asset; Release has no bank file. Changing a publication Boolean cannot enable a missing Release asset. Scientific, edition and rights approval remain independent prerequisites, with zero new approvals recorded.

Choice/Boolean and bounded finite numeric rules are deterministic. Reduced-fraction prompts require reduced form. Algebraic free text, proofs, error correction, fill steps and worked reasoning without validated structured rules are saved for teacher judgment. They do not receive fabricated correct marks. Worked problems have three persisted fields; paragraphs survive edits/restart. Retry/hint history, stale-response checks and duplicate submit protection are retained. A QA result separates checked answers, first-try unhinted answers and teacher-pending answers.

QA sessions grant no Gems or streak. Their difficulty order is a preview sequence; a validated adaptive curriculum or spaced-repetition scheduler is not claimed. QUESTION_REVIEW_LEDGER.csv lists all records and review needs. G9-H-U04-L01-Q04 has a coefficient/pi wording ambiguity; G9-A-U06-L04-Q06 needs a consistent quartile convention. Neither was silently edited or approved.
'''
    reports['CONTENT_REVIEW_STATUS.md']='''# Content review status

| Corpus | Availability | Scientific approval from this campaign |
|---|---|---:|
| G9 original bank | 192 internal Debug QA records, including six reused records | 0 / 192 |
| G9 textbook source | 10 units / 32 references | No new textbook lesson approval |
| BAC textbook source | 2 books / 14 units / 63 researches | No new textbook question extraction |
| BAC section CSV | 27 indexed activity/exercise sections; individual_items_extracted=False | Not an individual question bank |
| Existing BAC original proposals | 28 drafts in authoring documentation, not imported into the app | 0 / 28 new publication approvals |
| Preserved learning pilots/demo | Original fractions, Nature draft and 24 BAC demo lessons | Existing provenance retained; no official complete-curriculum claim |

All G9 questions require independent mathematical review, official edition comparison and rights approval. Structural validation and a passing numeric test are not teacher approval. Source PDF textbooks were not found in the supplied pack or D:/ENMATH search; existing catalogs and cached page images remain the source-index evidence. Final individual character assets and motion files were also unavailable.

No official exercise/solution was invented to fill the BAC index. Full individual source problems, lesson-level linkage and approval are still required before publication. The per-question review ledger is the handoff for the G9 bank.
'''
    reports['GAMIFICATION_REPORT.md']='''# Learning days, energy and Gems

Learning days are derived from persisted original-fractions/BAC practice attempts with actual submitted answers. The captured profile timezone determines local epoch days, including DST. Event IDs are deduplicated; multiple sessions on one date count once. A gap changes the current streak without deleting prior active-day history. Backward-clock anomalies and future events are rejected/deferred by a local high-water check. This is a local consistency guard, not trusted server time or tamper-proof anti-cheat.

Opening the app, browsing references/PDFs, saving bookmarks, plan edits, internal 192-bank QA sessions and unreviewed Nature sessions do not create new learning-day credit. Historical original practice evidence can restore days; no historical Gems reward is synthesized. The seven-day view opens real daily session counts.

EnergyMode is UNLIMITED. No decrement, lockout, purchase, advertisement or rewarded video is implemented. The UI shows infinity as this policy, not a fake numeric balance.

The existing SQLite Gems implementation, schema, policy, reward IDs and idempotency constraints are unchanged. Home/Profile read the actual current-owner wallet state and show unavailable/guest states honestly. New QA code has no wallet reward path. Existing SQLite and coordinator regression suites remain included.
'''
    reports['FIREBASE_INTEGRATION_REPORT.md']='''# Firebase readiness

Status: LOCAL_ONLY. Working live Firebase functions: zero. The repository contains no google-services.json, Firebase SDK/Auth/Firestore setup, verified EN.MATH project ID, deployed security rules or connected Firebase tool. No other company's project was used and no new cloud project was created.

The optional integration seam defines verified Auth UID/project/session identity, an explicit local-owner binding, typed document versions, immutable learning events, conflict detection, retry outcomes and an outbox interface. The document has no writable Gems balance. Local fake tests cover absent configuration, wrong project/UID, local account switch, logout during a request, offline retry, duplicate commands and changed-payload conflicts.

These are contract tests, not Firebase network/security-rule tests. No production Firebase adapter, durable production outbox, remote profile-link flow or rules deployment was claimed. All current accounts remain local. The UI exposes no cloud login or Connected badge.

Activation needs an explicitly authorized independent EN.MATH project matching com.ahmadmol.enmath, approved Auth providers, UID-owned schema, verified deployment rules, account-link consent/reconciliation, a durable isolated queue and staging tests for two accounts/offline/reconnect/conflicts/logout. Gems would require trusted reward-event verification; the local ledger must not become a client-writable cloud balance.

Official references consulted: https://firebase.google.com/docs/firestore/security/rules-conditions and https://firebase.google.com/docs/firestore/manage-data/enable-offline . No untested production rules were published.
'''
    reports['QA_RESULTS.md']=f'''# Actual QA results - 2026-11-02

| Check | Actual result |
|---|---|
| Pre-activation baseline | 468 Debug + 428 Release passed |
| Final Debug JVM and Compose host | {debug} passed, zero failures/errors/skips |
| Final Release JVM + actual Release-resource gate | {release} passed, zero failures/errors/skips |
| Python content/source/SQLite/Gems | 34 passed |
| Total final successful executions | {total} |
| Host configuration matrix | 72 distinct configurations; 360/393/800dp, both stages/languages/themes, 1/1.5/2 font |
| Android instrumentation compilation | PASS |
| Instrumented/device execution | NOT_RUN; no emulator, ADB or phone |
| Lint Debug / Release | PASS; zero Error/Fatal; {warnings['debug']} / {warnings['release']} warnings |
| Debug / Release APK assembly | PASS |
| Debug signature | Existing local debug key; verified separately |
| Release signing | UNSIGNED |
| Draft asset boundary | Debug present / Release absent, for both the 192 bank and old Nature asset |
| Backup restore | PASS; bundle clone + fsck, baseline 3f22440 recovered |
| Raster rendering | {render['status']}; zero current app images approved |
| Live Firebase Auth/Firestore/security rules | NOT_RUN; no configured project or transport |

Final command: gradlew :app:testDebugUnitTest :app:testReleaseUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:lintRelease :app:assembleDebug :app:assembleRelease -PnatureHostQa=true -PqaBuildDir=D:/ENMATH/EN.MATH/app/build-modern-qa --offline --max-workers=2 --console=plain . JBR 21 and the existing Gradle 8.11.1 / AGP 8.9.2 / Kotlin 2.1.20 / Compose BOM were retained.

New domain regressions cover content mapping/fields/grading, avatar owner isolation, storage failures/corruption, session epochs, saved viewport, timezone/DST/clock replay, QA persistence and multiline fields, duplicate checking, real practice days, unchanged Gems and cloud contract fakes. Updated old UI assertions follow the explicitly requested single journey and new five tabs. All unchanged regression suites still run.

Earlier failed attempts remain in evidence and are not counted as successful final tests. They found superseded navigation assumptions, a large-font wrapping bug, partially hidden sheet actions, stale no-longer-existing entry tags and a test-only missing wallet graph. Sheet actions are now visibly composed and exercised through accessibility semantics because Robolectric native modal touch dispatch still did not navigate. Actual platform gestures remain unverified.

Host geometry proves measured overlap/touch targets and text bounds, not rendered focus artifacts, pixels, IME, real Activity/process death, TalkBack, FPS or physical upgrade retention. Windows protection was not bypassed. {render.get('reason','')}
'''
    reports['KNOWN_LIMITATIONS.md']='''# Remaining acceptance work

- Three companion identities have neutral temporary portraits. Approved individual artwork and matching poses/animation files remain required. All nine intents currently fall back to the same owner's placeholder; no animation fidelity is claimed.
- All 192 Grade 9 questions remain drafts, absent from Release. Free reasoning without validated rules is teacher-deferred. Adaptive promotion, alternate-item generation and a spaced-review schedule are not implemented as certified learning features.
- The BAC source is still an index, with 28 separate original authoring proposals and no extracted complete official exercise bank. No new official solutions or content locks were invented.
- Firebase remains local-only. The SDK/adapter, authenticated profile linking, durable production sync queue and deployed UID rules need authorized project access and live security tests. Fake transport tests do not satisfy those checks.
- Windows Layoutlib raster rendering is blocked; zero current app screenshots are approved. Modal touch dispatch is unreliable in Robolectric, so only visible semantic actions are accepted there. Review actual gestures, circle/shadows/focus, insets, Arabic/math mixing, IME, TalkBack, orientation/process death, tablet appearance and performance on a later authorized device.
- Release APK is unsigned. No phone installation, app-data deletion or actual installed-app update was performed. Data retention is supported by scoped fixture regressions and unchanged legacy schemas, not a physical upgrade test.
- Existing legacy tools/demo/teacher components were retained where appropriate and inherit the shared theme; an exhaustive redesign of every historical screen is not claimed. Streak clock protection is local and can require a time check; it is not server anti-cheat.
'''
    reports['GIT_STATUS.md']=f'''# Source state at packaging

Branch: {branch}

Implementation commit: {head}

Baseline: 3f22440a35bf58c4963867aa05364be268839b82, version 1.0.5/6; app source equal to the 54ce759 reference. The new app is 1.1.0/7. No main merge, reset-hard, clean, force push or remote push was performed. The previous branches remain available.

The delivery copy of this file is finalized with the documentation commit after reports are committed. The source copy intentionally records the implementation commit to avoid a self-referencing commit hash. before-modern.bundle was verified and restored; the final incremental source bundle requires that baseline bundle. Generated build outputs and reference media remain outside Git.
'''
    reports['SUMMARY_AR.md']=f'''# ملخص التسليم

نُفّذ إصدار EN.MATH **1.1.0 / 7** على فرع `{branch}`، مع تنقل بخمسة أقسام وزر رئيسية دائري عائم، ورحلة واحدة تشمل وحدات التاسع العشر ودروسه الـ32. البكالوريا تستخدم النظام المشترك مع الحفاظ على كتابيها ووحداتها الـ14 ودروسها الـ63.

أُضيف اختيار شخصية محفوظ لكل ملف، ومركز أدوات وتقدم وحساب، وسلسلة أيام من تدريبات محفوظة فعلية، وطاقة غير محدودة. بقي نظام الجواهر القديم ومعرّفات الطلاب وبياناتهم دون ترحيل أو تصفير.

دُمجت **192 مسودة سؤال** كاملة في Debug الداخلي: ستة كانت موجودة و**186 جديدة**. لا توجد أسئلة معتمدة علميًا من هذه الحملة، ولا يظهر البنك في Release. تتطلب الإجابات الحرة مراجعة معلم، وتُحفظ الإجابات والتلميحات والأخطاء والخطوات متعددة الأسطر.

نجح **{total} تنفيذ اختبار** ({debug} Debug و{release} Release و34 Python)، ونجح البناء وLint. اختُبرت 72 تهيئة مضيفة للواجهات، تشمل الخط 200% واللوحي 800dp. لا توجد مزامنة Firebase فعلية، والشخصيات تستخدم رموزًا مؤقتة إلى حين توافر الأصول المعتمدة. الفحص البصري الحالي محجوب، واختبار لمس النوافذ السفلية على جهاز حقيقي غير منفّذ. Release غير موقّع.

لم يُستخدم هاتف أو Emulator أو ADB، ولم تُحذف بيانات أو يُدمج main أو تُرفع التغييرات إلى GitHub. تُفصّل التقارير الباقي وحدود التحقق دون ادعاء اكتمال المحتوى أو الاعتماد العلمي.
'''
    repo_docs=ROOT/'docs/MODERN_STUDENT_EXPERIENCE_2026-11-02';repo_docs.mkdir(parents=True,exist_ok=True)
    for name,body in reports.items():
        (out/name).write_text(body,encoding='utf-8');(repo_docs/name).write_text(body,encoding='utf-8')
    for name in ['QUESTION_REVIEW_LEDGER.csv','EVIDENCE.json']:
        if name=='QUESTION_REVIEW_LEDGER.csv':shutil.copyfile(out/'evidence'/name,repo_docs/name)
    evidence_out=out/'evidence'
    for file in args.qa.glob('*.log'):shutil.copyfile(file,evidence_out/file.name)
    for name in ['render-status.json','signature-status.json']:
        if (args.qa/name).exists():shutil.copyfile(args.qa/name,evidence_out/name)
    for variant in ['Debug','Release']:
        folder=evidence_out/f'test{variant}UnitTest';folder.mkdir(exist_ok=True)
        for file in (args.qa/f'build/test-results/test{variant}UnitTest').glob('TEST-*.xml'):shutil.copyfile(file,folder/file.name)
    for variant in ['debug','release']:
        for suffix in ['xml','html']:
            source=args.qa/f'build/reports/lint-results-{variant}.{suffix}'
            if source.exists():shutil.copyfile(source,evidence_out/source.name)
    print(json.dumps(dict(reports=len(reports),tests=total,branch=branch,implementation_commit=head,artifacts=files),ensure_ascii=False,indent=2))

if __name__=='__main__':main()
