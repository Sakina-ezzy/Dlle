package com.mycompany.dlle;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        try {
            Connection con = DBConnection.getConnection();

            if (con != null) {
                System.out.println("Database connected successfully!");
            }

        } catch (Exception e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}