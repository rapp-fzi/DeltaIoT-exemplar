from tabulate import tabulate, SEPARATING_LINE

from grouper import group_entries


class Show:
    def __init__(self):
        pass

    def show_generations(self, entries, score_entries):
        table_entries = []
        groups = group_entries(entries)
        for i, entry in enumerate(groups.items()):
            group, group_generations = entry
            for generation in group_generations[:-1]:
                table_entries.append([generation["Generation"], generation["Reward"], None])
            generation = group_generations[-1]
            score = score_entries[group]["average_score"]
            table_entries.append([generation["Generation"], generation["Reward"], score])
            if i < len(groups.values()) - 1:
                table_entries.append(SEPARATING_LINE)

        table_str = tabulate(table_entries,
                             headers=['Generation', 'Reward', 'Average Score'],
                             tablefmt="simple"
                             )
        print(table_str)
