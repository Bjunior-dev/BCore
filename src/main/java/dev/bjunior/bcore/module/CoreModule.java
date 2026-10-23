package dev.bjunior.bcore.module;

/**
 * Kontrak modul BCore. Semua fitur (homes, tpa, afk, bounty, clearlag, ...)
 * diimplementasikan sebagai modul agar bisa dinyalakan/dimatikan dari config.yml.
 */
public interface CoreModule {

    /** ID modul, dipakai di config: modules.<id>: true */
    String id();

    /** Dipanggil saat plugin aktif / reload. */
    void enable();

    /** Dipanggil saat plugin mati / reload; simpan data di sini. */
    void disable();
}
