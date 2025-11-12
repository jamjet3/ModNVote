package com.modnmetl.modnvote;

import com.modnmetl.modnvote.commands.ModNVoteCommand;
import com.modnmetl.modnvote.placeholders.ModNVoteExpansion;
import com.modnmetl.modnvote.storage.Database;
import com.modnmetl.modnvote.storage.VoteDao;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class ModNVotePlugin extends JavaPlugin {
    private Database database;
    private VoteDao voteDao;

    private final AtomicInteger yesCache = new AtomicInteger(0);
    private final AtomicInteger noCache = new AtomicInteger(0);
    private ExecutorService dbPool;

    private boolean auditVotesToConsole = true;
    private boolean auditBypassToConsole = true;
    private String bypassPermissionNode = "modnvote.bypass";

    private final int roundId = 1;
    private byte[] pepper;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        try {
            if (!getDataFolder().exists()) getDataFolder().mkdirs();
            File dbFile = new File(getDataFolder(), "modnvote.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            this.database = new Database(url);
            this.voteDao = new VoteDao(database);
            this.voteDao.init();
        } catch (Exception e) {
            getLogger().severe("Failed to initialise SQLite: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.pepper = loadOrCreatePepper(roundId);

        this.dbPool = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

        refreshCaches();

        ModNVoteCommand cmd = new ModNVoteCommand(this, voteDao, yesCache, noCache, dbPool);
        PluginCommand pc = getCommand("modnvote");
        if (pc != null) {
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ModNVoteExpansion(this, yesCache, noCache).register();
            getLogger().info("PlaceholderAPI detected; ModNVote placeholders registered.");
        }

        long periodTicks = 20L * Math.max(5, getConfig().getInt("cache.refresh_seconds", 30));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::refreshCaches, periodTicks, periodTicks);
    }

    @Override
    public void onDisable() {
        if (dbPool != null) dbPool.shutdownNow();
        if (database != null) database.closeQuietly();
    }

    public void refreshCaches() {
        try {
            int[] t = voteDao.getTally(roundId);
            yesCache.set(t[0]);
            noCache.set(t[1]);
        } catch (Exception e) {
            getLogger().warning("Failed to refresh caches: " + e.getMessage());
        }
    }

    public void loadConfigValues() {
        FileConfiguration c = getConfig();
        this.auditVotesToConsole = c.getBoolean("logging.audit_votes_to_console", true);
        this.auditBypassToConsole = c.getBoolean("logging.audit_bypass_to_console", true);
        this.bypassPermissionNode = c.getString("permissions.bypass_node", "modnvote.bypass");
    }

    public boolean isAuditVotesToConsole() { return auditVotesToConsole; }
    public boolean isAuditBypassToConsole() { return auditBypassToConsole; }
    public String getBypassPermissionNode() { return bypassPermissionNode; }
    public int getRoundId() { return roundId; }
    public byte[] getPepper() { return pepper; }

    public String msg(String path, String fallback) {
        String raw = getConfig().getString(path, fallback);
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    private byte[] loadOrCreatePepper(int roundId) {
        try {
            String pattern = getConfig().getString("integrity.pepper_file_pattern", "round-%d.key");
            Path p = getDataFolder().toPath().resolve(String.format(pattern, roundId));
            if (!Files.exists(p)) {
                byte[] key = new byte[32];
                new SecureRandom().nextBytes(key);
                Files.write(p, key);
                return key;
            }
            return Files.readAllBytes(p);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load/create pepper: " + e.getMessage(), e);
        }
    }
}
