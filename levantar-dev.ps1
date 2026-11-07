<#
  Levanta el sistema completo en desarrollo: base de datos, backend y frontend.

  Uso, desde la carpeta Barberia:
    powershell -ExecutionPolicy Bypass -File .\levantar-dev.ps1

  Requisitos: java (JDK 17+), node, npm y Docker Desktop abierto.
  Maven no hace falta: el backend trae su propio wrapper (mvnw.cmd).
  PostgreSQL corre en Docker: se usa el contenedor que publique el puerto 5432
  o, si no hay ninguno, se crea el contenedor barberia-db.
#>
$ErrorActionPreference = 'Continue'
$raiz = $PSScriptRoot
function Paso([string]$texto) { Write-Host "`n==> $texto" -ForegroundColor Yellow }
function Falla([string]$texto) {
    Write-Host "`nERROR: $texto" -ForegroundColor Red
    Read-Host 'Presiona Enter para cerrar'
    exit 1
}

# --- 1. Herramientas --------------------------------------------------------
Paso 'Revisando herramientas'
foreach ($comando in 'java', 'node', 'npm', 'docker') {
    if (-not (Get-Command $comando -ErrorAction SilentlyContinue)) {
        Falla "No se encontró '$comando'. Instálalo o agrégalo al PATH."
    }
}
$versionJava = (cmd /c 'java -version 2>&1' | Select-Object -First 1)
Write-Host "Java: $versionJava"
if ($versionJava -notmatch '"(1[7-9]|[2-9]\d)') {
    Falla 'El backend necesita JDK 17 o superior.'
}

# --- 2. Base de datos (PostgreSQL en Docker) -------------------------------
Paso 'Preparando la base de datos'
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Falla 'Docker no está corriendo. Abre Docker Desktop, espera a que esté listo y vuelve a correr el script.'
}

$contenedor = docker ps --filter 'publish=5432' --format '{{.Names}}' | Select-Object -First 1
if ($contenedor) {
    Write-Host "Se usará el contenedor '$contenedor', que publica el puerto 5432."
    # El superusuario es el POSTGRES_USER con que se creó el contenedor.
    $sugerido = "$(docker exec $contenedor printenv POSTGRES_USER)".Trim()
    if (-not $sugerido) { $sugerido = 'postgres' }
    $admin = Read-Host "Superusuario de PostgreSQL en ese contenedor (Enter = $sugerido)"
    if (-not $admin) { $admin = $sugerido }
} else {
    $contenedor = 'barberia-db'
    $admin = 'barberia'
    $existente = docker ps -a --filter "name=^$contenedor$" --format '{{.Names}}'
    if ($existente) {
        docker start $contenedor | Out-Null
    } else {
        Write-Host 'No hay ningún PostgreSQL en el puerto 5432; se crea el contenedor barberia-db.'
        docker run -d --name $contenedor -p 5432:5432 `
            -e POSTGRES_USER=barberia -e POSTGRES_PASSWORD=barberia -e POSTGRES_DB=barberia `
            -v barberia-datos:/var/lib/postgresql/data postgres:16 | Out-Null
    }
    if ($LASTEXITCODE -ne 0) { Falla "No se pudo iniciar el contenedor $contenedor." }
}

Write-Host 'Esperando a que PostgreSQL acepte conexiones...'
$listoBd = $false
for ($i = 0; $i -lt 30 -and -not $listoBd; $i++) {
    docker exec $contenedor pg_isready -q *> $null
    if ($LASTEXITCODE -eq 0) { $listoBd = $true } else { Start-Sleep -Seconds 2 }
}
if (-not $listoBd) { Falla "PostgreSQL no respondió en el contenedor $contenedor." }

# Dentro del contenedor, psql se conecta por el socket local y no pide contraseña.
function Consultar([string]$usuario, [string]$base, [string]$sql) {
    $resultado = docker exec $contenedor psql -U $usuario -d $base -tAc $sql
    if ($LASTEXITCODE -ne 0) { Falla "psql no pudo ejecutar: $sql" }
    return "$resultado".Trim()
}

function EjecutarArchivo([string]$archivo) {
    docker cp $archivo "${contenedor}:/tmp/barberia-script.sql" | Out-Null
    docker exec $contenedor psql -U barberia -d barberia -v ON_ERROR_STOP=1 -q -f /tmp/barberia-script.sql
    if ($LASTEXITCODE -ne 0) { Falla "Falló el script $archivo" }
}

if (-not (Consultar $admin 'postgres' "SELECT 1 FROM pg_roles WHERE rolname = 'barberia'")) {
    Consultar $admin 'postgres' "CREATE ROLE barberia LOGIN PASSWORD 'barberia'" | Out-Null
    Write-Host 'Usuario barberia creado.'
}
if (-not (Consultar $admin 'postgres' "SELECT 1 FROM pg_database WHERE datname = 'barberia'")) {
    Consultar $admin 'postgres' 'CREATE DATABASE barberia OWNER barberia' | Out-Null
    Write-Host 'Base de datos barberia creada.'
}

$hayTablas = Consultar 'barberia' 'barberia' "SELECT to_regclass('public.usuario') IS NOT NULL"
if ($hayTablas -ne 't') {
    EjecutarArchivo "$raiz\database\barberia.sql"
    EjecutarArchivo "$raiz\database\datos_iniciales.sql"
    Write-Host 'Esquema y cuentas iniciales creados.'
} else {
    Write-Host 'Las tablas ya existen; se revisan las columnas agregadas en los últimos módulos.'
    $columnaClave = Consultar 'barberia' 'barberia' `
        "SELECT count(*) FROM information_schema.columns WHERE table_name = 'usuario' AND column_name = 'contrasena_hash'"
    if ($columnaClave -eq '0') {
        Consultar 'barberia' 'barberia' 'ALTER TABLE usuario ADD COLUMN contrasena_hash VARCHAR(100)' | Out-Null
        Write-Host 'Columna usuario.contrasena_hash agregada.'
    }
    $columnaCliente = Consultar 'barberia' 'barberia' `
        "SELECT count(*) FROM information_schema.columns WHERE table_name = 'corte' AND column_name = 'cliente_id'"
    if ($columnaCliente -eq '0') {
        if ((Consultar 'barberia' 'barberia' 'SELECT count(*) FROM corte') -ne '0') {
            Falla 'La tabla corte tiene datos y le falta cliente_id. Agrega la columna a mano (ver README).'
        }
        Consultar 'barberia' 'barberia' 'ALTER TABLE corte ADD COLUMN cliente_id BIGINT NOT NULL REFERENCES usuario (id)' | Out-Null
        Write-Host 'Columna corte.cliente_id agregada.'
    }
    if ((Consultar 'barberia' 'barberia' 'SELECT count(*) FROM usuario WHERE contrasena_hash IS NOT NULL') -eq '0') {
        EjecutarArchivo "$raiz\database\datos_iniciales.sql"
        Write-Host 'Cuentas iniciales creadas.'
    }
}

# --- 3. Backend ---------------------------------------------------------------
if (-not $env:JWT_SECRETO) {
    # Secreto aleatorio solo para esta sesión de desarrollo.
    $bytes = New-Object byte[] 48
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $env:JWT_SECRETO = [Convert]::ToBase64String($bytes)
}
$env:DB_URL = 'jdbc:postgresql://localhost:5432/barberia'
$env:DB_USER = 'barberia'
$env:DB_PASSWORD = 'barberia'

Paso 'Backend: pruebas (la primera vez descarga Maven y las dependencias)'
Push-Location "$raiz\backend"
.\mvnw.cmd -B test
$pruebasBackend = $LASTEXITCODE
Pop-Location
if ($pruebasBackend -ne 0) { Falla 'Las pruebas del backend fallaron. Copia el error que aparece arriba.' }

Paso 'Backend: arrancando en otra ventana'
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$raiz\backend'; .\mvnw.cmd spring-boot:run"

Write-Host 'Esperando a que el backend responda en el puerto 8080...'
$listo = $false
for ($i = 0; $i -lt 90 -and -not $listo; $i++) {
    Start-Sleep -Seconds 2
    try {
        $conexion = New-Object Net.Sockets.TcpClient('localhost', 8080)
        $conexion.Close()
        $listo = $true
    } catch { }
}
if (-not $listo) { Falla 'El backend no arrancó en 3 minutos. Revisa la otra ventana.' }
Write-Host 'Backend listo.'

# --- 4. Frontend --------------------------------------------------------------
Paso 'Frontend: dependencias y pruebas'
Push-Location "$raiz\frontend"
npm install --no-fund --no-audit
if ($LASTEXITCODE -ne 0) { Pop-Location; Falla 'npm install falló.' }
npm test
$pruebasFrontend = $LASTEXITCODE
Pop-Location
if ($pruebasFrontend -ne 0) { Falla 'Las pruebas del frontend fallaron. Copia el error que aparece arriba.' }

Paso 'Frontend: arrancando en otra ventana'
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$raiz\frontend'; npm run dev"
Start-Sleep -Seconds 6
Start-Process 'http://localhost:5173'

Write-Host "`nSistema arriba en http://localhost:5173" -ForegroundColor Green
Write-Host 'Cuentas iniciales (contraseña Barberia2026):'
Write-Host '  dueno@barberia.com, barbero1@barberia.com, barbero2@barberia.com'
Write-Host 'Los clientes se registran desde la pantalla de ingreso.'
Read-Host "`nPresiona Enter para cerrar esta ventana (el backend y el frontend siguen corriendo)"
