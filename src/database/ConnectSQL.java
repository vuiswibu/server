/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database;

import java.sql.*;

/**
 *
 * @author boi09
 */
public class ConnectSQL {
    
    public Connection getConnection(){
        Connection conn=null;
        String jdbcURL = "jdbc:sqlserver://MSI\\SQLEXPRESS:1433;databaseName=caro_acc;encrypt=true;trustServerCertificate=true;";
        String jdbcUsername = "sa";
        String jdbcPassword = "123";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
            if(conn!=null){
                System.out.println("Connection to database succesful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection to database failed");
        }
        return conn;
    }  
}
