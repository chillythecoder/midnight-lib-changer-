
package lol.peng.midnightlibchanger

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.media.ToneGenerator
import android.media.AudioManager
import android.graphics.Typeface
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val pickLib = 101
    private val saveLib = 102
    private val pickFont = 103

    private var libraryBytes: ByteArray? = null
    private var libraryName = "No library selected"
    private var pendingExport: ByteArray? = null

    private val tone by lazy {
        ToneGenerator(AudioManager.STREAM_SYSTEM, 35)
    }

    private var sounds = true
    private var stars = true
    private var animations = true
    private var accent = Color.BLACK

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var status: TextView

    private val found = mutableListOf<FoundString>()
    private var chosenFont: Typeface? = null

    private data class FoundString(
        val offset: Int,
        val value: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accent = Color.BLACK
        showHome()
    }

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private fun click() {
        if (sounds) {
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 45)
        }
    }

    private fun base(title: String, subtitle: String) {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(18))

            background = NightDrawable(
                this@MainActivity,
                { stars },
                { animations }
            )
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        header.addView(TextView(this).apply {
            text = "✦  MIDNIGHT"
            setTextColor(Color.BLACK)
            textSize = 13f
            letterSpacing = .22f
            setTypeface(null, Typeface.BOLD)
        })

        header.addView(TextView(this).apply {
            text = title
            setTextColor(Color.BLACK)
            textSize = 27f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, 0)
        })

        header.addView(TextView(this).apply {
            text = subtitle
            setTextColor(0xff555555.toInt())
            textSize = 13f
            setPadding(0, dp(5), 0, dp(18))
        })

        root.addView(header)

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        status = TextView(this).apply {
            text = "MIDNIGHT LIB CHANGER  •  PENG.LOL"
            setTextColor(0xff555555.toInt())
            textSize = 10f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(14), 0, 0)
        }

        root.addView(status)
        setContentView(root)
    }

    private fun card(label: String, detail: String? = null) {

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.BLACK)
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), 0xff333333.toInt())
            }
        }

        box.addView(TextView(this).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        })

        if (detail != null) {
            box.addView(TextView(this).apply {
                text = detail
                setTextColor(0xffcccccc.toInt())
                textSize = 12f
                setPadding(0, dp(5), 0, 0)
            })
        }

        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(12)

        content.addView(box, lp)
    }

    private fun button(text: String, action: () -> Unit) {

        val b = TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(14), dp(15), dp(14), dp(15))

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.BLACK)
                cornerRadius = dp(13).toFloat()
            }

            setOnClickListener {
                click()

                animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(80)
                    .withEndAction {
                        animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()

                        action()
                    }
                    .start()
            }
        }

        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(10)

        content.addView(b, lp)
    }

    private fun field(hint: String, value: String = ""): EditText {

        val e = EditText(this).apply {
            setSingleLine(false)
            setText(value)
            this.hint = hint

            setTextColor(Color.BLACK)
            setHintTextColor(0xff777777.toInt())
            textSize = 14f

            setPadding(dp(12), dp(10), dp(12), dp(10))

            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.BLACK)
            }
        }

        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = dp(9)

        content.addView(e, lp)
        return e
    }

    private fun showHome() {

        base(
            "Your library, reimagined",
            "A native-library customization workspace."
        )

        card("LIBRARY WORKSPACE", libraryName)

        button("＋  Import .so / LIB file") {
            openPicker(pickLib, "*/*")
        }

        button("⌕  Scan library strings") {
            scanLibrary()
        }

        button("✎  Menu title & credits") {
            showTextEditor()
        }

        button("◈  Menu theme editor") {
            showThemes()
        }

        button("Aa  Font creator") {
            showFonts()
        }

        button("⚙  App settings") {
            showSettings()
        }

        card(
            "AUTOMATIC MODE",
            "Scans printable strings and offers safe in-place replacements. Binary layouts and compiled code are not rewritten."
        )

        button("Export modified library") {
            exportLibrary()
        }
    }

    private val maxLibraryBytes = 64L * 1024L * 1024L

    private fun readLibrary(uri: Uri): ByteArray {

        val input = contentResolver.openInputStream(uri)
            ?: throw java.io.IOException(
                "The selected file could not be opened."
            )

        return input.use { stream ->

            val output = ByteArrayOutputStream()
            val buffer = ByteArray(64 * 1024)
            var total = 0L

            while (true) {
                val count = stream.read(buffer)
                if (count == -1) break

                total += count

                if (total > maxLibraryBytes) {
                    throw java.io.IOException(
                        "Library is larger than the 64 MB import limit."
                    )
                }

                output.write(buffer, 0, count)
            }

            output.toByteArray()
        }
    }

    private fun openPicker(code: Int, type: String) {

        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                this.type = type
            },
            code
        )
    }

    @Deprecated("Deprecated Android API retained for compatibility")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK || data?.data == null) return

        val uri = data.data!!

        when (requestCode) {

            pickLib -> {
                status.text = "IMPORTING LIBRARY..."

                thread {
                    try {
                        val bytes = readLibrary(uri)

                        val name = uri.lastPathSegment
                            ?.substringAfterLast('/')
                            ?.takeIf { it.isNotBlank() }
                            ?: "Imported library"

                        runOnUiThread {
                            libraryBytes = bytes
                            libraryName = name

                            showHome()
                            toast("Library imported successfully")
                        }

                    } catch (e: Exception) {
                        runOnUiThread {
                            status.text =
                                "MIDNIGHT LIB CHANGER  •  PENG.LOL"

                            toast(
                                "Could not read file: ${e.message ?: "Unknown error"}"
                            )
                        }
                    }
                }
            }

            saveLib -> try {
                val bytes = pendingExport ?: return

                val output = contentResolver.openOutputStream(uri, "w")
                    ?: throw java.io.IOException(
                        "Could not create output file."
                    )

                output.use {
                    it.write(bytes)
                }

                toast("Export complete")

            } catch (e: Exception) {
                toast("Export failed: ${e.message}")
            }

            pickFont -> try {
                val input = contentResolver.openInputStream(uri)
                    ?: throw java.io.IOException("Could not open font.")

                val bytes = input.use {
                    it.readBytes()
                }

                val f = java.io.File(cacheDir, "preview-font.ttf")
                f.writeBytes(bytes)

                chosenFont = Typeface.createFromFile(f)

                toast("Font loaded for preview")
                showFonts()

            } catch (e: Exception) {
                toast("Could not load font: ${e.message}")
            }
        }
    }

    private fun scanLibrary() {

        val bytes = libraryBytes ?: run {
            toast("Import a library first")
            return
        }

        found.clear()

        var i = 0

        while (i < bytes.size) {

            if ((bytes[i].toInt() and 255) in 32..126) {

                val start = i

                while (
                    i < bytes.size &&
                    (bytes[i].toInt() and 255) in 32..126 &&
                    i - start < 256
                ) {
                    i++
                }

                if (i - start >= 4) {
                    val s = String(
                        bytes,
                        start,
                        i - start,
                        Charsets.US_ASCII
                    )

                    if (s.any { it.isLetter() } && !s.startsWith("http")) {
                        found.add(FoundString(start, s))
                    }
                }

            } else {
                i++
            }
        }

        showScanResults()
    }

    private fun showScanResults() {

        base(
            "Detected strings",
            "${found.size} printable text candidates found. Select a value to replace it safely."
        )

        if (found.isEmpty()) {
            card(
                "NO STRINGS FOUND",
                "This library may store text in resources, compressed data, or encrypted structures."
            )
        }

        found.take(120).forEach { item ->

            card(
                "OFFSET 0x${item.offset.toString(16)}",
                item.value
            )

            button("Edit this string") {

                val input = EditText(this).apply {
                    setText(item.value)
                    setTextColor(Color.BLACK)
                }

                android.app.AlertDialog.Builder(this)
                    .setTitle("Replace string")
                    .setMessage(
                        "Replacement must fit within ${item.value.toByteArray().size} ASCII bytes. Shorter text is padded with null bytes."
                    )
                    .setView(input)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Apply") { _, _ ->

                        val replacement = input.text.toString()

                        val old = item.value.toByteArray(Charsets.US_ASCII)
                        val fresh = replacement.toByteArray(Charsets.US_ASCII)

                        if (
                            fresh.size > old.size ||
                            fresh.any {
                                (it.toInt() and 255) !in 32..126
                            }
                        ) {
                            toast(
                                "Use printable ASCII no longer than the original."
                            )
                        } else {
                            val b = libraryBytes!!

                            for (k in old.indices) {
                                b[item.offset + k] =
                                    if (k < fresh.size) fresh[k] else 0
                            }

                            toast("String updated in memory")
                        }
                    }
                    .show()
            }
        }

        button("← Back") {
            showHome()
        }
    }

    private fun showTextEditor() {

        base(
            "Menu text editor",
            "Set the replacement values you want to use when matching strings are found."
        )

        val title = field("Main menu title", "Midnight")
        val credits = field("Credits", "Modified by PENG.LOL")

        val note = TextView(this).apply {
            text = "These values are saved as an edit plan. Run Scan library strings and replace matching entries; the app does not guess binary offsets."
            setTextColor(0xff555555.toInt())
            textSize = 12f
        }

        content.addView(note)

        button("Save text edit plan") {
            getPreferences(0).edit()
                .putString("menu_title", title.text.toString())
                .putString("credits", credits.text.toString())
                .apply()

            toast("Text edit plan saved")
        }

        button("← Back") {
            showHome()
        }
    }

    private fun showThemes() {

        base(
            "Menu theme editor",
            "Build a black-and-white theme preset for your menu."
        )

        val names = listOf(
            "Classic black",
            "Pure white",
            "Monochrome",
            "Midnight monochrome"
        )

        names.forEach { name ->
            button("✦  $name") {
                accent = Color.BLACK
                showThemes()
            }
        }

        val preview = TextView(this).apply {
            text = "THEME PREVIEW  •  Midnight menu"
            textSize = 17f
            setTextColor(Color.BLACK)
            setPadding(0, dp(18), 0, dp(18))
            setTypeface(null, Typeface.BOLD)
        }

        content.addView(preview)

        button("Export theme JSON") {

            val json = JSONObject()
                .put("name", "Midnight monochrome")
                .put("accent", "#000000")
                .put("background", "#FFFFFF")
                .put("surface", "#000000")
                .put("text", "#FFFFFF")

            pendingExport = json.toString(2).toByteArray()

            startActivityForResult(
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "application/json"
                    putExtra(
                        Intent.EXTRA_TITLE,
                        "midnight-theme.json"
                    )
                },
                saveLib
            )
        }

        button("← Back") {
            showHome()
        }
    }

    private fun showFonts() {

        base(
            "Font creator",
            "Preview custom typography and import TTF / OTF fonts."
        )

        val sample = field("Preview text", "Midnight menu")

        val preview = TextView(this).apply {
            text = "Midnight menu preview"
            textSize = 25f
            setTextColor(Color.BLACK)
            setPadding(0, dp(16), 0, dp(16))
            typeface = chosenFont ?: Typeface.DEFAULT_BOLD
        }

        content.addView(preview)

        sample.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    preview.text = s
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        button("Import TTF / OTF font") {
            openPicker(pickFont, "font/*")
        }

        button("Use bold style") {
            preview.typeface = Typeface.create(
                chosenFont ?: Typeface.DEFAULT,
                Typeface.BOLD
            )
        }

        button("Export font preview settings") {

            val json = JSONObject()
                .put("fontName", "Custom font")
                .put("sample", sample.text.toString())
                .put("style", "bold")

            pendingExport = json.toString(2).toByteArray()

            startActivityForResult(
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "application/json"
                    putExtra(
                        Intent.EXTRA_TITLE,
                        "font-preset.json"
                    )
                },
                saveLib
            )
        }

        button("← Back") {
            showHome()
        }
    }

    private fun showSettings() {

        base(
            "Settings",
            "Personalize the app experience."
        )

        fun toggle(
            label: String,
            initial: Boolean,
            onChange: (Boolean) -> Unit
        ) {
            val sw = Switch(this).apply {
                text = label
                setTextColor(Color.BLACK)
                isChecked = initial
                textSize = 15f
                setPadding(0, dp(12), 0, dp(12))

                setOnCheckedChangeListener { _, value ->
                    onChange(value)
                    click()
                }
            }

            content.addView(sw)
        }

        toggle("Animated black stars", stars) {
            stars = it
            root.invalidate()
        }

        toggle("Background motion", animations) {
            animations = it
            root.invalidate()
        }

        toggle("Button click sounds", sounds) {
            sounds = it
        }

        card(
            "APP INFO",
            "Midnight Lib Changer\nVersion 1.0.0\nMade by PENG.LOL"
        )

        button("Reset app appearance") {
            accent = Color.BLACK
            stars = true
            animations = true
            sounds = true
            showSettings()
        }

        button("← Back") {
            showHome()
        }
    }

    private fun exportLibrary() {

        val b = libraryBytes ?: run {
            toast("Import a library first")
            return
        }

        pendingExport = b

        startActivityForResult(
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "application/octet-stream"
                putExtra(
                    Intent.EXTRA_TITLE,
                    "modified-library.so"
                )
            },
            saveLib
        )
    }

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}
