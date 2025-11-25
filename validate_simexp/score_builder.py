from simulator import Simulator


class ScoreBuilder:
    def __init__(self):
        pass

    def build_scores(self, entries, strategy, seed):
        scores = []
        simulator = Simulator()
        for entry in entries:
            score = simulator.simulate(strategy, entry["Values"], seed)
            scores.append(score)
        return scores
