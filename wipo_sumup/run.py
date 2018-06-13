import csv
import sys


print('first_name,country_code,gender,probability,count')

in_files = ['wgnd_ctry.csv', 'wgnd_langctry.csv', 'wgnd_noctry.csv', 'wgnd_source.csv']
name_idx = [0, 0, 0, 0]
country_idx = [1, 5, -1, 1]
gender_idx = [2, 1, 1, 2]
writer = csv.writer(sys.stdout)
# Only counts when country_code is None
table_name_cnt = {}
table_gender_cnt = {}
# Only counts when country_code is not None
table_name2_cnt = {}
table_gender2_cnt = {}

for i, in_file in enumerate(in_files):
    in_file = open(in_file)
    reader = csv.reader(in_file)
    for row in reader:
        if len(row[name_idx[i]]) == 0 or row[gender_idx[i]] == '?' or len(row[gender_idx[i]]) == 0:
            continue

        name = row[name_idx[i]]
        country_code = row[country_idx[i]] if country_idx[i] >= 0 else ''
        space_idx = []
        for j, c in enumerate(name):
            if c == ' ':
                space_idx.append(j)

        for j in space_idx:
            tmp_name = name[:j].lower()
            table_name_cnt[tmp_name] = table_name_cnt.get(tmp_name, 0) + 1
            table_gender_cnt[tmp_name] = table_gender_cnt.get(tmp_name, 0) + (1 if row[gender_idx[i]] == 'M' else 0)

            if len(country_code) == 0:
                continue
            pair = (tmp_name.lower(), country_code.lower())
            table_name2_cnt[pair] = table_name2_cnt.get(pair,0) + 1
            table_gender2_cnt[pair] = table_gender2_cnt.get(pair, 0) + (1 if row[gender_idx[i]] == 'M' else 0)

for name in table_name_cnt:
    probability = float(table_gender_cnt[name]) / float(table_name_cnt[name])
    gender = 'male' if probability >= 0.5 else 'female'
    if probability < 0.5:
        probability = 1 - probability
    cnt = table_name_cnt[name]
    writer.writerow([name, '', gender, probability, cnt])

for name, country_code in table_name2_cnt:
    probability = float(table_gender2_cnt[(name, country_code)]) / float(table_name2_cnt[(name, country_code)])
    gender = 'male' if probability >= 0.5 else 'female'
    if probability < 0.5:
        probability = 1 - probability
    cnt = table_name2_cnt[(name, country_code)]
    writer.writerow([name, country_code, gender, probability, cnt])
