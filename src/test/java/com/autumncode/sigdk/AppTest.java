package com.autumncode.sigdk;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AppTest {
    DB db;

    {
        try {
            db = DB.newEmbeddedDB(3306);
            db.start();
            db.createDB("testdb");
        } catch (ManagedProcessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGeneratedKeys() throws Exception {
        buildTables();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb")) {
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO foo(data) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, "baz");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    int key = rs.getInt(1);
                    System.out.println("generated " + key);
                    try (PreparedStatement ps2 = conn.prepareStatement("INSERT INTO bar (foo_id) VALUES (?)")) {
                        ps2.setInt(1, key);
                        ps2.executeUpdate();
                    }
                }
            }
        }
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb")) {
            System.out.println("FROM FOO--------------");
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM foo")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getObject(1) + ":" + rs.getObject(2));
                    }
                }
            }
            System.out.println("FROM BAR--------------");
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM bar")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        System.out.println(rs.getObject(1) + ":" + rs.getObject(2));
                    }
                }
            }
        }
    }

    private void buildTables() throws Exception {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/testdb")) {
            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE foo(id INT AUTO_INCREMENT PRIMARY KEY, data VARCHAR(255));")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("CREATE TABLE bar(id INT AUTO_INCREMENT PRIMARY KEY, foo_id INT NOT NULL);")) {
                ps.executeUpdate();
            }
        }
    }
}