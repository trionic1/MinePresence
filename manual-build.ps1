# Manual build for MinePresence.
#
# Workaround for a machine-level problem: AF_UNIX sockets currently fail to
# connect on this PC (SocketException "Invalid argument"), which breaks the
# NIO loopback pipes Gradle needs for its daemon IPC on Java 16+. This script
# reproduces exactly what `gradlew build` produces for this project - the mod
# compiles against identity (intermediary == official) mappings with
# remap=false, so the jar is plain javac output plus resources, no remapping.
#
# Requires: Java 25 JDK at $JavaHome, and the Gradle/Loom caches from a
# previous successful dependency resolution (present since the 7/4 build).

param(
    [string]$JavaHome = 'C:\Program Files\Zulu\zulu-25'
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$props = @{}
Get-Content "$root\gradle.properties" | Where-Object { $_ -match '^\s*([^#=]+)=(.*)$' } | ForEach-Object {
    $props[$Matches[1].Trim()] = $Matches[2].Trim()
}
$version = $props['mod_version']
$mcVersion = $props['minecraft_version']

$gradleCache = "$env:USERPROFILE\.gradle\caches"
$loomCache = "$gradleCache\fabric-loom\$mcVersion"
$modules = "$gradleCache\modules-2\files-2.1"

function Find-Dep([string]$group, [string]$pattern) {
    $jar = Get-ChildItem "$modules\$group" -Recurse -Filter $pattern |
        Where-Object Name -notmatch 'sources|javadoc|natives' |
        Sort-Object Name -Descending | Select-Object -First 1
    if (-not $jar) { throw "Dependency not found: $group $pattern" }
    $jar.FullName
}

$classpath = @(
    "$loomCache\minecraft-client-only.jar"
    "$loomCache\minecraft-common.jar"
    (Find-Dep 'net.fabricmc' 'fabric-loader-*.jar')
    (Find-Dep 'net.fabricmc' 'sponge-mixin-*.jar')
    (Find-Dep 'org.lwjgl' 'lwjgl-3*.jar')
    (Find-Dep 'org.lwjgl' 'lwjgl-glfw-*.jar')
    (Find-Dep 'com.google.code.gson' 'gson-*.jar')
    (Find-Dep 'org.slf4j' 'slf4j-api-*.jar')
    (Find-Dep 'com.mojang' 'brigadier-*.jar')
    (Find-Dep 'com.mojang' 'datafixerupper-*.jar')
    (Find-Dep 'org.joml' 'joml-*.jar')
    (Find-Dep 'it.unimi.dsi' 'fastutil-*.jar')
    (Find-Dep 'com.google.guava' 'guava-*.jar')
    "$root\.gradle\loom-cache\remapped_mods\remapped\com\terraformersmc\modmenu-177623ad\$($props['modmenu_version'])\modmenu-177623ad-$($props['modmenu_version']).jar"
)
foreach ($jar in $classpath) {
    if (-not (Test-Path $jar)) { throw "Classpath jar missing: $jar" }
}

$work = "$root\build\manual"
if (Test-Path $work) { Remove-Item $work -Recurse -Force }
$classes = New-Item -ItemType Directory -Force "$work\classes"

$sources = Get-ChildItem "$root\src\client\java" -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& "$JavaHome\bin\javac.exe" --release 25 -encoding UTF-8 -proc:none `
    -cp ($classpath -join ';') -d $classes.FullName @sources
if ($LASTEXITCODE -ne 0) { throw "javac failed" }

# Resources, with the same property expansion processResources applies.
Copy-Item "$root\src\main\resources\*" "$work\classes" -Recurse
$fmj = Get-Content "$work\classes\fabric.mod.json" -Raw
$fmj = $fmj.Replace('${version}', $version).
    Replace('${minecraft_version}', $mcVersion).
    Replace('${loader_version}', $props['loader_version']).
    Replace('${modmenu_version}', $props['modmenu_version'])
# WriteAllText with explicit UTF8Encoding(false): PowerShell 5.1's utf8
# encoding adds a BOM, which JSON parsers reject.
[System.IO.File]::WriteAllText("$work\classes\fabric.mod.json", $fmj, [System.Text.UTF8Encoding]::new($false))
Copy-Item "$root\LICENSE" "$work\classes\LICENSE_$($props['archives_base_name'])"

$manifest = @"
Manifest-Version: 1.0
Fabric-Jar-Type: classes
Fabric-Minecraft-Version: $mcVersion
Fabric-Loader-Version: $($props['loader_version'])
Fabric-Mapping-Namespace: intermediary
"@
Set-Content "$work\MANIFEST.MF" -Value $manifest -Encoding ascii

New-Item -ItemType Directory -Force "$root\build\libs" | Out-Null
$out = "$root\build\libs\$($props['archives_base_name'])-$version.jar"
if (Test-Path $out) { Remove-Item $out -Force }
Push-Location "$work\classes"
& "$JavaHome\bin\jar.exe" --create --file $out --manifest "$work\MANIFEST.MF" .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "jar failed" }

Write-Host "Built: $out"
