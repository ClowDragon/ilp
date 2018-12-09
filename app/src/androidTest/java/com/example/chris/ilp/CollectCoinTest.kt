package com.example.chris.ilp

import android.support.test.runner.AndroidJUnit4
import com.mapbox.geojson.FeatureCollection
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectCoinTest {
    private var userCoins :String = "{\n"+"\"type\":\"FeatureCollection\",\n"+"\"features\":[]\n"+"}"
    @Test
    @Throws(Exception::class)

    fun loadNameAndStatus(){
        val geojsonmap = FeatureCollection.fromJson(userCoins)
        val fcs = geojsonmap.features()
        val feature = "{\"features\":[{\"geometry\":{\"coordinates\":[-3.1858926184808656,55.94587374992963],\"type\":\"Point\"},\"properties\":{\"id\":\"694c-173d-1b2b-dc67-4a2d-8c66\",\"value\":\"6.159613041613326\",\"currency\":\"QUID\",\"marker-symbol\":\"6\",\"marker-color\":\"#ffdf00\"},\"type\":\"Feature\"}],\"type\":\"FeatureCollection\"}"

        val newfc = FeatureCollection.fromJson(feature)
        val fc = newfc.features()
        fcs?.add(fc?.get(0))
    }

    fun assert(){
        Assert.assertNotEquals("{\n"+"\"type\":\"FeatureCollection\",\n"+"\"features\":[]\n"+"}", userCoins)
    }
}