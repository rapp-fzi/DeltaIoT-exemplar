import argparse
import json
import datetime
from pathlib import Path

from tabulate import tabulate, SEPARATING_LINE

from input_type import InputType
from strategy import Strategy
from score_builder import ScoreBuilder


class DateTimeEncoder(json.JSONEncoder):
    # Override the default method
    def default(self, obj):
        if isinstance(obj, (datetime.date, datetime.datetime)):
            return obj.isoformat()


def validate_file_exists(f) -> Path:
    path = Path(f)
    if not path.exists():
        raise argparse.ArgumentTypeError("{0} does not exist".format(f))
    return path


def as_path(f) -> Path:
    return Path(f)


class ValidateSimexp:

    def _write_result(self, args, generations):
        result = {
            'strategy': args.strategy,
            'date': datetime.datetime.now(datetime.timezone.utc),
            'generations': generations,
        }

        with args.result.open("w", encoding="utf-8") as f:
            json.dump(result, f, indent=2, cls=DateTimeEncoder)

    def group_entries(self, entries):
        groups = {}
        for item in entries:
            key = str(item['values'])
            groups.setdefault(key, []).append(item)
        return groups

    def _process_generations(self, generations):
        table_entries = []
        groups = self.group_entries(generations)
        for i, group in enumerate(groups.values()):
            for generation in group[:-1]:
                table_entries.append([generation["number"], generation["reward"], None])
            generation = group[-1]
            score = generation["score"]
            table_entries.append([generation["number"], generation["reward"], score])
            if i < len(groups.values()) - 1:
                table_entries.append(SEPARATING_LINE)

        table_str = tabulate(table_entries,
                             headers=['Generation', 'Reward', 'Average Score'],
                             tablefmt="simple"
                             )
        print(table_str)

    def main(self):
        parser = argparse.ArgumentParser(prog="validate_simexp", description="Validates SimExp results")
        default = ' (default: %(default)s)'
        parser.add_argument('infile', type=validate_file_exists)
        parser.add_argument('-r', '--result', type=as_path, help="result json file")
        parser.add_argument('--seed', type=int, help="simulator seed")
        parser.add_argument('--count', type=int, default=30, help="amount of simulations to run" + default)
        parser.add_argument('-t', '--type',
                                 choices=[type.type.lower() for type in InputType],
                                 default=InputType.JSON.name.lower(), help="select input file type" + default)
        parser.add_argument('-s', '--strategy', required=True,
                            choices=[strategy.name for strategy in Strategy],
                            help="strategy to execute")
        args = parser.parse_args()

        entries = None
        file_type = InputType[args.type.upper()]
        match file_type:
            case InputType.JSON:
                entries = file_type.load(args.infile)
            case InputType.CSV:
                entries = file_type.load(args.infile)

        score_builder = ScoreBuilder()
        scores = score_builder.build_scores(entries, args.strategy, args.seed, args.count)
        generations = []
        for i, entry in enumerate(entries):
            score = scores[i]
            generation = {
                'number': entry["Generation"],
                'values': entry["Values"],
                'reward': entry["Reward"],
                'score': score,
            }
            generations.append(generation)

        if args.result:
            self._write_result(args, generations)

        self._process_generations(generations)


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()

