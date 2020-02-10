/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.el.impl.feel

import io.zeebe.el.EvaluationContext
import io.zeebe.el.impl.MessagePackConverter
import io.zeebe.msgpack.spec.MsgPackReader
import org.camunda.feel.interpreter.VariableProvider
import org.camunda.feel.spi.CustomContext

class FeelVariableContext(context: EvaluationContext) extends CustomContext {

  override def variableProvider: VariableProvider = new ZeebeExpressionValueProvider

  class ZeebeExpressionValueProvider extends VariableProvider {

    val converter = new MessagePackConverter

    val reader = new MsgPackReader

    override def getVariable(name: String): Option[Any] = {
      Option(context.getVariable(name))
        .filter(value => value.capacity() > 0)
        .map(messagePack =>
          converter.readMessagePackValue(messagePack)
        )
    }

    override def keys: Iterable[String] = List.empty
  }

}
