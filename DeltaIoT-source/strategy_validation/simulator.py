from pathlib import Path
import shutil
import subprocess
import json

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

    def execute_simulator(self, strategy: StrategyKind, config_file, seed, tmp_path: Path):
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

    def _read_json_file(self, json_file):
        with json_file.open("r", encoding="utf-8") as f:
            result = json.load(f)
            return result
