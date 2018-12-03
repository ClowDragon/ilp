package com.example.chris.ilp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.geojson.FeatureCollection
import org.json.JSONObject

class TreatActivity:AppCompatActivity(){

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    private lateinit var treatingcoin : TextView
    private lateinit var remainChances : TextView
    private lateinit var uidfortreat : EditText
    private lateinit var savetobank : Button
    private lateinit var sendtoothers : Button
    private lateinit var returntowallet : Button
    private var userCoins :String = "for saving user coins"
    private var sendusercoin:String = "for receiving user coins"
    private var userGifts :String = "for saving user gifts"
    private var ratio : Double = 1.0
    private var limit : Double = 0.0
    private var index : Int = 0
    private var cointype = ""
    private var rates : String = ""
    private var usergold : Double = 0.0
    //variable result for judging UID is valid or not.
    private var result = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treat)

        //initialise fire base database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        //create views for layout.
        treatingcoin = findViewById<TextView>(R.id.treatingcoin)
        uidfortreat = findViewById<EditText>(R.id.uidfortreat)
        remainChances = findViewById<TextView>(R.id.remainChances)
        savetobank = findViewById<Button>(R.id.savetobank)
        sendtoothers = findViewById<Button>(R.id.sendtoother)
        returntowallet = findViewById<Button>(R.id.returntowallet)

        //extract the data transferred from wallet activity which contains the index of target coin.
        val extras = intent.extras
        if (extras != null) {
            index = extras.getInt("key")
            //The key argument here must match that used in the wallet activity
            cointype = extras.getString("type")
        }

        //load the data of chosen coin and show it to the screen
        loadCoin(auth.currentUser?.uid.toString())

        //set click listener to the button
        returntowallet.setOnClickListener {
            val intenttowallet = Intent(this@TreatActivity,walletActivity::class.java)
            startActivity(intenttowallet)
        }

        //add text listener to check if the content of edit text is a valid uid.
        uidfortreat.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(p0: Editable) {
                checkuidexist(p0.toString())
            }
        })

        //send the coin from current user to another.
        sendtoothers.setOnClickListener {
            if (uidfortreat.text.length!=28) {
                Toast.makeText(this@TreatActivity, "Please enter a valid uid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (!this.result) {
                Toast.makeText(this@TreatActivity, "uid not exist!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(uidfortreat.text.toString()==auth.currentUser?.uid.toString()){
                Toast.makeText(this@TreatActivity, "Can not send to yourself!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(cointype=="gift"){
                Toast.makeText(this@TreatActivity, "Can not send your gift to other!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {
                Toast.makeText(this@TreatActivity, "Success!", Toast.LENGTH_SHORT).show()
                //take information of current user and make changes
                val geoCoins = FeatureCollection.fromJson(this.userCoins)
                val coins = geoCoins.features()
                val targetcoin = coins!![index]
                //remove the chosen coin and delete it from the database.
                coins.removeAt(index)
                val newuserCoins = FeatureCollection.fromFeatures(coins)
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("userCoins").setValue(newuserCoins.toJson())

                //load information of the treating user and update database.
                val sendgeoCoins = FeatureCollection.fromJson(this.sendusercoin)
                val sendcoins = sendgeoCoins.features()
                //add the chosen coin to this user
                sendcoins!!.add(targetcoin)
                val newsenduserCoins = FeatureCollection.fromFeatures(sendcoins)
                dbRef.child("users").child(uidfortreat.text.toString()).child("gift").setValue(newsenduserCoins.toJson())
            }
            //back to wallet activity if success.
            val intenttowallet = Intent(this@TreatActivity,walletActivity::class.java)
            startActivity(intenttowallet)
        }

        //add button listener for saving the coin
        savetobank.setOnClickListener {
            //call the helper function saveToBank and intent to wallet activity if success.
            if(limit<25){
                saveToBank()
                Toast.makeText(this@TreatActivity,"Saved to Bank!",Toast.LENGTH_LONG).show()
                val intenttowallet = Intent(this@TreatActivity,walletActivity::class.java)
                startActivity(intenttowallet)
            }
            //if the limit reach 25 means no remaining chances for saving to bank thus disable the button.
            else{
                savetobank.isClickable = false
                Toast.makeText(this@TreatActivity,"Reached the limit!",Toast.LENGTH_LONG).show()
                savetobank.setBackgroundColor(Color.GRAY)
                return@setOnClickListener
            }
        }
    }

    //help function to load coins to local variables using value event listener.
    private fun loadCoin(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    userCoins = user.userCoins
                    rates = user.rates
                    usergold = user.gold
                    ratio = user.ratio
                    userGifts = user.gift

                    //get the limit from database and print to screen
                    limit = user.limit
                    val remain = 25-limit
                    val remaintext = "You have $remain chances for saving!"
                    remainChances.text = remaintext

                    val geoCoins = FeatureCollection.fromJson(user.userCoins)
                    val coins = geoCoins.features()
                    val targetfeature = coins!!.get(index)
                    //updating text on the screen to information of target coin.
                    val text1 = targetfeature.properties()!!.get("currency").toString()
                    val text2 = targetfeature.properties()!!.get("value").toString()
                    val combinedtext = "Coin type: "+text1 + "\n" + "Coin value: " + text2
                    treatingcoin.text = combinedtext
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

    //helper function to save the target coin to bank in form of gold.
    private fun saveToBank(){
        //create JSON object to get the value and currency of the target coin from whole list of coins.
        if(cointype=="userCoins") {
            val geoCoins = FeatureCollection.fromJson(this.userCoins)
            val coins = geoCoins.features()
            val targetcoin = coins!![index]
            val value = targetcoin.properties()!!.get("value").asDouble
            val type = targetcoin.properties()!!.get("currency").asString

            //create JSON object to get the rates of 4 types of currency.
            val ratesJson = JSONObject(rates)
            val thisrate = ratesJson.get(type).toString().toDouble()
            val gold = value * thisrate * ratio + usergold
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(gold)

            //we need to remove the coin after transferred to gold and delete from the database.
            coins.removeAt(index)
            val newuserCoins = FeatureCollection.fromFeatures(coins)
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("userCoins").setValue(newuserCoins.toJson())


            limit += 1
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("limit").setValue(limit)
        }
        //same method for saving gift to bank but no need to +1 to limit
        else{
            val geoGifts = FeatureCollection.fromJson(this.userGifts)
            val gifts = geoGifts.features()
            val targetgift = gifts!![index]

            val giftvalue = targetgift.properties()!!.get("value").asDouble
            val gifttype = targetgift.properties()!!.get("currency").asString

            val ratesJson = JSONObject(rates)
            val thisrate = ratesJson.get(gifttype).toString().toDouble()
            val gold = giftvalue * thisrate * ratio + usergold
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(gold)

            gifts.removeAt(index)
            val newuserGifts = FeatureCollection.fromFeatures(gifts)
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gift").setValue(newuserGifts.toJson())


        }
    }

    //helper function to check if the UID in the edit text exist.
    private fun checkuidexist(userId: String){
        val dataListener2 = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child("displayName").exists()){
                    //the UID is exist if it has a child root of displayName Thus we assign the result value to true
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    result = true
                    //load the user data of this UID in edit text.
                    sendusercoin = user.gift
                }
                else{
                    result = false
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        dbRef.child("users").child(userId).addListenerForSingleValueEvent(dataListener2)
    }
}