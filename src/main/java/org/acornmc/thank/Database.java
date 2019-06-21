package org.acornmc.thank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;

public abstract class Database {
    Thank plugin;
    Connection connection;
    int now;
    public abstract Connection getSQLConnection();
    public abstract void load();

    public Database(Thank instance){
        plugin = instance;
    }

    public void initializeThanks(){
        connection = getSQLConnection();

        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM thanks");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }
    public void initializeBans(){
        connection = getSQLConnection();

        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM bans");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public int thankcount(String thankerUuid, String thankeeUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            if (thankeeUuid.length() > 0 && thankerUuid.length() > 0) {
                ps = conn.prepareStatement("SELECT * FROM thanks WHERE thanker = '" + thankerUuid + "' AND thankee = '" + thankeeUuid + "';");
            } else if (thankerUuid.length() > 0) {
                ps = conn.prepareStatement("SELECT * FROM thanks WHERE thanker = '" + thankerUuid + "';");
            } else {
                ps = conn.prepareStatement("SELECT * FROM thanks WHERE thankee = '" + thankeeUuid + "';");
            }
            rs = ps.executeQuery();
            int thankCount = 0;
            while (rs.next()){
                thankCount++;
            }
            return thankCount;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }

    public int cooldownRemaining(String thankerUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            now = new Date().hashCode();
            int cooldown = plugin.getConfig().getInt("ThankCooldown");
            int cooldownIfAnyEntryIsNewer = now - 1000 * cooldown;
            ps = conn.prepareStatement("SELECT * FROM thanks WHERE thanker='" + thankerUuid +"' AND time>" + cooldownIfAnyEntryIsNewer + ";");
            rs = ps.executeQuery();
            if (rs.next()) {
                int secondsSinceLastThank = (now - rs.getInt("time"))/1000;
                return cooldown - secondsSinceLastThank;
            } else {
                return 0;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }

    public int thanklast(String thankerUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT time FROM thanks WHERE thanker='" + thankerUuid +"' ORDER BY time ASC;");
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("time");
            } else {
                return 0;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }

    public void addNewThanksEntry(String thankerUuid, String thankeeUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            now = new Date().hashCode();
            ps = conn.prepareStatement("INSERT INTO thanks (thanker, thankee, time) VALUES ('" + thankerUuid + "', '" + thankeeUuid + "', " + now + ");");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
    }

    public boolean thank4ThankDetected(String thankerUuid, String thankeeUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        now = new Date().hashCode();
        int cooldown = plugin.getConfig().getInt("ThankCooldown");
        int cooldownIfAnyEntryIsNewer = now - 1000 * cooldown;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT thankee FROM thanks WHERE thanker='" + thankeeUuid +"' AND time>" + cooldownIfAnyEntryIsNewer + ";");
            rs = ps.executeQuery();
            if (rs.next()) {
                String thankee = rs.getString("thankee");
                if (thankee.equals(thankerUuid)) {
                    return true;
                }
                return thank4ThankDetected(thankerUuid, thankee);
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
        return false;
    }

    public void addNewThankbanEntry(String target, int minutes) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM bans WHERE uuid='" + target + "';");
            ps.executeUpdate();

            now = new Date().hashCode();
            ps = conn.prepareStatement("INSERT INTO bans (uuid, time) VALUES ('" + target + "', '" + (now + minutes*1000*60) + ");");
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
    }
    public boolean checkThankbanned(String target) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT time FROM bans WHERE uuid='" + target + "';");
            rs = ps.executeQuery();
            if (rs.next()) {
                int time = rs.getInt("time");
                int now = new Date().hashCode();
                if (now > time) {
                    return false;
                }
                return true;
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, SQLError.sqlConnectionClose(), ex);
            }
        }
        return false;
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            SQLError.close(plugin, ex);
        }
    }
}