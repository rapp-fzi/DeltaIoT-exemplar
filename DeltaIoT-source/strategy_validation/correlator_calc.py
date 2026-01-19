import tabulate
import pandas as pd


class CorrelatorCalc:
    def calc_correlation(self, args):
        print("Using correlation data from: %s" % args.correlation)
        df = pd.read_csv(args.correlation)

        headers = ['Kind', 'Correlation']
        table_entries = []

        corr_pearson = df["Reward"].corr(
            df["Score"],
            method="pearson"  # "pearson" (default), "spearman", or "kendall"
        )
        table_entries.append(("Pearson", corr_pearson))

        corr_spearman = df["Reward"].corr(
            df["Score"],
            method="spearman"  # "pearson" (default), "spearman", or "kendall"
        )
        table_entries.append(("Spearman", corr_spearman))

        table_str = tabulate.tabulate(table_entries,
                                      headers=headers,
                                      tablefmt="simple"
                                      )
        print(table_str)
