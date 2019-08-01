package com.jdoneill.slopeangle

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jdoneill.slopeangle.R.id
import com.jdoneill.slopeangle.R.layout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        val toolbar: Toolbar = findViewById(id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(id.fab)
        fab.setOnClickListener { view: View? ->
            Snackbar.make(view!!, "Unregister Slope Angle", Snackbar.LENGTH_LONG)
                    .setAction("Stop") { v: View? ->
                        val sasFrag = fragmentManager.findFragmentById(id.fragment) as SlopeAngleSensorFragment
                        sasFrag.unregisterListeners()
                    }.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }
}