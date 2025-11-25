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

    def build_scores(self, entries, strategy, seed):
        groups = self._group_entries(entries)

        scores = []
        for group in groups.values():
            values = group[0]["Values"]
            score = self._simulator.simulate(strategy, values, seed)
            scores.extend([score] * len(group))
        return scores
