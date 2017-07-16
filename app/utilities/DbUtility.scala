package utilities

import models.{Booking_Transaction, Movie, User}
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import slick.driver.MySQLDriver.api._
import slick.jdbc.JdbcProfile
import slick.lifted.Tag

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** ********************************************************************************
  * Utility mechanism for providing the functionality of Database connectivity
  * using the slick library.
  * ********************************************************************************
  */

// slick table mapping for the User table in the database
class UserTable(tag: Tag) extends Table[User](tag, "User") {

  def email = column[String]("email", O.PrimaryKey)
  def password = column[String]("password")

  override def * = (email, password) <> (User.tupled, User.unapply)
}

// slick table mapping for the Movie table in the database
class MovieTable(tag: Tag) extends Table[Movie](tag, "Movie") {

  def movie_id = column[Int]("movie_id", O.PrimaryKey)
  def title = column[String]("title")
  def description = column[String]("description")

  override def * = (movie_id, title, description) <> (Movie.tupled, Movie.unapply)
}

// slick table mapping for the Booking_Transaction table in the database
class Booking_TransactionTable(tag: Tag) extends
  Table[Booking_Transaction](tag, "Booking_Transaction") {

  def trans_id = column[Long]("trans_id", O.PrimaryKey)
  def movie_id = column[String]("movie_id")
  def email = column[String]("email")
  def name = column[String]("name")
  def cardno = column[String]("cardno")
  def cvv = column[Int]("cvv")
  def exp = column[String]("exp")

  override def * = (trans_id, movie_id, email, name, cardno, cvv, exp) <>
    (Booking_Transaction.tupled, Booking_Transaction.unapply)

}

// The Database utility object. Uses slick object
object DbUtility {

  // json writes format for object of type Seq[Movies]
  val MovieSeqWrites = new Writes[Seq[Movie]] {
    def writes(movies: Seq[Movie]): JsValue = {

      // the sequence of movies
      val movieSeq = movies.map (
        movie => Json.obj(
          "movie_id" -> JsString(movie.movie_id.toString),
          "title" -> JsString(movie.title),
          "description" -> JsString(movie.description)
        )
      )

      Json.obj("movies" -> movieSeq)
    }
  }

  // slick database object
  val db = DatabaseConfigProvider.get[JdbcProfile](Play.current).db

  // table references
  val users = TableQuery[UserTable]
  val movies = TableQuery[MovieTable]
  val booking_transactions = TableQuery[Booking_TransactionTable]

  // method to register a new user in the database
  // this method adds data to the User table
  def register(email: String, password: String): Future[Option[String]] = {
    // following returns a Future[Option[User]]
    db.run(users.filter(_.email === email).result.headOption).flatMap {

      // if the email doesn't already exists, add the user
      case None => db.run(users += User(email, password))
                      .map(_=> Some("User successfully registered"))

      // else don't add the email to the table
      case _ => Future(None)
    }
  }

  // method to check if the user is authentic
  def authenticate(email: String, password: String): Future[Option[String]] = {
    // check if the credentials are present in the database
    db.run(users.filter(_.email === email).result.headOption).map{

      // if such a user (with given email id) exists
      case Some(User(mail, pwd)) => {
        if(pwd == password) Some(
          JwtUtility.createToken(Json.obj("email" -> email,
          "password" -> password).toString)
        )
        else None
      }

      // else
      case _ => None
    }

  }

  // to book tickets basically
  def addTransaction(movie_id: String,
                     email: String, name: String,
                     cardno: String, cvv: Int,
                     month: String, year: String): Future[Option[String]] = {

    db.run(booking_transactions.length.result).flatMap {
      x =>

        // I convert the month and year to a mysql date Datatype
        db.run(booking_transactions +=
          Booking_Transaction(x + 1, movie_id, email,
            name, cardno, cvv, year + "-" + month + "-01")).map {
          _ => Some("Transaction Successful")
        }
    }
  }

  // method to return the movies List
  // simply return all the possible movies in the movies table
  def getAllMovies: Future[Seq[Movie]] = db.run(movies.result)
}