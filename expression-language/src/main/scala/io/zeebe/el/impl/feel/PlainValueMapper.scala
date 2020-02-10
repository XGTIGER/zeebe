/**
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.el.impl.feel

import org.camunda.feel.interpreter.Val
import org.camunda.feel.spi.CustomValueMapper

object PlainValueMapper extends CustomValueMapper {

  // no custom transformation here - just delegate to the default mapper
  override def toVal(x: Any, innerValueMapper: Any => Val): Option[Val] = None

  // in order to keep the type information,
  // return Val type as it is without transforming it into an other type
  override def unpackVal(value: Val, innerValueMapper: Val => Any): Option[Any] = Some(value)

  // for accessing this object from Java
  val INSTANCE = PlainValueMapper
}
