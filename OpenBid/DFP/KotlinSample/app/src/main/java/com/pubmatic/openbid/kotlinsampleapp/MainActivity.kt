package com.pubmatic.openbid.kotlinsampleapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_home.*


class MainActivity : AppCompatActivity()  {

    var recycler: RecyclerView ? = null
    var list: ArrayList<String> ? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)

        recycler = findViewById(R.id.recycler_view)
        recycler!!.layoutManager = LinearLayoutManager(this)
        recycler!!.setHasFixedSize(true)

        list = ArrayList<String>()
        list?.add("Banner")
        list?.add("Interstitial")
        val recyclerAdapter = RecyclerAdapter(list, RecyclerItemListener())
        recycler!!.adapter = recyclerAdapter
    }

    fun displayActivity(position: Int){
        val intent :Intent
        if(position == 0){
            intent = Intent(this, DFPBannerActivity::class.java)
        }else{
            intent = Intent(this, DFPInterstitialActivity::class.java)
        }
        startActivity(intent)
    }


    inner class RecyclerItemListener : RecyclerAdapter.OnItemClickListener{
        override fun onItemClick(position: Int) {
            displayActivity(position)
        }

    }







}
