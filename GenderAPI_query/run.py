# -*- encoding: utf-8 -*-
import csv
import requests
import json
import time
import sys


BASE_URL = 'https://gender-api.com/get?key=MuufXFrtYBUGeCWnXQ'

name_set = set()
# results cached file
csv_file1 = open('gender_tmp.csv')
csv1 = csv.reader(csv_file1, delimiter=',')
gender_list = list(csv1)
for row in gender_list[1:]:
    name_set.add('\t'.join(row[:2]))
csv_file1.close()

# first names & country code to be queried
csv_file2 = open('names2query.csv')
csv2 = csv.reader(csv_file2, delimiter=',')
name_list = ['\t'.join(row[:2]) for row in list(csv2)[1:]]

csv_file3 = open('gender_tmp.csv', "a+")
file4 = open('fail.txt', "a+")

name_list = list(set(name_list) - name_set)
print('# of names to fetch: %s' % len(name_list))
print(name_list)
for name in name_list:
    name, country_code = name.split('\t')
    if len(name) < 1:
        continue

    name2 = name.replace(' ', '%20')
    query = BASE_URL
    query += '&name={}{}'.format(name2, ('&&country=' + country_code) if country_code else '')
    #print(query)

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
            #print(jsonobj)
        except Exception as e:
            print(e)
            sys.stdout.flush()
            file4.write('Fail to parse response to json: %s\n' % response)
            file4.flush()
            continue

        if jsonobj.get('name').encode('utf-8') != name:
            file4.write('Return Name not matched: request - %s response - %s\n' % (name, jsonobj.get('name').encode('utf-8')))
            file4.flush()

        tmp = [name, country_code, (jsonobj.get('gender') or '').encode('utf-8'), str(jsonobj.get('accuracy') or ''), str(jsonobj.get('samples') or '')]
        csv_file3.write(','.join(tmp) + '\n')
        csv_file3.flush()
    else:
        file4.write('None response: %s\n' % name)
        file4.flush()


csv_file2.close()
csv_file3.close()
file4.close()
