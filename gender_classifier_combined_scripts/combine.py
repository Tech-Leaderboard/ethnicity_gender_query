import sys
import csv

with open('tmp.csv', mode="r") as infile:
    reader = csv.reader(infile)
    my_list = list(reader)
    my_list[0] = my_list[0][:2] + ['com_gender', 'com_probability', 'com_count'] + my_list[0][2:]

    print isinstance(my_list[1][3], str)
    for i in range(1, len(my_list)):
        row = my_list[i]
        ssn_gender, ssn_probability, ssn_count = row[2:5]
        wipo_gender, wipo_probability, wipo_count = row[5:8]
        gi_country_code, gi_gender, gi_probability, gi_count = row[8:12]
        ga_country_code, ga_gender, ga_probability, ga_count = row[12:]

        com_gender, com_probability, com_count = None, None, None
        if ssn_gender and ssn_probability >= '0.95':
            com_gender, com_probability, com_count = ssn_gender, ssn_probability, ssn_count
        elif wipo_gender and wipo_probability >= '0.95':
            com_gender, com_probability, com_count = wipo_gender, wipo_probability, wipo_count
        elif gi_gender and gi_probability >= '0.95':
            com_gender, com_probability, com_count = gi_gender, gi_probability, gi_count
        elif ga_gender and ga_probability >= '0.95':
            com_gender, com_probability, com_count = ga_gender, ga_probability, ga_count

        if com_gender is None:
            if ssn_gender:
                com_gender, com_probability, com_count = ssn_gender, ssn_probability, ssn_count
            elif wipo_gender:
                com_gender, com_probability, com_count = wipo_gender, wipo_probability, wipo_count
            elif gi_gender:
                com_gender, com_probability, com_count = gi_gender, gi_probability, gi_count
            elif ga_gender:
                com_gender, com_probability, com_count = ga_gender, ga_probability, ga_count

        my_list[i] = my_list[i][:2] + [com_gender, com_probability, com_count] + my_list[i][2:]

    with open('combined.csv', mode="w") as outfile:
        writer = csv.writer(outfile, delimiter=',')
        writer.writerows(my_list)
