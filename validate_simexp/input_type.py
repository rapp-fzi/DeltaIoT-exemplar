import csv
import json
from enum import Enum
from typing import Callable
from dataclasses import dataclass

@dataclass
class Input:
    type: str
    load: Callable

def _read_csv_file(csv_file):
    fieldnames = ["Generation", "Reward", "Values"]
    reader = csv.DictReader(csv_file, fieldnames=fieldnames, delimiter=";")
    next(reader)  # skip header
    result = []
    for row in reader:
        generation = int(row['Generation'])
        reward = row['Reward']
        raw_values = row['Values'].split(",")
        values = {}
        for raw_value in raw_values:
            name, val = raw_value.split(":")
            values[name] = val.strip()
        result.append({"Generation": generation, "Reward": reward, "Values": values})
    return result


def _read_json_file(json_file):
    content = json.load(json_file)
    result = []
    for entry in content:
        generation = int(entry['generation'])
        reward = entry['reward']
        values = {name: value for name, value in entry['optimizables'].items()}
        result.append({"Generation": generation, "Reward": reward, "Values": values})
    return result


class InputType(Input, Enum):
    CSV = "csv", _read_csv_file
    JSON = "json", _read_json_file
