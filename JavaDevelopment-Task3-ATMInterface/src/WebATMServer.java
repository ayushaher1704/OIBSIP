import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebATMServer {
    private static final Bank bank = new Bank();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve the HTML page
        server.createContext("/", exchange -> {
            Path htmlPath = Files.exists(Path.of("../web/index.html"))
                    ? Path.of("../web/index.html")
                    : Path.of("web/index.html");
            byte[] html = Files.readAllBytes(htmlPath);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html);
            }
        });

        // Login endpoint
        server.createContext("/api/login", exchange -> {
            Map<String, String> form = parseForm(exchange);
            Account acc = bank.authenticate(form.get("userId"), form.get("pin"));
            if (acc != null) {
                sendJson(exchange, accountJson(acc, true, "Login successful"));
            } else {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Invalid User ID or PIN\"}");
            }
        });

        // Deposit endpoint
        server.createContext("/api/deposit", exchange -> {
            Map<String, String> form = parseForm(exchange);
            Account acc = bank.findAccount(form.get("userId"));
            double amount = parseAmount(form.get("amount"));
            if (acc == null) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Session expired. Please login again.\"}");
            } else if (amount <= 0) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Enter a valid positive amount.\"}");
            } else {
                acc.deposit(amount);
                sendJson(exchange, accountJson(acc, true, String.format(java.util.Locale.US, "Deposited Rs. %.2f", amount)));
            }
        });

        // Withdraw endpoint
        server.createContext("/api/withdraw", exchange -> {
            Map<String, String> form = parseForm(exchange);
            Account acc = bank.findAccount(form.get("userId"));
            double amount = parseAmount(form.get("amount"));
            if (acc == null) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Session expired. Please login again.\"}");
            } else if (amount <= 0) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Enter a valid positive amount.\"}");
            } else if (acc.withdraw(amount)) {
                sendJson(exchange, accountJson(acc, true, String.format(java.util.Locale.US, "Withdrawn Rs. %.2f", amount)));
            } else {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Insufficient balance.\"}");
            }
        });

        // Transfer endpoint
        server.createContext("/api/transfer", exchange -> {
            Map<String, String> form = parseForm(exchange);
            Account acc = bank.findAccount(form.get("userId"));
            String toId = form.getOrDefault("toId", "").trim();
            double amount = parseAmount(form.get("amount"));
            if (acc == null) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Session expired. Please login again.\"}");
            } else if (toId.equals(acc.getUserId())) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Cannot transfer to your own account.\"}");
            } else if (bank.findAccount(toId) == null) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Recipient User ID not found.\"}");
            } else if (amount <= 0) {
                sendJson(exchange, "{\"ok\":false,\"message\":\"Enter a valid positive amount.\"}");
            } else {
                Account recipient = bank.findAccount(toId);
                if (acc.transfer(recipient, amount)) {
                    sendJson(exchange, accountJson(acc, true, String.format(java.util.Locale.US, "Transferred Rs. %.2f to %s", amount, recipient.getHolderName())));
                } else {
                    sendJson(exchange, "{\"ok\":false,\"message\":\"Insufficient balance.\"}");
                }
            }
        });

        server.start();
        System.out.println("ATM Web Portal running at: http://localhost:8080");
    }

    private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String val = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private static double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return -1;
        try {
            double val = Double.parseDouble(raw.trim());
            return (Double.isFinite(val) && val > 0) ? val : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String accountJson(Account acc, boolean ok, String message) {
        StringBuilder historyJson = new StringBuilder("[");
        List<Transaction> list = acc.getTransactions();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) historyJson.append(",");
            historyJson.append("\"").append(escapeJson(list.get(i).toString())).append("\"");
        }
        historyJson.append("]");

        return String.format(java.util.Locale.US,
                "{\"ok\":%b,\"message\":\"%s\",\"userId\":\"%s\",\"holderName\":\"%s\",\"balance\":\"%.2f\",\"history\":%s}",
                ok, escapeJson(message), escapeJson(acc.getUserId()), escapeJson(acc.getHolderName()), acc.getBalance(), historyJson);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    
}