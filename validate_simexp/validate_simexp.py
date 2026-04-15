import argparse
from pathlib import Path


from input_type import InputType
from strategy import Strategy
from generator import Generator
from show import Show


def validate_file_exists(f) -> Path:
    path = Path(f)
    if not path.exists():
        raise argparse.ArgumentTypeError("{0} does not exist".format(f))
    return path


class ValidateSimexp:
    def _generate(self, args):
        file_type = InputType[args.type.upper()]
        generator = Generator()
        entries, score_entries = generator.generate(file_type, args.infile, args.strategy, args.seed,
                                                    args.no_validation, args.count, args.result)

        show = Show()
        show.show_generations(entries, score_entries)

    def main(self):
        parser = argparse.ArgumentParser(prog="validate_simexp", description="Validates SimExp results")
        default = ' (default: %(default)s)'

        subparsers = parser.add_subparsers(required=True, dest="subcommand", title='subcommands',
                                           description='valid subcommands', help='sub-command help')

        parser_generate = subparsers.add_parser('generate', help="generate ")
        parser_generate.add_argument('infile', type=validate_file_exists)
        parser_generate.add_argument('-r', '--result', type=Path, help="result json file")
        parser_generate.add_argument('--seed', type=int, help="simulator seed")
        parser_generate.add_argument('--count', type=int, default=30, help="amount of simulations to run" + default)
        parser_generate.add_argument('--no_validation', action='store_true', help="disable range validation")
        parser_generate.add_argument('-t', '--type',
                                     choices=[type.type.lower() for type in InputType],
                                     default=InputType.JSON.name.lower(), help="select input file type" + default)
        parser_generate.add_argument('-s', '--strategy', required=True,
                                     choices=[strategy.name for strategy in Strategy],
                                     help="strategy to execute")
        parser_generate.set_defaults(func=self._generate)

        args = parser.parse_args()
        args.func(args)


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()
