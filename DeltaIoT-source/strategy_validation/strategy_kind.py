from enum import Enum
from dataclasses import dataclass


@dataclass
class Input:
    extra_arguments: list


class StrategyKind(Input, Enum):
    NONE = ["noadaption"]
    DEFAULT = ["strategy", "-a", "Default", "-p", "empty.json"]
    EADEFAULT = ["strategy", "-a", "EADefault", "-p", "empty.json"]
    EASTRATEGY0A = ["strategy", "-a", "EAStrategy0a"]
    EASTRATEGY0B = ["strategy", "-a", "EAStrategy0b"]
    EASTRATEGY0C = ["strategy", "-a", "EAStrategy0c"]
    EASTRATEGY0D = ["strategy", "-a", "EAStrategy0d"]
