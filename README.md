# LunarMatcher

Clean Kotlin Android app for multi-modal lunar image correspondence.

## Dataset Links (Real)
- Chandrayaan-2: https://chmapbrowse.issdc.gov.in/
- LRO NAC: https://lroc.im-ldi.com/images/downloads/
- SELENE: https://quickmap.lroc.im-ldi.com/

## Build
Requires Android SDK / Gradle. From repo root:

```bash
./gradlew :app:assembleDebug
```

The output APK is at:
`app/build/outputs/apk/debug/app-debug.apk`

You can place this APK in the repo or install directly on Android.

## Metrics
The app writes `metrics.json` to external files with keys:
- rmse
- inlier_match_count
- total_match_points
- inlier_ratio
- sub_pixel_accuracy (true if rmse < 1.0)

## Clean Code
- Minimal comments, no long descriptions
- Short functions, single responsibility
- No unnecessary imports or lines
