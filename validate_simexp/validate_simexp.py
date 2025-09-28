import argparse
import csv

class ValidateSimexp:

    def _read_csv_file(self, csv_file):
        fieldnames = ["Generation","Reward","Values"]
        reader = csv.DictReader(csv_file, fieldnames=fieldnames, delimiter=";")
        next(reader) # skip header
        result = []
        for row in reader:
            generation = int(row['Generation'])
            reward = row['Reward']
            values = row['Values']
            result.append({"Generation": generation, "Reward": reward, "Values": values})
        return result

    def main(self):
        parser = argparse.ArgumentParser(prog="validate_simexp", description="Validates SimExp results")
        parser.add_argument('infile', type=argparse.FileType('r'))
        args = parser.parse_args()

        entries = self._read_csv_file(args.infile)
        for entry in entries:
            print("generation: %d -> %s" % (entry["Generation"], entry["Reward"]))


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()

