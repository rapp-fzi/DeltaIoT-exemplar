from pathlib import Path
import tempfile
import statistics
import csv
import json

import tabulate

from simulator import Simulator
from normalizer import Normalizer, NullNormalizer
from reward_calculator import AverageTotalRewardCalculator, SimexpRewardCalculator, SimulatorRewardCalculator


class Correlator:
    def correlate_strategies(self, args):
        print(f"correlate strategy: {args.strategy.name}")
        print(f"task count:         {len(args.task_file)}")
        print(f"runs:               {args.runs}")

        all_tasks = []
        for result_file in args.task_file:
            stats = self._analyze_task_result(result_file)
            if stats["status"] != "ABORT":
                all_tasks.append(stats)
        print(f"completed tasks:    {len(all_tasks)}")

        energy_normalizer = Normalizer(9, 26)
        packet_loss_normalizer = Normalizer(0.02, 0.4)
        simexp_reward_calculator = SimexpRewardCalculator(energy_normalizer, packet_loss_normalizer)
        total_reward_calculator = AverageTotalRewardCalculator(simexp_reward_calculator)
        simulator_reward_calculator = SimulatorRewardCalculator(energy_normalizer, packet_loss_normalizer)
        score_calculator = AverageTotalRewardCalculator(simulator_reward_calculator)

        all_data = []
        simulator = Simulator()
        simulator.init()
        score_type = "AVERAGE"
        for i, task_data in enumerate(all_tasks):
            with tempfile.TemporaryDirectory() as tmpdir_name:
                tmp_path = Path(tmpdir_name)
                config_file = self._create_strategy_conf(tmp_path, args.strategy, task_data["optimizables"])
                strat_id = "%s:%s" % (args.strategy.name, task_data["id"])
                suffix = " [%d/%d]" % (i+1, len(all_tasks))
                data = simulator.collect_simulation_data(simulator, args.runs, strat_id, args.strategy, config_file, args.seed, args.max_workers, suffix=suffix)
                if args.calc_average_score:
                    simulator_runs = self._qos_to_runs(data)
                    score = score_calculator.total_reward(simulator_runs)
                else:
                    score = statistics.mean([result["score"] for result in data])

                if args.calc_average_reward:
                    reward_type = "AVERAGE"
                    reward = total_reward_calculator.total_reward(task_data["runs"])
                else:
                    reward = task_data["reward"]
                    reward_type = task_data["reward_type"]

                all_data.append((task_data["id"], reward, reward_type, score, score_type, task_data["optimizables"]))

        table_entries = []
        for task_id, reward, reward_type, score, score_type, optimizables in all_data:
            name_values = ["%s=%s" % (key, value) for key, value in optimizables.items()]
            values = ",".join(name_values)
            table_entries.append((task_id, reward, reward_type, score, score_type, values))

        headers = ['ID', 'Reward', 'Reward type', 'Score', 'Score type', 'Values']

        with args.result.open("w", encoding="utf-8") as f:
            writer = csv.DictWriter(f, fieldnames=headers)
            writer.writeheader()
            for entry in table_entries:
                writer.writerow({'ID': entry[0],
                                 'Reward': entry[1],
                                 'Reward type': entry[2],
                                 'Score': entry[3],
                                 'Score type': entry[4],
                                 'Values': entry[5],
                                 })

        table_str = tabulate.tabulate(table_entries,
                                      headers=headers,
                                      tablefmt="simple"
                                      )
        print(table_str)

    def _qos_to_runs(self, simulator_runs):
        runs = []
        for data in simulator_runs:
            result = data["result"]
            run_entry = {}
            quality_attributes = {}
            packet_loss = []
            energy = []
            qos = result["qos"]
            for qos_entry in qos:
                packet_loss.append(qos_entry["packetLoss"])
                energy.append(qos_entry["powerConsumption"])

            quality_attributes["PacketLoss.props"] = packet_loss
            quality_attributes["EnergyConsumption.props"] = energy
            run_entry["quality_attributes"] = quality_attributes
            runs.append(run_entry)

        return runs

    def _create_strategy_conf(self, folder, strategy, values):
        strategy_path = folder / ("%s_conf.json" % strategy.name)
        with strategy_path.open("w", encoding="utf-8") as f:
            json.dump(values, f, indent=2)
        return strategy_path

    def _analyze_task_result(self, result_file):
        task_result = self._read_json_file(result_file)
        try:
            task_id = task_result["result"]["id"]
            status = task_result["result"]["status"]
            reward = task_result["result"]["reward"]
            reward_type = task_result["result"]["reward_type"]
            optimizables = task_result["optimizables"]
            if task_result["result"]["quality_measurements"]:
                runs = task_result["result"]["quality_measurements"]["runs"]
            else:
                runs = []
        except Exception as e:
            raise RuntimeError(f"file {result_file}") from e

        result = {
            "id": task_id,
            "status": status,
            "optimizables": optimizables,
            "reward": reward,
            "reward_type": reward_type,
            "runs": runs,
        }
        return result

    def _read_json_file(self, json_file):
        with json_file.open("r", encoding="utf-8") as f:
            result = json.load(f)
            return result
