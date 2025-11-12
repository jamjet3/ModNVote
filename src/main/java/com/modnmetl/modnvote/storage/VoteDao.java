package com.modnmetl.modnvote.storage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VoteDao {
    private final Database db;
    public VoteDao(Database db) { this.db = db; }

    public void init() throws SQLException {
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("PRAGMA journal_mode=WAL");
            st.executeUpdate("PRAGMA synchronous=NORMAL");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS participants (" +
                    "uuid TEXT NOT NULL, " +
                    "ip TEXT NOT NULL, " +
                    "bypass INTEGER NOT NULL, " +
                    "round_id INTEGER NOT NULL, " +
                    "created_at INTEGER NOT NULL, " +
                    "PRIMARY KEY(uuid, round_id))");

            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_participants_round ON participants(round_id)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_participants_ip_round ON participants(ip, round_id)");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS tally (" +
                    "round_id INTEGER PRIMARY KEY, " +
                    "yes_count INTEGER NOT NULL, " +
                    "no_count INTEGER NOT NULL, " +
                    "hmac TEXT NOT NULL)");

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO tally(round_id, yes_count, no_count, hmac) " +
                            "SELECT ?, 0, 0, '' WHERE NOT EXISTS (SELECT 1 FROM tally WHERE round_id = ?)")) {
                ps.setInt(1, 1);
                ps.setInt(2, 1);
                ps.executeUpdate();
            }
        }
    }

    public boolean hasUuidVoted(UUID uuid, int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM participants WHERE uuid = ? AND round_id = ? LIMIT 1")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, round);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public boolean hasIpVoted(String ip, int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM participants WHERE ip = ? AND round_id = ? LIMIT 1")) {
            ps.setString(1, ip);
            ps.setInt(2, round);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insertParticipation(UUID uuid, String ip, boolean bypass, int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO participants(uuid, ip, bypass, round_id, created_at) VALUES(?,?,?,?,?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ip);
            ps.setInt(3, bypass ? 1 : 0);
            ps.setInt(4, round);
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    public int[] getTally(int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT yes_count, no_count FROM tally WHERE round_id = ?")) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new int[]{rs.getInt(1), rs.getInt(2)};
                return new int[]{0,0};
            }
        }
    }

    public void updateTally(int round, int yes, int no, String hmac) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "UPDATE tally SET yes_count = ?, no_count = ?, hmac = ? WHERE round_id = ?")) {
            ps.setInt(1, yes);
            ps.setInt(2, no);
            ps.setString(3, hmac);
            ps.setInt(4, round);
            ps.executeUpdate();
        }
    }

    public String getStoredHmac(int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT hmac FROM tally WHERE round_id = ?")) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : ""; }
        }
    }

    public List<UUID> fetchAllUuids(int round) throws SQLException {
        List<UUID> out = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT uuid FROM participants WHERE round_id = ? ORDER BY uuid")) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(UUID.fromString(rs.getString(1)));
            }
        }
        return out;
    }

    public int countParticipants(int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM participants WHERE round_id = ?")) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public int countBypass(int round) throws SQLException {
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM participants WHERE round_id = ? AND bypass = 1")) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public List<VoteRow> fetchAllForAudit(int round) throws SQLException {
        List<VoteRow> out = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(
                "SELECT uuid, ip, bypass FROM participants WHERE round_id = ? ORDER BY ip, uuid")) {
            ps.setInt(1, round);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VoteRow r = new VoteRow();
                    r.uuid = UUID.fromString(rs.getString(1));
                    r.ip = rs.getString(2);
                    r.bypass = rs.getInt(3) == 1;
                    out.add(r);
                }
            }
        }
        return out;
    }

    public void resetAll(int round) throws SQLException {
        try (Connection c = db.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM participants WHERE round_id = ?")) {
                ps.setInt(1, round);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE tally SET yes_count = 0, no_count = 0, hmac = '' WHERE round_id = ?")) {
                ps.setInt(1, round);
                ps.executeUpdate();
            }
        }
    }

    public static class VoteRow {
        public UUID uuid;
        public String ip;
        public boolean bypass;
    }
}
