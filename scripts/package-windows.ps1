param([string]$JdkHome = $env:JAVA_HOME)

function Find-JdkHome {
    param([string]$RequestedHome)
    $candidates = @($RequestedHome, $env:JAVA_HOME)
    foreach ($root in @("$env:ProgramFiles\Java", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Microsoft")) {
        if (Test-Path $root) {
            $candidates += Get-ChildItem -Path $root -Directory -ErrorAction SilentlyContinue | Select-Object -ExpandProperty FullName
        }
    }
    foreach ($candidate in ($candidates | Where-Object { $_ } | Select-Object -Unique)) {
        if (Test-Path (Join-Path $candidate "bin\jpackage.exe")) { return $candidate }
    }
    return $null
}

$JdkHome = Find-JdkHome $JdkHome
if (-not $JdkHome) {
    throw "No se encontró un JDK con jpackage.exe. Instala JDK 21 con: winget install EclipseAdoptium.Temurin.21.JDK. Después abre una terminal nueva y vuelve a ejecutar este script."
}

if (-not $JdkHome) { throw "Define JAVA_HOME apuntando a un JDK 21." }
$jpackage = Join-Path $JdkHome "bin\jpackage.exe"
if (-not (Test-Path $jpackage)) { throw "No se encontró jpackage.exe en JAVA_HOME." }

$ErrorActionPreference = "Stop"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven no está instalado o no está en PATH. Instálalo con: winget install Apache.Maven. Después abre una terminal nueva y vuelve a ejecutar este script."
}
[xml]$pom = Get-Content -Raw "pom.xml"
$AppVersion = $pom.project.version
if ([string]::IsNullOrWhiteSpace($AppVersion)) { throw "No se pudo leer la versión de pom.xml." }
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
$mainJar = "jd-media-converter-$AppVersion.jar"
Copy-Item "target\$mainJar" "target\app\" -Force
& $jpackage --type exe --name "JD Media Converter" --app-version $AppVersion --vendor "Jplamec" --input "target\app" --main-jar $mainJar --main-class "com.jdmedia.App" --dest "dist" --icon "build\jd-media-converter.ico" --win-dir-chooser --win-menu --win-shortcut
