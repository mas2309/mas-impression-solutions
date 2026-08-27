# Desinstala el servicio del Agente de Impresion MAS.
# Pensado para ejecutarse con doble clic sobre DESINSTALAR.bat; si se ejecuta este
# .ps1 directamente sin permisos de administrador, se relanza solo pidiendole el
# permiso a Windows (UAC) en vez de fallar en seco.

$ErrorActionPreference = "Stop"

function Test-IsAdministrator {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = New-Object Security.Principal.WindowsPrincipal($identity)
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

if (-not (Test-IsAdministrator)) {
    Write-Host "Se necesitan permisos de administrador. Windows va a pedir tu permiso..." -ForegroundColor Yellow
    try {
        Start-Process powershell.exe -Verb RunAs -ArgumentList @(
            "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "`"$PSCommandPath`""
        ) | Out-Null
    } catch {
        Write-Host ""
        Write-Host "No se otorgo el permiso de administrador, asi que la desinstalacion no pudo continuar." -ForegroundColor Red
        Write-Host "Vuelve a intentarlo y elige 'Si' cuando Windows pida el permiso."
        Read-Host "Presiona ENTER para cerrar esta ventana"
    }
    exit
}

try {
    $installDir = "C:\Program Files\MasImpressionSolutions\PrintAgent"
    $exe = "$installDir\MasImpressionAgent.exe"

    if (-not (Test-Path $exe)) {
        Write-Host "No se encontro una instalacion en $installDir. Nada que hacer."
    } else {
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
    }
} catch {
    Write-Host ""
    Write-Host "== Algo fallo durante la desinstalacion ==" -ForegroundColor Red
    Write-Host $_.Exception.Message
    Write-Host "Contacta a soporte con este mensaje."
} finally {
    Write-Host ""
    Read-Host "Presiona ENTER para cerrar esta ventana"
}
