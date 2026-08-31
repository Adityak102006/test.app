package com.lunar.matcher

import org.opencv.core.*
import org.opencv.features2d.ORB
import org.opencv.features2d.BFMatcher
import org.opencv.calib3d.Calib3d
import org.opencv.imgproc.Imgproc

class ImageMatcher {

    fun match(source: Mat, reference: Mat): MatchResult {
        val srcGray = normalizeSun(source)
        val refGray = normalizeSun(reference)

        val pyramidSrc = buildPyramid(srcGray)
        val pyramidRef = buildPyramid(refGray)

        val srcPts = mutableListOf<Point>()
        val refPts = mutableListOf<Point>()

        for (i in pyramidSrc.indices) {
            val pair = detectAndMatch(pyramidSrc[i], pyramidRef[i])
            srcPts.addAll(pair.first)
            refPts.addAll(pair.second)
        }

        val (inSrc, inRef) = filterInliers(srcPts, refPts)
        val rmse = computeRMSE(inSrc, inRef)
        val ratio = if (srcPts.isNotEmpty()) inSrc.size.toFloat() / srcPts.size else 0f

        val uniformSrc = uniformSubsample(inSrc, inRef)

        return MatchResult(
            matchPoints = uniformSrc.first,
            rmse = rmse,
            inlierCount = inSrc.size,
            inlierRatio = ratio,
            totalPoints = srcPts.size
        )
    }

    private fun normalizeSun(img: Mat): Mat {
        val gray = Mat()
        if (img.channels() > 1) Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY)
        else gray.assign(img)
        val norm = Mat()
        Core.normalize(gray, norm, 0.0, 255.0, Core.NORM_MINMAX)
        return norm
    }

    private fun buildPyramid(img: Mat): List<Mat> {
        val pyr = mutableListOf(img)
        for (i in 1..2) {
            val down = Mat()
            Imgproc.pyrDown(pyr.last(), down)
            pyr.add(down)
        }
        return pyr
    }

    private fun detectAndMatch(s: Mat, r: Mat): Pair<List<Point>, List<Point>> {
        val orb = ORB.create(2000)
        val kpS = MatOfKeyPoint()
        val kpR = MatOfKeyPoint()
        val descS = Mat()
        val descR = Mat()
        orb.detectAndCompute(s, Mat(), kpS, descS)
        orb.detectAndCompute(r, Mat(), kpR, descR)
        val matcher = BFMatcher.create(Core.NORM_HAMMING, true)
        val raw = mutableListOf<MatOfDMatch>()
        matcher.knnMatch(descS, descR, raw, 2)
        val matches = mutableListOf<DMatch>()
        for (m in raw) {
            val best = m.toList()
            if (best.size >= 2 && best[0].distance < 0.75 * best[1].distance) {
                matches.add(best[0])
            }
        }
        val sPts = mutableListOf<Point>()
        val rPts = mutableListOf<Point>()
        val kpListS = kpS.toList()
        val kpListR = kpR.toList()
        for (m in matches) {
            sPts.add(Point(kpListS[m.queryIdx].pt.x, kpListS[m.queryIdx].pt.y))
            rPts.add(Point(kpListR[m.trainIdx].pt.x, kpListR[m.trainIdx].pt.y))
        }
        return Pair(sPts, rPts)
    }

    private fun filterInliers(src: List<Point>, ref: List<Point>): Pair<List<Point>, List<Point>> {
        if (src.size < 4) return Pair(src, ref)
        val srcMat = MatOfPoint2f(*src.toTypedArray())
        val refMat = MatOfPoint2f(*ref.toTypedArray())
        val mask = Mat()
        Calib3d.findHomography(srcMat, refMat, Calib3d.RANSAC, 5.0, mask)
        val maskData = mutableListOf<Boolean>()
        for (r in 0 until mask.rows()) {
            maskData.add(mask.get(r, 0)[0] > 0.5)
        }
        val inS = mutableListOf<Point>()
        val inR = mutableListOf<Point>()
        for (i in src.indices) {
            if (maskData.getOrElse(i) { false }) {
                inS.add(src[i])
                inR.add(ref[i])
            }
        }
        return Pair(inS, inR)
    }

    private fun computeRMSE(s: List<Point>, r: List<Point>): Double {
        if (s.isEmpty()) return 0.0
        var sum = 0.0
        for (i in s.indices) {
            val dx = s[i].x - r[i].x
            val dy = s[i].y - r[i].y
            sum += dx * dx + dy * dy
        }
        return kotlin.math.sqrt(sum / s.size)
    }

    private fun uniformSubsample(s: List<Point>, r: List<Point>): Pair<List<Point>, List<Point>> {
        val grid = 4
        val buckets = mutableMapOf<String, MutableList<Int>>()
        for (i in s.indices) {
            val gx = (s[i].x / 100).toInt() % grid
            val gy = (s[i].y / 100).toInt() % grid
            val key = "$gx-$gy"
            buckets.getOrPut(key) { mutableListOf() }.add(i)
        }
        val outS = mutableListOf<Point>()
        val outR = mutableListOf<Point>()
        for (bucket in buckets.values) {
            val idx = if (bucket.size > 1) bucket[bucket.size / 2] else bucket.first()
            outS.add(s[idx])
            outR.add(r[idx])
        }
        return Pair(outS, outR)
    }

    data class MatchResult(
        val matchPoints: List<Point>,
        val rmse: Double,
        val inlierCount: Int,
        val inlierRatio: Float,
        val totalPoints: Int
    )
}
