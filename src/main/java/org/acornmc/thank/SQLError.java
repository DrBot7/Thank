package org.acornmc.thank;

import java.util.logging.Level;

public class SQLError {
    public static void execute(Thank plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }

    public static void close(Thank plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }

    public static String sqlConnectionExecute(){
        return "Couldn't execute MySQL statement: ";
    }

    public static String sqlConnectionClose(){
        return "Failed to close MySQL connection: ";
    }

    public static String noSQLConnection(){
        return "Unable to retreive MYSQL connection: ";
    }

    public static String noTableFound(){
        return "Database Error: No Table Found";
    }
}
