import argparse
from pathlib import Path
import statistics
import csv

import tabulate

from strategy_kind import StrategyKind
from simulator import Simulator
from correlator import Correlator


class StrategyValidator:

    def _execute_runs(self, runs, strategy, config_file, args):
        strat_id = "%s:%s" % (strategy.name, config_file.stem)
        simulator = Simulator()
        simulator.init()
        qas = simulator.collect_simulation_data(simulator, runs, strat_id, strategy, config_file, args.seed, args.max_workers)
        energy_consumption_min = min([qa[0]["energyConsumption"]["min"] for qa in qas])
        energy_consumption_max = max([qa[0]["energyConsumption"]["max"] for qa in qas])
        energy_consumption_average = statistics.mean([qa[0]["energyConsumption"]["average"] for qa in qas])
        packet_loss_min = min([qa[0]["packetLoss"]["min"] for qa in qas])
        packet_loss_max = max([qa[0]["packetLoss"]["max"] for qa in qas])
        packet_loss_average = statistics.mean([qa[0]["packetLoss"]["average"] for qa in qas])
        normalized_score_average = statistics.mean([qa[1] for qa in qas])
        return (energy_consumption_min, energy_consumption_max, energy_consumption_average, packet_loss_min,
                packet_loss_max, packet_loss_average, normalized_score_average)

    def _analyze_quality_attributes(self, args):
        strategies = []
        for strat in args.strategy:
            strategies.append(strat)
        print(f"strategy count: {len(strategies)}")
        print(f"runs:           {args.runs}")
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

    def _extract_quality_attributes(self, args):
        print(f"strategy:       {args.strategy[0].name}")
        print(f"config count:   {len(args.config)}")
        print(f"runs:           {args.runs}")

        runs = []
        simulator = Simulator()
        simulator.init()
        for config in args.config:
            run = simulator.collect_simulation_data(simulator, args.runs, args.strategy[0], config.resolve(), args.seed, args.max_workers)
            runs.append(run)

        headers = ['ID', 'Values', 'Run', 'Sample', 'Energy', 'Packet Loss']
        with args.result.open("w", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=headers)
            writer.writeheader()
            for c, qa_list in enumerate(runs):
                for r, qa_entry in enumerate(qa_list):
                    optimizables = ["%s=%s" % (key, value) for key, value in qa_entry[2]["strategyConfig"].items()]
                    values = ",".join(optimizables)
                    for s, sample in enumerate(qa_entry[2]["qos"]):
                        writer.writerow({'ID': c,
                                     'Values': values,
                                     'Run': r,
                                     'Sample': s,
                                     'Energy': sample["powerConsumption"],
                                     'Packet Loss': sample["packetLoss"],
                                     })

    def main(self):
        def strategy_name(string):
            strategy = StrategyKind[string.upper()]
            return strategy

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

        subparsers = parser.add_subparsers(required=True, help='available subcommands')
        parser_quality_attributes = subparsers.add_parser('qa', help='quality attributes analyzer')
        parser_quality_attributes.add_argument('-r', '--result', type=Path)
        parser_quality_attributes.add_argument('--strategy',
                            required=True,
                            type=strategy_config,
                            metavar="{%s}" % ",".join([_type.name for _type in StrategyKind]),
                            help="adaption strategy format: strategy[:config file]")
        parser_quality_attributes.set_defaults(func=self._analyze_quality_attributes)

        parser_quality_attributes_raw = subparsers.add_parser('qa_raw', help='raw quality attributes extractor')
        parser_quality_attributes_raw.add_argument('-r', '--result', type=Path, required=True)
        parser_quality_attributes_raw.add_argument('--strategy', action='append',
                            required=True,
                            type=strategy_name,
                            metavar="{%s}" % ",".join([_type.name for _type in StrategyKind]),
                            help="adaption strategy")
        parser_quality_attributes_raw.add_argument('--config', action='append',
                            required=True,
                            type=Path,
                            help="adaption strategy config file")
        parser_quality_attributes_raw.set_defaults(func=self._extract_quality_attributes)

        parser_correlate = subparsers.add_parser('correlate', help='generate correlation data')
        parser_correlate.add_argument('task_file', type=Path, nargs='+')
        parser_correlate.add_argument('-r', '--result', type=Path, required=True, help="CSV result file")
        parser_correlate.add_argument('--strategy',
                            required=True,
                            type=strategy_name,
                            metavar="{%s}" % ",".join([_type.name for _type in StrategyKind]),
                            help="adaption strategy")
        parser_correlate.set_defaults(func=Correlator().correlate_strategies)

        args = parser.parse_args()

        args.func(args)


if __name__ == '__main__':
    v = StrategyValidator()
    v.main()
