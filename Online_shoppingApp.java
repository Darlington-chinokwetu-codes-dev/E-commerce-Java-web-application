import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.sql.*;

import database.DBConnection;

/**
 * ECOMMERCE WEB APPLICATIONS
 * 
 */
public class JAVAAPP extends JFrame {

    // ── Theme Colors ─────────────────────────────────────────────────────────────
    private final Color PRIMARY_BLUE = new Color(40, 116, 240);
    private final Color SECONDARY_ORANGE = new Color(251, 100, 27);
    private final Color SUCCESS_GREEN = new Color(56, 142, 60);
    private final Color ERROR_RED = new Color(211, 47, 47);
    private final Color BG_COLOR = new Color(241, 243, 246);

    // ── Application State
    // ─────────────────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private List<Product> allProducts = new ArrayList<>();
    private List<CartItem> cartItems = new ArrayList<>();
    private List<CartItem> itemsToBuy = new ArrayList<>();
    private List<Order> orderHistory = new ArrayList<>();

    // ── Session
    // ───────────────────────────────────────────────────────────────────
    // sessionUserId — users.ID (int PK), used for all order DB operations
    // sessionUsername — users.USERNAME, shown in the UI header only
    // custName — delivery name from the address form (can differ from login)
    private int sessionUserId = -1;
    private String sessionUsername = "";
    private String custName = "";
    private String custMobile = "";
    private String fullAddress = "";
    private String custCity = "";
    private String custDistrict = "";

    // ── UI Components
    // ─────────────────────────────────────────────────────────────
    private JPanel dashboardContent, cartPanel, ordersPanel, checkoutConfirmPanel;
    private JLabel cartCountLabel;
    private JTextField searchField;

    // ─────────────────────────────────────────────────────────────────────────────

    public JAVAAPP() {
        setTitle("SwiftStore Pro - Ultimate Shopping Mall");
        setSize(1300, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initDatabase();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createModernLoginScreen(), "LOGIN");
        mainPanel.add(createDashboardScreen(), "DASHBOARD");
        mainPanel.add(createCartScreen(), "CART");
        mainPanel.add(createAddressScreen(), "CHECKOUT_ADDR");
        mainPanel.add(createPaymentScreen(), "CHECKOUT_PAY");
        mainPanel.add(createFinalConfirmationScreen(), "CHECKOUT_CONFIRM");
        mainPanel.add(createOrdersScreen(), "ORDERS");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    // ── Load Products
    // ─────────────────────────────────────────────────────────────

    private void initDatabase() {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM products");
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                allProducts.add(new Product(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("specs"),
                        rs.getDouble("rating"),
                        rs.getString("brand")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Password Hashing
    // ──────────────────────────────────────────────────────────
    // Swap SHA-256 for BCrypt.hashpw / BCrypt.checkpw in production.

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(
                    password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    // ── Login Screen
    // ──────────────────────────────────────────────────────────────

    private JPanel createModernLoginScreen() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(BG_COLOR);

        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(400, 450));
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(220, 220, 220)));

        JPanel blueHeader = new JPanel(new GridBagLayout());
        blueHeader.setBackground(PRIMARY_BLUE);
        blueHeader.setPreferredSize(new Dimension(400, 100));
        JLabel welcome = new JLabel("SwiftStore Login");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 26));
        welcome.setForeground(Color.WHITE);
        blueHeader.add(welcome);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 20, 10, 20);

        JTextField u = new JTextField(15);
        JPasswordField p = new JPasswordField(15);
        JButton l = createStyledButton("LOGIN", Color.WHITE, SECONDARY_ORANGE);
        JButton r = createStyledButton("REGISTER", Color.WHITE, SUCCESS_GREEN);

        g.gridy = 0;
        form.add(new JLabel("Username"), g);
        g.gridy = 1;
        form.add(u, g);
        g.gridy = 2;
        form.add(new JLabel("Password"), g);
        g.gridy = 3;
        form.add(p, g);
        g.gridy = 4;
        form.add(l, g);
        g.gridy = 5;
        form.add(r, g);

        r.addActionListener(e -> JOptionPane.showMessageDialog(
                this, createRegisterScreen(), "Register", JOptionPane.PLAIN_MESSAGE));

        l.addActionListener(e -> {
            String enteredUser = u.getText().trim();
            String hashed = hashPassword(new String(p.getPassword()));

            // Query users by USERNAME and hashed PASSWORD, retrieve the int ID
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement ps2 = conn.prepareStatement(
                            "SELECT ID, USERNAME FROM users WHERE USERNAME = ? AND PASSWORD = ?")) {

                ps2.setString(1, enteredUser);
                ps2.setString(2, hashed);

                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        sessionUserId = rs2.getInt("ID"); // store int PK
                        sessionUsername = rs2.getString("USERNAME");
                        custName = sessionUsername;
                        cardLayout.show(mainPanel, "DASHBOARD");
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid username or password.");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage());
            }
        });

        card.add(blueHeader, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        container.add(card);
        return container;
    }

    // ── Register Screen
    // ───────────────────────────────────────────────────────────

    private JPanel createRegisterScreen() {
        JPanel mainP = new JPanel(new BorderLayout());

        JPanel header = new JPanel();
        header.setBackground(new Color(30, 144, 255));
        JLabel title = new JLabel("CREATE ACCOUNT");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        header.add(title);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        JTextField email = new JTextField(15);
        JTextField username = new JTextField(15);
        JPasswordField password = new JPasswordField(15);
        JTextField phone = new JTextField(15);

        g.gridx = 0;
        g.gridy = 0;
        form.add(new JLabel("Email"), g);
        g.gridx = 1;
        form.add(email, g);
        g.gridx = 0;
        g.gridy = 1;
        form.add(new JLabel("Username"), g);
        g.gridx = 1;
        form.add(username, g);
        g.gridx = 0;
        g.gridy = 2;
        form.add(new JLabel("Password"), g);
        g.gridx = 1;
        form.add(password, g);
        g.gridx = 0;
        g.gridy = 3;
        form.add(new JLabel("Phone"), g);
        g.gridx = 1;
        form.add(phone, g);

        JButton registerBtn = createStyledButton("REGISTER", Color.WHITE, SECONDARY_ORANGE);
        g.gridx = 0;
        g.gridy = 4;
        g.gridwidth = 2;
        form.add(registerBtn, g);

        registerBtn.addActionListener(e -> {
            if (username.getText().trim().isEmpty() || email.getText().trim().isEmpty()
                    || password.getPassword().length == 0) {
                JOptionPane.showMessageDialog(mainP, "Please fill in all required fields.");
                return;
            }
            boolean ok = registerUser(
                    username.getText().trim(),
                    email.getText().trim(),
                    new String(password.getPassword()),
                    phone.getText().trim());
            if (ok) {
                Window w = SwingUtilities.getWindowAncestor(registerBtn);
                if (w != null)
                    w.dispose();
            }
        });

        mainP.add(header, BorderLayout.NORTH);
        mainP.add(form, BorderLayout.CENTER);
        return mainP;
    }

    private boolean registerUser(String username, String email, String password, String phone) {
        String hashed = hashPassword(password);
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users(USERNAME, EMAIL, PASSWORD, PHONE) VALUES(?,?,?,?)")) {

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, hashed);
            ps.setString(4, phone);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.");
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage());
            return false;
        }
    }

    // ── Dashboard Screen
    // ──────────────────────────────────────────────────────────

    private JPanel createDashboardScreen() {
        JPanel mainDash = new JPanel(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_BLUE);
        header.setPreferredSize(new Dimension(1200, 65));

        JLabel titleLabel = new JLabel("SwiftStore Pro", JLabel.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.add(titleLabel, BorderLayout.CENTER);

        JButton logout = createStyledButton("<- Logout", Color.WHITE, PRIMARY_BLUE);
        logout.addActionListener(e -> {
            sessionUserId = -1;
            sessionUsername = "";
            cartItems.clear();
            updateCartCount();
            cardLayout.show(mainPanel, "LOGIN");
        });

        searchField = new JTextField(25);
        JButton sBtn = new JButton("Search");
        sBtn.addActionListener(e -> filterProducts(searchField.getText(), null));
        searchField.addActionListener(e -> sBtn.doClick());

        JPanel sBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        sBar.setOpaque(false);
        sBar.add(searchField);
        sBar.add(sBtn);

        JPanel userA = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        userA.setOpaque(false);
        cartCountLabel = new JLabel("CART (0)");
        cartCountLabel.setForeground(Color.WHITE);
        JButton cBtn = createStyledButton("MY CART", Color.WHITE, PRIMARY_BLUE);
        JButton oBtn = createStyledButton("MY ORDERS", Color.WHITE, PRIMARY_BLUE);
        userA.add(cartCountLabel);
        userA.add(cBtn);
        userA.add(oBtn);

        header.add(logout, BorderLayout.WEST);
        header.add(sBar, BorderLayout.CENTER);
        header.add(userA, BorderLayout.EAST);

        JPanel catBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        catBar.setBackground(Color.WHITE);
        String[] cats = { "All", "Mobiles", "Electronics", "Fashion", "Beauty", "Home", "Toys", "Books" };
        for (String cat : cats) {
            JButton b = new JButton(cat);
            b.setFont(new Font("SansSerif", Font.BOLD, 12));
            b.setContentAreaFilled(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> filterProducts("", cat.equals("All") ? null : cat));
            catBar.add(b);
        }

        dashboardContent = new JPanel(new GridLayout(0, 4, 15, 15));
        dashboardContent.setBackground(BG_COLOR);
        dashboardContent.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scroll = new JScrollPane(dashboardContent);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        int limit = Math.min(40, allProducts.size());
        if (limit > 0)
            refreshProductGrid(allProducts.subList(0, limit));

        mainDash.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout());
        body.add(catBar, BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);
        mainDash.add(body, BorderLayout.CENTER);

        cBtn.addActionListener(e -> {
            updateCartUI();
            cardLayout.show(mainPanel, "CART");
        });
        oBtn.addActionListener(e -> {
            updateOrdersUI();
            cardLayout.show(mainPanel, "ORDERS");
        });

        return mainDash;
    }

    private void filterProducts(String q, String c) {
        List<Product> f = allProducts.stream()
                .filter(p -> p.name.toLowerCase().contains(q.toLowerCase())
                        && (c == null || p.category.equals(c)))
                .limit(100)
                .collect(Collectors.toList());
        refreshProductGrid(f);
    }

    private void refreshProductGrid(List<Product> list) {
        dashboardContent.removeAll();
        for (Product p : list) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setBackground(Color.WHITE);
            card.setBorder(new LineBorder(new Color(230, 230, 230)));

            JLabel info = new JLabel("<html><div style='padding:10px;'><b>" + p.name + "</b><br>"
                    + "<font color='gray'>" + p.specs + "</font></div></html>");

            JLabel priceRating = new JLabel(
                    "  \u20B9" + (int) p.price + "  |  \u2605 " + String.format("%.1f", p.rating));
            priceRating.setForeground(SUCCESS_GREEN);
            priceRating.setFont(new Font("SansSerif", Font.BOLD, 13));

            JButton buy = createStyledButton("BUY NOW", Color.WHITE, SECONDARY_ORANGE);
            JButton add = createStyledButton("ADD TO CART", Color.WHITE, Color.BLACK);

            buy.addActionListener(e -> {
                itemsToBuy = new ArrayList<>();
                itemsToBuy.add(new CartItem(p, 1));
                cardLayout.show(mainPanel, "CHECKOUT_ADDR");
            });
            add.addActionListener(e -> addToCart(p));

            JPanel btns = new JPanel(new GridLayout(1, 2, 2, 0));
            btns.add(add);
            btns.add(buy);

            JPanel bot = new JPanel(new BorderLayout());
            bot.add(priceRating, BorderLayout.NORTH);
            bot.add(btns, BorderLayout.SOUTH);

            card.add(info, BorderLayout.CENTER);
            card.add(bot, BorderLayout.SOUTH);
            dashboardContent.add(card);
        }
        dashboardContent.revalidate();
        dashboardContent.repaint();
    }

    // ── Cart Screen
    // ───────────────────────────────────────────────────────────────

    private JPanel createCartScreen() {
        cartPanel = new JPanel(new BorderLayout());
        return cartPanel;
    }

    private void updateCartUI() {
        cartPanel.removeAll();

        JButton back = createStyledButton("<- BACK TO SHOPPING", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "DASHBOARD"));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        double total = 0;
        for (CartItem i : cartItems) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            row.setBackground(Color.WHITE);
            row.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

            JLabel nameLabel = new JLabel("<html><b>" + i.product.name + "</b></html>");
            nameLabel.setPreferredSize(new Dimension(300, 30));

            JButton minus = createStyledButton("-", Color.BLACK, new Color(220, 220, 220));
            minus.setPreferredSize(new Dimension(40, 30));
            JLabel qty = new JLabel(String.valueOf(i.quantity), SwingConstants.CENTER);
            qty.setPreferredSize(new Dimension(30, 30));
            JButton plus = createStyledButton("+", Color.BLACK, new Color(220, 220, 220));
            plus.setPreferredSize(new Dimension(40, 30));

            JLabel priceLabel = new JLabel("\u20B9" + (int) (i.product.price * i.quantity));
            priceLabel.setPreferredSize(new Dimension(100, 30));
            priceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            minus.addActionListener(e -> {
                if (i.quantity > 1)
                    i.quantity--;
                else
                    cartItems.remove(i);
                updateCartUI();
                updateCartCount();
            });
            plus.addActionListener(e -> {
                i.quantity++;
                updateCartUI();
                updateCartCount();
            });

            JButton rem = createStyledButton("REMOVE", Color.WHITE, ERROR_RED);
            rem.addActionListener(e -> {
                cartItems.remove(i);
                updateCartUI();
                updateCartCount();
            });

            row.add(nameLabel);
            row.add(minus);
            row.add(qty);
            row.add(plus);
            row.add(new JLabel("    "));
            row.add(priceLabel);
            row.add(rem);

            listPanel.add(row);
            total += i.product.price * i.quantity;
        }

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel totalDisp = new JLabel("TOTAL PAYABLE: \u20B9 " + (int) total);
        totalDisp.setFont(new Font("SansSerif", Font.BOLD, 18));

        JButton checkout = createStyledButton("PROCEED TO CHECKOUT", Color.WHITE, SUCCESS_GREEN);
        checkout.setPreferredSize(new Dimension(200, 40));
        checkout.addActionListener(e -> {
            if (!cartItems.isEmpty()) {
                itemsToBuy = new ArrayList<>(cartItems);
                cardLayout.show(mainPanel, "CHECKOUT_ADDR");
            }
        });

        footer.add(totalDisp, BorderLayout.WEST);
        footer.add(checkout, BorderLayout.EAST);

        cartPanel.add(back, BorderLayout.NORTH);
        cartPanel.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        cartPanel.add(footer, BorderLayout.SOUTH);
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    // ── Address Screen
    // ────────────────────────────────────────────────────────────

    private JPanel createAddressScreen() {
        JPanel p = new JPanel(new BorderLayout());

        JButton back = createStyledButton("<- BACK", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "CART"));

        JPanel header = new JPanel(new BorderLayout());
        header.add(back, BorderLayout.WEST);
        JLabel title = new JLabel("Delivery Address", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(title, BorderLayout.CENTER);
        header.add(new JLabel("  "), BorderLayout.EAST);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(new EmptyBorder(30, 50, 30, 50));

        JTextField n = new JTextField();
        JTextField m = new JTextField();
        JTextField d = new JTextField();
        JTextField s = new JTextField();
        JTextField c = new JTextField();
        JTextField di = new JTextField();

        form.add(new JLabel("Full Name:"));
        form.add(n);
        form.add(new JLabel("Mobile Number:"));
        form.add(m);
        form.add(new JLabel("Flat/Door No:"));
        form.add(d);
        form.add(new JLabel("Street/Area:"));
        form.add(s);
        form.add(new JLabel("City:"));
        form.add(c);
        form.add(new JLabel("District:"));
        form.add(di);

        JButton next = createStyledButton("CONTINUE TO PAYMENT", Color.WHITE, SUCCESS_GREEN);
        next.addActionListener(e -> {
            if (n.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your full name.");
                return;
            }
            if (m.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your mobile number.");
                return;
            }
            if (d.getText().trim().isEmpty() || s.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your full address.");
                return;
            }
            if (c.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your city.");
                return;
            }
            if (di.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your district.");
                return;
            }
            custName = n.getText().trim();
            custMobile = m.getText().trim();
            fullAddress = d.getText().trim() + ", " + s.getText().trim();
            custCity = c.getText().trim();
            custDistrict = di.getText().trim();
            cardLayout.show(mainPanel, "CHECKOUT_PAY");
        });

        p.add(header, BorderLayout.NORTH);
        p.add(form, BorderLayout.CENTER);
        p.add(next, BorderLayout.SOUTH);
        return p;
    }

    // ── Payment Screen
    // ────────────────────────────────────────────────────────────

    private JPanel createPaymentScreen() {
        JPanel p = new JPanel(new BorderLayout());

        JButton back = createStyledButton("<- BACK", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "CHECKOUT_ADDR"));

        JPanel opts = new JPanel(new GridLayout(4, 1, 10, 10));
        opts.setBorder(new EmptyBorder(50, 100, 50, 100));

        JRadioButton r1 = new JRadioButton("UPI / Google Pay");
        JRadioButton r2 = new JRadioButton("Credit/Debit Card");
        JRadioButton r3 = new JRadioButton("Cash on Delivery", true);
        ButtonGroup bg = new ButtonGroup();
        bg.add(r1);
        bg.add(r2);
        bg.add(r3);

        opts.add(new JLabel("Select Payment Method:"));
        opts.add(r1);
        opts.add(r2);
        opts.add(r3);

        JButton btn = createStyledButton("VIEW ORDER SUMMARY", Color.WHITE, SECONDARY_ORANGE);
        btn.addActionListener(e -> {
            updateFinalUI();
            cardLayout.show(mainPanel, "CHECKOUT_CONFIRM");
        });

        p.add(back, BorderLayout.NORTH);
        p.add(opts, BorderLayout.CENTER);
        p.add(btn, BorderLayout.SOUTH);
        return p;
    }

    // ── Order Confirmation Screen
    // ─────────────────────────────────────────────────

    private JPanel createFinalConfirmationScreen() {
        checkoutConfirmPanel = new JPanel(new BorderLayout());
        return checkoutConfirmPanel;
    }

    private void updateFinalUI() {
        checkoutConfirmPanel.removeAll();

        JButton back = createStyledButton("<- BACK", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "CHECKOUT_PAY"));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        center.add(new JLabel("<html><h3>Confirm Order</h3>"
                + "<b>Deliver to:</b> " + custName + "<br>"
                + "<b>Address:</b> " + fullAddress + ", " + custCity + ", " + custDistrict + "<br>"
                + "<b>Mobile:</b> " + custMobile + "</html>"));

        double total = 0;
        for (CartItem i : itemsToBuy) {
            center.add(new JLabel("- " + i.product.name + " (x" + i.quantity + ")"));
            total += i.product.price * i.quantity;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 3 + new Random().nextInt(4));
        final String dbDeliveryDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        final String displayDeliveryDate = new SimpleDateFormat("EEEE, dd MMM").format(cal.getTime());

        center.add(new JLabel("<html><br><font color='#388e3c'><b>Estimated Delivery: "
                + displayDeliveryDate + "</b></font></html>"));

        final double finalTotal = total;
        center.add(new JLabel("<html><br><b>TOTAL: \u20B9" + (int) total + "</b></html>"));

        JButton place = createStyledButton("CONFIRM & PLACE ORDER", Color.WHITE, SUCCESS_GREEN);
        place.addActionListener(e -> {
            String orderId = "ORD" + (10000 + new Random().nextInt(90000));

            // INSERT uses user_id (int), not a username string
            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO orders(order_id, user_id, total, status, order_date, delivery_date)"
                                    + " VALUES(?, ?, ?, ?, NOW(), ?)")) {

                ps.setString(1, orderId);
                ps.setInt(2, sessionUserId); // ← int FK matching orders.user_id
                ps.setDouble(3, finalTotal);
                ps.setString(4, "Ordered");
                ps.setString(5, dbDeliveryDate);
                ps.executeUpdate();

                cartItems.removeAll(itemsToBuy);
                updateCartCount();
                JOptionPane.showMessageDialog(this,
                        "Order placed successfully!\nOrder ID: " + orderId);
                cardLayout.show(mainPanel, "DASHBOARD");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to place order: " + ex.getMessage());
            }
        });

        checkoutConfirmPanel.add(back, BorderLayout.NORTH);
        checkoutConfirmPanel.add(center, BorderLayout.CENTER);
        checkoutConfirmPanel.add(place, BorderLayout.SOUTH);
        checkoutConfirmPanel.revalidate();
    }

    // ── Orders Screen
    // ─────────────────────────────────────────────────────────────

    private JPanel createOrdersScreen() {
        ordersPanel = new JPanel(new BorderLayout());
        return ordersPanel;
    }

    private void updateOrdersUI() {
        ordersPanel.removeAll();

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);

        orderHistory.clear();

        // SELECT uses user_id (int), not a username string
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC")) {

            ps.setInt(1, sessionUserId); // ← int FK matching orders.user_id

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    String formattedDate;
                    try {
                        java.util.Date parsed = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .parse(rs.getString("order_date"));
                        formattedDate = new SimpleDateFormat("dd MMM, HH:mm").format(parsed);
                    } catch (Exception ex) {
                        formattedDate = rs.getString("order_date");
                    }

                    String displayDelivery;
                    try {
                        java.util.Date parsed = new SimpleDateFormat("yyyy-MM-dd")
                                .parse(rs.getString("delivery_date"));
                        displayDelivery = new SimpleDateFormat("EEEE, dd MMM").format(parsed);
                    } catch (Exception ex) {
                        displayDelivery = rs.getString("delivery_date");
                    }

                    orderHistory.add(new Order(
                            rs.getString("order_id"),
                            rs.getString("status"),
                            new ArrayList<>(),
                            rs.getDouble("total"),
                            formattedDate,
                            displayDelivery));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (orderHistory.isEmpty()) {
            JLabel empty = new JLabel("No orders found.", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 16));
            empty.setForeground(Color.GRAY);
            listContainer.add(empty);
        }

        for (Order o : orderHistory) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setMaximumSize(new Dimension(1200, 180));
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(
                    new EmptyBorder(10, 10, 10, 10),
                    new LineBorder(Color.LIGHT_GRAY)));

            JPanel left = new JPanel(new GridLayout(3, 1));
            left.setOpaque(false);
            left.add(new JLabel("Order ID: #" + o.id
                    + "  |  Date: " + o.date
                    + "  |  Total: \u20B9" + (int) o.total));

            JLabel delDate = new JLabel("Arriving by: " + o.deliveryDate);
            delDate.setForeground(SUCCESS_GREEN);
            delDate.setFont(new Font("SansSerif", Font.BOLD, 12));
            left.add(delDate);

            JPanel tracker = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            tracker.setOpaque(false);
            String[] steps = { "Ordered", "Packed", "Shipped", "Delivered" };
            for (String step : steps) {
                JLabel lbl = new JLabel(step);
                if (o.status.equals("Cancelled")) {
                    lbl.setForeground(Color.LIGHT_GRAY);
                } else if (step.equals(o.status)) {
                    lbl.setForeground(SUCCESS_GREEN);
                    lbl.setText("\u25CF " + step);
                    lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                tracker.add(lbl);
            }
            left.add(tracker);

            JPanel right = new JPanel(new BorderLayout());
            right.setOpaque(false);

            if (!o.status.equals("Cancelled")) {
                JButton cancel = createStyledButton("CANCEL", Color.WHITE, ERROR_RED);
                cancel.addActionListener(e -> {
                    try (Connection conn = DBConnection.getConnection();
                            PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE orders SET status='Cancelled' WHERE order_id=?")) {
                        ps.setString(1, o.id);
                        ps.executeUpdate();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    o.status = "Cancelled";
                    updateOrdersUI();
                });
                right.add(cancel, BorderLayout.CENTER);
            } else {
                JLabel cl = new JLabel("CANCELLED");
                cl.setForeground(ERROR_RED);
                right.add(cl, BorderLayout.CENTER);
            }

            card.add(left, BorderLayout.CENTER);
            card.add(right, BorderLayout.EAST);
            listContainer.add(card);
        }

        JButton back = createStyledButton("<- BACK TO DASHBOARD", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "DASHBOARD"));

        ordersPanel.add(back, BorderLayout.NORTH);
        ordersPanel.add(new JScrollPane(listContainer), BorderLayout.CENTER);
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }

    // ── Helpers
    // ───────────────────────────────────────────────────────────────────

    private void addToCart(Product p) {
        for (CartItem item : cartItems) {
            if (item.product.id.equals(p.id)) {
                item.quantity++;
                updateCartCount();
                JOptionPane.showMessageDialog(this, "Increased quantity of " + p.name);
                return;
            }
        }
        cartItems.add(new CartItem(p, 1));
        updateCartCount();
        JOptionPane.showMessageDialog(this, p.name + " added to cart!");
    }

    private void updateCartCount() {
        int total = cartItems.stream().mapToInt(i -> i.quantity).sum();
        cartCountLabel.setText("CART (" + total + ")");
    }

    private JButton createStyledButton(String text, Color fg, Color bg) {
        JButton btn = new JButton(text);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Inner Classes
    // ─────────────────────────────────────────────────────────────

    class Product {
        String id, name, category, specs, brand;
        double price, rating;

        Product(String id, String name, String category, double price,
                String specs, double rating, String brand) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.specs = specs;
            this.rating = rating;
            this.brand = brand;
        }
    }

    class CartItem {
        Product product;
        int quantity;

        CartItem(Product p, int q) {
            this.product = p;
            this.quantity = q;
        }
    }

    class Order {
        String id, status, date, deliveryDate;
        List<CartItem> items;
        double total;

        Order(String id, String status, List<CartItem> items,
                double total, String date, String deliveryDate) {
            this.id = id;
            this.status = status;
            this.items = items;
            this.total = total;
            this.date = date;
            this.deliveryDate = deliveryDate;
        }
    }

    // ── Entry Point
    // ───────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JAVAAPP().setVisible(true));
    }
}
