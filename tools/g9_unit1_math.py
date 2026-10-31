"""Exact editorial checks for Unit 1. Not an Android grader or publication authority."""
from fractions import Fraction
from math import gcd, isqrt

CLASSIFICATIONS = ("integer", "terminating", "recurring", "irrational")

def integer(spec, key, positive=False):
    value = spec[key]
    if type(value) is not int or abs(value) > 1000000 or (positive and value <= 0):
        raise ValueError("Expected a bounded integer: " + key)
    return value

def radical(n):
    if n < 0: raise ValueError("Real principal square root requires n >= 0")
    if n == 0: return (0, 1)
    factor = max(d for d in range(1, isqrt(n) + 1) if n % (d*d) == 0)
    return factor, n // (factor*factor)

def result(spec):
    kind = spec["operation"]
    if kind == "classification":
        if "radicand" in spec:
            n = integer(spec, "radicand")
            if n < 0: raise ValueError("Negative real radicand")
            return "integer" if isqrt(n)**2 == n else "irrational"
        f = Fraction(integer(spec, "numerator"), integer(spec, "denominator", True))
        if f.denominator == 1: return "integer"
        d = f.denominator
        for prime in (2, 5):
            while d % prime == 0: d //= prime
        return "terminating" if d == 1 else "recurring"
    if kind == "gcd": return str(gcd(integer(spec, "a", True), integer(spec, "b", True)))
    if kind == "reduced_fraction":
        return str(Fraction(integer(spec, "numerator", True), integer(spec, "denominator", True)))
    if kind == "sqrt":
        n = integer(spec, "radicand")
        if n < 0 or isqrt(n)**2 != n: raise ValueError("Exact rational root required")
        return str(isqrt(n))
    if kind == "sqrt_square": return str(abs(integer(spec, "value")))
    if kind == "radical":
        a,b = radical(integer(spec, "radicand")); return [str(a), b]
    if kind == "combine_radicals":
        a,b = radical(integer(spec, "left")); c,d = radical(integer(spec, "right"))
        if b != d: raise ValueError("Only like radical terms supported")
        return [str(a + integer(spec, "rightSign")*c), b] if spec["rightSign"] in (-1,1) else None
    if kind == "rationalize":
        n = integer(spec, "numerator", True); d = integer(spec, "radicand", True)
        a,b = radical(d); return [str(Fraction(n, a*b)), b]
    raise ValueError("Unsupported Unit 1 verification")

def canonical(value):
    if isinstance(value, list):
        if len(value) != 2 or type(value[1]) is not int or not 0 <= value[1] <= 1000000:
            raise ValueError("Invalid radical option")
        a,b = radical(value[1])
        coefficient = Fraction(value[0])*a
        return (coefficient, b if coefficient else 1)
    if isinstance(value, str) and value in CLASSIFICATIONS: return value
    raise ValueError("Invalid choice value")

def validate_item(item):
    try:
        expected = result(item["verification"])
        if expected is None: return ["Invalid operation parameters"]
        if item.get("kind") == "choice":
            matches = [o["id"] for o in item["options"] if canonical(o.get("value")) == canonical(expected)]
            if matches != [item["answer"]]: return ["Exactly one option must match the independent result"]
            values = [canonical(o.get("value")) for o in item["options"]]
            if len(set(values)) != len(values): return ["Duplicate mathematical distractors"]
        elif item.get("kind") in ("exact_number", "reduced_fraction"):
            if Fraction(item["answer"]) != Fraction(expected): return ["Answer differs from Unit 1 mathematics"]
        elif item.get("verifiedResult") != expected:
            return ["Worked example differs from Unit 1 mathematics"]
        return []
    except (KeyError, TypeError, ValueError, ZeroDivisionError):
        return ["Missing/invalid Unit 1 verification"]
