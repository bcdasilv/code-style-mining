import json
import string # TODO: delete unnecessary inputs
import sys
from pycodestyle import *

import naming_analysis as naming

# TODO: check trailing whitespace? W291, W292, W293, W391
# TODO: W503, W504? says not enforced by PEP8?
INDENT_ERRORS = ["E111", "E112", "E113", "E121", "E122", "E123", "E124",
                 "E125", "E126", "E127", "E128", "E129", "E131", "E133"]
TABS_SPACES_ERRORS = ["E101", "E223", "E224", "E242", "E273", "E274", "W191"]
LINE_LENGTH_ERRORS = ["E501"]
BLANK_LINE_ERRORS = ["E301", "E302", "E303", "E304", "E305", "E306"]
IMPORT_ERRORS = ["E401", "E402"]


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


# Compares the file analysis results from Pycodestyle to the list
# of expected errors passed in through the macro
# Parameters:
# - counters: the error count results from Pycodestyle
# - report: the file analysis results from Pycodestyle
# - macro: the file error codes to consider; a sort of "filter" of the
#   Pycodestyle error codes of interest for this section
# Returns a tuple of dictionary of JSON results and number of errors found
# Note: This function assumes PEP 8 and Google style guides are the
# same for the style element being analyzed
# If the style guides differ, do not use this function
def json_check_errors(counters, report, macro):
    temp_dict = {"pep": None, "google": None, "errors": None}
    has_errs = False
    count = 0
    for err in macro:
        if err in counters:
            if not has_errs:
                has_errs = True
                temp_dict["pep"] = False
                temp_dict["google"] = False
                temp_dict["errors"] = {err: counters[err]}
            else:
                temp_dict["errors"][err] = counters[err]
            count += counters[err]
    if not has_errs:
        temp_dict["pep"] = True
        temp_dict["google"] = True
    return temp_dict, count


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

def count_total_file_errors(dict):
    count = 0
    for d in dict.keys():
        if dict[d] is None:
            pass
        elif d is "naming_analysis":
            for type in dict[d].keys():
                count += dict[d][type]["occurrences"]
        else:
            if dict[d]["errors"] is None:
                pass
            else:
                for code in dict[d]["errors"].keys():
                    count += dict[d]["errors"][code]
    return count

# Create the dictionary of analysis result values to be converted into JSON output
def create_json_dict(counters, report, file_name):
    obj = {"total_file_errors": None}
    names = naming.naming_results(file_name)
    indents = json_check_errors(counters, report, INDENT_ERRORS)
    tabs = json_check_errors(counters, report, TABS_SPACES_ERRORS)
    length = json_check_errors(counters, report, LINE_LENGTH_ERRORS)
    blanks = json_check_errors(counters, report, BLANK_LINE_ERRORS)
    imports = json_check_errors(counters, report, IMPORT_ERRORS)
    obj["naming_analysis"] = names[0]
    obj["indentation_analysis"] = indents[0]
    obj["tabs_vs_spaces_analysis"] = tabs[0]
    obj["line_length_analysis"] = length[0]
    obj["blank_lines_analysis"] = blanks[0]
    obj["imports_analysis"] = imports[0]
    obj["file_encoding_analysis"] = None
    obj["total_file_errors"] = names[1] +indents[1] + tabs[1] + length[1] + blanks[1] + imports[1]
    return obj


def collect_file_dict_results(file_name):
    # Collect the PEP8 reported errors according to Pycodestyle.
    sg = StyleGuide()
    # TODO: should be able to "max_line_length=80" in the parens, but not working. add Google line length support
    breport = BaseReport(options=sg.options)
    # TODO: check file actually exists. otherwise falsely outputting that it is compliant
    quiet_checker = Checker(file_name, report=breport)
    quiet_checker.check_all()
    counters = breport.counters
    # TODO: if a runtime error is thrown (E901, E902), still analyze the rest?
    js = create_json_dict(counters, breport, file_name)
    naming.cleanup()
    return js

def check_input(str):
    # TODO: double-check this function
    temp = str
    msg = "That is not a valid file name. Please double-check your input."
    if " " in temp:
        temp = temp.replace(" ", "")
    for s in string.whitespace:
        if s == " ":
            pass
        else:
            if s in temp:
                raise ValueError(msg)


def main(argv):
    # TODO: pycodestyle always throws EXTRANEOUS_WHITESPACE_REGEX ?
    file_name = argv[0]
    check_input(file_name)
    res = collect_file_dict_results(file_name)
    print(json.dumps(res))
    return res

if __name__ == '__main__':
    main(sys.argv[1:])