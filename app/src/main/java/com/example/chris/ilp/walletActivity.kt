package com.example.chris.ilp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.*
import com.example.chris.ilp.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.geojson.FeatureCollection

class walletActivity:AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var backToGame:Button
    private lateinit var rateOfToday:TextView
    private lateinit var walletLayout: LinearLayout
    private lateinit var listView: ListView
    private lateinit var listofgifts : ListView
    private var rates = ""
    private var listofcoins = ArrayList<String>()
    private var giftslist = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_wallet)

        //initialise views of layout
        backToGame = findViewById<Button>(id.backtogameButton)
        rateOfToday = findViewById<TextView>(id.rateOfToday)
        walletLayout = findViewById<LinearLayout>(id.walletLayout)
        listView = findViewById<ListView>(id.listview)
        listofgifts = findViewById<ListView>(id.listofgifts)


        //set up fire base database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        //call loadData function to load current user's data such as user Coins collected.
        loadData(auth.currentUser?.uid.toString())

        //add listener to pass index and type of target coin to treat Activity
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, p2, _ ->
            val i = Intent(this@walletActivity, TreatActivity::class.java)
            i.putExtra("type","userCoins")
            i.putExtra("key", p2)
            startActivity(i)
        }

        //add listener to pass index and type of selected gift to treat activity
        listofgifts.onItemClickListener = AdapterView.OnItemClickListener{ _,_,p2,_ ->
            val intentToTreat = Intent(this@walletActivity, TreatActivity::class.java)
            intentToTreat.putExtra("type","gift")
            intentToTreat.putExtra("key",p2)
            startActivity(intentToTreat)
        }

        //click listener to go back main activity
        backToGame.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    //helper function loadData which load today's exchange rate from firebase to the screen and pass coin values to view.
    private fun loadData(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    rates = user.rates
                    val textRates = user.rates
                    //display the rates to the screen.
                    val finalText = "Today's Exchange Rates: \n"+ textRates.split(",")[0].drop(1) +"\n"+textRates.split(",")[1]+ "\n" +textRates.split(",")[2] +"\n" +textRates.split(",")[3].dropLast(1)
                    rateOfToday.text = finalText

                    //create a collection to pass all value and type of coins to listView
                    val geoCoins = FeatureCollection.fromJson(user.userCoins)
                    val coins = geoCoins.features()

                    if (coins != null) {
                        for (coin in coins){
                            val text1 = coin.properties()!!.get("currency").toString()
                            val text2 = coin.properties()!!.get("value").toString()
                            listofcoins.add("$text1:$text2")
                        }
                    }
                    //create adapter to display.
                    val arrayAdapter = ArrayAdapter(this@walletActivity,android.R.layout.simple_list_item_1,listofcoins)
                    listView.adapter = arrayAdapter

                    //create a collection to pass all value and type of gifts to list of Gifts
                    val geoGifts = FeatureCollection.fromJson(user.gift)
                    val gifts = geoGifts.features()

                    if (gifts != null) {
                        for (gift in gifts){
                            val gifttype = gift.properties()!!.get("currency").toString()
                            val giftvalue = gift.properties()!!.get("value").toString()
                            giftslist.add("$gifttype:$giftvalue")
                        }
                    }
                    //create adapter to display.
                    val arrayAdapterforgifts = ArrayAdapter(this@walletActivity,android.R.layout.simple_list_item_1,giftslist)
                    listofgifts.adapter = arrayAdapterforgifts


                }
            }

            override fun onCancelled(error: DatabaseError) { }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

}