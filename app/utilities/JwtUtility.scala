package utilities

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import play.Logger

/** ****************************************************************************
  * Utility Object for providing the functionality of JWT i.e.
  * Json Web Tokens. This mechanism is used for authentication purposes.
  * ****************************************************************************
  */

object JwtUtility {

  // secret Tokenisation mechanism
  val JwtSecretKey = "shadows conceal the light" // It should for some time. I hope so
  val JwtSecretAlgo = "HS256"
  // These two values are eventually used for the on the go authentication

  // for testing the configuration of the tokenisation mechanism
  def ConfTest(): Unit = {
    Logger.info("Secret Key: " + JwtSecretKey)
    Logger.info("Secret Algorithm: " + JwtSecretAlgo)
  }

  // method for creating a token from a json String
  def createToken(payload: String): String = {
    val header = JwtHeader(JwtSecretAlgo)
    val claimsSet = JwtClaimsSet(payload)
    JsonWebToken(header, claimsSet, JwtSecretKey)
  }

  // method to validate the token against the
  // secret key
  def isValidToken(jwtToken: String): Boolean =
    JsonWebToken.validate(jwtToken, JwtSecretKey)

  // method for decoding the payload of a JWT token
  def decodePayload(jwtToken: String): Option[String] =
    jwtToken match {
      case JsonWebToken(_, claimsSet, _) => Some(claimsSet.asJsonString)
      case _                                          => None
    }
}
