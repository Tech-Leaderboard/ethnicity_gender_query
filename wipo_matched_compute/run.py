pfile = open('names_patents.csv')
wfile = open('names_w.csv')

plines = pfile.readlines()
wlines = wfile.readlines()

wset = set()
for line in wlines[1:]:
    line = line.strip()
    for i in range(2, len(line)):
        wset.add(line[:i])
print("size of wset: {}".format(len(wset)))

cnt = 0
for line in plines[1:]:
    line = line.strip()
    if line in wset:
        cnt += 1

print("Matched patents name ratio: {} / {}".format(cnt, len(plines) - 1))
