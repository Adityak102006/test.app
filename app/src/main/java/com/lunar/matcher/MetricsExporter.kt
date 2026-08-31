package com.lunar.matcher

import android.content.Context
import org.json.JSONObject
import java.io.File

class MetricsExporter(private val context: Context) {

    fun export(result: ImageMatcher.MatchResult, outFileName: String = "metrics.json") {
        val obj = JSONObject()
        obj.put("rmse", result.rmse)
        obj.put("inlier_match_count", result.inlierCount)
        obj.put("total_match_points", result.totalPoints)
        obj.put("inlier_ratio", result.inlierRatio)
        obj.put("sub_pixel_accuracy", result.rmse < 1.0)
        val file = File(context.getExternalFilesDir(null), outFileName)
        file.writeText(obj.toString(2))
    }
}
