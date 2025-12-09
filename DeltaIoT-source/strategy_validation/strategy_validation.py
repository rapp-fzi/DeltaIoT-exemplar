import os
import argparse
import subprocess
from pathlib import Path
import shutil
import tempfile
import json
import statistics
from concurrent.futures import ThreadPoolExecutor, as_completed

from tabulate import tabulate
from progress.bar import Bar

from strategy_kind import StrategyKind


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

    def _execute_simulator(self, strategy: StrategyKind, config_file, tmp_path: Path):
        strategy_conf = tmp_path / ("%s.json" % "empty")
        with strategy_conf.open("w", encoding="utf-8") as f:
            f.write(json.dumps({}, indent=2))

        result_file = "result.json"
        args = ["-jar", self._jar_file, "-r", result_file]
        args.append("--no_validation")
        args.extend(strategy.extra_arguments)
        if config_file:
            args.extend(['-p', str(config_file)])

        proc = self._run_cli(args, cli=self._java_path, cwd=str(tmp_path))
        if proc.returncode != 0:
            out = (proc.stdout or "") + (proc.stderr or "")
            raise RuntimeError(f"return code: {proc.returncode}\noutput:\n{out}")

        expected_result_file = tmp_path / result_file
        result = self._read_result_file(expected_result_file)
        return result

    def _collect_sample(self, strategy: StrategyKind, config_file):
        with tempfile.TemporaryDirectory() as tmpdir_name:
            result = self._execute_simulator(strategy, config_file, Path(tmpdir_name))
            return result["statistics"], result["normalizedScore"]

    def _collect_samples(self, count, strategy: StrategyKind, config_file, max_workers):
        samples = []
        with Bar("Sampling %12s" % strategy.name, max=count) as bar:
            with ThreadPoolExecutor(max_workers=max_workers) as executor:
                futures = []
                for i in range(0, count):
                    future = executor.submit(self._collect_sample, strategy, config_file)
                    futures.append(future)
                for future in as_completed(futures):
                    samples.append(future.result())
                    bar.next()
        return samples

    def _sample(self, sample_count, strategy, config_file, args):
        samples = self._collect_samples(sample_count, strategy, config_file, args.max_workers)
        energy_consumption_min = min([sample[0]["energyConsumption"]["min"] for sample in samples])
        energy_consumption_max = max([sample[0]["energyConsumption"]["max"] for sample in samples])
        energy_consumption_average = statistics.mean([sample[0]["energyConsumption"]["average"] for sample in samples])
        packet_loss_min = min([sample[0]["packetLoss"]["min"] for sample in samples])
        packet_loss_max = max([sample[0]["packetLoss"]["max"] for sample in samples])
        packet_loss_average = statistics.mean([sample[0]["packetLoss"]["average"] for sample in samples])
        normalized_score_average = statistics.mean([sample[1] for sample in samples])
        return (energy_consumption_min, energy_consumption_max, energy_consumption_average, packet_loss_min,
                packet_loss_max, packet_loss_average, normalized_score_average)

    def main(self):
        def strategy_config(string):
            tokens = string.split(":")
            strategy = StrategyKind[tokens[0].upper()]
            if len(tokens) > 1:
                config_file = Path(tokens[1])
                config_file = config_file.resolve()
                if not config_file.exists():
                    raise ValueError("not found: %s" % config_file)
            else:
                config_file = None
            return strategy, config_file

        parser = argparse.ArgumentParser(prog="strategy_validator", description="Validates DeltaIoT strategies")
        default = ' (default: %(default)s)'
        parser.add_argument('--sample_count', type=int, default=30, help="sample count" + default)
        parser.add_argument('--max_workers', type=int, default=1, help="max worker threads" + default)
        parser.add_argument('--strategy', action='append',
                            required=True,
                            type=strategy_config,
                            metavar="{%s}" % ",".join([_type.name for _type in StrategyKind]),
                            help="adaption strategy format: strategy[:config file]")
        args = parser.parse_args()

        if not self._java_path:
            raise RuntimeError("unable to find: %s" % self.BINARY_JAVA)
        if not self._jar_file:
            raise RuntimeError("unable to find: %s" % self._jar_file)

        strategies = []
        for strat in args.strategy:
            strategies.append(strat)
        print(f"strategy count: {len(strategies)}")
        print(f"sample count:   {args.sample_count}")
        samples = []
        for strat in strategies:
            sample = self._sample(args.sample_count, strat[0], strat[1], args)
            samples.append((strat[0], sample))

        table_entries = []
        for strategy, sample in samples:
            table_entries.append([strategy.name, *sample])
        table_str = tabulate(table_entries,
                             headers=['Strategy', 'Energy Min', 'Energy Max', 'Energy Average', 'Packet Loss Min', 'Packet Loss Max', 'Packet Loss Average', 'Normalized Score Average'],
                             tablefmt="simple"
                             )
        print(table_str)


if __name__ == '__main__':
    e = RangeEvaluator()
    e.main()
