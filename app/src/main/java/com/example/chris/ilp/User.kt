package com.example.chris.ilp

//created for saving user data to the fire base database.
data class User(val displayName: String="", val status:String="",
                val map:String="", val rates:String="", val userCoins :String= "",
                var gold :Double = 0.0, var ratio:Double = 1.0, var limit:Double = 0.0,
                val gift:String="", var VIPlevel:Int = 0)