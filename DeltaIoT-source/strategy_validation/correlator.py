from pathlib import Path
import tempfile
import statistics
import csv
import json

import tabulate

from simulator import Simulator


class Correlator:
    def correlate_strategies(self, args):
        print(f"correlate strategy: {args.strategy.name}")
        print(f"task count:         {len(args.task_file)}")
        print(f"runs:               {args.runs}")

        all_tasks = []
        for result_file in args.task_file:
            stats = self._analyze_task_result(result_file)
            all_tasks.append(stats)

        all_data = []
        simulator = Simulator()
        simulator.init()
        for task_id, optimizables, reward in all_tasks:
            with tempfile.TemporaryDirectory() as tmpdir_name:
                tmp_path = Path(tmpdir_name)
                config_file = self._create_strategy_conf(tmp_path, args.strategy, optimizables)
                strat_id = "%s:%s" % (args.strategy.name, task_id)
                data = simulator.collect_simulation_data(simulator, args.runs, strat_id, args.strategy, config_file, args.seed, args.max_workers)
                score_average = statistics.mean([result[1] for result in data])
                all_data.append((task_id, reward, score_average, optimizables))

        table_entries = []
        for task_id, reward, score_average, optimizables in all_data:
            name_values = ["%s=%s" % (key, value) for key, value in optimizables.items()]
            values = ",".join(name_values)
            table_entries.append((task_id, reward, score_average, values))

        headers = ['ID', 'Reward', 'Score', 'Values']

        with args.result.open("w", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=headers)
            writer.writeheader()
            for entry in table_entries:
                writer.writerow({'ID': entry[0],
                                 'Reward': entry[1],
                                 'Score': entry[2],
                                 'Values': entry[3],
                                 })

        table_str = tabulate.tabulate(table_entries,
                                      headers=headers,
                                      tablefmt="simple"
                                      )
        print(table_str)

    def _create_strategy_conf(self, folder, strategy, values):
        strategy_path = folder / ("%s_conf.json" % strategy.name)
        with strategy_path.open("w", encoding="utf-8") as f:
            json.dump(values, f, indent=2)
        return strategy_path

    def _analyze_task_result(self, result_file):
        task_result = self._read_json_file(result_file)
        task_id = task_result["result"]["id"]
        reward = task_result["result"]["reward"]
        optimizables = task_result["optimizables"]

        result = (
            task_id,
            optimizables,
            reward
        )
        return result

    def _read_json_file(self, json_file):
        with json_file.open("r", encoding="utf-8") as f:
            result = json.load(f)
            return result
