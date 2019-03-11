find_program(SPHINX_EXECUTABLE NAMES sphinx-build
        HINTS
        $ENV{SPHINX_DIR}
        PATH_SUFFIXES bin
        DOC "Sphinx documentation generator"
        )

include(FindPackageHandleStandardArgs)

if(NOT SPHINX_EXECUTABLE)
    message(STATUS "Sphinx generator not found! Skipping setup for documentation generator.")
    set(SPHINX_FOUND false)
else()
    find_package_handle_standard_args(Sphinx DEFAULT_MSG
            SPHINX_EXECUTABLE
            )

    mark_as_advanced(SPHINX_EXECUTABLE)
    set(SPHINX_FOUND true)
endif()

