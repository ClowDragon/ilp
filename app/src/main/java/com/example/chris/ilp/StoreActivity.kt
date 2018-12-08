package com.example.chris.ilp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StoreActivity: AppCompatActivity(){


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var usergold : TextView
    private lateinit var userCurrentRatio:TextView
    private lateinit var vipLevelText :TextView
    private lateinit var goldboosterbutton :Button
    private lateinit var upgradeVIPlevel :Button
    private lateinit var resetlimit : Button
    private lateinit var backtogameimageButton: ImageButton
    private var userRatio = 1.0
    private var userGold = 0.0
    private var vipLevel = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        usergold = findViewById<TextView>(R.id.usergold)
        userCurrentRatio = findViewById<TextView>(R.id.userCurrentRatio)
        vipLevelText = findViewById<TextView>(R.id.vipleveletext)
        goldboosterbutton = findViewById<Button>(R.id.goldboosterbutton)
        upgradeVIPlevel = findViewById<Button>(R.id.viplevelupbutton)
        resetlimit = findViewById(R.id.nolimitbutton)
        backtogameimageButton = findViewById<ImageButton>(R.id.backtogameimageButton)

        //load user data from database to screen using helper function loadUserRatio
        loadUserRatio(auth.currentUser?.uid.toString())

        //Back to game button
        backtogameimageButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        //button for upgrade vip level
        upgradeVIPlevel.setOnClickListener {
            //maximum vip level is 4
            if(vipLevel<4){
                //need vip level +1 times 1000 gold to upgrade.
                val judge  = (vipLevel+1)*1000
                if (userGold>judge){
                    Toast.makeText(this@StoreActivity,"Upgraded your VIP level!",Toast.LENGTH_LONG).show()
                    dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(userGold-judge)
                    dbRef.child("users").child(auth.currentUser?.uid.toString()).child("ratio").setValue(userRatio+0.5)
                    dbRef.child("users").child(auth.currentUser?.uid.toString()).child("VIPlevel").setValue(vipLevel+1)
                }
                else{
                    Toast.makeText(this@StoreActivity,"Not enough gold need $judge!",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }else{
                Toast.makeText(this@StoreActivity,"You have reached the highest VIP level!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

        //using 2000 gold to reset limit to 0.
        resetlimit.setOnClickListener{
            if (userGold<2000){
                Toast.makeText(this@StoreActivity,"Not enough gold!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else{
                Toast.makeText(this@StoreActivity,"Reset your limit successfully!",Toast.LENGTH_LONG).show()
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(userGold-2000)
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("limit").setValue(0)
                loadUserRatio(auth.currentUser?.uid.toString())
            }
        }

        //get a booster from this button and update ratio and gold
        goldboosterbutton.setOnClickListener {
            if(userGold<1000){
                Toast.makeText(this@StoreActivity,"Not enough gold!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else{
                Toast.makeText(this@StoreActivity,"You get a gold booster!!",Toast.LENGTH_LONG).show()
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("ratio").setValue(userRatio*2)
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(userGold-1000)
                loadUserRatio(auth.currentUser?.uid.toString())
            }
        }
    }


    private fun loadUserRatio(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    userRatio = user.ratio
                    userGold = user.gold
                    vipLevel = user.VIPlevel

                    val levelText = "VIP "+user.VIPlevel
                    vipLevelText.text = levelText

                    val textofgold = "Your gold: "+"%.3f".format(user.gold)
                    usergold.text = textofgold

                    val textofratio = "Your exchange ratio is : " + user.ratio.toString()
                    userCurrentRatio.text = textofratio
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

}