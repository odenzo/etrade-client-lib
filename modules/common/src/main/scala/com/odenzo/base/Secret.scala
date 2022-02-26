package com.odenzo.base

import scala.util.Random

/** Note: User oprint instead of pprint to continue masking */
case class Secret(secret: String):
  override def toString = s"${secret.take(2)}...${secret.takeRight(2)}"

// implicit  show: Show[Secret]          = Show.fromToString[Secret]

object Secret:
  def generatePassword(len: Int = 15): String = Random.nextString(len)
  def generate: Secret                        = Secret(generatePassword())
  def generate(len: Int = 15): Secret         = Secret(generatePassword(len))

case class LoginCreds(user: String, password: Secret):
  def genForUser(user: String) = LoginCreds(user, Secret.generate(15))
