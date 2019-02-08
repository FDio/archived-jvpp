#!/usr/bin/env python

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

import os
import subprocess
import unittest
from log import get_logger

API_FILES_PATH = "java/"

# Registry jar file name prefix
REGISTRY_JAR_PREFIX = "jvpp-registry"


class TestJVpp(unittest.TestCase):
    """ JVPP Core Test Case """

    def invoke_for_jvpp_core(self, api_jar_name, test_class_name):
        self.jvpp_connection_test(api_jar_name=api_jar_name,
                                  test_class_name=test_class_name)

    def test_vpp_core_callback_api(self):
        """ JVPP Core Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-core",
                                  test_class_name="io.fd.jvpp.core.test."
                                                  "CallbackApiTest")

    def test_vpp_registry_callback_api(self):
        """ JVPP Core Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-registry",
                                  test_class_name="io.fd.jvpp.test."
                                                  "ConnectionTest")

    def test_vpp_core_future_api(self):
        """JVPP Core Future Api Test Case"""
        self.invoke_for_jvpp_core(api_jar_name="jvpp-core",
                                  test_class_name="io.fd.jvpp.core.test."
                                                  "FutureApiTest")

    def test_vpp_acl_callback_api(self):
        """ JVPP Acl Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-acl",
                                  test_class_name="io.fd.jvpp.acl.test."
                                                  "CallbackApiTest")

    def test_vpp_acl_future_api(self):
        """JVPP Acl Future Api Test Case"""
        self.invoke_for_jvpp_core(api_jar_name="jvpp-acl",
                                  test_class_name="io.fd.jvpp.acl.test."
                                                  "FutureApiTest")

    def test_vpp_ioamexport_callback_api(self):
        """ JVPP Ioamexport Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-ioamexport",
                                  test_class_name="io.fd.jvpp.ioamexport."
                                                  "test.CallbackApiTest")

    def test_vpp_ioamexport_future_api(self):
        """JVPP Ioamexport Future Api Test Case"""
        self.invoke_for_jvpp_core(api_jar_name="jvpp-ioamexport",
                                  test_class_name="io.fd.jvpp.ioamexport."
                                                  "test.FutureApiTest")

    def test_vpp_ioampot_callback_api(self):
        """ JVPP Ioampot Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-ioampot",
                                  test_class_name="io.fd.jvpp.ioampot."
                                                  "test.CallbackApiTest")

    def test_vpp_ioampot_future_api(self):
        """JVPP Ioampot Future Api Test Case"""
        self.invoke_for_jvpp_core(api_jar_name="jvpp-ioampot",
                                  test_class_name="io.fd.jvpp.ioampot."
                                                  "test.FutureApiTest")

    def test_vpp_ioamtrace_callback_api(self):
        """ JVPP Ioamtrace Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-ioamtrace",
                                  test_class_name="io.fd.jvpp.ioamtrace."
                                                  "test.CallbackApiTest")

    def test_vpp_ioamtrace_future_api(self):
        """JVPP Ioamtrace Future Api Test Case"""
        self.invoke_for_jvpp_core(api_jar_name="jvpp-ioamtrace",
                                  test_class_name="io.fd.jvpp.ioamtrace."
                                                  "test.FutureApiTest")

    def test_vpp_snat_callback_api(self):
        """ JVPP Snat Callback Api Test Case """
        self.invoke_for_jvpp_core(api_jar_name="jvpp-nat",
                                  test_class_name="io.fd.jvpp.nat.test."
                                                  "CallbackApiTest")

    def test_vpp_snat_future_api(self):
        """JVPP Snat Future Api Test Case"""
        self.invoke_for_jvpp_core(api_jar_name="jvpp-nat",
                                  test_class_name="io.fd.jvpp.nat.test."
                                                  "FutureApiTest")

    @staticmethod
    def full_jar_name(install_dir, jar_name, version):
        return os.path.join(install_dir, API_FILES_PATH,
                            "{0}-{1}.jar".format(jar_name, version))

    def jvpp_connection_test(self, api_jar_name, test_class_name):
        self.logger = get_logger(api_jar_name)
        install_dir = os.path.abspath('..')
        self.logger.info("Install directory : {0}".format(install_dir))
        self.shm_prefix = "honeycomb"
        proc = subprocess.Popen(['../version'], stdout=subprocess.PIPE)
        version = proc.stdout.read().split("-")[0]
        registry_jar_path = self.full_jar_name(install_dir, REGISTRY_JAR_PREFIX, version)
        self.logger.info("JVpp Registry jar path : {0}".format(registry_jar_path))
        if not os.path.isfile(registry_jar_path):
            raise Exception(
                "JVpp Registry jar has not been found: {0}"
                .format(registry_jar_path))

        api_jar_path = self.full_jar_name(install_dir, api_jar_name, version)
        self.logger.info("Api jar path : {0}".format(api_jar_path))
        if not os.path.isfile(api_jar_path):
            raise Exception(
                "Api jar has not been found: {0}".format(api_jar_path))

        # passes shm prefix as parameter to create connection with same value
        command = ["sudo", "java", "-cp",
                   "{0}:{1}".format(registry_jar_path, api_jar_path),
                   test_class_name, "/vpe-api"]

        self.process = subprocess.Popen(command, shell=False,
                                        stdout=subprocess.PIPE,
                                        stderr=subprocess.PIPE, bufsize=1,
                                        universal_newlines=True)

        out, err = self.process.communicate()
        self.logger.info("Process output : {0}{1}".format(os.linesep, out))

        if self.process.returncode != 0:
            raise subprocess.CalledProcessError(self.process.returncode, command,
                                                "Command {0} failed with return code: {1}.{2}"
                                                "Process error output: {2}{3}"
                                                .format(command, self.process.returncode, os.linesep, err))

    def tearDown(self):
        self.logger.info("Tearing down jvpp test")
        super(TestJVpp, self).tearDown()
        if hasattr(self, 'process') and self.process.poll() is None:
            self.process.kill()
