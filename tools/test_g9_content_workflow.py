import copy
import tempfile
import unittest
from pathlib import Path
from g9_content_workflow import exact_expression, inventory, new_document, topics, validate

def text(value="Original fixture"):
    return dict(ar="نص اختبار أصلي", en=value)

def complete_draft():
    doc = new_document("G9-ALG-U01-L03")
    doc["author"] = "TEST FIXTURE — not released"
    doc["objectives"] = [text()]
    for section in doc["sections"]:
        section.update(title=text(), body=text(), math="a/b", conditions=[text()])
    doc["examples"] = [dict(prompt=text(), steps=[dict(reason=text(), math="42/70 = 3/5")], check=dict(expression="42/70", expected="3/5"))]
    question = dict(id="fixture-q", kind="reduced_fraction", prompt=text(), hint=text(), correction=text(), numerator=42, denominator=56, answer="3/4")
    doc["questions"] = [question]
    doc["checkpoint"] = dict(question, id="fixture-check")
    return doc

class G9ContentWorkflowTest(unittest.TestCase):
    def test_all_32_topics_keep_real_source_identity_and_publication_limits(self):
        with tempfile.TemporaryDirectory() as directory:
            rows = inventory(Path(directory))
            self.assertEqual(32, len(rows)); self.assertEqual(32, len({r["lessonId"] for r in rows}))
            self.assertEqual(1, sum(r["runtimeAvailability"] == "BOUNDED_ORIGINAL_PILOT" for r in rows))
            self.assertTrue(all(not r["textbookReady"] and r["independentEducatorReview"] == "NOT_PERFORMED" for r in rows))
            self.assertEqual(19, sum(r["sourceId"] == "G9-ALG" for r in rows))
    def test_empty_authoring_template_is_never_a_ready_lesson(self):
        for lesson in topics():
            self.assertTrue(validate(new_document(lesson)))
            self.assertTrue(validate(new_document(lesson), require_release=True))
    def test_complete_draft_can_pass_structure_without_becoming_a_release(self):
        self.assertEqual([], validate(complete_draft()))
        self.assertTrue(validate(complete_draft(), require_release=True))
    def test_changed_source_edition_hash_and_pages_are_rejected(self):
        for key, value in (("edition", "2026-2027"), ("sourceSha256", "invented"), ("pdfFrom", 99), ("lessonId", "Bac")):
            doc = complete_draft(); doc["source"][key] = value
            self.assertTrue(validate(doc))
    def test_fraction_equivalence_is_not_enough_without_irreducibility(self):
        doc = complete_draft(); doc["questions"][0]["answer"] = "6/8"
        self.assertTrue(validate(doc))
    def test_answer_must_match_the_actual_exact_arithmetic(self):
        doc = complete_draft(); doc["questions"][0].update(kind="exact_number", answer="9", check=dict(expression="2+3", expected="5"))
        self.assertTrue(validate(doc))
    def test_duplicate_checkpoint_ids_and_missing_hints_are_rejected(self):
        doc = complete_draft(); doc["checkpoint"]["id"] = "fixture-q"
        self.assertTrue(validate(doc))
        doc = complete_draft(); doc["questions"][0]["hint"] = dict(ar="", en="")
        self.assertTrue(validate(doc))
    def test_proofs_and_constructions_cannot_be_graded_automatically(self):
        for kind in ("proof", "construction"):
            doc = complete_draft(); doc["questions"][0].update(kind=kind, grading="AUTO", rubric=[text()])
            self.assertTrue(validate(doc))
    def test_pass_review_requires_actual_evidence_metadata(self):
        doc = complete_draft(); doc["reviews"]["mathematics"]["status"] = "PASS"
        self.assertTrue(validate(doc))
    def test_arithmetic_is_bounded_and_never_executes_author_code(self):
        self.assertEqual(exact_expression("42/70"), exact_expression("3/5"))
        for value in ("__import__('os').system('whoami')", "open('private')", "2**99999", "1/0", "[1,2]"):
            with self.assertRaises((ValueError, ZeroDivisionError)):
                exact_expression(value)
    def test_malformed_document_is_rejected(self):
        for doc in ([], {}, dict(source=[], reviews={}), dict(source={}, reviews={}, objectives="text")):
            self.assertTrue(validate(doc))

if __name__ == "__main__": unittest.main(verbosity=2)
