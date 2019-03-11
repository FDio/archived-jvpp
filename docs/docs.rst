.. _JVPP DOCUMENTATION:

.. toctree::

Building JVPP documentation
===========================

JVPP documentation uses restructured text format as source for generating documentation using sphinx generator.

Dependencies
------------

You need to have sphinx installed in order to build the documentation for JVPP. To do so you can use pip to install it.

.. code-block:: console

  pip install sphinx

You can use virtual environment for python or you can install it directly to the system.


Building the documentation
--------------------------

In order to build the documentation you can use "docs" target which is automatically generated for make if sphinx
generator is available.

.. code-block:: console

  make docs

After successful build the generated documentation is available in build-root/html directory.

Cleanup (Optional)
------------------

If you need to clean the build and remove all data that was generated during build (including documentation) you can
use the clean.sh script.

.. code-block:: console

  ./clean.sh
