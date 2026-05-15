package com.nammamistri

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nammamistri.calculator.CalculatorFragment
import com.nammamistri.team.TeamFragment
import com.nammamistri.photos.PhotosFragment
import com.nammamistri.rates.RatesFragment
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Step 1: Make app draw behind the status bar ──────
        //
        //   By default Android reserves space for the status bar
        //   and draws your app BELOW it.
        //
        //   This line tells Android: "Let my app draw edge-to-edge
        //   including BEHIND the status bar area."
        //
        //   We then manually add padding equal to the status bar
        //   height so the title text is NOT hidden behind it.
        //
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        // ── Step 2: Get the status bar spacer view ───────────
        val statusBarSpacer = findViewById<View>(R.id.status_bar_spacer)

        // ── Step 3: Measure status bar height and apply ──────
        //
        //   ViewCompat.setOnApplyWindowInsetsListener waits until
        //   Android has measured the screen and knows the exact
        //   height of the status bar (varies by phone — could be
        //   24dp, 28dp, 32dp, etc.)
        //
        //   We set that height as the spacer height so the orange
        //   toolbar fills BEHIND the status bar but the text sits
        //   safely BELOW the icons.
        //
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content)
        ) { _, insets ->
            val statusBarHeight = insets
                .getInsets(WindowInsetsCompat.Type.statusBars())
                .top                                    // height in pixels

            // Apply that height to the invisible spacer view
            val params = statusBarSpacer.layoutParams
            params.height = statusBarHeight
            statusBarSpacer.layoutParams = params

            insets   // return insets so other views can also use them
        }

        // ── Step 4: Load Calculator screen first ─────────────
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CalculatorFragment())
                .commit()
        }

        // ── Step 5: Switch screens on tab tap ────────────────
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_calculator -> CalculatorFragment()
                R.id.nav_team       -> TeamFragment()
                R.id.nav_photos     -> PhotosFragment()
                R.id.nav_rates      -> RatesFragment()
                else                -> CalculatorFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }
}