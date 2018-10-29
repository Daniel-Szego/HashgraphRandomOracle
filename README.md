# HashgraphRandomOracle

Experimental implementation for a native random oracle with the help of Swirlds SDK
you can run with the SDK 18.05.23

- Each node creates random integer in the range of randomRange
- At the moment only simple random numbers are used
- The consensus is supposed to be reached in calculatingStarts
- The algorithm for random number generation is:
  sha256(noderandom_1 || noderandom_2 || noderandom_3 || noderandom_4) % randomRange

Further information on the algorithm is to be found at:
https://danielszego.blogspot.com/2018/09/how-to-create-real-decentralized-random.html



  


