package com.lunar.matcher

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private var sourcePath: String? = null
    private var refPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.tvStatus)
        findViewById<Button>(R.id.btnSource).setOnClickListener { pickSource() }
        findViewById<Button>(R.id.btnRef).setOnClickListener { pickRef() }
        findViewById<Button>(R.id.btnProcess).setOnClickListener { process() }
    }

    private fun pickSource() {
        sourcePath = "assets/ch2_sample.png"
        status.text = "Source: ${sourcePath}"
    }

    private fun pickRef() {
        refPath = "assets/lro_nac_sample.png"
        status.text = "Reference: ${refPath}"
    }

    private fun process() {
        if (sourcePath == null || refPath == null) {
            Toast.makeText(this, "Select source and reference", Toast.LENGTH_SHORT).show()
            return
        }
        status.text = "Matching..."
        CoroutineScope(Dispatchers.Default).launch {
            val result = runMatching()
            withContext(Dispatchers.Main) {
                status.text = "RMSE=${String.format("%.2f", result.rmse)} Inliers=${result.inlierCount}/${result.totalPoints} Ratio=${String.format("%.2f", result.inlierRatio)}"
                MetricsExporter(this@MainActivity).export(result)
                Toast.makeText(this@MainActivity, "Metrics saved: metrics.json", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun runMatching(): ImageMatcher.MatchResult {
        val matcher = ImageMatcher()
        val src = loadMat(sourcePath!!)
        val ref = loadMat(refPath!!)
        return matcher.match(src, ref)
    }

    private fun loadMat(path: String): Mat {
        return if (path.startsWith("assets/")) {
            val bitmap = android.graphics.BitmapFactory.decodeStream(assets.open(path.replace("assets/", "")))
            val mat = Mat()
            Utils.bitmapToMat(bitmap, mat)
            mat
        } else {
            val mat = Mat()
            Imgcodecs.imread(path, Imgcodecs.IMREAD_COLOR).copyTo(mat)
            mat
        }
    }
}
