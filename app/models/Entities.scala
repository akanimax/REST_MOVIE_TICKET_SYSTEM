package models


/** *******************************************************************
  * Set of Entities that make up the business logic of the REST system
  * These are the models used by the system
  * ********************************************************************
  * */

// Model for User
case class User(email: String, password: String)

// Model for a Movie
case class Movie(movie_id: Int, title: String, description: String)

// Model for a booking transaction
case class Booking_Transaction(trans_id: Long,
                               movie_id: String,
                               email: String,
                               name: String,
                               cardno: String,
                               cvv: Int,
                               exp: String)