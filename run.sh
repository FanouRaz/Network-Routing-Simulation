#!/bin/bash

mkdir -p classes
javac --class-path .:lib/* -d classes Main.java
java --class-path classes:lib/* Main 
