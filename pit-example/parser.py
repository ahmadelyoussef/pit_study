import sys
files = open("trace.txt")
file_list =  files.readlines()

key = sys.argv[1]
for each_line in file_list:
    if(key in each_line):
        print each_line
