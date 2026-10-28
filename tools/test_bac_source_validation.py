"""Host-only regression tests: narrowly allow verified sharing and retain source defects."""
import contextlib
import csv
import importlib.util
import io
import json
from pathlib import Path
import shutil
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
spec = importlib.util.spec_from_file_location('bac_importer', ROOT / 'tools/import_bac_source.py')
importer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(importer)


class SourceValidationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix='enmath-source-regression-')
        self.directory = Path(self.temp.name)
        shutil.copytree(ROOT / 'docs/curriculum/bac/source', self.directory / 'source')
        shutil.copy2(ROOT / 'docs/curriculum/bac/AUDIT_AND_CROSSWALK.json', self.directory)
        importer.DIR = self.directory
        importer.SRC = self.directory / 'source'
        importer.OUT = self.directory / 'BacSourceData.kt'

    def tearDown(self):
        self.temp.cleanup()

    def run_import(self):
        with contextlib.redirect_stdout(io.StringIO()):
            importer.import_source()
        return json.loads((self.directory / 'SOURCE_VALIDATION.json').read_text(encoding='utf-8'))

    def extend_lesson(self, lid, printed_end, pdf_end):
        file = importer.SRC / 'BAC_CURRICULUM_CATALOG.json'
        data = json.loads(file.read_bytes())
        lesson = next(l for u in data['units'] for l in u['lessons'] if l['id'] == lid)
        lesson.update(source_page_end=printed_end, pdf_page_end=pdf_end)
        file.write_text(json.dumps(data, ensure_ascii=False), encoding='utf-8')
        file = importer.SRC / 'BAC_LESSONS.csv'
        rows = list(csv.DictReader(io.StringIO(file.read_text(encoding='utf-8-sig'))))
        row = next(r for r in rows if r['lesson_id'] == lid)
        row.update(print_end=str(printed_end), pdf_end=str(pdf_end))
        file.write_text(importer.csv_text(rows), encoding='utf-8')

    def test_verified_shared_page_is_information_not_an_overlap_error(self):
        result = self.run_import()
        self.assertEqual(1, len(result['verifiedSharedPages']))
        self.assertEqual(164, result['verifiedSharedPages'][0]['pdfPage'])
        self.assertFalse(any(i['code'] == 'OVERLAPPING_LESSON_RANGE' for i in result['issues']))
        self.assertEqual('DETAILED_BOUNDARY_REVIEW_PENDING', result['remainingSourceReviews'][0]['status'])

    def test_widened_overlap_is_not_hidden_by_verified_pair(self):
        self.extend_lesson('bac-1-u05-l04', 167, 168)
        result = self.run_import()
        self.assertEqual([], result['verifiedSharedPages'])
        self.assertTrue(any(i['code'] == 'OVERLAPPING_LESSON_RANGE' for i in result['issues']))

    def test_other_overlap_is_still_reported(self):
        self.extend_lesson('bac-1-u05-l03', 163, 164)
        result = self.run_import()
        self.assertTrue(any(i['code'] == 'OVERLAPPING_LESSON_RANGE' and 'bac-1-u05-l03' in i['ids'] for i in result['issues']))

    def test_four_stale_raw_anchors_are_reported_and_regenerated(self):
        result = self.run_import()
        self.assertEqual(4, result['issueCount'])
        self.assertEqual(4, result['regeneratedAnchorCorrections'])
        rows = list(csv.DictReader(io.StringIO((self.directory / 'PAGE_INVENTORY_REGENERATED.csv').read_text(encoding='utf-8'))))
        for page in range(165, 169):
            row = next(r for r in rows if r['book'] == '1' and int(r['pdf_page']) == page)
            self.assertEqual('bac-1-u05-l05', row['section_ids'])
            self.assertIn('bac-1-u05-l04', row['raw_section_ids'])
            self.assertEqual('False', row['manual_page_content_reviewed'])

    def test_output_is_deterministic_and_check_detects_stale_generation(self):
        self.run_import()
        with contextlib.redirect_stdout(io.StringIO()):
            importer.import_source(check=True)
        importer.OUT.write_text('// stale', encoding='utf-8')
        with self.assertRaisesRegex(AssertionError, 'Stale output'):
            importer.import_source(check=True)

    def test_duplicate_pdf_page_is_rejected(self):
        file = importer.SRC / 'BAC_PAGE_INVENTORY.csv'
        rows = list(csv.DictReader(io.StringIO(file.read_text(encoding='utf-8-sig'))))
        rows[1]['pdf_page'] = rows[0]['pdf_page']
        rows[1]['printed_page'] = rows[0]['printed_page']
        file.write_text(importer.csv_text(rows), encoding='utf-8')
        with self.assertRaises(AssertionError):
            self.run_import()


if __name__ == '__main__':
    unittest.main()
