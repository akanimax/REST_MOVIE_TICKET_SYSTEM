package controllers

import javax.inject._

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import utilities.{DbUtility, JwtUtility}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("The rest Endpoint System is Ready"))
  }

  def register = Action.async(parse.formUrlEncoded) {
    request =>
      val email= request.body("email").head
      val password = request.body("password").head

      val failureMessage = "User email is already registered"

      DbUtility.register(email, password)
        .map {
          case Some(msg) => Ok(Json.obj("status" -> "OK", "msg" -> msg))
          case _ => BadRequest(Json.obj("status" -> "FAIL", "msg" -> failureMessage))
        }
  }

  def authenticate = Action.async(parse.formUrlEncoded) {
    request =>
      val email= request.body("email").head
      val password = request.body("password").head

      val failureMessage = "Login Unsuccessful"

      DbUtility.authenticate(email, password)
        .map {
          case Some(token) => Ok(Json.obj("status" -> "SUCCESS", "token" -> token))
          case _ => BadRequest(Json.obj("status" -> "FAIL", "msg" -> failureMessage))
        }
  }

  def getMovies = Action.async {
    implicit val format = DbUtility.MovieSeqWrites
    DbUtility.getAllMovies.map(x => Ok(Json.toJson(x)))
  }

  // secure action
  def book = Action.async(parse.formUrlEncoded) {
    request =>
      // extract the token from the request header
      // use empty string if one doesn't exist
      val token = request.headers.get("token").getOrElse("")

      // check if the token passes the validation test
      if(JwtUtility.isValidToken(token)) {
        // the token is valid. So perform the book action
        val payload = JwtUtility.decodePayload(token)

        payload match {
          case Some(credentials) => {
            // extract the email from the credentials
            val email = (Json.parse(credentials) \ "email").as[String]

            // extract the remaining data from the request form
            val movie_id = request.body("movie_id").head
            val name = request.body("name").head
            val cardno = request.body("cardno").head
            val cvv = request.body("cvv").head
            val month = request.body("month").head
            val year = request.body("year").head

            // perform the DBtransaction
            DbUtility.addTransaction(
              movie_id, email,
              name, cardno,
              Integer.parseInt(cvv),
              month, year
            ).map {
              case Some(msg) => Ok(Json.obj("status" -> "OK", "msg" -> msg))
              case None => BadRequest(
                Json.obj("status" -> "FAIL", "msg" -> "Transaction Failed")
              )
            }
          }

          case _ => Future(Unauthorized(
            Json.obj("status" -> "FAIL", "msg" -> "Credentials Invalid in token")
          ))
        }
      }

      else Future(Unauthorized(
        Json.obj("status" -> "FAIL", "msg" -> "No authentication token found"))
      )
  }

}
