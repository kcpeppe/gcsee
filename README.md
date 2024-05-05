# gcsee

GCSee is a Java Swing GUI that makes use of [GCToolKit](https://github.com/microsoft/gctoolkit) to create simplified views of a serial, parallel, CMS, or G1GC garbage collection log. The supported views are

1. Summary tables
   1. basic set of statics
   2. summary of individual GC cycles
2. Charts for
   1. Heap Occupancy after each Garbage Collection cycle
   2. Pause times
   3. Allocation Rates

You may find a more robust set of scripts @ [mo-beck](https://github.com/mo-beck/JVM-Performance-Engineering/tree/main/GCscripts)