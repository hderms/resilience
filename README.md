# Resilience

The goal is to have a function that takes a Flow[A, Future[B \/ Error]],  an errorSink Sink[(A, Error)], and an alternateInput Source[A] and composes them altogether so that we can get the following behavior:
1. The future produced by Flow[A, Future[B \/ Error]] is executed
2. Errors from the future are sent down the errorSink along with the input A that caused it
3. inputs of type A can be streamed to this Flow directly and merged in along with it's normal inlet. 

An example of this utility would be streaming (A, Error) tuples to a DB table so you can query what input failed and why it failed. Particularly adventurous programmers could stream the results of this table into alternateInput in some manner so that errors could be more persistent and retried.


