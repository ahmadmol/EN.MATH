"""Import the supplied original DRAFT bank into Debug only; never grant publication approval."""
import argparse
import collections
import hashlib
import json
from pathlib import Path
import re
import unicodedata

ROOT = Path(__file__).resolve().parents[1]

def normalized(value):
    return re.sub(r"[\sـًٌٍَُِّْأإآ]", lambda m: "ا" if m[0] in "أإآ" else "", unicodedata.normalize("NFKC", value))

def ingest(source):
    data = json.loads(source.read_text(encoding="utf-8"))
    catalog = json.loads((ROOT / "docs/curriculum/g9/source/G9_CURRICULUM_CATALOG_INDEX.json").read_text(encoding="utf-8"))
    targets = [(u, lesson) for u in catalog["units"] for lesson in u["lessons"]]
    assert len(data["questions"]) == 192 and len(data["lessons"]) == len(targets) == 32
    mappings = []
    for lesson in data["lessons"]:
        # Check the title AND unit/book boundary; IDs alone are not sufficient evidence.
        book = "G9-ALG" if lesson["book"] == "الجبر" else "G9-GEO"
        unit_order = int(lesson["unitId"].rsplit("U", 1)[1])
        candidates = [(u, l) for u, l in targets if u["sourceBookId"] == book
                      and u["orderInBook"] == unit_order and normalized(l["title"]) == normalized(lesson["name"])]
        # A small, explicit editorial alias list documents wording differences in the reference.
        aliases = {
            "G9-A-U02-L01": "G9-ALG-U02-L01",
            "G9-A-U02-L03": "G9-ALG-U02-L03",  # مطابقات / متطابقات
            "G9-A-U03-L02": "G9-ALG-U03-L02",  # الجداء الصفري
            "G9-H-U01-L01": "G9-GEO-U01-L01",
            "G9-H-U02-L02": "G9-GEO-U02-L02",  # inverse theorem wording
            "G9-H-U04-L01": "G9-GEO-U04-L01",  # أجسام / مجسمات
        }
        if not candidates and lesson["id"] in aliases:
            candidates = [(u, l) for u, l in targets if l["lessonId"] == aliases[lesson["id"]]]
        if len(candidates) != 1:
            raise ValueError(f"Explicit mapping required: {lesson['id']} {lesson['name']}; candidates={candidates}")
        u, l = candidates[0]
        mappings.append(dict(draftLessonId=lesson["id"], sourceLessonId=l["lessonId"],
                             draftTitle=lesson["name"], sourceTitle=l["title"], sourceUnitId=u["unitId"],
                             sourceBookId=book, goal=lesson["goal"]))
    assert len({m["sourceLessonId"] for m in mappings}) == 32
    by_id = {m["draftLessonId"]: m for m in mappings}
    assert len({q["id"] for q in data["questions"]}) == 192
    for q in data["questions"]:
        mapping = by_id[q["lessonId"]]
        assert q["id"].startswith(q["lessonId"] + "-Q")
        assert q["unitId"] == q["lessonId"].rsplit("-L", 1)[0]
        assert q["book"] == ("الجبر" if mapping["sourceBookId"] == "G9-ALG" else "الهندسة")
        assert q["reviewStatus"] == "draft_needs_teacher_review" and q["contentVersion"] == 1
        assert q["difficulty"] in (1, 2, 3)
        assert all(q.get(k) for k in ("prompt", "answer", "explanation", "hint", "gradingMode", "sourceTitle"))
        if q["interaction"] == "single_choice":
            assert q["choices"][q["correctOptionIndex"]] == q["answer"]
    counts = collections.Counter(q["lessonId"] for q in data["questions"])
    assert set(counts.values()) == {6}
    legacy = json.loads((ROOT / "app/src/debug/assets/content/g9/nature_numbers_pilot.json").read_text(encoding="utf-8"))
    existing = {q["id"]: q for q in legacy["questions"]}
    for q in data["questions"]:
        if q["id"] in existing:
            assert all(q[field] == existing[q["id"]][field] for field in ("prompt", "answer", "explanation", "hint"))
    output = dict(data, lessonMapping=mappings, importProvenance=dict(
        sourceSha256=hashlib.sha256(source.read_bytes()).hexdigest(),
        previouslyIntegratedIds=sorted(existing), newlyIntegratedCount=192-len(existing),
        publication="DEBUG_QA_ONLY", mathApproval=None, editionApproval=None, rightsApproval=None))
    return output

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("source", type=Path)
    parser.add_argument("--output", type=Path, default=ROOT / "app/src/debug/assets/content/g9_original_draft_v1.json")
    args = parser.parse_args()
    output = ingest(args.source)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(dict(questions=len(output["questions"]), mappings=len(output["lessonMapping"]),
                         interactions=dict(collections.Counter(q["interaction"] for q in output["questions"])),
                         pending=192, approved=0), ensure_ascii=False))

if __name__ == "__main__":
    main()
