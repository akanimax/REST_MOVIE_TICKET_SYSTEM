package models

/**
  * Created by botman on 15/7/17.
  */

case class User(email: String, password: String)

case class Movie(movie_id: Int, title: String, description: String)

case class Booking_Transaction(trans_id: Long,
                               movie_id: String,
                               email: String,
                               name: String,
                               cardno: String,
                               cvv: Int,
                               exp: String)