## Circe encoder/decoder implementation for ADT to JSON with a configurable type field.

This library provides an efficient, type safe and macro based 
ADT to JSON encoder/decoder for circe with configurable JSON type field mappings.

Based on [abdolence/circe-tagged-adt-codec](https://github.com/abdolence/circe-tagged-adt-codec)
When you have ADTs (as trait and case classes) defined like this

```scala
sealed trait TestEvent

case class MyEvent1(anyYourField : String /*, ...*/) extends TestEvent
case class MyEvent2(anyOtherField : Long /*, ...*/) extends TestEvent
// ...
```

and you would like to encode them to JSON like this:

```json
{
  "type" : "my-event-1",
  "any_your_field" : "my-data", 
  "..." : "..."
}
```

The main objectives here are:
- Avoid JSON type field in Scala case class definitions.
- Configurable JSON type field values and their mapping to case classes. They don't have to be Scala class names.
- Avoid writing circe Encoder/Decoder manually.
- Check at the compile time JSON type field mappings and Scala case classes.

### Scala support
- Scala v2.12 / v2.13
- Scala.js v1+

### Getting Started
Add the following to your `build.sbt`:

```scala
libraryDependencies += "io.github.antonkw" %% "circe-tagged-adt-codec" % "0.0.7-SNAPSHOT"
```

or if you need Scala.js support:

```scala
libraryDependencies += "io.github.antonkw" %%% "circe-tagged-adt-codec" % "0.0.7-SNAPSHOT"
```

### Licence
Apache Software License (ASL)

### Author
Abdulla Abdurakhmanov

Fork by Anton Kovalevsky
