/*
 * Copyright 2019 Abdulla Abdurakhmanov (abdulla@latestbit.com)
 * Copyright 2022 (the file had been edited) Anton Kovalevsky (antonkw.mail@gmail.com)
 *
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

import io.circe.{ ACursor, Decoder }

/**
 * Auxiliary JSON object to ADT case classes converter
 *
 * @tparam T
 *   A trait type
 */
trait JsonTaggedAdtDecoder[T] {

  /**
   * Convert a current JSON context specified with a cursor and a JSON type field value to suitable
   * case class instance
   * @param jsonTypeFieldValue
   *   a JSON type field value
   * @param cursor
   *   JSON decoding cursor
   * @return
   *   decoding result of the instance of a case class, accordingly to a jsonTypeFieldValue and a
   *   trait type
   */
  def fromJsonObject(
      jsonTypeFieldValue: String,
      cursor: ACursor
  ): Decoder.Result[T]
}
