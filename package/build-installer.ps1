# Arma el paquete instalable del Agente de Impresion MAS:
# bootJar + runtime Java recortado (jlink) + WinSW + scripts de instalacion, todo en un .zip
# listo para distribuir. Se ejecuta desde cualquier lado; resuelve rutas relativas a este script.
#
# Uso:  .\package\build-installer.ps1

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$packageDir = $PSScriptRoot
$buildDir = Join-Path $repoRoot "build"
$distDir = Join-Path $repoRoot "dist"
$stagingDir = Join-Path $distDir "mas-impression-solutions"

# Modulos JDK verificados con jdeps contra el jar real + los que jdeps no detecta por reflexion
# pero se necesitan igual: jdk.charsets (Cp850, usado por TicketFormatter), java.logging
# (puente JUL/SLF4J), jdk.crypto.ec (TLS saliente hacia el backend).
$jlinkModules = "java.base,java.compiler,java.desktop,java.instrument,java.management," +
    "java.naming,java.net.http,java.prefs,java.scripting,java.security.jgss,java.sql," +
    "java.logging,java.xml,jdk.jfr,jdk.unsupported,jdk.charsets,jdk.crypto.ec"

Write-Host "== 1/4: Compilando el jar ==" -ForegroundColor Cyan
Push-Location $repoRoot
try {
    & ".\gradlew.bat" bootJar --console=plain
    if ($LASTEXITCODE -ne 0) { throw "gradlew bootJar fallo (codigo $LASTEXITCODE)" }
} finally {
    Pop-Location
}

$jar = Get-ChildItem (Join-Path $buildDir "libs") -Filter "*.jar" |
    Where-Object { $_.Name -notlike "*-plain.jar" } | Select-Object -First 1
if (-not $jar) { throw "No se encontro el jar generado en build\libs" }

Write-Host "== 2/4: Generando el runtime Java recortado (jlink) ==" -ForegroundColor Cyan
$runtimeDir = Join-Path $buildDir "runtime"
if (Test-Path $runtimeDir) { Remove-Item -Recurse -Force $runtimeDir }
& jlink --module-path "$env:JAVA_HOME\jmods" --add-modules $jlinkModules `
    --strip-debug --no-man-pages --no-header-files --compress=zip-6 `
    --output $runtimeDir
if ($LASTEXITCODE -ne 0) { throw "jlink fallo (codigo $LASTEXITCODE)" }

Write-Host "== 3/4: Ensamblando el paquete ==" -ForegroundColor Cyan
if (Test-Path $stagingDir) { Remove-Item -Recurse -Force $stagingDir }
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

Copy-Item $jar.FullName (Join-Path $stagingDir "mas-impression-solutions.jar")
Copy-Item -Recurse $runtimeDir (Join-Path $stagingDir "runtime")
Copy-Item (Join-Path $packageDir "winsw\MasImpressionAgent.exe") $stagingDir
Copy-Item (Join-Path $packageDir "winsw\MasImpressionAgent.xml") $stagingDir
Copy-Item (Join-Path $packageDir "install.ps1") $stagingDir
Copy-Item (Join-Path $packageDir "uninstall.ps1") $stagingDir
Copy-Item (Join-Path $packageDir "INSTRUCTIVO.txt") $stagingDir

Write-Host "== 4/4: Comprimiendo ==" -ForegroundColor Cyan
$version = (Get-Date -Format "yyyyMMdd")
$zipPath = Join-Path $distDir "mas-impression-solutions-$version.zip"
$latestZipPath = Join-Path $distDir "mas-impression-solutions-latest.zip"
if (Test-Path $zipPath) { Remove-Item -Force $zipPath }
Compress-Archive -Path (Join-Path $stagingDir "*") -DestinationPath $zipPath
Copy-Item $zipPath $latestZipPath -Force

Write-Host ""
Write-Host "Listo:" -ForegroundColor Green
Write-Host "  $zipPath  (copia con fecha, para tu propio historial)"
Write-Host "  $latestZipPath  <- sube ESTE al bucket, siempre con este mismo nombre"
$sizeMb = [math]::Round((Get-Item $zipPath).Length / 1MB, 1)
Write-Host "Tamano: $sizeMb MB"
