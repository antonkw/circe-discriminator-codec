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

import io.circe.JsonObject

/**
 * Auxiliary ADT case classes to JSON object converter
 *
 * @tparam T
 *   A trait type
 */
trait JsonTaggedAdtEncoder[T] {

  /**
   * Convert a trait to circe JsonObject
   * @param obj
   *   an instance of T
   * @return
   *   Encoded JSON object and its JSON type field value
   */
  def toJsonObject( obj: T ): ( JsonObject, String )

}
