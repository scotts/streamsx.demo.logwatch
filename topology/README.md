# Java Topology LogWatch

This is a work in progress; the full application is not yet done.

# Differences from LogWatch

Implemented in the Java API, rather than in SPL.

## Compiling and Executing

Right now, it is hardcoded to just run in standalone mode. To compile:

    $ ant

The `build.xml` file assumes that you have installed `streamsx.topology` in your home 
directory. If you have not, then you will need to change `build.xml` to point to your 
`streamsx.topology` install.

To run in standalone mode:

    $ ant run

## Contact

Scott Schneider, scott.a.s@us.ibm.com
