from simulator import Simulator


class ScoreBuilder:
    def __init__(self):
        pass

    def build_scores(self, entries, args):
        scores = []
        simulator = Simulator()
        for entry in entries:
            score = simulator.simulate(args.strategy, entry["Values"], args.seed)
            generation = {
                'number': entry["Generation"],
                'reward': entry["Reward"],
                'score': score,
            }
            scores.append(score)
        return scores
