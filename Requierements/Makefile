# Copyright (c) 2018 Cisco and/or its affiliates.
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

export WS_ROOT=$(CURDIR)
export BR=$(WS_ROOT)/build-root
CCACHE_DIR?=$(BR)/.ccache
GDB?=gdb
PLATFORM?=vpp
SAMPLE_PLUGIN?=no
STARTUP_DIR?=$(PWD)
MACHINE=$(shell uname -m)
SUDO?=sudo

,:=,
define disable_plugins
$(if $(1), \
  "plugins {" \
  $(patsubst %,"plugin %_plugin.so { disable }",$(subst $(,), ,$(1))) \
  " }" \
  ,)
endef

MINIMAL_STARTUP_CONF="							\
unix { 									\
	interactive 							\
	cli-listen /run/vpp/cli.sock					\
	gid $(shell id -g)						\
	$(if $(wildcard startup.vpp),"exec startup.vpp",)		\
}									\
$(if $(DPDK_CONFIG), "dpdk { $(DPDK_CONFIG) }",)			\
$(call disable_plugins,$(DISABLED_PLUGINS))				\
"

GDB_ARGS= -ex "handle SIGUSR1 noprint nostop"

#
# OS Detection
#
# We allow Darwin (MacOS) for docs generation; VPP build will still fail.
ifneq ($(shell uname),Darwin)
OS_ID        = $(shell grep '^ID=' /etc/os-release | cut -f2- -d= | sed -e 's/\"//g')
OS_VERSION_ID= $(shell grep '^VERSION_ID=' /etc/os-release | cut -f2- -d= | sed -e 's/\"//g')
endif

ifeq ($(filter ubuntu debian,$(OS_ID)),$(OS_ID))
PKG=deb
else ifeq ($(filter rhel centos fedora opensuse opensuse-leap opensuse-tumbleweed,$(OS_ID)),$(OS_ID))
PKG=rpm
endif

# +libganglia1-dev if building the gmond plugin

DEB_DEPENDS  = curl build-essential autoconf automake ccache
DEB_DEPENDS += debhelper dkms git libtool libapr1-dev dh-systemd
DEB_DEPENDS += libconfuse-dev git-review exuberant-ctags cscope pkg-config
DEB_DEPENDS += lcov chrpath autoconf indent clang-format libnuma-dev
DEB_DEPENDS += python-all python-dev python-virtualenv python-pip libffi6 check
DEB_DEPENDS += libboost-all-dev libffi-dev python-ply libmbedtls-dev
DEB_DEPENDS += cmake ninja-build

# ADD JDK for JVPP
ifeq ($(OS_VERSION_ID),14.04)
	DEB_DEPENDS += openjdk-8-jdk-headless
	DEB_DEPENDS += libssl-dev
else ifeq ($(OS_ID)-$(OS_VERSION_ID),debian-8)
	DEB_DEPENDS += openjdk-8-jdk-headless
	DEB_DEPENDS += libssl-dev
	APT_ARGS = -t jessie-backports
else ifeq ($(OS_ID)-$(OS_VERSION_ID),debian-9)
	DEB_DEPENDS += default-jdk-headless
	DEB_DEPENDS += libssl1.0-dev
else
	DEB_DEPENDS += default-jdk-headless
	DEB_DEPENDS += libssl-dev
endif

# ADD JDK for JVPP
RPM_DEPENDS  = redhat-lsb glibc-static java-1.8.0-openjdk-devel yum-utils

RPM_DEPENDS += apr-devel
RPM_DEPENDS += numactl-devel
RPM_DEPENDS += check check-devel
RPM_DEPENDS += boost boost-devel
RPM_DEPENDS += selinux-policy selinux-policy-devel
RPM_DEPENDS += cmake3 ninja-build

ifeq ($(OS_ID)-$(OS_VERSION_ID),fedora-25)
	RPM_DEPENDS += subunit subunit-devel
	RPM_DEPENDS += openssl-devel
	RPM_DEPENDS += python-devel python2-ply
	RPM_DEPENDS += python2-virtualenv
	RPM_DEPENDS += mbedtls-devel
	RPM_DEPENDS_GROUPS = 'C Development Tools and Libraries'
else ifeq ($(shell if [ "$(OS_ID)" = "fedora" ]; then test $(OS_VERSION_ID) -gt 25; echo $$?; fi),0)
	RPM_DEPENDS += subunit subunit-devel
	RPM_DEPENDS += compat-openssl10-devel
	RPM_DEPENDS += python2-devel python2-ply
	RPM_DEPENDS += python2-virtualenv
	RPM_DEPENDS += mbedtls-devel
	RPM_DEPENDS_GROUPS = 'C Development Tools and Libraries'
else
	RPM_DEPENDS += openssl-devel
	RPM_DEPENDS += python-devel python-ply
	RPM_DEPENDS += python-virtualenv
	RPM_DEPENDS += devtoolset-7
	RPM_DEPENDS_GROUPS = 'Development Tools'
endif

# +ganglia-devel if building the ganglia plugin

RPM_DEPENDS += chrpath libffi-devel rpm-build

SUSE_NAME= $(shell grep '^NAME=' /etc/os-release | cut -f2- -d= | sed -e 's/\"//g' | cut -d' ' -f2)
SUSE_ID= $(shell grep '^VERSION_ID=' /etc/os-release | cut -f2- -d= | sed -e 's/\"//g' | cut -d' ' -f2)
RPM_SUSE_BUILDTOOLS_DEPS = autoconf automake ccache check-devel chrpath
RPM_SUSE_BUILDTOOLS_DEPS += clang cmake indent libtool make ninja python-ply

# ADD JDK for JVPP
RPM_SUSE_DEVEL_DEPS = glibc-devel-static java-1_8_0-openjdk-devel libnuma-devel

RPM_SUSE_DEVEL_DEPS += libopenssl-devel openssl-devel mbedtls-devel

RPM_SUSE_PYTHON_DEPS = python-devel python3-devel python-pip python3-pip
RPM_SUSE_PYTHON_DEPS += python-rpm-macros python3-rpm-macros

RPM_SUSE_PLATFORM_DEPS = distribution-release shadow rpm-build

ifeq ($(OS_ID),opensuse)
ifeq ($(SUSE_NAME),Tumbleweed)
	RPM_SUSE_DEVEL_DEPS = libboost_headers-devel libboost_thread-devel gcc
	RPM_SUSE_PYTHON_DEPS += python2-ply python2-virtualenv
endif
ifeq ($(SUSE_ID),15.0)
	RPM_SUSE_DEVEL_DEPS = libboost_headers-devel libboost_thread-devel gcc6
	RPM_SUSE_PYTHON_DEPS += python2-ply python2-virtualenv
else
	RPM_SUSE_DEVEL_DEPS += boost_1_61-devel gcc6
	RPM_SUSE_PYTHON_DEPS += python-virtualenv
endif
endif

ifeq ($(OS_ID),opensuse-leap)
ifeq ($(SUSE_ID),15.0)
	RPM_SUSE_DEVEL_DEPS = libboost_headers-devel libboost_thread-devel gcc6
	RPM_SUSE_PYTHON_DEPS += python2-ply python2-virtualenv
endif
endif

RPM_SUSE_DEPENDS += $(RPM_SUSE_BUILDTOOLS_DEPS) $(RPM_SUSE_DEVEL_DEPS) $(RPM_SUSE_PYTHON_DEPS) $(RPM_SUSE_PLATFORM_DEPS)

ifneq ($(wildcard $(STARTUP_DIR)/startup.conf),)
        STARTUP_CONF ?= $(STARTUP_DIR)/startup.conf
endif

ifeq ($(findstring y,$(UNATTENDED)),y)
CONFIRM=-y
FORCE=--force-yes
endif

TARGETS = vpp

ifneq ($(SAMPLE_PLUGIN),no)
TARGETS += sample-plugin
endif

.PHONY: help wipe wipe-release build build-release rebuild rebuild-release
.PHONY: run run-release debug debug-release build-vat run-vat pkg-deb pkg-rpm
.PHONY: ctags cscope
.PHONY: test test-debug retest retest-debug test-doc test-wipe-doc test-help test-wipe
.PHONY: test-cov test-wipe-cov

define banner
	@echo "========================================================================"
	@echo " $(1)"
	@echo "========================================================================"
	@echo " "
endef

help:
	@echo "Make Targets:"
	@echo " install-dep         - install software dependencies"
	@echo ""

$(BR)/.deps.ok:
ifeq ($(findstring y,$(UNATTENDED)),y)
	make install-dep
endif
ifeq ($(filter ubuntu debian,$(OS_ID)),$(OS_ID))
	@MISSING=$$(apt-get install -y -qq -s $(DEB_DEPENDS) | grep "^Inst ") ; \
	if [ -n "$$MISSING" ] ; then \
	  echo "\nPlease install missing packages: \n$$MISSING\n" ; \
	  echo "by executing \"make install-dep\"\n" ; \
	  exit 1 ; \
	fi ; \
	exit 0
else ifneq ("$(wildcard /etc/redhat-release)","")
	@for i in $(RPM_DEPENDS) ; do \
	    RPM=$$(basename -s .rpm "$${i##*/}" | cut -d- -f1,2,3)  ;	\
	    MISSING+=$$(rpm -q $$RPM | grep "^package")	   ;    \
	done							   ;	\
	if [ -n "$$MISSING" ] ; then \
	  echo "Please install missing RPMs: \n$$MISSING\n" ; \
	  echo "by executing \"make install-dep\"\n" ; \
	  exit 1 ; \
	fi ; \
	exit 0
endif
	@touch $@

install-dep:
ifeq ($(filter ubuntu debian,$(OS_ID)),$(OS_ID))
ifeq ($(OS_VERSION_ID),14.04)
	@sudo -E apt-get $(CONFIRM) $(FORCE) install software-properties-common
	@sudo -E add-apt-repository ppa:openjdk-r/ppa $(CONFIRM)
endif
ifeq ($(OS_ID)-$(OS_VERSION_ID),debian-8)
	@grep -q jessie-backports /etc/apt/sources.list /etc/apt/sources.list.d/* 2> /dev/null \
           || ( echo "Please install jessie-backports" ; exit 1 )
endif
	@sudo -E apt-get update
	@sudo -E apt-get $(APT_ARGS) $(CONFIRM) $(FORCE) install $(DEB_DEPENDS)
else ifneq ("$(wildcard /etc/redhat-release)","")
ifeq ($(OS_ID),rhel)
	@sudo -E yum-config-manager --enable rhel-server-rhscl-7-rpms
else ifeq ($(OS_ID),centos)
	@sudo -E yum install $(CONFIRM) centos-release-scl-rh
endif
	@sudo -E yum groupinstall $(CONFIRM) $(RPM_DEPENDS_GROUPS)
	@sudo -E yum install $(CONFIRM) $(RPM_DEPENDS)
	@sudo -E debuginfo-install $(CONFIRM) glibc openssl-libs mbedtls-devel zlib
else ifeq ($(filter opensuse-tumbleweed,$(OS_ID)),$(OS_ID))
	@sudo -E zypper refresh
	@sudo -E zypper install -y $(RPM_SUSE_DEPENDS)
else ifeq ($(filter opensuse-leap,$(OS_ID)),$(OS_ID))
	@sudo -E zypper refresh
	@sudo -E zypper install -y $(RPM_SUSE_DEPENDS)
else ifeq ($(filter opensuse,$(OS_ID)),$(OS_ID))
	@sudo -E zypper refresh
	@sudo -E zypper install -y $(RPM_SUSE_DEPENDS)
else
	$(error "This option currently works only on Ubuntu, Debian, RHEL, CentOS or openSUSE systems")
endif

define make
	@make -C $(BR) PLATFORM=$(PLATFORM) TAG=$(1) $(2)
endef

$(BR)/scripts/.version:
ifneq ("$(wildcard /etc/redhat-release)","")
	$(shell $(BR)/scripts/version rpm-string > $(BR)/scripts/.version)
else
	$(shell $(BR)/scripts/version > $(BR)/scripts/.version)
endif

DIST_FILE = $(BR)/vpp-$(shell src/scripts/version).tar
DIST_SUBDIR = vpp-$(shell src/scripts/version|cut -f1 -d-)


