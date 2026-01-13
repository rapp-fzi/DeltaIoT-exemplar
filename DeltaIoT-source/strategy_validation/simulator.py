from pathlib import Path
import shutil
import subprocess
import json
import tempfile
from concurrent.futures import ThreadPoolExecutor, as_completed

from progress.bar import Bar

from strategy_kind import StrategyKind


class Simulator:
    BINARY_JAVA = "java"

    def __init__(self):
        script_dir = Path(__file__).resolve().parent
        self._jar_file = script_dir.joinpath("../SimulatorConsole/target/SimulatorConsole-0.0.1-SNAPSHOT.jar")
        self._java_path = shutil.which(self.BINARY_JAVA)

    def init(self):
        if not self._java_path:
            raise RuntimeError("unable to find: %s" % self.BINARY_JAVA)
        if not self._jar_file:
            raise RuntimeError("unable to find: %s" % self._jar_file)

    def collect_simulation_data(self, simulator, count, name, strategy: StrategyKind, config_file, seed, max_workers):
        qas = []
        with Bar("Execute %18s" % name, max=count) as bar:
            with ThreadPoolExecutor(max_workers=max_workers) as executor:
                futures = []
                for i in range(0, count):
                    future = executor.submit(self._execute_run, simulator, strategy, config_file, seed)
                    futures.append(future)
                for future in as_completed(futures):
                    qas.append(future.result())
                    bar.next()
        return qas

    def _execute_run(self, simulator, strategy: StrategyKind, config_file, seed):
        with tempfile.TemporaryDirectory() as tmpdir_name:
            result = self._execute_simulator(strategy, config_file, seed, Path(tmpdir_name))
            return result["statistics"], result["normalizedScore"], result

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
        result = self._read_json_file(expected_result_file)
        return result

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

    def _read_json_file(self, json_file):
        with json_file.open("r", encoding="utf-8") as f:
            result = json.load(f)
            return result
