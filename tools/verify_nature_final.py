"""Read-only APK/test evidence collection. Never invokes ADB or opens student data."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile


def suites(folder):
    rows = [ET.parse(p).getroot() for p in folder.glob("TEST-*.xml")]
    return {"suites": len(rows), **{
        key: sum(int(row.get(key, "0")) for row in rows)
        for key in ("tests", "failures", "errors", "skipped")
    }}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("build", type=Path)
    parser.add_argument("evidence", type=Path)
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    apks = {}
    asset = "assets/content/g9/nature_numbers_pilot.json"
    for variant, name in (("debug", "app-debug.apk"), ("release", "app-release-unsigned.apk")):
        apk = args.build / "outputs/apk" / variant / name
        with zipfile.ZipFile(apk) as package:
            names = package.namelist()
            present = asset in names
            assert present == (variant == "debug"), (variant, "Wrong pilot asset boundary")
            assert not any("authoring" in path or "question_bank" in path for path in names)
            if present:
                doc = json.loads(package.read(asset))
                assert doc["contentStatus"] == "DRAFT_NEEDS_TEACHER_REVIEW"
                assert len(doc["questions"]) == 6
                assert all(q["reviewStatus"] == "draft_needs_teacher_review" and
                           not q["officialExamVerified"] for q in doc["questions"])
            apks[variant] = {"path": str(apk), "sha256": hashlib.sha256(apk.read_bytes()).hexdigest(),
                             "bytes": apk.stat().st_size, "pilot_asset_present": present,
                             "asset_paths": [p for p in names if p.startswith("assets/")]}
    debug = suites(args.evidence / "unit-debug")
    release = suites(args.evidence / "unit-release")
    for result in (debug, release):
        assert result["tests"] >= 358 and result["failures"] == result["errors"] == 0
        assert result["skipped"] == 0
    runtime = suites(args.evidence / "host-runtime")
    release_runtime = suites(args.evidence / "release-host")
    rendered = suites(args.evidence / "compose-render")
    for result, count in ((runtime, 5), (release_runtime, 1), (rendered, 100)):
        assert result["tests"] == count and result["failures"] == result["errors"] == result["skipped"] == 0
    lint = {}
    for variant in ("debug", "release"):
        issues = ET.parse(args.build / "reports" / f"lint-results-{variant}.xml").getroot().findall("issue")
        counts = {level: sum(issue.get("severity") == level for issue in issues)
                  for level in ("Fatal", "Error", "Warning", "Information")}
        assert counts["Fatal"] == counts["Error"] == 0
        lint[variant] = counts
    previous_badging = (args.evidence / "previous-badging.txt").read_text(encoding="utf-8-sig")
    final_badging = (args.evidence / "final-badging.txt").read_text(encoding="utf-8-sig")
    def code(text):
        return int(re.search(r"versionCode='(\d+)'", text)[1])
    def package(text):
        return re.search(r"package: name='([^']+)'", text)[1]
    def certificate(name):
        text = (args.evidence / name).read_text(encoding="utf-8-sig")
        assert text.startswith("Verifies")
        return re.search(r"certificate SHA-256 digest: ([a-f0-9]+)", text)[1]
    fingerprint = certificate("final-signature.txt")
    assert fingerprint == certificate("previous-signature.txt")
    assert package(previous_badging) == package(final_badging) == "com.ahmadmol.enmath"
    assert code(final_badging) > code(previous_badging)
    pilot_a = root / "app/src/debug/assets/content/g9/nature_numbers_pilot.json"
    pilot_b = root / "app/src/w1Qa/assets/content/g9/nature_numbers_pilot.json"
    assert pilot_a.read_bytes() == pilot_b.read_bytes()
    result = {"apk": apks, "unit_debug": debug, "unit_release": release,
              "host_runtime": runtime, "release_host_runtime": release_runtime,
              "compose_renders": rendered, "lint": lint,
              "upgrade": {"previous_version_code": code(previous_badging), "version_code": code(final_badging),
                          "same_certificate": True, "certificate_sha256": fingerprint,
                          "scope": "Local APK pair only; handset update NOT_RUN; Release unsigned"},
              "content_review": "NOT_APPROVED; release pilot absent; no publication authorized",
              "device_execution": "NOT_RUN"}
    args.evidence.mkdir(parents=True, exist_ok=True)
    (args.evidence / "artifact-evidence.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"status": "PASS", "debug": debug, "release": release,
                      "sha256": apks["debug"]["sha256"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
