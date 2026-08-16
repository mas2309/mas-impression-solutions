#Requires -RunAsAdministrator
# Desinstala el servicio del Agente de Impresion MAS.
# Ejecutar como Administrador.

$ErrorActionPreference = "Stop"

$installDir = "C:\Program Files\MasImpressionSolutions\PrintAgent"
$exe = "$installDir\MasImpressionAgent.exe"

if (-not (Test-Path $exe)) {
    Write-Host "No se encontro una instalacion en $installDir. Nada que hacer."
    exit 0
}

Write-Host "== Desinstalando el Agente de Impresion MAS ==" -ForegroundColor Cyan

Push-Location $installDir
try {
    & ".\MasImpressionAgent.exe" stop
    & ".\MasImpressionAgent.exe" uninstall
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "Servicio desinstalado." -ForegroundColor Green
Write-Host "Los archivos en '$installDir' y los datos en 'C:\ProgramData\MasImpressionSolutions'"
Write-Host "no se borraron automaticamente (config e historial de impresion). Borralos a mano si quieres una limpieza completa."
