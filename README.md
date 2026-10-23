# BCore

Plugin core ekonomi (dan pondasi event engine) untuk **Paper 26.3 / Minecraft 26.3**, **Java 25**.

> Catatan jujur: kode ini adalah **penulisan ulang** dari nol dengan fitur yang sama seperti `EconomyCore-1.0.0.jar`, bukan hasil dekompilasi jar tersebut (jar hanya berisi bytecode, source aslinya tidak ada).

## Fitur 0.1.0

- Saldo per UUID, disimpan di `plugins/BCore/balances.yml`
- `/balance [player]`, `/pay <player> <jumlah>`, `/baltop`
- Admin: `/eco give|take|set|reset|reload [player] [jumlah]`
- `/bcore version|reload`
- Autosave async (`storage.save-interval-seconds`, default 300 detik)
- Batas saldo & saldo awal bisa diatur di `config.yml`
- API internal `economy()` untuk dipakai modul/plugin lain (mis. EcoEvents)

## Build (otomatis via GitHub Actions)

Setiap push ke `main` akan menjalankan workflow **Build BCore**:

1. Buka tab **Actions** → pilih run terbaru → bagian **Artifacts** → unduh `BCore-jar`.
2. Isi zip: `BCore-0.1.0.jar` → taruh di `plugins/`, lalu restart server.

## Build manual

```bash
mvn -B clean package
# hasil: target/BCore-0.1.0.jar
```

Butuh JDK 25. Dependency: `io.papermc.paper:paper-api:26.3.build.+` (scope provided).

## Instalasi di Pterodactyl (egg Paper)

- Server software: **Paper 26.3** (stable build)
- Docker image / startup: **Java 25** (mis. `ghcr.io/pterodactyl/yolks:java_25` atau image egg Paper yang sudah 25)
- Upload `BCore-0.1.0.jar` ke `/plugins`, restart, cek console: `BCore aktif (api 26.x / Java 25)`.

## Struktur

```
src/main/java/dev/bjunior/bcore/
├── BCorePlugin.java
├── command/  (Balance, Pay, Baltop, EcoAdmin, BCore)
├── economy/  (EcoService)
└── listener/ (PlayerListener)
```
