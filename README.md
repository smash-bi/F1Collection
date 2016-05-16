# F1Collection
F1 Collection contains Java implementation of collection libraries using off heap memory to store fixed length binary data. Since accessing off heap memory in Java is currently a bit slower than on heap memory, F1 Collection is more suited for applications require large amount of memory (in GB or TB) where using on heap collection implementation will severely affect application performance due to the impact from garbage collection activities.

Currently F1 Collection provides a key value lookup F1 Binary Map. In the near future we will add other collection libraries including list and sorted version of list and map.

# F1BinaryMap

F1BinaryMap is a small foot print off heap binary FIXED length key value data lookup
that is backed by various options of memory media such as memory mapped file or direct off heap memory. 
F1BinaryMap can handle very large binary data efficiently.

The binary region of the map is separated into
3 regions - Map Header Region, Hash Bucket Region, and Record Region.

Map Header Region consists of 9 long fields (72 bytes) with starting offset 0  
LOCK protects storage allocation operations  
SBRK size of sbrk area in recordRegion, i.e., index of last usable record  
END last unused record in sbrk area, i.e., exceeds sbrk => increase sbrk  
FREE stack (LIFO) of free recordRegion, chained together by hash-chain link  
RECORD SIZE size of each record  
NO OF FILES current no of memory mapped file backing the map  
NO OF BUCKETS current no of buckets in the map  
MAX MAP SIZE maximum size of the map in bytes  
SIZE current no of records in the map  

Hash Bucket Region consists of no of buckets of 2 long fields (2 bytes * no of buckets) with offset 72  
TOP address to the first record in the bucket  
LOCK protects bucket record allocation/deallocation operations  
 
Record Region consists of 1 long field, followed by the key and then the value with offset 72 + 16 * no of buckets  
LINK long field to store the address of the linked record node  
KEY key of the data  
VALUE value of the data 

F1BinaryMap, through various contructors, can store the data in direct off heap memory or via memory mapped file. F1BinaryMap also provides isConcurrentMap option to optionally handle the put and get access in a concurrently safe manner. F1BinaryMap, when used in single threaded access, also provide a zero copy get access to reduce the overhead from memory copy.

MapPerformanceTest can be used to evaluate the performance:

smash.f1.collection.MapPerformanceTest [NO OF RECORDS] [IMPLEMENTATION] [OPTIONAL MEMORY MAPPED FILE]  

[NO OF RECORDS] no of records will be used for testing  
[IMPLEMENTATION] implementation to be used for the test. Valid values are:  
       HashMap - performance test against java.util.HashMap  
                 (Please use -Xmx14g -Xms14g -XX:NewSize=10g to allocate enough memory for the test)
       ConcurrentHashMap - performance test against java.util.concurrent.ConcurrentHashMap  
                           (Please use -Xmx14g -Xms14g -XX:NewSize=10g to allocate enough memory for the test)
       F1BinaryMap - performance test against F1BinaryMap with memory mapped file backing. [OPTIONAL MEMORY MAPPED FILE] needs to be supplied
       F1BinaryMapDirect - performance test against F1BinaryMap with direct off heap memory
[OPTIONAL MEMORY MAPPED FILE] when F1BinaryMap is supplied as [IMPLEMENTATION], needs to supply the name and path of the memory mapped file to be used to back the data storage

TestData has 6 longs fields with the following layout:  
long key1  
long key2  
long value1 (set to have the same value as key1)  
long value2 (set to have the same value as key2)  
long value3 (set to have the same value as key2)  
long value4 (set to have the same value as key1)  

Each test will run 20 iterations and print out the total time to take to complete the following test in milliseconds
Test Put - Putting [NO OF RECORDS] records to the map
Test Get - Getting [NO OF RECORDS] records from the map by key and verify the TestData is correct (value1=key1, value2=key2, value3=key2, value4=key1)
Test Zero Copy - Same as Test Get except using a zero copy approach to read the data




F1BinaryMap is a collaborative development effort between Smash.bi development team and University of Waterloo. We specially thank Professor Peter Buhr, Processor Martin Karsten, and Xianda Sun from University of Waterloo to contribute bulk of the F1BinaryMap development to make it one of the most efficient off heap key value lookup.
