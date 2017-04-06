#!/bin/bash

cd ../prg
javac -d bin -sourcepath src src/compiler/Main.java
cd ../src
make $*
