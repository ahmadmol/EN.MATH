param([string]$Serial = 'emulator-5554')
$ErrorActionPreference = 'Stop'
if (-not $Serial.StartsWith('emulator-')) { throw 'QA resets demo state: select a dedicated emulator.' }
$projectDir = Split-Path $PSScriptRoot -Parent
$adbPath = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
$sizes = @(@(360,800), @(375,812), @(390,844), @(412,915), @(960,1280), @(1280,800))
Push-Location $projectDir
try {
    & $adbPath -s $Serial pull /sdcard/Android/data/com.ahmadmol.enmath/files/product-screenshots/. docs/screenshots
    foreach ($size in $sizes) {
        $width = $size[0]
        $height = $size[1]
        & $adbPath -s $Serial shell wm density 160
        & $adbPath -s $Serial shell wm size "${width}x${height}"
        $log = & $adbPath -s $Serial shell am instrument -w -r -e class 'com.ahmadmol.enmath.AppFlowTest#guestProtectionAndAdaptiveTabs' com.ahmadmol.enmath.test/androidx.test.runner.AndroidJUnitRunner
        $log | Set-Content "docs/qa/ui-${width}.txt" -Encoding utf8
        if (($log -join "`n") -notmatch 'OK \(1 test\)') { throw "UI test failed at width $width" }
        foreach ($screen in @('home','study','solver')) {
            & $adbPath -s $Serial pull "/sdcard/Android/data/com.ahmadmol.enmath/files/product-screenshots/adaptive-${screen}.png" "docs/screenshots/${screen}-${width}.png"
        }
        Write-Output "PASS ${width}x${height} dp"
    }
} finally {
    & $adbPath -s $Serial shell wm size reset
    & $adbPath -s $Serial shell wm density reset
    Pop-Location
}
