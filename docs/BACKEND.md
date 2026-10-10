# Future backend contract (not provisioned)

Android screens consume `LearningState` through `AppViewModel` and a `LearningRepository`. The local implementation stores a versioned JSON product snapshot over a single seed. Replace the repository and engine adapters without changing screen layouts. UI validation and role guards are demo behavior, not security boundaries for a real service.

Suggested relational schema; identifiers become backend UUIDs, timestamps UTC, and all foreign keys indexed:

| Tables | Key fields / relationships |
|---|---|
| profiles | id (auth user), display_name, locale, theme |
| user_roles | user_id → profiles, role; assigned by trusted server logic |
| student_profiles | user_id → profiles, grade, curriculum_id |
| teacher_profiles | user_id → profiles, subjects, experience, institution |
| curriculums | id, grade, country, localized title |
| books | id, curriculum_id, order, localized title/description |
| topics | id, book_id, order, localized description |
| lessons | id, topic_id, order, objectives, concept blocks, formulas, worked example, checkpoint |
| practice_sets | id, topic_id, kind (practice/quiz) |
| questions | id, topic_id, kind, prompt, expression, answer key (server only), explanation |
| question_options | id, question_id, order, localized label |
| classrooms | id, teacher_id, title, invite code, current_topic |
| classroom_members | classroom_id, student_id, joined_at (unique pair) |
| assignments | id, classroom_id, teacher_id, due_at, points, attempts, hints, late_allowed |
| assignment_questions | assignment_id, question_id, order, points |
| assignment_submissions | id, assignment_id, student_id, attempt, submitted_at, reviewed_at, feedback |
| student_answers | submission_id, question_id, response, score, feedback |
| topic_progress | student_id, topic_id, lesson_fraction, practice_accuracy, best_quiz, mastery_score |
| lesson_progress | student_id, lesson_id, completed_at (unique pair) |
| practice_attempts | id, student_id, set_id, answers, correctness, seconds, completed_at |
| solver_history | id, user_id, expression, engine_version, result, created_at |
| bookmarks | user_id, entity_type, entity_id (unique tuple) |
| notifications | id, recipient_id, type, payload, read_at, created_at |
| teacher_resources | id, teacher_id, topic_id, type, body, updated_at |
| resource_shares | resource_id, classroom_id |

Authorization / RLS plan:

- Students access their own progress, attempts, submissions, bookmarks, history and inbox only.
- A teacher reads only owned classrooms, their members and assignments in those classrooms; resource creation/sharing must verify ownership.
- Curriculum is readable according to guest/authenticated access policy; answer keys for live assignments must not be exposed to a student before allowed feedback.
- Role assignment is never trusted from a client field. Grade/score, submission deadline and attempt limits are checked transactionally on the server.
- Aggregate mastery is computed from persisted facts using a versioned rule; the current 40/40/20 weighting is an illustrative demo rule, not a validated educational model.
- No backend service, cloud database, credentials, real authentication or secret key is included in this prototype.

Migration requirements: per-user isolation, conflict handling, migrations/backups, query pagination, cancellable loading/error states, idempotent completion/submission, real enrollment, scheduled reminders and push delivery. The demo uses Ahmed's local student persona across student sessions to make teacher/student data consistency reviewable on one device.
