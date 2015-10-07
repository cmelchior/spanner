Gauge
=====

Gauge is a micro benchmarking framework for the Android platform.

It is a fork of the original Caliper project started by Google: code.google.com/p/caliper


## Benchmarking with Gauge

Each invocation of Gauge is called a *Run*. Each run consists of 1 benchmark class and one or more methods.

A run has different *axis'*, e.g. method to run, parameters to use and VM parameters.

A *Scenario* is a unique combination of these axis'.

A *Instrument* determines what is measured in any given scenario. Most commonly runtime is measured, but you 
could also measure memory usage or some arbitrary value.

The combination of a *Scenario* and an *Instrument* is called an *Experiment*. An experiment is thus a full 
description of what test(s) to run and how to measure it.

Running a experiment is called a *Trial*. A trial produces one or more benchmark values.

In an ideal world it should be enough to run one trial as it would always produce reliable, reproducible 
results. This is not always the case as we are running inside a virtual machine and do not have full 
control over the operating system.

For that reason we normally conduct multiple trials to increase the confidence in our result.

Run 
  * Options
  * Configuration
    
  * For each axis combination:  
      <- Scenario
           - BenchmarkClass
           - Method
           - Other options
    
           * For each instrument
             <-- Experiment: Scenario + Instrument
               <-- [i...j] Trial
                
             
     



## How to use

1) Gradle setup
2) Example of unit test with annotations


## Resources

- http://jeremymanson.blogspot.dk/2009/12/allocation-instrumenter-for-java_10.html



## Old docs

To build this project with Maven:

1. cd caliper
mvn eclipse:configure-workspace eclipse:eclipse install

2. To build examples
cd examples
mvn eclipse:configure-workspace eclipse:eclipse install
