import subprocess
from pathlib import Path
import shutil
import json

import pytest

BINARY_JAVA = "java"


@pytest.fixture(scope="session")
def cli_path():
    path = shutil.which(BINARY_JAVA)
    if path:
        return path
    pytest.skip(f"{BINARY_JAVA} not found on PATH")


@pytest.fixture(scope="session")
def script_dir():
    return Path(__file__).resolve().parent

@pytest.fixture(scope="session")
def jar_file(script_dir):
    return script_dir.joinpath("../SimulatorConsole/target/SimulatorConsole-0.0.1-SNAPSHOT.jar")

def run_cli(cmd_args, cli, cwd):
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
        check=False,
    )
    return proc


@pytest.mark.parametrize('CHANGE_POWER_VALUE', [1,  4])
@pytest.mark.parametrize('POWER_MIN', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
def test_strategy1a(jar_file, cli_path, tmp_path, CHANGE_POWER_VALUE, POWER_MIN, POWER_MIN_MAX_DELTA):
    strategy = "EAStrategy1a"
    strategy_conf = tmp_path / ("%s.json" % strategy)
    config = {
            "CHANGE_POWER_VALUE": CHANGE_POWER_VALUE,
            "POWER_MIN": POWER_MIN,
            "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
    }
    with strategy_conf.open("w", encoding="utf-8") as f:
        f.write(json.dumps(config, indent=2))

    result_file = "result.json"
    args = ["-jar", jar_file, "-r", result_file, "strategy", "-a", strategy, "-p", strategy_conf]
    proc = run_cli(args, cli=cli_path, cwd=str(tmp_path))

    out = (proc.stdout or "") + (proc.stderr or "")
    assert proc.returncode == 0, f"exit {proc.returncode}\nOUT:\n{out}"
    expected_result_file = tmp_path / result_file
    assert expected_result_file.exists()

