import pycodestyle

#sg = pycodestyle.StyleGuide()
#execute_pep8({'ignore': sg.options.ignore, 'select': sg.options.select, 'max_line_length': sg.options.max_line_length,}, "messy/for.py")


"""infile_name = files[0]
infile = open(infile_name, 'r')
print(infile.read())
infile.close()"""

"""def execute_pep8(pep8_options, source):
    #Execute pycodestyle via python method calls.
    class QuietReport(pycodestyle.BaseReport):

        #Version of checker that does not print.

        def __init__(self, options):
            super(QuietReport, self).__init__(options)
            self.__full_error_results = []

        def error(self, line_number, offset, text, check):
            #Collect errors.
            code = super(QuietReport, self).error(line_number,
                                                  offset,
                                                  text,
                                                  check)
            if code:
                self.__full_error_results.append(
                    {'id': code,
                     'line': line_number,
                     'column': offset + 1,
                     'info': text})

        def full_error_results(self):
            # Return error results in detail.
            # Results are in the form of a list of dictionaries. Each
            # dictionary contains 'id', 'line', 'column', and 'info'.
            return self.__full_error_results

    checker = pycodestyle.Checker('', lines=source, reporter=QuietReport,
                                  **pep8_options)
    checker.check_all()
    return checker.report.full_error_results()"""
