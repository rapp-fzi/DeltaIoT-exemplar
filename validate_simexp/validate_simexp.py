import argparse
import csv
import json
from enum import Enum


class InputType(Enum):
    CSV = "csv"
    JSON = "json"


class ValidateSimexp:
    def _read_csv_file(self, csv_file):
        fieldnames = ["Generation", "Reward", "Values"]
        reader = csv.DictReader(csv_file, fieldnames=fieldnames, delimiter=";")
        next(reader) # skip header
        result = []
        for row in reader:
            generation = int(row['Generation'])
            reward = row['Reward']
            raw_values = row['Values'].split(",")
            values = {}
            for raw_value in raw_values:
                name, val = raw_value.split(":")
                values[name] = val.strip()
            result.append({"Generation": generation, "Reward": reward, "Values": values})
        return result

    def _read_json_file(self, json_file):
        content = json.load(json_file)
        result = []
        for entry in content:
            generation = int(entry['generation'])
            reward = entry['reward']
            values = {name: value for name, value in entry['optimizables'].items()}
            result.append({"Generation": generation, "Reward": reward, "Values": values})
        return result

    def main(self):
        parser = argparse.ArgumentParser(prog="validate_simexp", description="Validates SimExp results")
        default = ' (default: %(default)s)'
        parser.add_argument('infile', type=argparse.FileType('r'))
        parser.add_argument('-t', '--type',
                                 choices=[type.name.lower() for type in InputType],
                                 default=InputType.JSON.name.lower(), help="select input file type" + default)
        args = parser.parse_args()

        entries = None
        file_type = InputType[args.type.upper()]
        match file_type:
            case InputType.JSON:
                entries = self._read_json_file(args.infile)
            case InputType.CSV:
                entries = self._read_csv_file(args.infile)

        for entry in entries:
            print("generation: %d -> %s (%s)" % (entry["Generation"], entry["Reward"], ",".join(["%s:%s" % (k, v) for k, v in entry["Values"].items()])))


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()

