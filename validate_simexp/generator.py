import json
import datetime
from pathlib import Path

from input_type import InputType
from score_builder import ScoreBuilder
from strategy import Strategy
from grouper import group_entries


class DateTimeEncoder(json.JSONEncoder):
    # Override the default method
    def default(self, obj):
        if isinstance(obj, (datetime.date, datetime.datetime)):
            return obj.isoformat()


class Generator:
    def __init__(self):
        pass

    def generate(self, file_type: InputType, infile: Path, strategy: Strategy, seed: int, no_validation: bool,
                 count: int, result: Path | None) -> None:
        entries = None
        match file_type:
            case InputType.JSON:
                entries = file_type.load(infile)
            case InputType.CSV:
                entries = file_type.load(infile)

        score_builder = ScoreBuilder()
        score_entries = score_builder.build_scores(entries, strategy, seed, no_validation, count)

        if result:
            print("Generate result file: %s" % result)
            result_content = self._generate_result(strategy, entries, score_entries)
            self._write_result(result, result_content)

    def _generate_result(self, strategy: Strategy, entries, score_entries):
        groups = group_entries(entries)
        grouped_entries = []
        last_group = None
        last_reward = None
        for i, entry in enumerate(groups.items()):
            current_reward = None
            group, group_generations = entry
            group_generation_entries = []
            for generation in group_generations:
                group_generation_entries.append({
                    "generation": generation["Generation"],
                    "reward": generation["Reward"],
                })
                if current_reward is None:
                    current_reward = generation["Reward"]

            m_ordenal_r = None
            m_ordenal_s = None
            if last_group:
                m_ordenal_r = 0 if current_reward >= last_reward else 1

                last_score = score_entries[last_group]["average_score"]
                current_score = score_entries[group]["average_score"]
                m_ordenal_s = 0 if current_score >= last_score else 1
            else:
                last_group = group
                last_reward = current_reward

            grouped_entries.append({
                "optimizable values": {name: value for name, value in group_generations[0]["Values"].items()},
                "score": {
                    "average": score_entries[group]["average_score"],
                    "scores": score_entries[group]["scores"],
                },
            "mordenal": {
                    "reward": m_ordenal_r,
                    "score": m_ordenal_s,
                },
                "entries": group_generation_entries,
            })

        now = datetime.datetime.now(datetime.timezone.utc)
        now = now.astimezone()
        result = {
            'strategy': strategy,
            'date': now,
            'groups': grouped_entries,
        }
        return result

    def _write_result(self, result_file: Path, result_content):
        with result_file.open("w", encoding="utf-8") as f:
            json.dump(result_content, f, indent=2, cls=DateTimeEncoder)
