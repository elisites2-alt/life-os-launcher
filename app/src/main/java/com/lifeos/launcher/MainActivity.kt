
package com.lifeos.launcher

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.text.TextWatcher
import android.text.Editable

class MainActivity : Activity() {

    private val backgroundColor = Color.rgb(8, 13, 30)
    private val panelColor = Color.rgb(19, 29, 52)
    private val cyan = Color.rgb(60, 230, 220)
    private lateinit var appList: LinearLayout
    private lateinit var searchBox: EditText

    data class AppItem(
        val label: String,
        val packageName: String,
        val icon: android.graphics.drawable.Drawable
    )

    private var installedApps = listOf<AppItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = backgroundColor
        window.navigationBarColor = backgroundColor
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        buildScreen()
        loadApps()
        renderApps("")
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 26, 22, 16)
            setBackgroundColor(backgroundColor)
        }

        val brand = TextView(this).apply {
            text = "L I F E   O S"
            textSize = 25f
            setTextColor(cyan)
            typeface = Typeface.create(
                "sans-serif-medium", Typeface.BOLD
            )
        }
        root.addView(brand)

        val tagline = TextView(this).apply {
            text = "ONE PHONE. MANY LIVES."
            textSize = 10f
            letterSpacing = 0.18f
            setTextColor(Color.LTGRAY)
            setPadding(0, 6, 0, 18)
        }
        root.addView(tagline)

        val clock = TextView(this).apply {
            text = "Your world, in one place."
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(0, 14, 0, 18)
        }
        root.addView(clock)

        searchBox = EditText(this).apply {
            hint = "Search apps..."
            textSize = 16f
            setSingleLine(true)
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setPadding(18, 12, 18, 12)
            background = rounded(panelColor, 20)
        }
        root.addView(
            searchBox,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 54
            )
        )

        val section = TextView(this).apply {
            text = "YOUR APPS"
            textSize = 12f
            letterSpacing = 0.15f
            setTextColor(cyan)
            setPadding(2, 24, 0, 12)
        }
        root.addView(section)

        val scroll = ScrollView(this)
        appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(appList)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(0, 0, 1f)
        )

        val footer = TextView(this).apply {
            text = "LIFE OS  •  YOUR LIFE, CONNECTED"
            gravity = Gravity.CENTER
            textSize = 10f
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 8)
        }
        root.addView(footer)

        setContentView(root)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int,
                count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int,
                before: Int, count: Int
            ) {
                renderApps(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun rounded(
        color: Int,
        radius: Float
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
            setStroke(1, Color.rgb(42, 66, 94))
        }
    }

    @Suppress("DEPRECATION")
    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        installedApps = packageManager
            .queryIntentActivities(intent, 0)
            .mapNotNull { info ->
                val activityInfo = info.activityInfo
                    ?: return@mapNotNull null

                AppItem(
                    label = info.loadLabel(packageManager).toString(),
                    packageName = activityInfo.packageName,
                    icon = info.loadIcon(packageManager)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun renderApps(query: String) {
        if (!::appList.isInitialized) return

        appList.removeAllViews()

        val matches = installedApps.filter {
            it.label.contains(query, ignoreCase = true)
        }

        if (matches.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No matching apps found."
                setTextColor(Color.LTGRAY)
                textSize = 15f
                setPadding(8, 18, 8, 18)
            }
            appList.addView(empty)
            return
        }

        matches.forEach { app ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 12, 14, 12)
                background = rounded(panelColor, 18)
            }

            val iconView = android.widget.ImageView(this).apply {
                setImageDrawable(app.icon)
            }

            row.addView(
                iconView,
                LinearLayout.LayoutParams(46, 46)
            )

            val name = TextView(this).apply {
                text = app.label
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(14, 0, 0, 0)
            }

            row.addView(
                name,
                LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
            )

            row.setOnClickListener {
                val launchIntent =
                    packageManager.getLaunchIntentForPackage(
                        app.packageName
                    )

                if (launchIntent != null) {
                    try {
                        startActivity(launchIntent)
                    } catch (_: Exception) {
                        Toast.makeText(
                            this,
                            "Unable to open ${app.label}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            appList.addView(
                row,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 10
                }
            )
        }
    }
}
