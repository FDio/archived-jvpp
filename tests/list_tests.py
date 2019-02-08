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

import unittest


def main():
    """Discovers unittests in current directory and prints them as list of tests.

    Example:
        Parse test function name 'test_vpp_acl_callback_api (test_jvpp.TestJVpp)' to test descriptor:
        test_jvpp.TestJVpp.test_vpp_acl_callback_api

    """
    suite = unittest.TestLoader().discover("")
    for root_test in suite:
        tests = root_test._tests
        for parent_test in tests:
            for jvpp_test in parent_test._tests:
                test_name = jvpp_test.__str__().split()
                print(test_name[1][1:-1] + "." + test_name[0])


if __name__ == "__main__":
    main()
