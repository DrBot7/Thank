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
    public String table = "thanks";
    int now;
    public abstract Connection getSQLConnection();
    public abstract void load();

    public Database(Thank instance){
        plugin = instance;
    }

    public void initialize(){
        connection = getSQLConnection();

        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table);
            ResultSet rs = ps.executeQuery();
            close(ps,rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public int Thankcount(String thankerUuid, String thankeeUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            if (thankeeUuid.length() > 0 && thankerUuid.length() > 0) {
                ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE thanker = '" + thankerUuid + "' AND thankee = '" + thankeeUuid + "';");
            } else if (thankerUuid.length() > 0) {
                ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE thanker = '" + thankerUuid + "';");
            } else {
                ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE thankee = '" + thankeeUuid + "';");
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

    public int CooldownRemaining(String thankerUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            now = new Date().hashCode();
            int cooldown = plugin.getConfig().getInt("ThankCooldown");
            int cooldownIfAnyEntryIsNewer = now - 1000 * cooldown;
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE thanker='" + thankerUuid +"' AND time>" + cooldownIfAnyEntryIsNewer + ";");
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

    public void addNewEntry(String thankerUuid, String thankeeUuid) {
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

    public boolean Thank4ThankDetected(String thankerUuid, String thankeeUuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        now = new Date().hashCode();
        int cooldown = plugin.getConfig().getInt("ThankCooldown");
        int cooldownIfAnyEntryIsNewer = now - 1000 * cooldown;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT thankee FROM " + table + " WHERE thanker='" + thankeeUuid +"' AND time>" + cooldownIfAnyEntryIsNewer + ";");
            rs = ps.executeQuery();
            if (rs.next()) {
                String thankee = rs.getString("thankee");
                if (thankee.equals(thankerUuid)) {
                    return true;
                }
                return Thank4ThankDetected(thankerUuid, thankee);
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