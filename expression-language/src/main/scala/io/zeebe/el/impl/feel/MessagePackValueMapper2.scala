/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.el.impl.feel

import io.zeebe.msgpack.spec.{MsgPackReader, MsgPackType, MsgPackWriter}
import io.zeebe.util.buffer.BufferUtil
import io.zeebe.util.buffer.BufferUtil.bufferAsString
import org.agrona.{DirectBuffer, ExpandableArrayBuffer}
import org.camunda.feel.interpreter.{Val, _}
import org.camunda.feel.spi.CustomValueMapper

object MessagePackValueMapper2 extends CustomValueMapper {

  val reader = new MsgPackReader
  val writer = new MsgPackWriter

  override def toVal(x: Any, innerValueMapper: Any => Val): Option[Val] = x match {
    case buffer: DirectBuffer => {

      reader.wrap(buffer, 0, buffer.capacity())

      val offset = reader.getOffset

      val token = reader.readToken()

      token.getType match {
        case MsgPackType.NIL => Some(ValNull)
        case MsgPackType.BOOLEAN => Some(ValBoolean(token.getBooleanValue))
        case MsgPackType.INTEGER => Some(ValNumber(token.getIntegerValue))
        case MsgPackType.FLOAT => Some(ValNumber(token.getFloatValue))
        case MsgPackType.STRING => {
          val asString = bufferAsString(token.getValueBuffer)
          Some(ValString(asString))
        }
        case MsgPackType.ARRAY => {

          val itemOffsets = for (i <- 0 until token.getSize) yield {
            val offset = reader.getOffset
            reader.skipValue()
            offset
          }

          val items = for (itemOffset <- itemOffsets) yield {
            val item = BufferUtil.cloneBuffer(buffer, itemOffset, buffer.capacity() - itemOffset)
            innerValueMapper.apply(item)
          }

          Some(ValList(items.toList))
        }
        case MsgPackType.MAP => {
          val document = BufferUtil.cloneBuffer(buffer, offset, buffer.capacity() - offset)
          val context = new MessagePackContext(document)
          Some(ValContext(context))
        }
        case _ => None
      }
    }
    case _ => None
  }

  override def unpackVal(value: Val, innerValueMapper: Val => Any): Option[Any] = {
    val buffer = new ExpandableArrayBuffer()
    writer.wrap(buffer, 0)

    val v = value match {
      case ValNull => {
        writer.writeNil()
        Some(writer)
      }
      case ValString(string) => {
        writer.writeString(BufferUtil.wrapString(string))
        Some(writer)
      }
      case ValNumber(number) if number.isWhole => {
        writer.writeInteger(number.longValue)
        Some(writer)
      }
      case ValNumber(number) => {
        writer.writeFloat(number.doubleValue)
        Some(writer)
      }
      case ValBoolean(boolean) => {
        writer.writeBoolean(boolean)
        Some(writer)
      }
      case ValList(items) => {
        writer.writeArrayHeader(items.size)
        // avoid overriding the writer
        items.map(innerValueMapper)
        Some(writer)
      }
      case ValContext(context) => {
        val variables = context.variableProvider.getVariables
        writer.writeMapHeader(variables.size)

        for ((key, value) <- variables) {

          writer.writeString(BufferUtil.wrapString(key))
          // avoid overriding the writer
          innerValueMapper.apply(value.asInstanceOf[Val])
        }

        Some(writer)
      }
      case _ => None

    }

    v.map(writer => BufferUtil.cloneBuffer(buffer, 0, writer.getOffset))
  }

  val INSTANCE = MessagePackValueMapper2

}
