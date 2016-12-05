import sys
import re

file = open(sys.argv[1])
file_list =  file.readlines()

mutator = False
categ =[]
total = []
killed =[]
no_coverage =[]
MS =[]

for line in file_list:
    if "- Mutators" in line:
        mutator = True
        continue

    if(mutator): 
        if "org.pitest.mutationtest" in line:
            key = line[49:]
            categ.append(key)
#           print key
            continue
 
        if "Generated" in line:
            total0 = re.match('.*Generated\s(\d+)',line)
            killed0 = re.match('.*Killed\s(\d+)',line)
            total0 = total0.group(1)
            killed0 = killed0.group(1)
        
            total.append(float(total0))
            killed.append(float(killed0))

#           print "total: " + str(total0)
#           print "killed: " + str (killed0)

        if "NO_COVERAGE" in line:
            no_coverage0 = re.match('.*NO_COVERAGE\s(\d+)',line)
            no_coverage0 = no_coverage0.group(1)
        
            no_coverage.append(float(no_coverage0))
#           print "no_coverage: " + str(no_coverage[0])

i = 0
killed_all = 0
total_all = 0
no_coverage_all = 0 

for elem in categ:
    ratio = float(killed[i]/(total[i] - no_coverage[i]))    
    MS.append(ratio)
#    print elem.strip() 
    print str(MS[i]*100) 
    
    killed_all = float(killed_all) + killed[i]
    total_all = float(total_all) + total[i]
    no_coverage_all = float(no_coverage_all) - no_coverage[i]
#    print "total: " + str(total[i])
#    print "killed: " + str(killed[i])
#    print "NoCoverage: " + str(no_coverage[i])
    i = i + 1

ms_all = float(killed_all/(total_all - no_coverage_all)) * 100
print "Mutation Score all: " + str(ms_all)

