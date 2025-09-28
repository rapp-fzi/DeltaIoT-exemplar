import argparse
import csv
import json

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
        parser.add_argument('infile', type=argparse.FileType('r'))
        args = parser.parse_args()

        #entries = self._read_csv_file(args.infile)
        entries = self._read_json_file(args.infile)
        for entry in entries:
            print("generation: %d -> %s (%s)" % (entry["Generation"], entry["Reward"], ",".join(["%s:%s" % (k, v) for k, v in entry["Values"].items()])))


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()

