param(
    [ValidateSet("local", "dev", "prod")]
    [string]$Environment = "local"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$environmentFile = Join-Path $projectRoot ".env.$Environment"

if (-not (Test-Path -LiteralPath $environmentFile)) {
    throw "Arquivo ausente: $environmentFile. Copie .env.$Environment.example e preencha os valores."
}

foreach ($line in Get-Content -LiteralPath $environmentFile) {
    $trimmedLine = $line.Trim()
    if (-not $trimmedLine -or $trimmedLine.StartsWith("#")) {
        continue
    }

    $separatorIndex = $trimmedLine.IndexOf("=")
    if ($separatorIndex -lt 1) {
        throw "Linha invalida em $environmentFile. Use CHAVE=valor."
    }

    $key = $trimmedLine.Substring(0, $separatorIndex).Trim()
    $value = $trimmedLine.Substring($separatorIndex + 1).Trim()
    if ($key -notmatch "^[A-Z_][A-Z0-9_]*$") {
        throw "Nome de variavel invalido em $environmentFile."
    }

    if (($value.StartsWith('"') -and $value.EndsWith('"')) -or
        ($value.StartsWith("'") -and $value.EndsWith("'"))) {
        $value = $value.Substring(1, $value.Length - 2)
    }

    Set-Item -Path "Env:$key" -Value $value
}

$env:SPRING_PROFILES_ACTIVE = $Environment
& (Join-Path $projectRoot "mvnw.cmd") spring-boot:run
exit $LASTEXITCODE
