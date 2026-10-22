# Instala el Agente de Impresion MAS como servicio de Windows.
# Pensado para ejecutarse con doble clic sobre INSTALAR.bat; si se ejecuta este
# .ps1 directamente sin permisos de administrador, se relanza solo pidiendole
# el permiso a Windows (UAC) en vez de fallar en seco.

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
        Write-Host "No se otorgo el permiso de administrador, asi que la instalacion no pudo continuar." -ForegroundColor Red
        Write-Host "Vuelve a intentarlo y elige 'Si' cuando Windows pida el permiso."
        Read-Host "Presiona ENTER para cerrar esta ventana"
    }
    exit
}

try {
    $installDir = "C:\Program Files\MasImpressionSolutions\PrintAgent"
    $dataDir = "C:\ProgramData\MasImpressionSolutions\data"
    $sourceDir = $PSScriptRoot
    $serviceName = "MasImpressionAgent"

    $alreadyInstalled = [bool](Get-Service -Name $serviceName -ErrorAction SilentlyContinue)

    Write-Host "== $(if ($alreadyInstalled) { 'Actualizando' } else { 'Instalando' }) el Agente de Impresion MAS ==" -ForegroundColor Cyan
    Write-Host "Destino: $installDir"

    if ($alreadyInstalled) {
        Write-Host "Ya hay una instalacion previa: deteniendo el servicio para actualizar los archivos..."
        Push-Location $installDir
        try {
            & ".\MasImpressionAgent.exe" stop
        } finally {
            Pop-Location
        }
    }

    New-Item -ItemType Directory -Force -Path $installDir | Out-Null
    New-Item -ItemType Directory -Force -Path $dataDir | Out-Null

    Write-Host "Copiando archivos..."
    Copy-Item -Path "$sourceDir\*" -Destination $installDir -Recurse -Force `
        -Exclude @("install.ps1", "uninstall.ps1", "INSTALAR.bat", "DESINSTALAR.bat", "INSTRUCTIVO.txt")

    Push-Location $installDir
    try {
        if (-not $alreadyInstalled) {
            Write-Host "Registrando el servicio de Windows..."
            & ".\MasImpressionAgent.exe" install
            if ($LASTEXITCODE -ne 0) {
                throw "No se pudo registrar el servicio (codigo $LASTEXITCODE)."
            }
        }

        Write-Host "Iniciando el servicio..."
        & ".\MasImpressionAgent.exe" start
        if ($LASTEXITCODE -ne 0) {
            throw "El servicio no arranco (codigo $LASTEXITCODE). Revisa los logs en $installDir\logs."
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
} catch {
    Write-Host ""
    Write-Host "== Algo fallo durante la instalacion ==" -ForegroundColor Red
    Write-Host $_.Exception.Message
    Write-Host "Contacta a soporte con este mensaje y el archivo INSTRUCTIVO.txt."
} finally {
    Write-Host ""
    Read-Host "Presiona ENTER para cerrar esta ventana"
}
