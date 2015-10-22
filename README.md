Spanner
=====

Spanner is a micro benchmarking framework designed to run on the Android platform.

It is a fork of the original Caliper project for Java started by Google: code.google.com/p/caliper

# Getting started


## Getting Spanner

* JCenter
* Adding to gradle.build


## Running Spanner

* Example of unit test
* Example of stand-alone tests
* How to use baseline
* Web UI

# Using this repo

* Current version is defined in *versiont.txt*


**Build a new SNAPSHOT**
1) Check version in `version.txt`
2) 
    > ./gradlew spanner:build

**Installing a local SNAPSHOT**

    >./gradlew clean spanner:publishToMavenLocal


**Releasing a new version**



# Benchmarking

## Why should I benchmark?


## Benchmarking with Spanner

Each invocation of Spanner is called a *Run*. Each run consists of 1 benchmark class and one or more methods.

A run has different *axis'*, e.g. method to run, parameters to use and VM parameters.

A *Scenario* is a unique combination of these axis'.

A *Instrument* determines what is measured in any given scenario. Most commonly runtime is measured, but you 
could also measure memory usage or some arbitrary value.

The combination of a *Scenario* and an *Instrument* is called an *Experiment*. An experiment is thus a full 
description of what test(s) to run and how to measure it.

Running an experiment is called a *Trial*.

Each trial consists of one or more *Measurements*. 

In an ideal world it should be enough to run one trial with one measurement as it would always produce reliable, 
reproducible  results. This is not always the case as we are running inside a virtual machine and do not have full 
control over the operating system. For that reason we normally conduct multiple measurement in each trial in order to 
smooth our irregularities and gain confidence in our results. 

Each trial will output statistics about the measurements like min, max, mean. For those reasons it is also preferable to 
run multiple trials so the confidence in the output from a Trial can be validated.

The output from a Spanner benchmark is the results of all the experiments.

## Benchmarking pitfalls

### JIT / AOT

Dalvik uses Just-in-time compilation.
ART uses Ahead-of-time compilation.

Just-in-time compilers will analyze the code while it runs and optimize it while it is running, for this reason it is
important to do warmup in these kind of environments.

Ahead-of-time compilers do not modify the code while it is running, as such when running on 

* Running tests in a different process
* Warmup
* JIT: Code being converted to native code


### Measuring time

* Clock drift (System.nanoTime() / System.currentTimeMillis())
* Clock granularity

* Find granularity
* Make sure that test runs longer than granularity
* Use appropriate system calls for measuring time.


### Benchmark variance

* Garbage collector
* Many layers between Java code and CPU instructions
* Kernel controls thread scheduler
* CPU behaves differently under different loads

* Enable fly-mode
* Disable as many sensors as possible
* Remove as many apps as possible
* Minimize GC


### Benchmark overhead

* Method calls
* Iterators
* Getting a timestamp


### Compiler optimizations

* Compiler can reorder/remove code.
* Compile to native code.

### Interpreting results

* Be mindful of measured overhead.
* Results do not say anything about the absolute speed.


## Math

* What is the confidence interval. 
* What is variance, how to interpret it.


## FAQ

**Why spanner?**

Because a Spanner is much more useful than a Caliper when working with Androids.


## Resources

- http://jeremymanson.blogspot.dk/2009/12/allocation-instrumenter-for-java_10.html