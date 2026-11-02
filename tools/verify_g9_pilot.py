"""Read-only source and original pilot math checks. No textbook content is copied into the repo."""
import hashlib
import json
import math
from fractions import Fraction
from pathlib import Path
import re
from pypdf import PdfReader

root = Path(__file__).resolve().parents[1]
manifest = json.loads((root / "docs/curriculum/g9/source/MANIFEST.json").read_bytes())
source = manifest["books"]["G9-ALG"]
pdf = Path.home() / "Downloads" / source["fileName"]
assert pdf.is_file(), "Source PDF is missing; do not claim source validation"
assert hashlib.sha256(pdf.read_bytes()).hexdigest() == source["sha256"], "Wrong source PDF"
assert len(PdfReader(pdf).pages) == source["pages"]
code = (root / "app/src/main/java/com/ahmadmol/enmath/core/data/Grade9Pilot.kt").read_text(encoding="utf-8")
questions = re.findall(r'G9FractionQuestion\("([^"]+)", (\d+), (\d+), "(\d+/\d+)"', code)
assert len(questions) == 5
for qid, a, b, answer in questions:
    n, d = map(int, answer.split("/"))
    assert n > 0 and d > 0 and math.gcd(n, d) == 1, qid
    assert Fraction(int(a), int(b)) == Fraction(n, d), qid
examples = [(42, 70, 14, 3, 5), (84, 126, 42, 2, 3)]
for a, b, divisor, n, d in examples:
    assert math.gcd(a, b) == divisor
    assert a // divisor == n and b // divisor == d
    assert a * d == b * n and math.gcd(n, d) == 1
print(json.dumps({"status": "PASS", "sourceId": "G9-ALG", "sha256": source["sha256"],
    "pdfPages": source["pages"], "visuallyReviewedPages": [14, 15], "sourceEdition": "2025-2026",
    "pilotLessonId": "G9-ALG-U01-L03", "originalQuestionsChecked": len(questions),
    "workedExamplesChecked": len(examples), "official2026_2027Alignment": "NOT_VERIFIED",
    "independentHumanReview": "NOT_PERFORMED", "fullTextbookReadyLessons": 0,
    "originalPilotTopics": 1, "otherTopicsUnavailable": 31, "gemsAwarded": False}, ensure_ascii=False))
