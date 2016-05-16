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

F1BinaryMap is a collaborative development effort between Smash.bi development team and University of Waterloo. We specially thank Professor Peter Buhr, Processor Martin Karsten, and Xianda Sun from University of Waterloo to contribute bulk of the F1BinaryMap development to make it one of the most efficient off heap key value lookup.
