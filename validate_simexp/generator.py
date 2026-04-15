import json
import datetime
from pathlib import Path

from input_type import InputType
from score_builder import ScoreBuilder
from strategy import Strategy


class DateTimeEncoder(json.JSONEncoder):
    # Override the default method
    def default(self, obj):
        if isinstance(obj, (datetime.date, datetime.datetime)):
            return obj.isoformat()


class Generator:
    def __init__(self):
        pass

    def generate(self, file_type: InputType, infile: Path, strategy: Strategy, seed: int, no_validation: bool,
                 count: int, result: Path | None):
        entries = None
        match file_type:
            case InputType.JSON:
                entries = file_type.load(infile)
            case InputType.CSV:
                entries = file_type.load(infile)

        score_builder = ScoreBuilder()
        score_entries = score_builder.build_scores(entries, strategy, seed, no_validation, count)

        if result:
            self._write_result(result, strategy, entries, score_entries)

        return entries, score_entries

    def _write_result(self, result_file: Path, strategy: Strategy, entries, score_entries):
        groups = self.group_entries(entries)
        group_entries = []
        for i, entry in enumerate(groups.items()):
            group, group_generations = entry
            group_generation_entries = []
            for generation in group_generations:
                group_generation_entries.append({
                    "generation": generation["Generation"],
                    "reward": generation["Reward"],
                })
            group_entries.append({
                "optimizable values": {name: value for name, value in group_generations[0]["Values"].items()},
                "score": {
                    "average": score_entries[group]["average_score"],
                    "scores": score_entries[group]["scores"],
                },
                "entries": group_generation_entries,
            })

        result = {
            'strategy': strategy,
            'date': datetime.datetime.now(datetime.timezone.utc),
            'groups': group_entries,
        }

        with result_file.open("w", encoding="utf-8") as f:
            json.dump(result, f, indent=2, cls=DateTimeEncoder)

    def group_entries(self, entries):
        groups = {}
        for item in entries:
            key = str(item['Values'])
            groups.setdefault(key, []).append(item)
        return groups

