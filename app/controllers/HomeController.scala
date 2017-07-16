package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._
import utilities.{DbUtility, JwtUtility}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * Our desired REST endSystem
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Action not really required. But I left it here.
  // This can be modified through the templates to display
  // an explanation of the REST end system and how to use it.
  // I usually do that. But Didn't here.
  def index = Action {
    Ok(views.html.index("The rest Endpoint System is Ready"))
  }

  // Action for registering a user
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

  //Action for authenticating the user
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

  // Action to acquire the list of movies
  def getMovies = Action.async {
    implicit val format = DbUtility.MovieSeqWrites
    DbUtility.getAllMovies.map(x => Ok(Json.toJson(x)))
  }

  // Action to book the ticket for a particular movie
  // checks the token before performing the transaction
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

            // perform the database transaction
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
