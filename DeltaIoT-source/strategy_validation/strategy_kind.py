from enum import Enum
from dataclasses import dataclass


@dataclass
class Input:
    type: str
    extra_arguments: list


class StrategyKind(Input, Enum):
    NONE = "none", []
    DEFAULT = "default", ["strategy", "-a", "Default", "-p", "empty.json"]
    EADEFAULT = "eadefault", ["strategy", "-a", "EADefault", "-p", "empty.json"]
