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
    EASTRATEGY1A = ["strategy", "-a", "EAStrategy1a"]
    EASTRATEGY1B = ["strategy", "-a", "EAStrategy1b"]
    EASTRATEGY1C = ["strategy", "-a", "EAStrategy1c"]
    EASTRATEGY1D = ["strategy", "-a", "EAStrategy1d"]
    EASTRATEGY2D = ["strategy", "-a", "EAStrategy2d"]
