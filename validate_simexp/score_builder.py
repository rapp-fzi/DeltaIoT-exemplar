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

    def _build_score(self, strategy, seed, no_validation, values, count):
        scores = []
        for i in range(count):
            score = self._simulator.simulate(strategy, values, seed, no_validation)
            scores.append(score)
        return scores

    def build_scores(self, entries, strategy, seed, no_validation, count):
        groups = self._group_entries(entries)

        score_entries = {}
        for group, value in groups.items():
            values = value[0]["Values"]
            scores = self._build_score(strategy, seed, no_validation, values, count)
            average_score = sum(scores) / len(scores)
            score_entries[group] = {
                "average_score": average_score,
                "scores": scores,
            }
        return score_entries
