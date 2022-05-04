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

package io.github.antonkw.circe.adt.codec.macros.impl

import io.github.antonkw.circe.adt.codec.JsonTaggedAdtDecoder
import io.github.antonkw.circe.adt.codec.{JsonAdt, JsonAdtPassThrough, JsonTaggedAdtDecoder}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object JsonTaggedAdtDecoderMacroImpl extends JsonTaggedAdtMacroBase {

  /**
   * ADT / JSON type field based trait encoding and decoding implementation as a macro
   * @tparam T
   *   a trait type
   * @return
   *   a converter instance of T
   */
  def createAdtDecoderMacro[T : c.WeakTypeTag](
      c: blackbox.Context
  ): c.Expr[JsonTaggedAdtDecoder[T]] = {
    import c.universe._

    def createSingleClassConverterExpr( caseClassConfig: JsonAdtConfig[Symbol] ) = {
      // format: off
      c.Expr[JsonTaggedAdtDecoder[T]] (
        q"""
			   new JsonTaggedAdtDecoder[${caseClassConfig.symbol}] with io.circe.generic.extras.AutoDerivation {
	                import io.circe.{ JsonObject, Decoder, ACursor, DecodingFailure }
                    import io.circe.generic.extras.Configuration


                    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

			        override def fromJsonObject(jsonTypeFieldValue : String, cursor: ACursor) : Decoder.Result[${caseClassConfig.symbol}] = {
                        if(jsonTypeFieldValue != ${caseClassConfig.jsonAdtType}) {
                            Left(DecodingFailure(s"Unknown json type received: '$$jsonTypeFieldValue'.", cursor.history))
                        }
                        else {
                            cursor.as[${caseClassConfig.symbol}](exportDecoder[${caseClassConfig.symbol}].instance)
                        }
	                }
				}
			 """
      )
      // format: on
    }

    def parseSymbolObject( symbol: Symbol ): Tree = {
      c.parse( symbol.fullName )
    }

    def createConverterExpr(
        traitSymbol: Symbol,
        caseClassesConfig: Iterable[JsonAdtConfig[Symbol]]
    ) = {
      // format: off
		c.Expr[JsonTaggedAdtDecoder[T]] (
		q"""
			   new JsonTaggedAdtDecoder[${traitSymbol}] with io.circe.generic.extras.AutoDerivation {
	                import io.circe.{ JsonObject, Decoder, ACursor, DecodingFailure }
                    import io.circe.syntax._
                    import io.circe.generic.extras.Configuration
                    implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

			        override def fromJsonObject(jsonTypeFieldValue : String, cursor: ACursor) : Decoder.Result[${traitSymbol}] = {
                       
			            jsonTypeFieldValue match {
	                        case ..${caseClassesConfig.
                                    map { jsonAdtConfig =>
                                        if(jsonAdtConfig.hasDataToEncode)
                                            cq"""${jsonAdtConfig.jsonAdtType} => cursor.as[${jsonAdtConfig.symbol}]"""
                                        else {
                                            cq"""${jsonAdtConfig.jsonAdtType} => {
                                                Right(${parseSymbolObject(jsonAdtConfig.symbol)})
                                            }"""
                                        }
                                    }.toList :+
                                        cq"""_ =>
                                            Left(DecodingFailure(s"Unknown json type received: '$$jsonTypeFieldValue'.", cursor.history))
                                        """
							}
	                    }
	                }
				}
			 """
		)
	    // format: on
    }

    generateCodec( c )(
      createSingleClassConverterExpr( _ )
    )( createConverterExpr( _, _ ) )

  }
}
