param([string]$JdkHome = $env:JAVA_HOME)

if (-not $JdkHome) { throw "Define JAVA_HOME apuntando a un JDK 21." }
$jpackage = Join-Path $JdkHome "bin\jpackage.exe"
if (-not (Test-Path $jpackage)) { throw "No se encontró jpackage.exe en JAVA_HOME." }

$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "build" | Out-Null

# Icono ICO para el instalador y los accesos directos de Windows.
Add-Type -AssemblyName System.Drawing
$bitmap = New-Object System.Drawing.Bitmap 256, 256
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.Clear([System.Drawing.Color]::FromArgb(37, 99, 235))
$brush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
$graphics.FillPolygon($brush, [System.Drawing.Point[]]@((New-Object System.Drawing.Point 99,92),(New-Object System.Drawing.Point 99,170),(New-Object System.Drawing.Point 170,131)))
$icon = [System.Drawing.Icon]::FromHandle($bitmap.GetHicon())
$stream = [System.IO.File]::Open("build\jd-media-converter.ico", [System.IO.FileMode]::Create)
$icon.Save($stream); $stream.Close(); $graphics.Dispose(); $brush.Dispose(); $bitmap.Dispose()

mvn clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=target\app"
Copy-Item "target\jd-media-converter-1.0.0.jar" "target\app\" -Force
& $jpackage --type exe --name "JD Media Converter" --app-version "1.0.0" --vendor "Jplamec" --input "target\app" --main-jar "jd-media-converter-1.0.0.jar" --main-class "com.jdmedia.App" --dest "dist" --icon "build\jd-media-converter.ico" --win-dir-chooser --win-menu --win-shortcut
