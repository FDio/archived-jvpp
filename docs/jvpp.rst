.. _JVPP:

.. toctree::

Building JVPP from source
=========================

JVPP uses cmake to configure and build source code. The minimal version of cmake is 3.5.
Ensure that you have proper version of cmake installed.

JVPP depends on VPP packages (Debian or Centos):
* vpp
* vpp-plugins
* vpp-dev (vpp-devel on Centos)

Vpp and vpp-plugins packages contain API source file, which are used to generate Java API bindings. Vpp dev package
contains header files needed for build.

Obtaining the source
--------------------

You can get JVPP source code from gerrit.fd.io using git:
git clone https://gerrit.fd.io/r/jvpp

or from github:
https://github.com/FDio/jvpp.git


Install dependencies (Optional, one time only)
------------------

You can install all dependencies using provided install-dep target:

.. code-block:: console

  make install-dep

Cleanup (Optional)
------------------

Whenever you need to clean the setup you can use "clean.sh" script in the root folder. This cleans the build and clears
the cache. This is usefull mostly when you change something in JVPP to ensure everything is rebuild from scratch.

Configuring using cmake
-----------------------

Configuration using cmake is very easy, the whole process is automated. All you need to do is issue following command
from JVPP's root directory:

.. code-block:: console

  cmake .

This will configure all variables and setup the build.

Building the source
-------------------

To build the source use make command:

.. code-block:: console

  make

You can also install the library using (you need to use sudo or have root privileges to install libraries):

.. code-block:: console

  sudo make install

Building the packages
---------------------

During the setup using cmake the operating system is automatically recognised and everything is set up so you can build
packages for your current operating system. Only Debian based (tested on Ubuntu) and Centos (tested on Centos 7)
are supported for now.

To build the package you need to call:

.. code-block:: console

  make package

You can find the packages in build-root/packages folder.

Getting JVPP jar
================

VPP provides java bindings which can be downloaded at:

* https://nexus.fd.io/content/repositories/fd.io.release/io/fd/vpp/jvpp-core/19.01/jvpp-core-19.01.jar

Getting JVPP via maven
------------------------------------

**1. Add the following to the repositories section in your ~/.m2/settings.xml to pick up the fd.io maven repo:**

.. code-block:: console

  <repository>
   <id>fd.io-release</id>
   <name>fd.io-release</name>
   <url>https://nexus.fd.io/content/repositories/fd.io.release/</url>
   <releases>
     <enabled>false</enabled>
   </releases>
   <snapshots>
     <enabled>true</enabled>
   </snapshots>
 </repository>

For more information on setting up maven repositories in settings.xml, please look at:

* https://maven.apache.org/guides/mini/guide-multiple-repositories.html 

**2. Then you can get JVPP by putting in the dependencies section of your pom.xml file:**

.. code-block:: console

 <dependency>
   <groupId>io.fd.vpp</groupId>
   <artifactId>jvpp-core</artifactId>
   <version>19.01</version>
 </dependency>

For more information on maven dependency managment, please look at:

* https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html


Installing JVPP manually to local maven repository
--------------------------------------------------

Once JVPP is successfully built, you can install it to local .m2 repository.
To do so use provided script in JVPP root directory:

.. code-block:: console

  ./install_jvpp.sh

