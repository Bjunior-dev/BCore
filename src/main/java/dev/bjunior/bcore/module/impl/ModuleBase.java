package dev.bjunior.bcore.module.impl;

import dev.bjunior.bcore.BCorePlugin;
import dev.bjunior.bcore.module.CoreModule;
import dev.bjunior.bcore.util.Msg;
import org.bukkit.command.CommandSender;

/** Basis modul: akses plugin, config, dan pesan. */
public abstract class ModuleBase implements CoreModule {

    protected final BCorePlugin plugin;

    protected ModuleBase(BCorePlugin plugin) {
        this.plugin = plugin;
    }

    protected String prefix() {
        return plugin.msg().raw("prefix", "&8[&bBCore&8] &r");
    }

    protected void send(CommandSender to, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        to.sendMessage(Msg.of(text));
    }

    protected void ok(CommandSender to, String text) {
        send(to, prefix() + text);
    }

    protected boolean flag(String path, boolean def) {
        return plugin.getConfig().getBoolean(path, def);
    }

    protected int integer(String path, int def) {
        return plugin.getConfig().getInt(path, def);
    }
}
