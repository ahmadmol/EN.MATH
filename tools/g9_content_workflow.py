"""Grade 9 editorial workflow. This tool never publishes content or changes student data."""
import argparse
import ast
import csv
from fractions import Fraction
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "docs/curriculum/g9/source/G9_CURRICULUM_CATALOG_INDEX.json"
PILOT = "G9-ALG-U01-L03"
KINDS = {"reduced_fraction", "exact_number", "choice", "proof", "construction"}
SECTIONS = {"explanation", "definition", "formula", "summary"}
REVIEWS = ("source", "rights", "mathematics", "pedagogy", "editionAlignment")

def topics():
    catalog = json.loads(CATALOG.read_bytes())
    sources = {b["bookId"]: b for b in catalog["sources"]}
    return {lesson["lessonId"]: dict(lessonId=lesson["lessonId"], title=lesson["title"],
        unitId=unit["unitId"], unitTitle=unit["title"], sourceId=unit["sourceBookId"],
        sourceSha256=sources[unit["sourceBookId"]]["sha256"], edition=catalog["editionLabelOnSource"],
        pdfFrom=lesson["sourcePages"]["from"], pdfTo=lesson["sourcePages"]["to"])
        for unit in catalog["units"] for lesson in unit["lessons"]}

def new_document(lesson_id):
    source = topics()[lesson_id]
    return dict(schemaVersion=1, stage="G9", source=source, contentRevision="draft-v1",
        publication="DRAFT_ONLY", author="", objectives=[], prerequisites=[],
        sections=[dict(kind=kind, title=dict(ar="", en=""), body=dict(ar="", en=""), math="",
            conditions=[]) for kind in ("explanation", "definition", "formula", "summary")],
        examples=[], questions=[], checkpoint=None,
        reviews={kind: dict(status="PENDING", reviewer="", reviewedAt="", evidence="") for kind in REVIEWS},
        independentEducatorReview="NOT_PERFORMED", textbookTextCopied=False, grantsGems=False)

def exact_expression(expression):
    """Bounded arithmetic only. Never eval author-supplied code."""
    if not isinstance(expression, str) or not expression or len(expression) > 200:
        raise ValueError("Missing/oversized arithmetic expression")
    tree = ast.parse(expression, mode="eval")
    if len(list(ast.walk(tree))) > 80:
        raise ValueError("Expression is too complex")
    def visit(node):
        if isinstance(node, ast.Constant) and type(node.value) in (int, float):
            value = Fraction(str(node.value))
        elif isinstance(node, ast.UnaryOp) and isinstance(node.op, (ast.UAdd, ast.USub)):
            value = visit(node.operand) * (-1 if isinstance(node.op, ast.USub) else 1)
        elif isinstance(node, ast.BinOp):
            a, b = visit(node.left), visit(node.right)
            if isinstance(node.op, ast.Add): value = a + b
            elif isinstance(node.op, ast.Sub): value = a - b
            elif isinstance(node.op, ast.Mult): value = a * b
            elif isinstance(node.op, ast.Div): value = a / b
            elif isinstance(node.op, ast.Pow) and b.denominator == 1 and abs(b) <= 20: value = a ** int(b)
            else: raise ValueError("Unsupported arithmetic operation")
        else: raise ValueError("Only literal bounded arithmetic is supported")
        if max(value.numerator.bit_length(), value.denominator.bit_length()) > 512:
            raise ValueError("Arithmetic result is too large")
        return value
    return visit(tree.body)

def _validate(document, require_release=False):
    errors = []
    if not isinstance(document, dict): return ["Document must be an object"]
    for key in ("source", "reviews"):
        if not isinstance(document.get(key), dict): return [key + " must be an object"]
    for key in ("objectives", "sections", "examples", "questions"):
        if not isinstance(document.get(key), list) or not all(isinstance(x, dict) for x in document[key]):
            return [key + " must be a list of objects"]
    def check(condition, message):
        if not condition: errors.append(message)
    def bilingual(copy):
        return isinstance(copy, dict) and all(isinstance(copy.get(k), str) and copy[k].strip() for k in ("ar", "en"))
    def math_check(check_spec, label):
        try:
            check(exact_expression(check_spec["expression"]) == Fraction(check_spec["expected"]), label + ": arithmetic mismatch")
        except (ValueError, KeyError, TypeError, SyntaxError, ZeroDivisionError, OverflowError):
            errors.append(label + ": invalid/unsupported arithmetic check")
    check(document.get("schemaVersion") == 1 and document.get("stage") == "G9", "Wrong schema/stage")
    check(document.get("publication") in ("DRAFT_ONLY", "REVIEWED_CANDIDATE"), "Invalid editorial publication state")
    source = document.get("source", {})
    check(source == topics().get(source.get("lessonId")), "Source identity/title/pages differ from the real catalog")
    check(isinstance(document.get("author"), str) and bool(document["author"].strip()), "Author is required")
    check(isinstance(document.get("contentRevision"), str) and bool(document["contentRevision"].strip()), "Content revision is required")
    check(document.get("grantsGems") is False, "Editorial drafts cannot award Gems")
    check(document.get("textbookTextCopied") is False, "Only original content is allowed in this workflow")
    objectives = document.get("objectives", [])
    check(bool(objectives) and all(bilingual(x) for x in objectives), "Complete bilingual objectives are required")
    sections = document.get("sections", [])
    check(SECTIONS.issubset({x.get("kind") for x in sections}), "Explanation/definition/formula/summary sections are required")
    for i, section in enumerate(sections):
        check(bilingual(section.get("title")) and bilingual(section.get("body")), f"Section {i}: incomplete bilingual text")
        if section.get("kind") == "formula":
            check(bool(section.get("math", "").strip()) and bool(section.get("conditions")), f"Section {i}: formula and conditions required")
    examples = document.get("examples", [])
    check(bool(examples), "At least one worked example is required")
    for i, example in enumerate(examples):
        check(bilingual(example.get("prompt")), f"Example {i}: prompt required")
        steps = example.get("steps", [])
        check(bool(steps) and all(bilingual(x.get("reason")) and bool(x.get("math")) for x in steps), f"Example {i}: justified mathematical steps required")
        if "check" in example: math_check(example["check"], f"Example {i}")
        else: check(bool(example.get("manualReviewEvidence")), f"Example {i}: arithmetic check or manual evidence required")
    questions = document.get("questions", [])
    checkpoint = document.get("checkpoint")
    check(bool(questions) and isinstance(checkpoint, dict), "Practice questions and a separate checkpoint are required")
    all_questions = questions + ([checkpoint] if isinstance(checkpoint, dict) else [])
    ids = [q.get("id") for q in all_questions]
    check(all(ids) and len(set(ids)) == len(ids), "Question/checkpoint IDs must be nonempty and unique")
    for q in all_questions:
        label = str(q.get("id", "question"))
        kind = q.get("kind")
        check(kind in KINDS, label + ": unsupported question template")
        for key in ("prompt", "hint", "correction"):
            check(bilingual(q.get(key)), label + ": missing " + key)
        if kind == "reduced_fraction":
            try:
                n, d = q["numerator"], q["denominator"]
                a, b = map(int, q["answer"].split("/"))
                check(type(n) is int and type(d) is int and n > 0 and d > 0 and a > 0 and b > 0 and
                    math.gcd(a, b) == 1 and n*b == d*a, label + ": answer must be exactly equal and reduced")
            except (ValueError, KeyError, TypeError): errors.append(label + ": invalid fraction data")
        elif kind == "exact_number":
            math_check(q.get("check", {}), label)
            try: check(Fraction(q["answer"]) == exact_expression(q["check"]["expression"]), label + ": answer differs from the computed value")
            except (KeyError, ValueError, TypeError, SyntaxError, ZeroDivisionError): errors.append(label + ": invalid exact answer")
        elif kind == "choice":
            options = q.get("options", [])
            option_ids = [o.get("id") for o in options]
            check(len(options) >= 2 and len(set(option_ids)) == len(options) and all(option_ids) and
                all(bilingual(o.get("label")) for o in options) and q.get("answer") in option_ids, label + ": invalid choices")
        elif kind in ("proof", "construction"):
            check(q.get("grading") == "MANUAL" and bool(q.get("rubric")), label + ": proof/construction needs a manual rubric")
            if kind == "construction": check(bilingual(q.get("figureAlt")), label + ": accessible figure description required")
    if document.get("authoringPack") == "g9-u01-v1":
        from g9_unit1_math import validate_item
        for item in examples + all_questions:
            errors.extend(str(item.get("id", "example")) + ": " + e for e in validate_item(item))
    for name in REVIEWS:
        review = document.get("reviews", {}).get(name, {})
        check(review.get("status") in ("PENDING", "PASS", "FAIL"), name + ": invalid review status")
        if review.get("status") == "PASS":
            check(all(isinstance(review.get(k), str) and review[k].strip() for k in ("reviewer", "reviewedAt", "evidence")), name + ": PASS requires reviewer/date/evidence")
        if require_release: check(review.get("status") == "PASS", name + ": review not passed")
    if require_release:
        check(document.get("publication") == "REVIEWED_CANDIDATE", "Explicit reviewed-candidate status required")
        check(document.get("independentEducatorReview") == "EVIDENCED" and
            document.get("reviews", {}).get("pedagogy", {}).get("reviewerRole") == "INDEPENDENT_EDUCATOR",
            "Independent educator evidence is required for a full release candidate")
    return errors

def validate(document, require_release=False):
    try: return _validate(document, require_release)
    except (TypeError, KeyError, AttributeError, ValueError): return ["Invalid nested field types or values; no content is published"]

def inventory(output):
    drafts = {l["lessonId"]: l for p in (ROOT / "docs/curriculum/education/units").glob("*.json") for l in json.loads(p.read_bytes()).get("lessons", [])}
    def field(lid, key):
        return "BOUNDED_ORIGINAL_PILOT" if lid == PILOT else "UNREVIEWED_DRAFT" if drafts.get(lid, {}).get(key) else "NOT_AUTHORED"
    rows = [dict(**source, authoring="ORIGINAL_PILOT" if lid == PILOT else "UNREVIEWED_DRAFT" if lid in drafts else "NOT_AUTHORED",
        runtimeAvailability="BOUNDED_ORIGINAL_PILOT" if lid == PILOT else "UNAVAILABLE",
        sourceIndex="INDEXED", mathematics="SOURCE_AND_AUTOMATED_CHECKS" if lid == PILOT else "PENDING",
        objectives=field(lid, "objectives"), explanations=field(lid, "intuition"), examples=field(lid, "examples"),
        formulas=field(lid, "rules"), questions=field(lid, "questions"), answers=field(lid, "questions"),
        rights="ORIGINAL_PILOT_TEXT_ONLY_NO_BOOK_REDISTRIBUTION" if lid == PILOT else "PENDING",
        independentEducatorReview="NOT_PERFORMED", edition2026_2027="NOT_VERIFIED", textbookReady=False,
        publication="PILOT_ONLY" if lid == PILOT else "BLOCKED") for lid, source in topics().items()]
    for row in rows:
        draft = ROOT / "docs/curriculum/g9/authoring/unit1" / (row["lessonId"] + ".json")
        if draft.exists():
            document = json.loads(draft.read_bytes())
            row.update(editorialDraft=str(draft.relative_to(ROOT)), editorialReadiness=
                "AUTHORED_SOFTWARE_CHECKED_PENDING_EDUCATOR" if not validate(document) else "INCOMPLETE_OR_BLOCKED")
    output.mkdir(parents=True, exist_ok=True)
    (output / "CONTENT_READINESS.json").write_text(json.dumps(rows, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
    with (output / "CONTENT_READINESS.csv").open("w", encoding="utf-8-sig", newline="") as file:
        writer = csv.DictWriter(file, fieldnames=list(dict.fromkeys(k for row in rows for k in row))); writer.writeheader(); writer.writerows(rows)
    return rows

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="command", required=True)
    p = sub.add_parser("new"); p.add_argument("lesson_id", choices=topics()); p.add_argument("output", type=Path)
    p = sub.add_parser("validate"); p.add_argument("input", type=Path); p.add_argument("--require-release", action="store_true")
    p = sub.add_parser("inventory"); p.add_argument("output", type=Path)
    args = parser.parse_args()
    if args.command == "new":
        # Exclusive creation: never overwrite an author's work.
        with args.output.open("x", encoding="utf-8") as file: json.dump(new_document(args.lesson_id), file, ensure_ascii=False, indent=2)
        print("Created DRAFT_ONLY; empty fields intentionally fail validation. Nothing is published.")
    elif args.command == "validate":
        errors = validate(json.loads(args.input.read_bytes()), args.require_release)
        print(json.dumps(dict(status="INCOMPLETE_OR_BLOCKED" if errors else "CHECKS_PASS_NOT_PUBLISHED", errors=errors), ensure_ascii=False, indent=2))
        raise SystemExit(bool(errors))
    else:
        rows = inventory(args.output); print(json.dumps(dict(topics=len(rows), originalPilots=1, textbookReady=0, otherTopicsUnavailable=31)))

if __name__ == "__main__": main()
