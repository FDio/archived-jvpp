#!/usr/bin/env bash
echo "JVPP cleanup started"
make clean
rm -rf build-root/packages/*
rm -rf java/*.jar
# clean cmake cache
find . -iwholename '*cmake*' -not -name CMakeLists.txt -delete
# clean cpack cache
find . -iwholename '*cpack*' -delete
echo "... cleanup finished."
