from pathlib import Path
import json

from tabulate import tabulate, SEPARATING_LINE

from grouper import group_entries


class Show:
    def show_result(self, result_file: Path) -> None:
        table_entries = []
        content = self._load_results(result_file)
        print("Results of: %s" % content["strategy"])
        groups = content["groups"]
        for i, group in enumerate(groups):
            average_score = group["score"]["average"]
            for index, entry in enumerate(group["entries"]):
                score = None
                if index == len(group["entries"]) - 1:
                    score = average_score
                table_entries.append([entry["generation"], entry["reward"], score])
            if i < len(groups) - 1:
                table_entries.append(SEPARATING_LINE)

        table_str = tabulate(table_entries,
                             headers=['Generation', 'Reward', 'Average Score'],
                             tablefmt="simple"
                             )
        print(table_str)

    def _load_results(self, result_file: Path) -> list:
        with result_file.open("r", encoding="utf-8") as f:
            content = json.load(f)
            return content
