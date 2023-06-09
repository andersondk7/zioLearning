# Model design
The approach is to separate the model into:
- data
  - use enums when there are alternatives to an implementation (in scala 2 this would be a sealed trait with case class/case objects)
  - use final case class when simply grouping or needing fined grained control
- operations
  - traditional scala operations
    - companion objects for
      - construction/deconstruction
      - transformation (ex. cats show)
    - functional objects for modification
  - zio effects when needed

# Logging
1. When use a traditional typesafe.Logger in operations, when calling the operations, they  *must* be wrapped in ZIO.attemptBlocking

# Testing
- testing data/operations can be done with scala test approach as it is simpler
- testing effects *must* be done in an Object that extends _ZIOSpec_ such as _ZIOSpecDefault_