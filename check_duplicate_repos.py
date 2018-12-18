
file_name = "v02.txt" #TODO: parameterize

#TODO: clean up this code and make it more efficient
def check_duplicate_repos():
    infile = open(file_name, 'r')
    lines = infile.readlines()
    for i in range(len(lines)):
        if lines[i].startswith(" ") or lines[i] == "" or lines[i] == "\n":
            continue
        temp = lines[i]
        if temp.startswith("#"):
            temp = temp[1:len(temp)]
        print(temp)
        if check_duplicate_line(temp, lines, i+1):
            print("Duplicate repo found: ", temp)
            return True
    print("No duplicates")
    return False

#TODO: It doesn't detect duplicates in the error section of the file
def check_duplicate_line(line, lines, index):
    for i in range(index, len(lines)):
        if lines[i].startswith(" ") or lines[i] == "" or lines[i] == "\n":
            continue
        temp = lines[i]
        if temp.startswith("#"):
            temp = temp[1:len(temp)]        
        if line.startswith(temp):
            return True
    return False

def main():
    print("Starting checking...")
    check_duplicate_repos()
    print("Final checking.")

main()
