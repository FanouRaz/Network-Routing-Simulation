#!/bin/bash

mkdir -p classes
mkdir -p screenshots
javac --class-path .:lib/* -d classes Main.java
java --class-path classes:lib/* Main 
