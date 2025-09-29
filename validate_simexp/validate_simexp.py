import argparse
from enum import Enum

from input_type import InputType


class Strategy(Enum):
    EAStrategy1a = "EAStrategy1a"
    EAStrategy1b = "EAStrategy1b"
    EAStrategy1c = "EAStrategy1c"


class ValidateSimexp:

    def main(self):
        parser = argparse.ArgumentParser(prog="validate_simexp", description="Validates SimExp results")
        default = ' (default: %(default)s)'
        parser.add_argument('infile', type=argparse.FileType('r'))
        parser.add_argument('-t', '--type',
                                 choices=[type.type.lower() for type in InputType],
                                 default=InputType.JSON.name.lower(), help="select input file type" + default)
        parser.add_argument('-s', '--strategy', required=True,
                            choices=[strategy.name.lower() for strategy in Strategy],
                            help="strategy to execute")
        args = parser.parse_args()

        entries = None
        file_type = InputType[args.type.upper()]
        match file_type:
            case InputType.JSON:
                entries = file_type.load(args.infile)
            case InputType.CSV:
                entries = file_type.load(args.infile)

        for entry in entries:
            print("generation: %d -> %s (%s)" % (entry["Generation"], entry["Reward"], ",".join(["%s:%s" % (k, v) for k, v in entry["Values"].items()])))


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()

