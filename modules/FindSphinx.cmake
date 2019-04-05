# Copyright (c) 2019 Cisco and/or its affiliates.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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

