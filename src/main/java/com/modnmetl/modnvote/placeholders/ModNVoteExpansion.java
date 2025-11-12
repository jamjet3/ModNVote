package com.modnmetl.modnvote.placeholders;

import com.modnmetl.modnvote.ModNVotePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class ModNVoteExpansion extends PlaceholderExpansion {
    private final ModNVotePlugin plugin;
    private final AtomicInteger yes;
    private final AtomicInteger no;

    public ModNVoteExpansion(ModNVotePlugin plugin, AtomicInteger yes, AtomicInteger no) {
        this.plugin = plugin;
        this.yes = yes;
        this.no = no;
    }

    @Override public String getIdentifier() { return "modnvote"; }
    @Override public String getAuthor() { return "MODN METL LTD"; }
    @Override public String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        switch (params.toLowerCase()) {
            case "yes":   return Integer.toString(yes.get());
            case "no":    return Integer.toString(no.get());
            case "total": return Integer.toString(yes.get() + no.get());
            default: return null;
        }
    }
}
