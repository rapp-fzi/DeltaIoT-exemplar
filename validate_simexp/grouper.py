
def group_entries(entries):
    groups = {}
    for item in entries:
        key = str(item['Values'])
        groups.setdefault(key, []).append(item)
    return groups

