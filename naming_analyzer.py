import ast
import json
import string
import sys

# Naming Styles
CW = "CapWords"
LOWER = "lowercase"
MIXED = "mixedCase"
SNAKE_CW = "Capwords_Snakecase"
SNAKE_LOWER = "lower_snakecase"
SNAKE_MIXED = "mixed_Snakecase"
SNAKE_UPPER = "UPPER_SNAKECASE"
UPPER = "UPPERCASE"

FUNC_NAMES = []
CLASS_NAMES = []

TUP_PEP = 0
TUP_GOOG = 1
TUP_COUNT = 2

# JSON Field Names
CLASSES = "classes"
COUNT = "definitions"
FUNCS = "functions"
GOOGLE = "google"
PEP = "pep"
OCCURS = "occurrences"


class FuncLister(ast.NodeVisitor):
    def visit_FunctionDef(self, node):
        collect_function_names(node.name)
        self.generic_visit(node)

    def visit_ClassDef(self, node):
        collect_class_names(node.name)
        self.generic_visit(node)

def check_name_style(s):
    ind = s.find("_")
    if ind is not -1: # TODO: __init__ cases?
        if s.islower():
            return SNAKE_LOWER
        if s.isupper():
            return SNAKE_UPPER
        if s.istitle():
            return SNAKE_CW
        else:
            return SNAKE_MIXED
    else:
        if s.islower():
            return LOWER
        if s.isupper():
            return UPPER
        if s[0:1].isupper():
            return CW
        else:
            return MIXED


def collect_class_names(name):
    global CLASS_NAMES
    CLASS_NAMES.append(name)


def collect_function_names(name):
    global FUNC_NAMES
    FUNC_NAMES.append(name)


def check_class_names():
    pep = True
    google = True
    count = 0
    for name in CLASS_NAMES:
        if check_name_style(name) is not CW:
            pep = False
            google = False
            count += 1
    return pep, google, count


def check_function_names():
    pep_temp = True
    google_temp = True
    count_temp = 0
    for name in FUNC_NAMES:
        style = check_name_style(name)
        if style is not SNAKE_LOWER and style is not LOWER:
            pep_temp = False
            google_temp = False
            count_temp += 1
    return pep_temp, google_temp, count_temp


def collect_json_dict(classes, funcs):
    count = 0
    d = {CLASSES: {COUNT: len(CLASS_NAMES), PEP: classes[TUP_PEP],
                   GOOGLE: classes[TUP_GOOG], OCCURS: classes[TUP_COUNT]},
         FUNCS: {COUNT: len(FUNC_NAMES), PEP: funcs[TUP_PEP],
                GOOGLE: funcs[TUP_GOOG], OCCURS: funcs[TUP_COUNT]}}
    for key in d.keys():
        count += d[key][OCCURS]
    return d, count


def naming_results(file_name):
    with open(file_name, "r") as f:
        tree = ast.parse(f.read())
    FuncLister().visit(tree)
    classes = check_class_names()
    funcs = check_function_names()
    return collect_json_dict(classes, funcs)


def cleanup():
    global CLASS_NAMES, FUNC_NAMES
    CLASS_NAMES = []
    FUNC_NAMES = []


def check_input(str):
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
    file_name = argv[0]
    check_input(file_name)
    print(json.dumps(naming_results(file_name)))
    return naming_results(file_name)


if __name__ == '__main__':
    main(sys.argv[1:])