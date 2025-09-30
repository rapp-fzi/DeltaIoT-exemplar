import json
import subprocess
from pathlib import Path


class Simulator:
    BINARY_JAVA = "java"

    def __init__(self):
        script_dir = Path(__file__).resolve().parent
        self._jar = script_dir.joinpath("../DeltaIoT-source/SimulatorConsole/target/SimulatorConsole-0.0.1-SNAPSHOT.jar")

    def _run_simulator(self, cmd_args, cwd):
        proc = subprocess.run(
            [self.BINARY_JAVA, "-jar", self._jar, *cmd_args],
            text=True,
            capture_output=True,
            cwd=str(cwd),
            check=True,
        )
        out = (proc.stdout or "") + (proc.stderr or "")
        if proc.returncode != 0:
            raise RuntimeError("Return code: %s:\n%s" % (proc.returncode, out))

    def _create_strategy_conf(self, folder, strategy, values):
        strategy_path = folder / ("%s_conf.json" % strategy.name)
        with strategy_path.open("w", encoding="utf-8") as f:
            json.dump(values, f, indent=2)
        return strategy_path

    def simulate(self, strategy, values):
        result_file = "result.json"
        tmp_path = Path("/tmp/ve")
        tmp_path.mkdir(parents=True, exist_ok=True)
        strategy_conf = self._create_strategy_conf(tmp_path, strategy, values)
        args = ["-r", result_file, "strategy", "-a", strategy.name, "-p", strategy_conf]
        self._run_simulator(args, cwd=tmp_path)
        result_path = tmp_path / result_file
        with result_path.open("r", encoding="utf-8") as f:
            result = json.load(f)
            return result["score"]
