import copy
import json
import unittest
from pathlib import Path
from fractions import Fraction
from math import gcd
from g9_content_workflow import validate
from g9_unit1_math import result

PACK=Path(__file__).resolve().parents[1]/'docs/curriculum/g9/authoring/unit1'
class Unit1Tests(unittest.TestCase):
    def test_equivalent_radical_distractors_are_rejected(self):
        from g9_unit1_math import validate_item
        q=dict(kind='choice',verification=dict(operation='radical',radicand=8),answer='a',
            options=[dict(id='a',value=['2',2]),dict(id='b',value=['1',8])])
        self.assertTrue(validate_item(q))
        q['options'][1]['value']=['3',2]
        self.assertEqual([],validate_item(q))
    def test_every_authored_item_and_release_gate(self):
        docs=[json.loads(p.read_bytes()) for p in PACK.glob('*.json')]
        self.assertEqual(4,len(docs))
        for d in docs:
            self.assertEqual([],validate(d),d['source']['lessonId'])
            self.assertTrue(validate(d,True))
            self.assertEqual('DRAFT_ONLY',d['publication'])
            for q in d['questions']+[d['checkpoint']]:
                with self.subTest(question=q['id']):
                    wrong=copy.deepcopy(d)
                    item=next(x for x in wrong['questions']+[wrong['checkpoint']] if x['id']==q['id'])
                    item['answer']='impossible'
                    self.assertTrue(validate(wrong))
    def test_gcd_matches_exhaustive_divisors(self):
        for a in range(1,60):
            for b in range(1,60):
                brute=max(d for d in range(1,min(a,b)+1) if a%d==0 and b%d==0)
                self.assertEqual(str(brute),result(dict(operation='gcd',a=a,b=b)))
    def test_classification_matches_decimal_remainder_cycles(self):
        for n in range(-10,11):
            for d in range(1,50):
                remainder=n%d;seen=set()
                while remainder and remainder not in seen:
                    seen.add(remainder);remainder=(10*remainder)%d
                category='integer' if n%d==0 else 'terminating' if remainder==0 else 'recurring'
                self.assertEqual(category,result(dict(operation='classification',numerator=n,denominator=d)))
    def test_radical_equivalence_and_squarefree_remainder(self):
        for n in range(1,500):
            a,b=result(dict(operation='radical',radicand=n));a=Fraction(a)
            self.assertEqual(n,a*a*b)
            self.assertFalse(any(b%(d*d)==0 for d in range(2,b+1)))
    def test_rationalization_preserves_value_and_sign(self):
        for n in range(1,15):
            for d in range(1,100):
                a,b=result(dict(operation='rationalize',numerator=n,radicand=d));a=Fraction(a)
                self.assertGreater(a,0);self.assertEqual(Fraction(n*n,d),a*a*b)
    def test_invalid_domain_and_bounds(self):
        for s in [dict(operation='sqrt',radicand=-1),dict(operation='sqrt',radicand=2),dict(operation='gcd',a=0,b=5),dict(operation='rationalize',numerator=1,radicand=0)]:
            with self.assertRaises(ValueError):result(s)

if __name__=='__main__':unittest.main(verbosity=2)
