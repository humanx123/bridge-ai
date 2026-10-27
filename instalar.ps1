# Instala bridge-ai en Windows (comandos «bridge-ai» e «ia»): comprueba Node.js y añade esta carpeta al PATH del usuario.
$ErrorActionPreference = 'Stop'
$aqui = $PSScriptRoot

if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
  Write-Host 'Falta Node.js (20.18 o más nuevo). Instálalo con:' -ForegroundColor Yellow
  Write-Host '  winget install OpenJS.NodeJS.LTS'
  Write-Host '  (o descárgalo de https://nodejs.org) y vuelve a ejecutar instalar.cmd'
  exit 1
}
node -e "const [a,b]=process.versions.node.split('.').map(Number);process.exit(a>20||(a===20&&b>=18)?0:1)"
if ($LASTEXITCODE -ne 0) { Write-Host "Tu Node.js es $(node -v): bridge-ai necesita 20.18 o más nuevo (https://nodejs.org)." -ForegroundColor Yellow; exit 1 }
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { Write-Host 'Aviso: falta git (winget install Git.Git); hace falta para instalar FreeLLMAPI desde bridge-ai.' -ForegroundColor Yellow }

$ruta = [Environment]::GetEnvironmentVariable('Path', 'User')
$partes = @($ruta -split ';' | Where-Object { $_ -ne '' })
if ($partes -notcontains $aqui) {
  [Environment]::SetEnvironmentVariable('Path', (($partes + $aqui) -join ';'), 'User')
  Write-Host "Añadido al PATH del usuario: $aqui"
}
$env:Path = "$env:Path;$aqui"
Write-Host 'Listo. Abre una ventana nueva de cmd o PowerShell y escribe:  bridge-ai' -ForegroundColor Green
