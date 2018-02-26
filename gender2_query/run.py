import csv
import urllib2
import json
import time
import sys


BASE_URL = 'https://api.genderize.io/?'
API_KEY = '8ee87528e6948e9bf1304ff1c44d4600'

name_set = set()
csv_file1 = open('gender_tmp.csv')
csv1 = csv.reader(csv_file1, delimiter=',')
gender_list = list(csv1)
for row in gender_list[1:]:
    name_set.add(row[0])
csv_file1.close()

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
        response = urllib2.urlopen(query).read().decode("utf-8")
    except:
        print query
        sys.stdout.flush()
        file4.write('Fail response: %s\n' % name)
        file4.flush()

    if response is not None:
        jsonobj = json.loads(response)
        if jsonobj.get('name') != name:
            file4.write('Return Name not matched: request - %s response - %s\n' % (name, jsonobj.get('name')))
            file4.flush()
        else:
            tmp = [jsonobj.get('name'), jsonobj.get('gender') or '', str(jsonobj.get('probability') or ''), str(jsonobj.get('count') or '')]
            csv_file3.write(','.join(tmp) + '\n')
            csv_file3.flush()
    else:
        file4.write('None response: %s\n' % name)
        file4.flush()


csv_file3.close()
file4.close()



'''
with open(file_name) as myfile:
    lines = myfile.readlines()
    #row0 += ['gender', 'probability', 'count']
    res_file = open('gender_tmp.csv',"w+")

    for line in lines:
        idx1 = line.find('/?')
        idx2 = line.find('&')

        fname = line[idx1: idx2][7:]

        if fname not in table:
            try:
                print fname.replace('%20', ' ')
                query = ('%sname=%s' % (BASE_URL, fname.replace(' ', '%20')))
                response = urllib2.urlopen(query).read().decode("utf-8")
                data = json.loads(response)
                if data.get('gender') is not None:
                    tmp = [fname.replace('%20', ' '), data.get('gender') or '', str(data.get('probability') or ''), str(data.get('count') or '')]
                    res_file.write(','.join(tmp) + '\n')
                    table[fname] = tmp[:]
            except:
                print(query)

    res_file.close()
'''
