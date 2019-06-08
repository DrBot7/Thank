package org.acornmc.thank;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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

    public int Thankcount(Player thanker, Player thankee) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        String thankerUuid = thanker.getUniqueId().toString().replace("-", "");
        String thankeeUuid = thankee.getUniqueId().toString().replace("-", "");
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE thanker = '" + thankerUuid + "' AND thankee = '" + thankeeUuid + "';");
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

    public int CooldownRemaining(Player thanker) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            String thankerUuid = thanker.getUniqueId().toString().replace("-", "");
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

    public void addNewEntry(Player thanker, Player thankee) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            now = new Date().hashCode();
            String thankerUuid = thanker.getUniqueId().toString().replace("-", "");
            String thankeeUuid = thankee.getUniqueId().toString().replace("-", "");
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