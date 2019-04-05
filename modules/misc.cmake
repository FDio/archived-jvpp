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

##############################################################################
# Highlight WARNING and ERROR messages
##############################################################################
function(message)
  list(GET ARGV 0 type)
  if("$ENV{TERM}" STREQUAL "xterm-256color" OR "$ENV{TERM}" STREQUAL "xterm-color" OR "$ENV{TERM}" STREQUAL "xterm")
    string(ASCII 27 esc)
    set(red "${esc}[1;31m")
    set(yellow "${esc}[1;33m")
    set(cyan "${esc}[36m")
    set(white "${esc}[37;1m")
    set(reset "${esc}[m")
  endif()
  if(type STREQUAL FATAL_ERROR OR type STREQUAL SEND_ERROR)
    list(REMOVE_AT ARGV 0)
    _message(${type} "${red}${ARGV}${reset}")
  elseif(type STREQUAL WARNING)
    list(REMOVE_AT ARGV 0)
    _message(STATUS "${yellow}${ARGV}${reset}")
  elseif(type STREQUAL STATUS)
    list(REMOVE_AT ARGV 0)
    _message(STATUS "${cyan}${ARGV}${reset}")
  elseif(type STREQUAL INFO)
    list(REMOVE_AT ARGV 0)
    _message("${white}${ARGV}${reset}")
  elseif(type STREQUAL INFO1)
    list(REMOVE_AT ARGV 0)
    _message("    ${white}${ARGV}${reset}")
  else()
    _message(${ARGV})
  endif()
endfunction()

##############################################################################
# aligned config output
##############################################################################
function(info desc val)
  if("$ENV{TERM}" STREQUAL "xterm-256color" OR "$ENV{TERM}" STREQUAL "xterm-color" OR "$ENV{TERM}" STREQUAL "xterm")
    string(ASCII 27 esc)
    set(reset "${esc}[m")
    set(white "${esc}[37;1m")
  endif()
  string(LENGTH ${desc} len)
  while (len LESS 20)
    set (desc "${desc} ")
    string(LENGTH ${desc} len)
  endwhile()
  if("${val}" MATCHES ";")
    string(REPLACE ";" "\n                          " val "${val}")
  endif()
  _message("    ${white}${desc}${reset}: ${val}")
endfunction()

##############################################################################
# Convert to camel case string from lower case underscored string.
#
# :param input: the input lowercase underscored string
# :type input: string
# :param output: the output camelcase string
# :type output: string
##############################################################################
function(camel_case_string input output)
    string(REPLACE "_" ";" list ${input})
    foreach(SUBSTR ${list})
        string(SUBSTRING ${SUBSTR} 0 1 FIRST_LETTER)
        string(TOUPPER ${FIRST_LETTER} FIRST_LETTER)
        string(REGEX REPLACE "^.(.*)" "${FIRST_LETTER}\\1" SUBSTR "${SUBSTR}")
        string(APPEND result ${SUBSTR})
    endforeach(SUBSTR)
    set(${output} "${result}" PARENT_SCOPE)
endfunction()

##############################################################################
# string append
##############################################################################

macro(string_append var str)
  if (NOT ${var})
    set(${var} "${str}")
  else()
    set(${var} "${${var}} ${str}")
  endif()
endmacro()

# OS RELATED VARIABLES
list(APPEND DebianBasedOS "Ubuntu" "LinuxMint")
list(APPEND RHBasedOS "CentOS")
unset(RELEASE_ID)
unset(RELEASE_CODENAME)
find_program(LSB_RELEASE_EXEC lsb_release)
execute_process(COMMAND ${LSB_RELEASE_EXEC} -is
        OUTPUT_VARIABLE RELEASE_ID
        OUTPUT_STRIP_TRAILING_WHITESPACE
        )

execute_process(COMMAND ${LSB_RELEASE_EXEC} -cs
        OUTPUT_VARIABLE RELEASE_CODENAME
        OUTPUT_STRIP_TRAILING_WHITESPACE
        )
