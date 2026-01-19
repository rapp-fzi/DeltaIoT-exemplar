from enum import Enum
from dataclasses import dataclass
from typing import Callable

import tabulate
import pandas as pd
from progress.bar import Bar

import numpy as np
from scipy.stats import pearsonr, spearmanr


@dataclass
class MethodMixin:
    corr_name: str
    method: Callable


class CorrelationMethod(MethodMixin, Enum):
    PEARSON = "Pearson", pearsonr
    SPEARMAN = "Spearman", spearmanr


class CorrelatorCalc:
    def calc_correlation(self, args):
        print("Using correlation data from: %s" % args.correlation)
        df = pd.read_csv(args.correlation)

        if args.seed is not None:
            np.random.seed(args.seed)

        headers = ['Kind', 'Correlation', 'p value', 'CI lower', 'CI upper', 'CI', 'Sample size']

        table_entries = []

        corr_pearson = self._correlation_with_ci(df["Reward"], df["Score"], CorrelationMethod.PEARSON)
        table_entries.append((corr_pearson["corr_name"], corr_pearson["correlation"],
                              corr_pearson["p_value"],
                              corr_pearson["ci_lower"], corr_pearson["ci_upper"], corr_pearson["ci"],
                              len(df["Reward"])
                              ))

        corr_spearman = self._correlation_with_ci(df["Reward"], df["Score"], CorrelationMethod.SPEARMAN)
        table_entries.append((corr_spearman["corr_name"], corr_spearman["correlation"],
                              corr_spearman["p_value"],
                              corr_spearman["ci_lower"], corr_spearman["ci_upper"], corr_spearman["ci"],
                              len(df["Reward"])
                              ))

        table_str = tabulate.tabulate(table_entries,
                                      headers=headers,
                                      tablefmt="simple"
                                      )
        print(table_str)

    def _correlation_with_ci(self, x, y, corr_method: CorrelationMethod, n_boot=10000, ci=95):
        """
        Compute correlation coefficient (Pearson or Spearman) with bootstrap confidence intervals.

        x, y : list
            Paired observations
        method : 'pearson' or 'spearman'
        n_boot : int
            Number of bootstrap resamples
        ci : float
            Confidence level (e.g., 95)
        """

        # Calculate observed correlation
        corr, p_value = corr_method.method(x, y)

        # Bootstrap confidence intervals
        boot_corrs = []
        n = len(x)
        with Bar("Bootstrap confidence intervals for: %s" % corr_method.corr_name, max=n_boot) as bar:
            for _ in range(n_boot):
                idx = np.random.choice(n, n, replace=True)
                boot_corr, _ = corr_method.method(x[idx], y[idx])
                boot_corrs.append(boot_corr)
                bar.next()

        # Two-sided CI
        alpha = (100 - ci) / 2
        ci_lower = np.percentile(boot_corrs, alpha)
        ci_upper = np.percentile(boot_corrs, 100 - alpha)

        return {
            'corr_name': corr_method.corr_name,
            'correlation': corr,
            'p_value': p_value,
            'ci_lower': ci_lower,
            'ci_upper': ci_upper,
            'ci': ci,
        }
