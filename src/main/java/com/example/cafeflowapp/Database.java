package com.example.cafeflowapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String URL = "jdbc:sqlite:cafeflow.db";

    static {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement st = conn.createStatement()) {

            // Create Tables
            st.execute("CREATE TABLE IF NOT EXISTS customers (id INTEGER PRIMARY KEY AUTOINCREMENT, full_name TEXT NOT NULL, email TEXT UNIQUE NOT NULL, password TEXT NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS menu_items (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, price REAL NOT NULL, stock INTEGER NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS orders (order_id TEXT PRIMARY KEY, customer_name TEXT, customer_email TEXT, items_summary TEXT, total_amount REAL, status TEXT, order_date TEXT)");

        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // for customer
    public static boolean insertCustomer(String fullName, String email, String password) throws SQLException {
        String sql = "INSERT INTO customers(full_name, email, password) VALUES(?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName); ps.setString(2, email); ps.setString(3, password);
            return ps.executeUpdate() == 1;
        }
    }

    public static Customer findCustomer(String email, String password) throws SQLException {
        String sql = "SELECT * FROM customers WHERE email = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email); ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new Customer(rs.getInt("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("password"));
            }
        }
        return null;
    }

    public static boolean updateCustomer(int id, String fullName, String email, String password) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ?, password = ? WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName); ps.setString(2, email); ps.setString(3, password); ps.setInt(4, id);
            return ps.executeUpdate() == 1;
        }
    }

    // in menu
    public static List<MenuItem> getMenu() {
        List<MenuItem> list = new ArrayList<>();
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM menu_items")) {
            while (rs.next()) list.add(new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("price"), rs.getInt("stock")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean addMenuItem(String name, double price, int stock) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO menu_items(name, price, stock) VALUES(?,?,?)")) {
            ps.setString(1, name); ps.setDouble(2, price); ps.setInt(3, stock);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { return false; }
    }

    public static boolean deleteMenuItem(String name) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM menu_items WHERE name = ?")) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public static boolean updateStock(String name, int newStock) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE menu_items SET stock = ? WHERE name = ?")) {
            ps.setInt(1, newStock); ps.setString(2, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // order
    public static boolean placeOrder(String orderId, String name, String email, String items, double total) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO orders(order_id, customer_name, customer_email, items_summary, total_amount, status, order_date) VALUES(?,?,?,?,?,?, datetime('now'))")) {
            ps.setString(1, orderId); ps.setString(2, name); ps.setString(3, email); ps.setString(4, items); ps.setDouble(5, total); ps.setString(6, "Pending");
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { return false; }
    }

    public static List<OrderModel> getAllOrders() {
        List<OrderModel> list = new ArrayList<>();
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM orders")) {
            while (rs.next()) list.add(new OrderModel(rs.getString("order_id"), rs.getString("customer_name"), rs.getString("customer_email"), rs.getString("items_summary"), rs.getDouble("total_amount"), rs.getString("status")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static List<String> getOrderHistory(String email) {
        List<String> history = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE customer_email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) history.add(String.format("ID: %s | $%.2f | %s | Items: %s", rs.getString("order_id"), rs.getDouble("total_amount"), rs.getString("status"), rs.getString("items_summary")));
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }

    public static boolean updateOrderStatus(String orderId, String status) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
            ps.setString(1, status); ps.setString(2, orderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // dashboard and order

    public static double getTodaySales() {
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(total_amount) FROM orders WHERE date(order_date) = date('now')")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    public static int getTotalOrdersCount() {
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM orders")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getPendingOrdersCount() {
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM orders WHERE status = 'Pending'")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static int getLowStockCount() {
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM menu_items WHERE stock < 5")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // count total for today
    public static int getTodayItemsSold() {
        int count = 0;
        String sql = "SELECT items_summary FROM orders WHERE date(order_date) = date('now')";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String summary = rs.getString(1);
                if (summary != null && !summary.isEmpty()) {
                    String[] items = summary.split(", ");
                    for (String item : items) {
                        try {
                            String[] parts = item.split(" x");
                            if (parts.length == 2) {
                                count += Integer.parseInt(parts[1].trim());
                            }
                        } catch (Exception e) { }
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    // complete order
    public static int getTodayCompletedOrders() {
        try (Connection conn = getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM orders WHERE status = 'Confirmed' AND date(order_date) = date('now')")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}