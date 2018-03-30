import csv
import sys

print('f_name,gender,probability,count')

in_files = ['wgnd_ctry.csv', 'wgnd_langctry.csv', 'wgnd_noctry.csv', 'wgnd_source.csv']
name_idx = [0, 0, 0 , 0]
gender_idx = [2, 1, 1, 2]
writer = csv.writer(sys.stdout)
table_name_cnt = {}
table_gender_cnt = {}

for i, in_file in enumerate(in_files):
    in_file = open(in_file)
    reader = csv.reader(in_file)
    for row in reader:
        if len(row[name_idx[i]]) == 0 or row[gender_idx[i]] == '?':
            continue

        name = row[name_idx[i]]
        space_idx = []
        for j, c in enumerate(name):
            if c == ' ':
                space_idx.append(j)

        for j in space_idx:
            tmp_name = name[:j].lower()
            table_name_cnt[tmp_name] = table_name_cnt.get(tmp_name, 0) + 1
            table_gender_cnt[tmp_name] = table_gender_cnt.get(tmp_name, 0) + (1 if row[gender_idx[i]] == 'M' else 0)
        table_name_cnt[name.lower()] = table_name_cnt.get(name.lower(),0) + 1
        table_gender_cnt[name.lower()] = table_gender_cnt.get(name.lower(), 0) + (1 if row[gender_idx[i]] == 'M' else 0)

for name in table_name_cnt:
    probability = float(table_gender_cnt[name]) / float(table_name_cnt[name])
    gender = 'male' if probability >= 0.5 else 'female'
    if probability < 0.5:
        probability = 1 - probability
    cnt = table_name_cnt[name]

    writer.writerow([name, gender, probability, cnt])
