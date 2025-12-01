import math

def calculate_required_samples(confidence_percent, epsilon):
    """
    Computes the number of samples needed so that BOTH:
      min(X) ≤ ε
      max(X) ≥ 1 - ε
    hold with probability ≥ confidence_percent.

    Uses the exact order-statistics formulas for Uniform(0,1).
    """
    p = confidence_percent / 100.0

    # We want: P(min ≤ ε AND max ≥ 1 - ε) ≥ p
    # For uniform(0,1):
    #   P(min > ε) = (1 - ε)^n
    #   P(max < 1 - ε) = (1 - ε)^n  (symmetry)
    # Using union bound (safe and simple):
    #   1 − 2(1 − ε)^n ≥ p
    # → (1 − ε)^n ≤ (1 − p)/2
    # → n ≥ log((1 − p)/2) / log(1 − ε)

    rhs = (1 - p) / 2
    if rhs <= 0:
        raise ValueError("Confidence must be < 100%.")

    n = math.log(rhs) / math.log(1 - epsilon)
    return math.ceil(n)
