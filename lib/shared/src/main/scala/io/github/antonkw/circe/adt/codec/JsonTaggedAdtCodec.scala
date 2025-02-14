/*
 * Copyright 2019 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 * Copyright 2022 (the file had been edited) Anton Kovalevsky (antonkw.mail@gmail.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.antonkw.circe.adt.codec

import io.circe.{ Decoder, Encoder, HCursor, Json, JsonObject }

/**
 * Object provides access to the factory methods for circe Encoder/Decoder
 */
object JsonTaggedAdtCodec {

  /**
   * Default implementation of encoding JSON with a type field
   *
   * @param typeFieldName
   *   a JSON field name to encode type name
   * @param encoder
   *   converter for trait and its case classes
   * @param obj
   *   an object to encode
   * @tparam T
   *   A trait type
   * @return
   *   Encoded json object with a type field
   */
  protected def defaultJsonTypeFieldEncoder[T](
      typeFieldName: String
  )( encoder: JsonTaggedAdtEncoder[T], obj: T ): JsonObject = {
    val ( jsonObj, typeFieldValue ) = encoder.toJsonObject( obj )
    jsonObj.add( typeFieldName, Json.fromString( typeFieldValue ) )
  }

  /**
   * Default implementation of decoding JSON with a type field
   *
   * @param typeFieldName
   *   a JSON field name to decode type name
   * @param decoder
   *   converter for trait and its case classes
   * @param cursor
   *   JSON context cursor
   * @tparam T
   *   A trait type
   * @return
   *   Decode result
   */
  protected def defaultJsonTypeFieldDecoder[T](
      typeFieldName: String
  )(
      decoder: JsonTaggedAdtDecoder[T],
      cursor: HCursor
  ): Decoder.Result[T] = {

    cursor.get[Option[String]]( typeFieldName ).flatMap {
      case Some( typeFieldValue ) =>
        decoder.fromJsonObject(
          jsonTypeFieldValue = typeFieldValue,
          cursor = cursor
        )
      case _ =>
        Decoder.failedWithMessage[T](
          s"'$typeFieldName' isn't specified in json."
        )( cursor )
    }
  }

  /**
   * Default implementation of encoding JSON with with pure enum trait without any values
   *
   * @param encoder
   *   converter for trait and its case classes
   * @param obj
   *   an object to encode
   * @tparam T
   *   A trait type
   * @return
   *   Encoded json object with a type field
   */
  protected def defaultJsonPureEnumEncoder[T]()(
      encoder: JsonTaggedAdtEncoder[T],
      obj: T
  ): Json = {
    val ( _, typeFieldValue ) = encoder.toJsonObject( obj )
    Json.fromString( typeFieldValue )
  }

  /**
   * Default implementation of decoding JSON with pure enum trait without any values
   *
   * @param decoder
   *   converter for trait and its case classes
   * @param cursor
   *   JSON context cursor
   * @tparam T
   *   A trait type
   * @return
   *   Decode result
   */
  protected def defaultJsonPureEnumDecoder[T]()(
      decoder: JsonTaggedAdtDecoder[T],
      cursor: HCursor
  ): Decoder.Result[T] = {

    cursor.as[String].flatMap { typeFieldValue =>
      decoder.fromJsonObject(
        jsonTypeFieldValue = typeFieldValue,
        cursor = cursor
      )
    }
  }

  /**
   * Create ADT / JSON type field base decoder with a specified type field decoding implementation
   *
   * @param typeFieldDecoder
   *   JSON type field decoding implementation
   * @param adtDecoder
   *   implicitly created JSON converter for trait and its case classes
   * @tparam T
   *   A trait type
   * @return
   *   circe Decoder of T
   */
  def createDecoderDefinition[T](
      typeFieldDecoder: (
          JsonTaggedAdtDecoder[T],
          HCursor
      ) => Decoder.Result[T]
  )( implicit adtDecoder: JsonTaggedAdtDecoder[T] ): Decoder[T] =
    ( cursor: HCursor ) => {
      typeFieldDecoder( adtDecoder, cursor )
    }

  /**
   * Create ADT / JSON type field base encoder with a specified type field encoding implementation
   *
   * @param typeFieldEncoder
   *   JSON type field encoding implementation
   * @param adtEncoder
   *   implicitly created JSON converter for trait and its case classes
   * @tparam T
   *   A trait type
   * @return
   *   circe Encoder of T
   */
  def createEncoderDefinition[T](
      typeFieldEncoder: ( JsonTaggedAdtEncoder[T], T ) => JsonObject
  )( implicit adtEncoder: JsonTaggedAdtEncoder[T] ): Encoder.AsObject[T] =
    ( obj: T ) => {
      typeFieldEncoder( adtEncoder, obj )
    }

  /**
   * Create ADT / JSON type field base encoder
   *
   * @param typeFieldName
   *   a JSON field name to encode type name
   * @param adtEncoder
   *   implicitly created JSON converter for trait and its case classes
   * @tparam T
   *   A trait type
   * @return
   *   circe Encoder of T
   */
  def createEncoder[T](
      typeFieldName: String
  )( implicit adtEncoder: JsonTaggedAdtEncoder[T] ): Encoder.AsObject[T] =
    createEncoderDefinition[T]( defaultJsonTypeFieldEncoder( typeFieldName ) )

  def createCodec[T](
      typeFieldName: String
  )( implicit
      adtEncoder: JsonTaggedAdtEncoder[T],
      adtDecoder: JsonTaggedAdtDecoder[T]
  ): ( Encoder.AsObject[T], Decoder[T] ) =
    ( createEncoder[T]( typeFieldName ), createDecoder( typeFieldName ) )

  def createEventTypeCodec[T]( implicit
      adtEncoder: JsonTaggedAdtEncoder[T],
      adtDecoder: JsonTaggedAdtDecoder[T]
  ): ( Encoder.AsObject[T], Decoder[T] ) =
    ( createEncoder[T]( "event_type" ), createDecoder( "event_type" ) )

  /**
   * Create ADT / JSON type field base decoder
   *
   * @param typeFieldName
   *   a JSON field name to decode type name
   * @param adtDecoder
   *   implicitly created JSON converter for trait and its case classes
   * @tparam T
   *   A trait type
   * @return
   *   circe Decoder of T
   */
  def createDecoder[T](
      typeFieldName: String
  )( implicit adtDecoder: JsonTaggedAdtDecoder[T] ): Decoder[T] =
    createDecoderDefinition[T]( defaultJsonTypeFieldDecoder( typeFieldName ) )

  /**
   * Create ADT / JSON enum objects encoder
   *
   * @param adtEncoder
   *   implicitly created JSON converter for trait and its case classes
   * @tparam T
   *   A trait type
   * @return
   *   circe Encoder of T
   */
  def createPureEnumEncoder[T]()( implicit
      adtEncoder: JsonTaggedAdtEncoder[T]
  ): Encoder[T] =
    ( obj: T ) => {
      defaultJsonPureEnumEncoder()( adtEncoder, obj )
    }

  /**
   * Create ADT / JSON pure enum objects decoder
   *
   * @param adtDecoder
   *   implicitly created JSON converter for trait and its case classes
   * @tparam T
   *   A trait type
   * @return
   *   circe Decoder of T
   */
  def createPureEnumDecoder[T]()( implicit adtDecoder: JsonTaggedAdtDecoder[T] ): Decoder[T] =
    createDecoderDefinition[T]( defaultJsonPureEnumDecoder() )

}
