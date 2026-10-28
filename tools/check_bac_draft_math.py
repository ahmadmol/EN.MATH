"""Independent exact arithmetic review of the 28 ORIGINAL drafts, outside the app/test banks.

This checks proposed numeric values, not educator approval, wording, diagrams or textbook answers.
"""
import csv
import hashlib
import json
from fractions import Fraction as F
from math import comb, factorial, isqrt
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIR = ROOT / 'docs/curriculum/bac'
raw = (DIR / 'source/BAC_ORIGINAL_PRACTICE_DRAFTS.json').read_bytes()
digest = hashlib.sha256(raw).hexdigest()
assert digest == '3493bfb2748ccea8272c2ded0848982726b18a6b179ed20bd5d022387e75d4ed', 'Draft source changed; review derivations again'
items = json.loads(raw)['items']
# Derivations from the actual prompts, independently of their proposed answers.
derivations = {
    'bac-1-u01': [(2 + (6 - 1) * 3, 'u_n=u_1+(n-1)d'), (sum(range(1, 6)), 'finite integer sum')],
    'bac-1-u02': [(0, 'reciprocal tends to zero at positive infinity'), (2 + 2, 'cancel x-2 for x!=2, evaluate x+2')],
    'bac-1-u03': [(3 * 2**2 + 2, 'derivative of x^3+2x is 3x^2+2'), (F(4, 2), 'vertex -b/(2a), a>0')],
    'bac-1-u04': [(0, 'reciprocal sequence tends to zero'), (F(2, 1), 'ratio of leading coefficients')],
    'bac-1-u05': [(3, 'ln(exp(t))=t for real t'), (2 + 3, 'positive product logarithm identity')],
    'bac-1-u06': [(1, 'nonzero base to zero power'), (7, 'exp(ln(t))=t for t>0')],
    'bac-1-u07': [(2**3 - 0**3, 'integrate 3x^2 to x^3'), (3**2 - 0**2, 'integrate 2x to x^2')],
    'bac-2-u01': [(isqrt((4-1)**2 + (6-2)**2 + (3-3)**2), 'Euclidean distance, radicand 25'), (F(0+2, 2), 'midpoint first coordinate')],
    'bac-2-u02': [(1*2 + 2*(-1) + 3*0, 'three-coordinate scalar product'), (2*3 + 0*4 + 1*(-2), 'three-coordinate scalar product')],
    'bac-2-u03': [(1 + 2*2, 'parametric first coordinate at t=2'), (3, 'intersection fixes third coordinate at 3')],
    'bac-2-u04': [(isqrt(3**2 + 4**2), 'complex modulus, radicand 25'), (2**2 + 1**2, 'product of complex conjugates')],
    'bac-2-u05': [(1, 'quarter turn maps (x,y) to (-y,x)'), (3 + (-2), 'translation adds second coordinates')],
    'bac-2-u06': [(comb(5, 2), 'unordered selection of two from five'), (factorial(4), 'factorial definition')],
    'bac-2-u07': [(F(1, 4) / F(1, 2), 'conditional probability, denominator nonzero'), (comb(2, 1)*F(1, 2)*F(1, 2), 'binomial mass n=2,k=1,p=1/2')],
}
results = []
for item in items:
    number = int(item['id'].rsplit('-', 1)[1])
    result, method = derivations[item['source_unit_id']][number - 1]
    expected = F(result); proposed = F(item['correct_answer'])
    assert proposed == expected, item['id']
    assert not item['grants_gems'] and not item['mastery_countable'] and item['lesson_id'] is None
    results.append(dict(id=item['id'], unitId=item['source_unit_id'], independentValue=str(expected), proposedValue=item['correct_answer'],
                        numericalProposalCheck='PASS', method=method, educatorReview='PENDING', wordingReview='PENDING',
                        figureReview='UNKNOWN', checkerApproval='PENDING', publication=item['publication_status'], grantsGems=False))
assert len(results) == 28
with (DIR / 'DRAFT_MATH_CHECK.csv').open('w', encoding='utf-8', newline='') as handle:
    writer = csv.DictWriter(handle, fieldnames=list(results[0])); writer.writeheader(); writer.writerows(results)
print(json.dumps(dict(draftSourceSha256=digest, numericalProposalsChecked=28, numericalMismatches=0,
                     educatorApproved=0, published=0, officialTextbookAnswerKeysChecked=0)))
