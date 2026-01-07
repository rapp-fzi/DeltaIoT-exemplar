import argparse
import subprocess
from pathlib import Path
import shutil
import tempfile
import json
import statistics
import csv
from concurrent.futures import ThreadPoolExecutor, as_completed

import tabulate
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

    def _execute_simulator(self, strategy: StrategyKind, config_file, seed, tmp_path: Path):
        strategy_conf = tmp_path / ("%s.json" % "empty")
        with strategy_conf.open("w", encoding="utf-8") as f:
            f.write(json.dumps({}, indent=2))

        result_file = "result.json"
        args = ["-jar", self._jar_file, "-r", result_file]
        if seed is not None:
            args.extend(['--seed', str(seed)])
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

    def _execute_run(self, strategy: StrategyKind, config_file, seed):
        with tempfile.TemporaryDirectory() as tmpdir_name:
            result = self._execute_simulator(strategy, config_file, seed, Path(tmpdir_name))
            return result["statistics"], result["normalizedScore"]

    def _collect_qas(self, count, strategy: StrategyKind, config_file, seed, max_workers):
        qas = []
        strat_id = "%s:%s" % (strategy.name, config_file.stem)
        with Bar("Execute %18s" % strat_id, max=count) as bar:
            with ThreadPoolExecutor(max_workers=max_workers) as executor:
                futures = []
                for i in range(0, count):
                    future = executor.submit(self._execute_run, strategy, config_file, seed)
                    futures.append(future)
                for future in as_completed(futures):
                    qas.append(future.result())
                    bar.next()
        return qas

    def _execute_runs(self, runs, strategy, config_file, args):
        qas = self._collect_qas(runs, strategy, config_file, args.seed, args.max_workers)
        energy_consumption_min = min([qa[0]["energyConsumption"]["min"] for qa in qas])
        energy_consumption_max = max([qa[0]["energyConsumption"]["max"] for qa in qas])
        energy_consumption_average = statistics.mean([qa[0]["energyConsumption"]["average"] for qa in qas])
        packet_loss_min = min([qa[0]["packetLoss"]["min"] for qa in qas])
        packet_loss_max = max([qa[0]["packetLoss"]["max"] for qa in qas])
        packet_loss_average = statistics.mean([qa[0]["packetLoss"]["average"] for qa in qas])
        normalized_score_average = statistics.mean([qa[1] for qa in qas])
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
        parser.add_argument('--runs', type=int, default=30, help="run count" + default)
        parser.add_argument('--max_workers', type=int, default=1, help="max worker threads" + default)
        parser.add_argument('--seed', type=int, help="simulator seed")
        parser.add_argument('-r', '--result', type=Path)
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
        print(f"sample count:   {args.runs}")
        runs = []
        for strat in strategies:
            run = self._execute_runs(args.runs, strat[0], strat[1], args)
            runs.append((strat[0], strat[1], run))

        table_entries = []
        for strategy, config, run in runs:
            table_entries.append([strategy.name, config.name, *run])

        headers = ['Strategy', 'Config', 'Energy Min', 'Energy Max', 'Energy Average', 'Packet Loss Min',
                   'Packet Loss Max', 'Packet Loss Average', 'Normalized Score Average']

        if args.result:
            with args.result.open("w", encoding="utf-8") as f:
                writer = csv.DictWriter(f, fieldnames=headers)
                writer.writeheader()
                for entry in table_entries:
                    writer.writerow({'Strategy': entry[0],
                                     'Config': entry[1],
                                     'Energy Min': entry[2],
                                     'Energy Max': entry[3],
                                     'Energy Average': entry[4],
                                     'Packet Loss Min': entry[5],
                                     'Packet Loss Max': entry[6],
                                     'Packet Loss Average': entry[7],
                                     'Normalized Score Average': entry[8],
                                     })

        table_entries.append(tabulate.SEPARATING_LINE)
        table_entries.append(["total", None,
                              min([stats[0] for _,_,stats in runs]), max([stats[1] for _,_,stats in runs]), statistics.mean([stats[2] for _,_,stats in runs]),
                              min([stats[3] for _,_,stats in runs]), max([stats[4] for _,_,stats in runs]),
                              statistics.mean([stats[5] for _,_,stats in runs]),
                              None
                              ])

        table_str = tabulate.tabulate(table_entries,
                             headers=headers,
                             tablefmt="simple"
                             )
        print(table_str)


if __name__ == '__main__':
    e = RangeEvaluator()
    e.main()
