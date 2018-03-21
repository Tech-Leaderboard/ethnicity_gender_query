# -*- encoding: utf-8 -*-
import csv
import requests
import json
import time
import sys


BASE_URL = 'https://api.genderize.io/?'
API_KEY = '8ee87528e6948e9bf1304ff1c44d4600'

name_set = set()
# results cached file
csv_file1 = open('gender_tmp.csv')
csv1 = csv.reader(csv_file1, delimiter=',')
gender_list = list(csv1)
for row in gender_list[1:]:
    name_set.add(row[0])
csv_file1.close()

# first names to be queried
csv_file2 = open('names2query.csv')
name_list = csv_file2.readlines()
name_list = [x.strip() for x in name_list]
csv_file2.close()

csv_file3 = open('gender_tmp.csv', "a")
file4 = open('fail.txt', "w+")

name_list = list(set(name_list) - name_set)
print('# of names to fetch: %s' % len(name_list))
for name in name_list:
    name2 = name.replace(' ', '%20')
    query = BASE_URL
    query += 'name={}&apikey={}'.format(name2, API_KEY)

    response = None
    try:
        response = requests.get(query)
    except Exception as e:
        print(e)
        sys.stdout.flush()
        file4.write('Fail response: %s\n' % name)
        file4.flush()

    if response is not None:
        try:
            jsonobj = response.json()
        except Exception as e:
            print(e)
            sys.stdout.flush()
            file4.write('Fail to parse response to json: %s\n' % response)
            file4.flush()
            continue

        if jsonobj.get('name').encode('utf-8') != name:
            file4.write('Return Name not matched: request - %s response - %s\n' % name, jsonobj.get('name').encode('utf-8'))
            file4.flush()
        else:
            tmp = [name, (jsonobj.get('gender') or '').encode('utf-8'), str(jsonobj.get('probability') or ''), str(jsonobj.get('count') or '')]
            csv_file3.write(','.join(tmp) + '\n')
            csv_file3.flush()
    else:
        file4.write('None response: %s\n' % name)
        file4.flush()


csv_file3.close()
file4.close()
