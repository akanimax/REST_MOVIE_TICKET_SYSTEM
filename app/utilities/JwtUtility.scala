package utilities

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import play.Logger

/**
  * Created by botman on 15/7/17.
  */

object JwtUtility {

  // secret Tokenisation mechanism
  val JwtSecretKey = "I love Nikita Chinchwade"
  val JwtSecretAlgo = "HS256"
  // These two values are eventually used for the on the go authentication

  // for testing the configuration of the tokenisation mechanism
  def ConfTest = {
    Logger.info("Secret Key: " + JwtSecretKey)
    Logger.info("Secret Algorithm: " + JwtSecretAlgo)
  }

  def createToken(payload: String): String = {
    val header = JwtHeader(JwtSecretAlgo)
    val claimsSet = JwtClaimsSet(payload)
    JsonWebToken(header, claimsSet, JwtSecretKey)
  }

  def isValidToken(jwtToken: String): Boolean =
    JsonWebToken.validate(jwtToken, JwtSecretKey)

  // although this method is not required for the current scenario,
  // I thought I will still implement as it might be required in the
  // future specs for development
  def decodePayload(jwtToken: String): Option[String] =
    jwtToken match {
      case JsonWebToken(header, claimsSet, signature) => Some(claimsSet.asJsonString)
      case _                                          => None
    }
}
