from simulator import Simulator


class ScoreBuilder:
    def __init__(self):
        self._simulator = Simulator()

    def build_scores(self, entries, strategy, seed):
        scores = []
        for entry in entries:
            score = self._simulator.simulate(strategy, entry["Values"], seed)
            scores.append(score)
        return scores
