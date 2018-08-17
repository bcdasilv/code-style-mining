from pydriller import GitRepository
from pycodestyle import *

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

# TODO: verify that the split works properly on other categories
def error_msg(report, prefix):
    msg = report.get_statistics(prefix)[0]
    index = msg.index(prefix)
    fin = msg[index+len(prefix)+1:]
    return fin


def check_imports(counters, report):
    has_errs = False
    for err in IMPORT_ERRORS:
        if err in counters:
            has_errs = True
            print("{}, {} occurrences: {}".format(err, counters[err], error_msg(report, err)))
    if not has_errs:
        print("Import statements conform to PEP8.")


def main():
    # Collect the PEP8 reported errors according to pycodestyle.
    sg = StyleGuide()
    breport = BaseReport(options=sg.options)
    quiet_checker = Checker("messy/for.py", report=breport)
    quiet_checker.check_all()
    counters = breport.counters
    print(counters)  # TODO: delete this
    print()
    # Divide the errors into categories
    check_imports(counters, breport)

    # loud_checker = Checker("messy/imports.py", report=StandardReport(options=sg.options))
    # loud_checker.check_all()


if __name__ == '__main__':
    main()
