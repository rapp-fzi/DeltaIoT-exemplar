
class Normalizer:
    def __init__(self, lower, upper):
        self._lower = lower
        self._upper = upper

    def normalize(self, value: float) -> float:
        return self._normalize(value, self._lower, self._upper)

    def _normalize(self, value: float, lower: float, upper: float) -> float:
        if value > upper:
            return 0

        if value < lower:
            return 1

        return (1 / (upper - lower)) * (upper - value)
