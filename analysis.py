from pydriller import GitRepository
import sys
from pycodestyle import *

# TODO: check trailing whitespace? W291, W292, W293, W391
# TODO: W503, W504? says not enforced by PEP8?
INDENT_ERRORS = ["E111", "E112", "E113", "E121", "E122", "E123", "E124",
                 "E125", "E126", "E127", "E128", "E129", "E131", "E133"]
TABS_SPACES_ERRORS = ["E101", "E223", "E224", "E242", "E273", "E274", "W191"]
LINE_LENGTH_ERRORS = ["E501"]
BLANK_LINE_ERRORS = ["E301", "E302", "E303", "E304", "E305", "E306"]
IMPORT_ERRORS = ["E401", "E402"]

#for f in GitRepository("https://github.com/django/django.git").files():
#    print('file name: ', f)

"""repo = GitRepository("django")
files = repo.files()
print(files)"""

# TODO: WORKS. commented out to test parsing
"""files = GitRepository("django").files()
for f in files:
    # open only the python files
    if f[len(f)-3:] == ".py":
        pass
        #print(f)"""


def error_msg(report, prefix):
    msg = report.get_statistics(prefix)[0]
    index = msg.index(prefix)
    fin = msg[index+len(prefix)+1:]
    return fin


def check_errors(counters, report, header, macro, clean):
    print("{} Errors:".format(header))
    has_errs = False
    for err in macro:
        if err in counters:
            has_errs = True
            print("    {}, {} occurrence{} {}".format(err, counters[err],
                                                       "s:" if counters[err] > 1 else ": ",
                                                       error_msg(report, err)))
    if not has_errs:
        print("    None. {} statements conform to PEP 8.".format(clean))

#TODO: write wiki for indents
def check_indents(counters, report):
    check_errors(counters, report, "Indentation", INDENT_ERRORS, "Indentations of")

def check_tabs_spaces(counters, report):
    check_errors(counters, report, "Tabs vs. Spaces", TABS_SPACES_ERRORS, "Space-indented")

def check_line_length(counters, report):
    check_errors(counters, report, "Line Length", LINE_LENGTH_ERRORS, "Line length of")

def check_blank_lines(counters, report):
    check_errors(counters, report, "Blank Line", BLANK_LINE_ERRORS, "Blank line")

def check_imports(counters, report):
    check_errors(counters, report, "Import Statement", IMPORT_ERRORS, "Import")


def main(argv):
    # TODO: error-check input
    print(type(argv))
    print(argv)
    # TODO: clean up
    file_name = argv[0]
    # Collect the PEP8 reported errors according to pycodestyle.
    sg = StyleGuide()
    breport = BaseReport(options=sg.options)
    quiet_checker = Checker(file_name, report=breport)
    quiet_checker.check_all()
    counters = breport.counters
    print(counters)  # TODO: delete this
    print()

    # TODO: if a runtime error is thrown (E901, E902), still analyze the rest?

    # Divide the errors into categories
    check_indents(counters, breport)
    check_tabs_spaces(counters, breport)
    check_line_length(counters, breport)
    check_blank_lines(counters, breport)
    check_imports(counters, breport)

    # loud_checker = Checker("messy/imports.py", report=StandardReport(options=sg.options))
    # loud_checker.check_all()


if __name__ == '__main__':
    #main()
    main(sys.argv[1:])
    #main("messy/hodgepodge.py")