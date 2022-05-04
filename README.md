## Circe encoder/decoder implementation for ADT to JSON with a configurable type field.

This library provides an efficient, type safe and macro based 
ADT to JSON encoder/decoder for circe with configurable JSON type field mappings.

Based on [abdolence/circe-tagged-adt-codec](https://github.com/abdolence/circe-tagged-adt-codec)

Main difference is automatic derivation of codecs for all case classes.

`io.circe.generic.extras.Configuration.default.withSnakeCaseMemberNames` is used to derive codecs for case classes.
Hence, the purpose of macro is to have snake_case_serilized json with explicitly specified discriminator value. 


### Example
Let us have ADT:

```scala
sealed trait TestEvent

case class MyEvent1(anyYourField : String /*, ...*/) extends TestEvent
case class MyEvent2(anyOtherField : Long /*, ...*/) extends TestEvent
// ...
```

Automatic derivation comprises two steps.

#### Root codec
For root `TestEvent` (companion object is good place for that) you have simple function (individual createEncoder/createDecoder are in place too):
```scala
object TestEvent {
  import io.circe.syntax.EncoderOps
  import io.github.antonkw.circe.adt.codec._

  implicit val (e: Encoder[TestEvent], d: Decoder[TestEvent]) = JsonTaggedAdtCodec.createCodec[TestEvent]("event_type")
}
```

#### Set discriminator value
```scala
@JsonAdt("my-event-1")
case class MyEvent1(anyYourField : String /*, ...*/) extends TestEvent

@JsonAdt("my-event-2")
case class MyEvent2(anyOtherField : Long /*, ...*/) extends TestEvent
```

There is a `createEventTypeCodec` that fixes **event_type** as field.
```scala
implicit val (e: Encoder[TestEvent], d: Decoder[TestEvent]) = JsonTaggedAdtCodec.createEventTypeCodec[TestEvent]
```

Resulting json would look like:

```json
{
  "event_type" : "my-event-1",
  "any_your_field" : "my-data", 
  "..." : "..."
}
```

The main objectives here are:
- Avoid JSON type field in Scala case class definitions.
- Configurable JSON type field values and their mapping to case classes. They don't have to be Scala class names.
- Avoid writing circe Encoder/Decoder manually.
- Check at the compile time JSON type field mappings and Scala case classes.

### TODO
- Configuration of derivation (like snake vs. camel case)
- Exhaustiveness compile check?

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
