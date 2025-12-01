import argparse
import subprocess
from pathlib import Path
import shutil
import tempfile
import json
import statistics

from tabulate import tabulate

from sample_estimator import calculate_required_samples


class RangeEvaluator:
    BINARY_JAVA = "java"

    def __init__(self):
        script_dir = Path(__file__).resolve().parent
        self._jar_file = script_dir.joinpath("../SimulatorConsole/target/SimulatorConsole-0.0.1-SNAPSHOT.jar")
        self._java_path = shutil.which(self.BINARY_JAVA)

    def _run_cli(self, cmd_args, cli, cwd):
        """
        Run external CLI and return subprocess.CompletedProcess.
        - cmd_args: list of args (not including binary)
        - cli: path to binary
        """
        proc = subprocess.run(
            [cli, *cmd_args],
            text=True,
            capture_output=True,
            cwd=cwd,
        )
        return proc

    def _read_result_file(self, result_file):
        with result_file.open("r", encoding="utf-8") as f:
            result = json.load(f)
            return result

    def _execute_simulator(self, strategy, config, tmp_path):
        strategy_conf = None
        if strategy:
            strategy_conf = tmp_path / ("%s.json" % strategy)
            with strategy_conf.open("w", encoding="utf-8") as f:
                f.write(json.dumps(config, indent=2))

        result_file = "result.json"
        args = ["-jar", self._jar_file, "-r", result_file]
        args.append("--no_validation")
        if strategy:
            args.extend([ "strategy", "-a", strategy, "-p", strategy_conf])

        proc = self._run_cli(args, cli=self._java_path, cwd=str(tmp_path))
        if proc.returncode != 0:
            out = (proc.stdout or "") + (proc.stderr or "")
            raise RuntimeError(f"return code: {proc.returncode}\noutput:\n{out}")

        expected_result_file = tmp_path / result_file
        result = self._read_result_file(expected_result_file)
        return result

    def _collect_samples(self, count):
        samples = []
        for i in range(0, count):
            with tempfile.TemporaryDirectory() as tmpdir_name:
                strategy = None
                config = {}
                result = self._execute_simulator(strategy, config, Path(tmpdir_name))
                samples.append(result["statistics"])
                #print(f"result: {json.dumps(result["statistics"], indent=2)}")
        return samples

    def main(self):
        parser = argparse.ArgumentParser(prog="range_evaluator", description="Establish DeltaIoT range boundaries")
        default = ' (default: %(default)s)'
        #parser.add_argument('-r', '--result', type=as_path, help="result json file")
        parser.add_argument('--confidence', type=int, default=95, help="confidence in percent" + default)
        parser.add_argument('--accuracy', type=float, default=0.01, help="accuracy of result" + default)
        args = parser.parse_args()

        if not self._java_path:
            raise RuntimeError("unable to find: %s" % self.BINARY_JAVA)
        if not self._jar_file:
            raise RuntimeError("unable to find: %s" % self._jar_file)

        sample_count = calculate_required_samples(args.confidence, args.accuracy)
        print(f"sample count for confidence = {args.confidence}% and accuracy = {args.accuracy}: {sample_count}")
        samples = self._collect_samples(sample_count)
        energy_consumption_min = min([sample["energyConsumption"]["min"] for sample in samples])
        energy_consumption_max = max([sample["energyConsumption"]["max"] for sample in samples])
        energy_consumption_average = statistics.mean([sample["energyConsumption"]["average"] for sample in samples])
        packet_loss_min = min([sample["packetLoss"]["min"] for sample in samples])
        packet_loss_max = max([sample["packetLoss"]["max"] for sample in samples])
        packet_loss_average = statistics.mean([sample["packetLoss"]["average"] for sample in samples])

        table_entries = []
        table_entries.append(["energy consumption", energy_consumption_min, energy_consumption_max, energy_consumption_average])
        table_entries.append(["packet loss", packet_loss_min, packet_loss_max, packet_loss_average])
        table_str = tabulate(table_entries,
                             headers=['Attribute', 'Min', 'Max', 'Average'],
                             tablefmt="simple"
                             )
        print(table_str)



if __name__ == '__main__':
    e = RangeEvaluator()
    e.main()
