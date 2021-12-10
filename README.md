Hungry Philosophers
===================

Solutions for [dining philosophers problem](https://en.wikipedia.org/wiki/Dining_philosophers_problem).

Implemented approaches:

* [ATOMIC](src/main/java/org/solveme/philosophers/strategies/Atomic.java)
* [SYNCHRONIZED](src/main/java/org/solveme/philosophers/strategies/Synchronized.java)
* [NOTIFY](src/main/java/org/solveme/philosophers/strategies/Notify.java)
* [MANAGED](src/main/java/org/solveme/philosophers/strategies/Managed.java)

## Quick Start

    make install
    make build-app
    ./run-app.sh ATOMIC

## Build

Maven is used for building and packaging project.

There are several shortcuts for simplifying building and usage presented as [Makefile](Makefile). 
To see all available shortcuts call `make` from project root 
(you need to have [make](https://en.wikipedia.org/wiki/Make_(software)) installed).


# Usage

If you want to run program from an IDE refer to [DinnerApp](src/main/java/org/solveme/philosophers/DinnerApp.java).

If you want to run it from the console you have to package program first.
There are two types of packaging:

The distributable program built by appassembler plugin

    make build-app
    ./run-app.sh

and shaded "fat" JAR

    make build-uberjar
    ./run-shaded.sh


# Progressbar note

During execution there would be several progressbars,
that would reflect how many time each philosopher spend on thinking/eating. 
This progressbars may be displayed not properly sometimes (e.g. in IDE). 
However, they look good in my Linux terminal (both X session and plain tty).
If you experience problems with it, you can use `-NP` option to disable progressbar displaying. 