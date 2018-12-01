package com.example.chris.ilp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.*
import com.example.chris.ilp.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import org.json.JSONObject

class walletActivity:AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var backToGame:Button
    private lateinit var rateOfToday:TextView
    private lateinit var walletLayout: LinearLayout
    private lateinit var listView: ListView
    private var rates = ""
    private var listofcoins = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_wallet)
        backToGame = findViewById<Button>(id.backtogameButton)
        rateOfToday = findViewById<TextView>(id.rateOfToday)
        walletLayout = findViewById<LinearLayout>(id.walletLayout)
        listView = findViewById<ListView>(id.listview)



        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        loadData(auth.currentUser?.uid.toString())


        listView.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, p2, p3 ->
            val value = p2
            //val rate = rates
            val i = Intent(this@walletActivity, TreatActivity::class.java)
            i.putExtra("key", value)
            //i.putExtra("rate",rate)
            startActivity(i)
        }

        backToGame.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadData(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    rates = user.rates
                    //UserCoins = user.userCoins
                    val textRates = user.rates

                    rateOfToday.text = "Today's Exchange Rates: \n"+ textRates.split(",")[0].drop(1) +"\n"+textRates.split(",")[1]+ "\n" +textRates.split(",")[2] +"\n" +textRates.split(",")[3].dropLast(1)




                    val geoCoins = FeatureCollection.fromJson(user.userCoins)
                    val coins = geoCoins.features()

                    if (coins != null) {
                        for (coin in coins){
                            val text1 = coin.properties()!!.get("currency").toString()
                            val text2 = coin.properties()!!.get("value").toString()
                            //addCoinToWallet(text1+":"+text2)
                            listofcoins.add(text1+":"+text2)
                        }
                    }

                    val arrayAdapter = ArrayAdapter(this@walletActivity,android.R.layout.simple_list_item_1,listofcoins)
                    listView.adapter = arrayAdapter


                }
            }

            override fun onCancelled(error: DatabaseError) { }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

    private fun addCoinToWallet(Str :String){
        // Create a new TextView instance programmatically
        val text_view: TextView = TextView(this)
        // Creating a LinearLayout.LayoutParams object for text view
        var params : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // This will define text view width
                LinearLayout.LayoutParams.WRAP_CONTENT // This will define text view height
        )
        // Add margin to the text view
        params.setMargins(10,10,10,10)
        // Now, specify the text view width and height (dimension)
        text_view.layoutParams = params
        // Display some text on the newly created text view
        text_view.text = Str
        // Set the text view font/text size
        text_view.setTextSize(TypedValue.COMPLEX_UNIT_SP,30F)
        // Set the text view text color
        text_view.setTextColor(Color.RED)
        // Make the text viw text bold italic
        text_view.setTypeface(text_view.typeface, Typeface.BOLD_ITALIC)
        // Change the text view font
        text_view.typeface = Typeface.MONOSPACE
        // Change the text view background color
        text_view.setBackgroundColor(Color.YELLOW)
        // Put some padding on text view text
        text_view.setPadding(50,10,10,10)
        // Finally, add the text view to the view group
        walletLayout.addView(text_view)
    }
}