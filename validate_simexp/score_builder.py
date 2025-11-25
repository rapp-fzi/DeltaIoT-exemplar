from simulator import Simulator


class ScoreBuilder:
    def __init__(self):
        self._simulator = Simulator()

    def _group_entries(self, entries):
        groups = {}
        for item in entries:
            key = str(item['Values'])
            groups.setdefault(key, []).append(item)
        return groups

    def _build_score(self, strategy, seed, values, count):
        scores = []
        for i in range(count):
            score = self._simulator.simulate(strategy, values, seed)
            scores.append(score)
        average_score = sum(scores) / len(scores)
        return average_score

    def build_scores(self, entries, strategy, seed, count):
        groups = self._group_entries(entries)

        scores = []
        for group in groups.values():
            values = group[0]["Values"]
            score = self._build_score(strategy, seed, values, count)
            scores.extend([score] * len(group))
        return scores
