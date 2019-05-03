This package contains basic tests for jvpp. To run the tests:

- Make sure VPP is running
- From JVPP's java/ folder execute:
  - "sudo java -cp jvpp-registry-19.08.jar:jvpp-stats-19.08.jar io.fd.jvpp.stats.test.FutureApiTest"
- For debug use:
  - "sudo java -verbose:jni -Xcheck:jni -cp jvpp-registry-19.08.jar:jvpp-stats-19.08.jar io.fd.jvpp.stats.test.FutureApiTest"
  - in the case there is a core dump generated use GDB for backtrace:
    - "sudo gdb java core"
    - then in GDB console use "bt full" command to print backtrace.
    - if libraries are missing use "info sharedlibrary" in GDB to find out path and name of missing libraries.
      You can copy missing libraries to desired location (usually /tmp/, because the libs are extracted from jars on startup)