import argparse

class ValidateSimexp:
    def main(self):
        parser = argparse.ArgumentParser(prog="validate_simexp", description="Validates SimExp results")
        #parser.add_argument('infile', type=argparse.FileType('r'))
        args = parser.parse_args()


if __name__ == '__main__':
    v = ValidateSimexp()
    v.main()

