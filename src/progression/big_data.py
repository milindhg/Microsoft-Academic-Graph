from itertools import groupby
from collections import defaultdict
import operator
file = open('C:\\Users\\ritesh717\\Downloads\\keyword_year.csv', 'r')

str = raw_input("Type in a keyword")
count = 0
d = {}
for line in file:
    items = line.rstrip('\n').split(',')
    if items[0] == str:
        if items[0] in d:
            #print d
            #raw_input()
            d[items[0]].append(items[1])
        else:
            d[items[0]] = [items[1]]
    count += 1

list_values = d[str]

max_freq = max([len(list(group)) for key, group in groupby(list_values)])
d1 = defaultdict(int)
for i in list_values:
    #print type(i)
    #raw_input()
    d1[i] += 1
result = max(d1.iteritems(), key=lambda x: x[1])
#print count
print "List of items", sorted(d1.items(), key=operator.itemgetter(1), reverse = 1)
print "Most popular year for the keyword is ", result

file = open('C:\\Users\\ritesh717\\Downloads\\keyword_year.csv', 'r')
str1 = raw_input("\n Type in a year")
count1 = 0
dl = {}
for line in file:
    items = line.rstrip('\n').split(',')
    if items[1] == str1:
        if items[1] in dl:
            #print d
            #raw_input()
            dl[items[1]].append(items[0])
        else:
            dl[items[1]] = [items[0]]
    count += 1

#print dl
list_values1 = dl[str1]

max_freq1 = max([len(list(group)) for key, group in groupby(list_values1)])
dl1 = defaultdict(int)
for i in list_values1:
    #print type(i)
    #raw_input()
    dl1[i] += 1
result = max(dl1.iteritems(), key=lambda x: x[1])
#print count
print "List of items", sorted(dl1.items(), key=operator.itemgetter(1), reverse = 1)
print "Most popular keyword for the year", str1, "is", result