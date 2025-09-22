import argparse


class BorderTest:

    def main(self):
        parser = argparse.ArgumentParser(prog="border_test", description="Simulator border test")
        args = parser.parse_args()


if __name__ == '__main__':
    bt = BorderTest()
    bt.main()
