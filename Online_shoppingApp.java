import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

/**
 * ECOMMERCE WEB APPLICATIONS
 * Features: Quantity Controls, Search, Categories, and Fixed Delivery Date
 * Tracking.
 */
public class OnlineShoppingApp extends JFrame {

    // Theme Colors
    private final Color PRIMARY_BLUE = new Color(40, 116, 240);
    private final Color SECONDARY_ORANGE = new Color(251, 100, 27);
    private final Color SUCCESS_GREEN = new Color(56, 142, 60);
    private final Color ERROR_RED = new Color(211, 47, 47);
    private final Color BG_COLOR = new Color(241, 243, 246);

    // Logic State
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private List<Product> allProducts;
    private List<CartItem> cartItems = new ArrayList<>();
    private List<CartItem> itemsToBuy = new ArrayList<>();
    private List<Order> orderHistory = new ArrayList<>();

    // User Session
    private String custName = "", custMobile = "", fullAddress = "", custCity = "", custDistrict = "";

    // UI Components
    private JPanel dashboardContent, cartPanel, ordersPanel, checkoutConfirmPanel;
    private JLabel cartCountLabel;
    private JTextField searchField;

    public OnlineShoppingApp() {
        setTitle("SwiftStore Pro - Ultimate Shopping Mall");
        setSize(1300, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initDatabase();

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Register All Screens
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

    private void initDatabase() {
        allProducts = new ArrayList<>();
        String[] categories = { "Mobiles", "Electronics", "Fashion", "Beauty", "Home", "Toys", "Books" };
        Random rand = new Random();

        for (String cat : categories) {
            for (int i = 1; i <= 1000; i++) {
                String name, specs, brand;
                double rating = 3.2 + (rand.nextDouble() * 1.8);
                double price = 150 + rand.nextInt(95000);

                if (cat.equals("Mobiles")) {
                    brand = new String[] { "Apple", "Samsung", "OnePlus", "Google" }[rand.nextInt(4)];
                    name = brand + " Smartphone X" + i;
                    specs = (8 + rand.nextInt(8)) + "GB RAM | " + (128 * (rand.nextInt(3) + 1)) + "GB ROM";
                } else if (cat.equals("Books")) {
                    brand = "Publishing";
                    name = "The " + new String[] { "Fiction", "History", "Science" }[rand.nextInt(3)] + " Tales Vol."
                            + i;
                    specs = "Best Seller | Hardcover Edition";
                    price = 200 + rand.nextInt(2000);
                } else {
                    brand = cat + " Brand";
                    name = cat + " Premium Item #" + i;
                    specs = "Premium Quality " + cat;
                }
                allProducts.add(new Product("ID" + cat.charAt(0) + i, name, cat, price, specs, rating, brand));
            }
        }
    }

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
        g.fill = 2;
        g.insets = new Insets(10, 20, 10, 20);
        JTextField u = new JTextField(15);
        JPasswordField p = new JPasswordField(15);
        JButton l = createStyledButton("LOGIN", Color.WHITE, SECONDARY_ORANGE);
        g.gridy = 0;
        form.add(new JLabel("Username (any)"), g);
        g.gridy = 1;
        form.add(u, g);
        g.gridy = 2;
        form.add(new JLabel("Password"), g);
        g.gridy = 3;
        form.add(p, g);
        g.gridy = 4;
        form.add(l, g);

        l.addActionListener(e -> cardLayout.show(mainPanel, "DASHBOARD"));
        card.add(blueHeader, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        container.add(card);
        return container;
    }

    private JPanel createDashboardScreen() {
        JPanel mainDash = new JPanel(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_BLUE);
        header.setPreferredSize(new Dimension(1200, 65));

        JButton logout = createStyledButton("<- Logout", Color.WHITE, PRIMARY_BLUE);
        logout.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

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

        refreshProductGrid(allProducts.subList(0, 40));

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
                .filter(p -> (p.name.toLowerCase().contains(q.toLowerCase())) && (c == null || p.category.equals(c)))
                .limit(100).collect(Collectors.toList());
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

    private JPanel createCartScreen() {
        cartPanel = new JPanel(new BorderLayout());
        return cartPanel;
    }

    private void updateCartUI() {
        cartPanel.removeAll();
        JButton b = createStyledButton("<- BACK TO SHOPPING", Color.WHITE, PRIMARY_BLUE);
        b.addActionListener(e -> cardLayout.show(mainPanel, "DASHBOARD"));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        double total = 0;
        for (CartItem i : cartItems) {
            JPanel r = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            r.setBackground(Color.WHITE);
            r.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

            JLabel nameLabel = new JLabel("<html><b>" + i.product.name + "</b></html>");
            nameLabel.setPreferredSize(new Dimension(300, 30));

            // Quantity Controls
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
                if (i.quantity > 1) {
                    i.quantity--;
                } else {
                    cartItems.remove(i);
                }
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

            r.add(nameLabel);
            r.add(minus);
            r.add(qty);
            r.add(plus);
            r.add(new JLabel("    "));
            r.add(priceLabel);
            r.add(rem);

            listPanel.add(r);
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

        cartPanel.add(b, BorderLayout.NORTH);
        cartPanel.add(new JScrollPane(listPanel), BorderLayout.CENTER);
        cartPanel.add(footer, BorderLayout.SOUTH);
        cartPanel.revalidate();
        cartPanel.repaint();
    }

    private JPanel createAddressScreen() {
        JPanel p = new JPanel(new BorderLayout());

        // BACK BUTTON
        JButton back = createStyledButton("<- BACK", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "CART"));

        JPanel header = new JPanel(new BorderLayout());
        header.add(back, BorderLayout.WEST);
        JLabel title = new JLabel("Delivery Address", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(title, BorderLayout.CENTER);
        header.add(new JLabel("          "), BorderLayout.EAST); // Spacer

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(new EmptyBorder(30, 50, 30, 50));
        JTextField n = new JTextField(), m = new JTextField(), d = new JTextField(), s = new JTextField(),
                c = new JTextField(), di = new JTextField();

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
            if (n.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter name");
                return;
            }
            custName = n.getText();
            custMobile = m.getText();
            fullAddress = d.getText() + ", " + s.getText();
            custCity = c.getText();
            custDistrict = di.getText();
            cardLayout.show(mainPanel, "CHECKOUT_PAY");
        });

        p.add(header, BorderLayout.NORTH);
        p.add(form, BorderLayout.CENTER);
        p.add(next, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createPaymentScreen() {
        JPanel p = new JPanel(new BorderLayout());

        // BACK BUTTON
        JButton back = createStyledButton("<- BACK", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "CHECKOUT_ADDR"));

        JPanel opts = new JPanel(new GridLayout(4, 1, 10, 10));
        opts.setBorder(new EmptyBorder(50, 100, 50, 100));
        JRadioButton r1 = new JRadioButton("UPI / Google Pay"), r2 = new JRadioButton("Credit/Debit Card"),
                r3 = new JRadioButton("Cash on Delivery", true);
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

    private JPanel createFinalConfirmationScreen() {
        checkoutConfirmPanel = new JPanel(new BorderLayout());
        return checkoutConfirmPanel;
    }

    private void updateFinalUI() {
        checkoutConfirmPanel.removeAll();

        // BACK BUTTON
        JButton back = createStyledButton("<- BACK", Color.WHITE, PRIMARY_BLUE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "CHECKOUT_PAY"));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        center.add(new JLabel("<html><h3>Confirm Order</h3><br><b>Deliver to:</b> " + custName + "</html>"));

        double total = 0;
        for (CartItem i : itemsToBuy) {
            center.add(new JLabel("- " + i.product.name + " (x" + i.quantity + ")"));
            total += i.product.price * i.quantity;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 3 + new Random().nextInt(4));
        final String officialDeliveryDate = new SimpleDateFormat("EEEE, dd MMM").format(cal.getTime());

        center.add(new JLabel("<html><br><font color='#388e3c'><b>Estimated Delivery: " + officialDeliveryDate
                + "</b></font></html>"));

        final double finalTotal = total;
        center.add(new JLabel("<html><br><b>TOTAL: \u20B9" + (int) total + "</b></html>"));

        JButton place = createStyledButton("CONFIRM & PLACE ORDER", Color.WHITE, SUCCESS_GREEN);
        place.addActionListener(e -> {
            orderHistory.add(0, new Order("ORD" + (10000 + new Random().nextInt(90000)), "Ordered",
                    new ArrayList<>(itemsToBuy), finalTotal, officialDeliveryDate));
            cartItems.removeAll(itemsToBuy);
            updateCartCount();
            JOptionPane.showMessageDialog(this, "Order Placed Successfully!");
            cardLayout.show(mainPanel, "DASHBOARD");
        });

        checkoutConfirmPanel.add(back, BorderLayout.NORTH);
        checkoutConfirmPanel.add(center, BorderLayout.CENTER);
        checkoutConfirmPanel.add(place, BorderLayout.SOUTH);
        checkoutConfirmPanel.revalidate();
    }

    private JPanel createOrdersScreen() {
        ordersPanel = new JPanel(new BorderLayout());
        return ordersPanel;
    }

    private void updateOrdersUI() {
        ordersPanel.removeAll();
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_COLOR);

        for (Order o : orderHistory) {
            JPanel card = new JPanel(new BorderLayout(10, 10));
            card.setMaximumSize(new Dimension(1200, 180));
            card.setBackground(Color.WHITE);
            card.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), new LineBorder(Color.LIGHT_GRAY)));

            JPanel left = new JPanel(new GridLayout(3, 1));
            left.setOpaque(false);
            left.add(new JLabel("Order ID: #" + o.id + " | Date: " + o.date));

            JLabel delDate = new JLabel("Arriving by: " + o.deliveryDate);
            delDate.setForeground(SUCCESS_GREEN);
            delDate.setFont(new Font("SansSerif", Font.BOLD, 12));
            left.add(delDate);

            JPanel tracker = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
            tracker.setOpaque(false);
            String[] steps = { "Ordered", "Packed", "Shipped", "Delivered" };
            for (String s : steps) {
                JLabel step = new JLabel(s);
                if (o.status.equals("Cancelled")) {
                    step.setForeground(Color.LIGHT_GRAY);
                } else if (s.equals(o.status)) {
                    step.setForeground(SUCCESS_GREEN);
                    step.setText("\u25CF " + s);
                    step.setFont(new Font("SansSerif", Font.BOLD, 12));
                }
                tracker.add(step);
            }
            left.add(tracker);

            JPanel right = new JPanel(new BorderLayout());
            right.setOpaque(false);
            if (!o.status.equals("Cancelled")) {
                JButton cancel = createStyledButton("CANCEL", Color.WHITE, ERROR_RED);
                cancel.addActionListener(e -> {
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
        cartCountLabel.setText("CART (" + cartItems.size() + ")");
    }

    private JButton createStyledButton(String t, Color f, Color b) {
        JButton btn = new JButton(t);
        btn.setForeground(f);
        btn.setBackground(b);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    class Product {
        String id, name, category, specs, brand;
        double price, rating;

        Product(String i, String n, String c, double p, String s, double r, String b) {
            id = i;
            name = n;
            category = c;
            price = p;
            specs = s;
            rating = r;
            brand = b;
        }
    }

    class CartItem {
        Product product;
        int quantity;

        CartItem(Product p, int q) {
            product = p;
            quantity = q;
        }
    }

    class Order {
        String id, status, date, deliveryDate;
        List<CartItem> items;
        double total;

        Order(String i, String s, List<CartItem> items, double total, String fixedDate) {
            id = i;
            status = s;
            this.items = items;
            this.total = total;
            this.date = new SimpleDateFormat("dd MMM, HH:mm").format(new Date());
            this.deliveryDate = fixedDate;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new OnlineShoppingApp().setVisible(true));
    }
}
