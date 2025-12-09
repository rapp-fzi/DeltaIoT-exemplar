from enum import Enum
from dataclasses import dataclass


@dataclass
class Input:
    extra_arguments: list


class StrategyKind(Input, Enum):
    NONE = ["noadaption"]
    DEFAULT = ["strategy", "-a", "Default", "-p", "empty.json"]
    EADEFAULT = ["strategy", "-a", "EADefault", "-p", "empty.json"]
