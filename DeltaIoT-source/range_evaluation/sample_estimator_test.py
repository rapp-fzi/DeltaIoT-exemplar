import pytest

from sample_estimator import calculate_required_samples


def test_required_samples_basic_95():
    n = calculate_required_samples(95, 0.01)

    assert isinstance(n, int), "Output should be an integer"
    assert n > 0, "Number of samples must be positive"


def test_required_samples_basic_99():
    n = calculate_required_samples(99, 0.001)

    assert isinstance(n, int)
    assert n > 0


def test_required_samples_100():
    with pytest.raises(ValueError):
        calculate_required_samples(100, 0.01)


def test_required_samples_110():
    with pytest.raises(ValueError):
        calculate_required_samples(110, 0.01)


def test_required_samples_0():
    with pytest.raises(ValueError):
        calculate_required_samples(0, 0.01)


def test_required_samples_monotonicity_epsilon():
    n1 = calculate_required_samples(95, 0.01)
    n2 = calculate_required_samples(95, 0.02)
    assert n2 < n1, "Higher epsilon should reduce required samples"


def test_required_samples_monotonicity_confidence():
    n_low = calculate_required_samples(90, 0.01)
    n_high = calculate_required_samples(99, 0.01)
    assert n_high > n_low, "Higher confidence requires more samples"
