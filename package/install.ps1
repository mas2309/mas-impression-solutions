#Requires -RunAsAdministrator
# Instala el Agente de Impresion MAS como servicio de Windows.
# Ejecutar como Administrador: clic derecho sobre este archivo -> "Ejecutar con PowerShell"
# (o "Ejecutar como administrador" si aparece esa opcion).

$ErrorActionPreference = "Stop"

$installDir = "C:\Program Files\MasImpressionSolutions\PrintAgent"
$dataDir = "C:\ProgramData\MasImpressionSolutions\data"
$sourceDir = $PSScriptRoot

Write-Host "== Instalando el Agente de Impresion MAS ==" -ForegroundColor Cyan
Write-Host "Destino: $installDir"

New-Item -ItemType Directory -Force -Path $installDir | Out-Null
New-Item -ItemType Directory -Force -Path $dataDir | Out-Null

Write-Host "Copiando archivos..."
Copy-Item -Path "$sourceDir\*" -Destination $installDir -Recurse -Force `
    -Exclude @("install.ps1", "uninstall.ps1", "INSTRUCTIVO.txt")

Push-Location $installDir
try {
    Write-Host "Registrando el servicio de Windows..."
    & ".\MasImpressionAgent.exe" install
    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo registrar el servicio (codigo $LASTEXITCODE). Revisa que estes ejecutando como Administrador."
    }

    Write-Host "Iniciando el servicio..."
    & ".\MasImpressionAgent.exe" start
    if ($LASTEXITCODE -ne 0) {
        throw "El servicio se registro pero no arranco (codigo $LASTEXITCODE). Revisa los logs en $installDir\logs."
    }
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "== Listo ==" -ForegroundColor Green
Write-Host "El agente ya corre como servicio de Windows y arrancara solo con el equipo."
Write-Host "Abre http://localhost:9310/admin en el navegador de esta PC para:"
Write-Host "  1. Copiar el token de emparejamiento."
Write-Host "  2. Configurar la impresora (red, USB o Bluetooth)."
Write-Host ""
Write-Host "Luego, en el punto de venta (Configuracion > Impresion local), pega ese token y guarda."
