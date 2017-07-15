package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._
import utilities.DbUtility

import scala.concurrent.ExecutionContext.Implicits.global

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

}
