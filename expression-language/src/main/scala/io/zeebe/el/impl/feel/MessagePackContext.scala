/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.el.impl.feel

import io.zeebe.msgpack.spec.MsgPackReader
import io.zeebe.util.buffer.BufferUtil
import org.agrona.DirectBuffer
import org.camunda.feel.interpreter.VariableProvider
import org.camunda.feel.spi.CustomContext

class MessagePackContext(variables: DirectBuffer) extends CustomContext {

  override def variableProvider: VariableProvider = new ZeebeExpressionValueProvider

  class ZeebeExpressionValueProvider extends VariableProvider {

    val reader = new MsgPackReader

    override def getVariable(name: String): Option[Any] = {

      reader.wrap(variables, 0, variables.capacity())

      val size = reader.readMapHeader()

      for (i <- 0 until size) {

        val token = reader.readToken()
        val key = token.getValueBuffer

        if (key == BufferUtil.wrapString(name)) {

          val offset = reader.getOffset
          val value = BufferUtil.cloneBuffer(reader.getBuffer, offset, variables.capacity() - offset)
          return Some(value)

        } else {
          reader.skipValue()
        }
      }

      None
    }

    override def keys: Iterable[String] = {

      reader.wrap(variables, 0, variables.capacity())

      val size = reader.readMapHeader()

      for (i <- 0 until size) yield {

        val token = reader.readToken()
        val keyBuffer = token.getValueBuffer

        val key = BufferUtil.bufferAsString(keyBuffer)

        reader.skipValue()

        key
      }
    }
  }

}
