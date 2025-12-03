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


def _execute_simulator(jar_file, cli_path, strategy: str, config: dict, tmp_path: Path):
    strategy_conf = tmp_path / ("%s.json" % strategy)
    with strategy_conf.open("w", encoding="utf-8") as f:
        f.write(json.dumps(config, indent=2))

    result_file = "result.json"
    args = ["-jar", jar_file, "-r", result_file, "--no_validation", "strategy", "-a", strategy, "-p", strategy_conf]
    proc = run_cli(args, cli=cli_path, cwd=str(tmp_path))

    out = (proc.stdout or "") + (proc.stderr or "")
    assert proc.returncode == 0, f"exit {proc.returncode}\nOUT:\n{out}\nstrategy config:\n{config}"
    expected_result_file = tmp_path / result_file
    assert expected_result_file.exists(), f"missing result file\nstrategy config:\n{config}"


@pytest.mark.parametrize('CHANGE_POWER_VALUE', [1,  4])
@pytest.mark.parametrize('POWER_MIN', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
def test_strategy1a(jar_file, cli_path, tmp_path, CHANGE_POWER_VALUE, POWER_MIN, POWER_MIN_MAX_DELTA):
    strategy = "EAStrategy1a"
    config = {
            "CHANGE_POWER_VALUE": CHANGE_POWER_VALUE,
            "POWER_MIN": POWER_MIN,
            "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE', [1,  4])
@pytest.mark.parametrize('POWER_MIN', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
@pytest.mark.parametrize('CHANGE_DIST_VALUE', [1, 5])
def test_strategy1b(jar_file, cli_path, tmp_path, CHANGE_POWER_VALUE, POWER_MIN, POWER_MIN_MAX_DELTA, CHANGE_DIST_VALUE):
    strategy = "EAStrategy1b"
    config = {
            "CHANGE_POWER_VALUE": CHANGE_POWER_VALUE,
            "POWER_MIN": POWER_MIN,
            "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
            "CHANGE_DIST_VALUE": CHANGE_DIST_VALUE,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE', [1,  4])
@pytest.mark.parametrize('POWER_MIN', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
@pytest.mark.parametrize('CHANGE_DIST_VALUE_7_8', [1, 5])
@pytest.mark.parametrize('CHANGE_DIST_VALUE_15_16', [1, 5])
@pytest.mark.parametrize('CHANGE_DIST_VALUE_5_6', [1, 5])
def test_strategy1c(jar_file, cli_path, tmp_path, CHANGE_POWER_VALUE, POWER_MIN, POWER_MIN_MAX_DELTA,
                    CHANGE_DIST_VALUE_7_8, CHANGE_DIST_VALUE_15_16, CHANGE_DIST_VALUE_5_6):
    strategy = "EAStrategy1c"
    config = {
        "CHANGE_POWER_VALUE": CHANGE_POWER_VALUE,
        "POWER_MIN": POWER_MIN,
        "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
        "CHANGE_DIST_VALUE_7_8": CHANGE_DIST_VALUE_7_8,
        "CHANGE_DIST_VALUE_15_16": CHANGE_DIST_VALUE_15_16,
        "CHANGE_DIST_VALUE_5_6": CHANGE_DIST_VALUE_5_6,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE1', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE2', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE3', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE4', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE5', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE6', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE7', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE8', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE9', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE10', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE11', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE12', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE13', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE14', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE15', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE16', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE17', [1,  4])
@pytest.mark.parametrize('POWER_MIN', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
def test_strategy2a(jar_file, cli_path, tmp_path,
                    CHANGE_POWER_VALUE1,
                    #CHANGE_POWER_VALUE2, CHANGE_POWER_VALUE3, CHANGE_POWER_VALUE4,
                    #CHANGE_POWER_VALUE5, CHANGE_POWER_VALUE6, CHANGE_POWER_VALUE7, CHANGE_POWER_VALUE8,
                    #CHANGE_POWER_VALUE9, CHANGE_POWER_VALUE10, CHANGE_POWER_VALUE11, CHANGE_POWER_VALUE12,
                    #CHANGE_POWER_VALUE13, CHANGE_POWER_VALUE14, CHANGE_POWER_VALUE15, CHANGE_POWER_VALUE16,
                    #CHANGE_POWER_VALUE17,
                    POWER_MIN,
                    POWER_MIN_MAX_DELTA
                    ):
    strategy = "EAStrategy2a"
    config = {
            "CHANGE_POWER_VALUE1": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE2": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE3": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE4": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE5": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE6": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE7": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE8": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE9": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE10": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE11": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE12": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE13": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE14": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE15": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE16": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE17": CHANGE_POWER_VALUE1,
            "POWER_MIN": POWER_MIN,
            "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE1', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE2', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE3', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE4', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE5', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE6', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE7', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE8', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE9', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE10', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE11', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE12', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE13', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE14', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE15', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE16', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE17', [1,  4])
@pytest.mark.parametrize('POWER_MIN', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
@pytest.mark.parametrize('CHANGE_DIST_VALUE', [1, 5])
def test_strategy2b(jar_file, cli_path, tmp_path,
                    CHANGE_POWER_VALUE1,
                    #CHANGE_POWER_VALUE2, CHANGE_POWER_VALUE3, CHANGE_POWER_VALUE4,
                    #CHANGE_POWER_VALUE5, CHANGE_POWER_VALUE6, CHANGE_POWER_VALUE7, CHANGE_POWER_VALUE8,
                    #CHANGE_POWER_VALUE9, CHANGE_POWER_VALUE10, CHANGE_POWER_VALUE11, CHANGE_POWER_VALUE12,
                    #CHANGE_POWER_VALUE13, CHANGE_POWER_VALUE14, CHANGE_POWER_VALUE15, CHANGE_POWER_VALUE16,
                    #CHANGE_POWER_VALUE17,
                    POWER_MIN,
                    POWER_MIN_MAX_DELTA,
                    CHANGE_DIST_VALUE
                    ):
    strategy = "EAStrategy2b"
    config = {
            "CHANGE_POWER_VALUE1": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE2": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE3": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE4": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE5": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE6": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE7": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE8": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE9": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE10": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE11": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE12": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE13": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE14": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE15": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE16": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE17": CHANGE_POWER_VALUE1,
            "POWER_MIN": POWER_MIN,
            "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
            "CHANGE_DIST_VALUE": CHANGE_DIST_VALUE,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE1', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE2', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE3', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE4', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE5', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE6', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE7', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE8', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE9', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE10', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE11', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE12', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE13', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE14', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE15', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE16', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE17', [1,  4])
@pytest.mark.parametrize('POWER_MIN1', [0, 5])
#@pytest.mark.parametrize('POWER_MIN2', [0, 5])
#@pytest.mark.parametrize('POWER_MIN3', [0, 5])
#@pytest.mark.parametrize('POWER_MIN4', [0, 5])
#@pytest.mark.parametrize('POWER_MIN5', [0, 5])
#@pytest.mark.parametrize('POWER_MIN6', [0, 5])
#@pytest.mark.parametrize('POWER_MIN7', [0, 5])
#@pytest.mark.parametrize('POWER_MIN8', [0, 5])
#@pytest.mark.parametrize('POWER_MIN9', [0, 5])
#@pytest.mark.parametrize('POWER_MIN10', [0, 5])
#@pytest.mark.parametrize('POWER_MIN11', [0, 5])
#@pytest.mark.parametrize('POWER_MIN12', [0, 5])
#@pytest.mark.parametrize('POWER_MIN13', [0, 5])
#@pytest.mark.parametrize('POWER_MIN14', [0, 5])
#@pytest.mark.parametrize('POWER_MIN15', [0, 5])
#@pytest.mark.parametrize('POWER_MIN16', [0, 5])
#@pytest.mark.parametrize('POWER_MIN17', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
def test_strategy3a(jar_file, cli_path, tmp_path,
                    CHANGE_POWER_VALUE1,
                    #CHANGE_POWER_VALUE2, CHANGE_POWER_VALUE3, CHANGE_POWER_VALUE4,
                    #CHANGE_POWER_VALUE5, CHANGE_POWER_VALUE6, CHANGE_POWER_VALUE7, CHANGE_POWER_VALUE8,
                    #CHANGE_POWER_VALUE9, CHANGE_POWER_VALUE10, CHANGE_POWER_VALUE11, CHANGE_POWER_VALUE12,
                    #CHANGE_POWER_VALUE13, CHANGE_POWER_VALUE14, CHANGE_POWER_VALUE15, CHANGE_POWER_VALUE16,
                    #CHANGE_POWER_VALUE17,
                    POWER_MIN1,
                    #POWER_MIN2, POWER_MIN3, POWER_MIN4, POWER_MIN5, POWER_MIN6, POWER_MIN7, POWER_MIN8,
                    #POWER_MIN9, POWER_MIN10, POWER_MIN11, POWER_MIN12, POWER_MIN13, POWER_MIN14, POWER_MIN15, POWER_MIN16,
                    #POWER_MIN17,
                    POWER_MIN_MAX_DELTA
                    ):
    strategy = "EAStrategy3a"
    config = {
            "CHANGE_POWER_VALUE1": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE2": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE3": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE4": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE5": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE6": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE7": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE8": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE9": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE10": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE11": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE12": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE13": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE14": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE15": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE16": CHANGE_POWER_VALUE1,
            "CHANGE_POWER_VALUE17": CHANGE_POWER_VALUE1,
            "POWER_MIN1": POWER_MIN1,
            "POWER_MIN2": POWER_MIN1,
            "POWER_MIN3": POWER_MIN1,
            "POWER_MIN4": POWER_MIN1,
            "POWER_MIN5": POWER_MIN1,
            "POWER_MIN6": POWER_MIN1,
            "POWER_MIN7": POWER_MIN1,
            "POWER_MIN8": POWER_MIN1,
            "POWER_MIN9": POWER_MIN1,
            "POWER_MIN10": POWER_MIN1,
            "POWER_MIN11": POWER_MIN1,
            "POWER_MIN12": POWER_MIN1,
            "POWER_MIN13": POWER_MIN1,
            "POWER_MIN14": POWER_MIN1,
            "POWER_MIN15": POWER_MIN1,
            "POWER_MIN16": POWER_MIN1,
            "POWER_MIN17": POWER_MIN1,
            "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE1', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE2', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE3', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE4', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE5', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE6', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE7', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE8', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE9', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE10', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE11', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE12', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE13', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE14', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE15', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE16', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE17', [1,  4])
@pytest.mark.parametrize('POWER_MIN1', [0, 5])
#@pytest.mark.parametrize('POWER_MIN2', [0, 5])
#@pytest.mark.parametrize('POWER_MIN3', [0, 5])
#@pytest.mark.parametrize('POWER_MIN4', [0, 5])
#@pytest.mark.parametrize('POWER_MIN5', [0, 5])
#@pytest.mark.parametrize('POWER_MIN6', [0, 5])
#@pytest.mark.parametrize('POWER_MIN7', [0, 5])
#@pytest.mark.parametrize('POWER_MIN8', [0, 5])
#@pytest.mark.parametrize('POWER_MIN9', [0, 5])
#@pytest.mark.parametrize('POWER_MIN10', [0, 5])
#@pytest.mark.parametrize('POWER_MIN11', [0, 5])
#@pytest.mark.parametrize('POWER_MIN12', [0, 5])
#@pytest.mark.parametrize('POWER_MIN13', [0, 5])
#@pytest.mark.parametrize('POWER_MIN14', [0, 5])
#@pytest.mark.parametrize('POWER_MIN15', [0, 5])
#@pytest.mark.parametrize('POWER_MIN16', [0, 5])
#@pytest.mark.parametrize('POWER_MIN17', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
@pytest.mark.parametrize('CHANGE_DIST_VALUE', [1, 5])
def test_strategy3b(jar_file, cli_path, tmp_path,
                    CHANGE_POWER_VALUE1,
                    #CHANGE_POWER_VALUE2, CHANGE_POWER_VALUE3, CHANGE_POWER_VALUE4,
                    #CHANGE_POWER_VALUE5, CHANGE_POWER_VALUE6, CHANGE_POWER_VALUE7, CHANGE_POWER_VALUE8,
                    #CHANGE_POWER_VALUE9, CHANGE_POWER_VALUE10, CHANGE_POWER_VALUE11, CHANGE_POWER_VALUE12,
                    #CHANGE_POWER_VALUE13, CHANGE_POWER_VALUE14, CHANGE_POWER_VALUE15, CHANGE_POWER_VALUE16,
                    #CHANGE_POWER_VALUE17,
                    POWER_MIN1,
                    #POWER_MIN2, POWER_MIN3, POWER_MIN4, POWER_MIN5, POWER_MIN6, POWER_MIN7, POWER_MIN8,
                    #POWER_MIN9, POWER_MIN10, POWER_MIN11, POWER_MIN12, POWER_MIN13, POWER_MIN14, POWER_MIN15, POWER_MIN16,
                    #POWER_MIN17,
                    POWER_MIN_MAX_DELTA,
                    CHANGE_DIST_VALUE
                    ):
    strategy = "EAStrategy3b"
    config = {
        "CHANGE_POWER_VALUE1": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE2": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE3": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE4": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE5": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE6": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE7": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE8": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE9": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE10": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE11": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE12": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE13": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE14": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE15": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE16": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE17": CHANGE_POWER_VALUE1,
        "POWER_MIN1": POWER_MIN1,
        "POWER_MIN2": POWER_MIN1,
        "POWER_MIN3": POWER_MIN1,
        "POWER_MIN4": POWER_MIN1,
        "POWER_MIN5": POWER_MIN1,
        "POWER_MIN6": POWER_MIN1,
        "POWER_MIN7": POWER_MIN1,
        "POWER_MIN8": POWER_MIN1,
        "POWER_MIN9": POWER_MIN1,
        "POWER_MIN10": POWER_MIN1,
        "POWER_MIN11": POWER_MIN1,
        "POWER_MIN12": POWER_MIN1,
        "POWER_MIN13": POWER_MIN1,
        "POWER_MIN14": POWER_MIN1,
        "POWER_MIN15": POWER_MIN1,
        "POWER_MIN16": POWER_MIN1,
        "POWER_MIN17": POWER_MIN1,
        "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
        "CHANGE_DIST_VALUE": CHANGE_DIST_VALUE,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)


@pytest.mark.parametrize('CHANGE_POWER_VALUE1', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE2', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE3', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE4', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE5', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE6', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE7', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE8', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE9', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE10', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE11', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE12', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE13', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE14', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE15', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE16', [1,  4])
#@pytest.mark.parametrize('CHANGE_POWER_VALUE17', [1,  4])
@pytest.mark.parametrize('POWER_MIN1', [0, 5])
#@pytest.mark.parametrize('POWER_MIN2', [0, 5])
#@pytest.mark.parametrize('POWER_MIN3', [0, 5])
#@pytest.mark.parametrize('POWER_MIN4', [0, 5])
#@pytest.mark.parametrize('POWER_MIN5', [0, 5])
#@pytest.mark.parametrize('POWER_MIN6', [0, 5])
#@pytest.mark.parametrize('POWER_MIN7', [0, 5])
#@pytest.mark.parametrize('POWER_MIN8', [0, 5])
#@pytest.mark.parametrize('POWER_MIN9', [0, 5])
#@pytest.mark.parametrize('POWER_MIN10', [0, 5])
#@pytest.mark.parametrize('POWER_MIN11', [0, 5])
#@pytest.mark.parametrize('POWER_MIN12', [0, 5])
#@pytest.mark.parametrize('POWER_MIN13', [0, 5])
#@pytest.mark.parametrize('POWER_MIN14', [0, 5])
#@pytest.mark.parametrize('POWER_MIN15', [0, 5])
#@pytest.mark.parametrize('POWER_MIN16', [0, 5])
#@pytest.mark.parametrize('POWER_MIN17', [0, 5])
@pytest.mark.parametrize('POWER_MIN_MAX_DELTA', [4, 10])
@pytest.mark.parametrize('CHANGE_DIST_VALUE_7_8', [1, 5])
@pytest.mark.parametrize('CHANGE_DIST_VALUE_15_16', [1, 5])
@pytest.mark.parametrize('CHANGE_DIST_VALUE_5_6', [1, 5])
def test_strategy3c(jar_file, cli_path, tmp_path,
                    CHANGE_POWER_VALUE1,
                    #CHANGE_POWER_VALUE2, CHANGE_POWER_VALUE3, CHANGE_POWER_VALUE4,
                    #CHANGE_POWER_VALUE5, CHANGE_POWER_VALUE6, CHANGE_POWER_VALUE7, CHANGE_POWER_VALUE8,
                    #CHANGE_POWER_VALUE9, CHANGE_POWER_VALUE10, CHANGE_POWER_VALUE11, CHANGE_POWER_VALUE12,
                    #CHANGE_POWER_VALUE13, CHANGE_POWER_VALUE14, CHANGE_POWER_VALUE15, CHANGE_POWER_VALUE16,
                    #CHANGE_POWER_VALUE17,
                    POWER_MIN1,
                    #POWER_MIN2, POWER_MIN3, POWER_MIN4, POWER_MIN5, POWER_MIN6, POWER_MIN7, POWER_MIN8,
                    #POWER_MIN9, POWER_MIN10, POWER_MIN11, POWER_MIN12, POWER_MIN13, POWER_MIN14, POWER_MIN15, POWER_MIN16,
                    #POWER_MIN17,
                    POWER_MIN_MAX_DELTA,
                    CHANGE_DIST_VALUE_7_8, CHANGE_DIST_VALUE_15_16, CHANGE_DIST_VALUE_5_6
                    ):
    strategy = "EAStrategy3c"
    config = {
        "CHANGE_POWER_VALUE1": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE2": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE3": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE4": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE5": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE6": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE7": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE8": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE9": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE10": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE11": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE12": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE13": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE14": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE15": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE16": CHANGE_POWER_VALUE1,
        "CHANGE_POWER_VALUE17": CHANGE_POWER_VALUE1,
        "POWER_MIN1": POWER_MIN1,
        "POWER_MIN2": POWER_MIN1,
        "POWER_MIN3": POWER_MIN1,
        "POWER_MIN4": POWER_MIN1,
        "POWER_MIN5": POWER_MIN1,
        "POWER_MIN6": POWER_MIN1,
        "POWER_MIN7": POWER_MIN1,
        "POWER_MIN8": POWER_MIN1,
        "POWER_MIN9": POWER_MIN1,
        "POWER_MIN10": POWER_MIN1,
        "POWER_MIN11": POWER_MIN1,
        "POWER_MIN12": POWER_MIN1,
        "POWER_MIN13": POWER_MIN1,
        "POWER_MIN14": POWER_MIN1,
        "POWER_MIN15": POWER_MIN1,
        "POWER_MIN16": POWER_MIN1,
        "POWER_MIN17": POWER_MIN1,
        "POWER_MIN_MAX_DELTA": POWER_MIN_MAX_DELTA,
        "CHANGE_DIST_VALUE_7_8": CHANGE_DIST_VALUE_7_8,
        "CHANGE_DIST_VALUE_15_16": CHANGE_DIST_VALUE_15_16,
        "CHANGE_DIST_VALUE_5_6": CHANGE_DIST_VALUE_5_6,
    }
    _execute_simulator(jar_file, cli_path, strategy, config, tmp_path)
