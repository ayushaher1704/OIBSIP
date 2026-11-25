import java.util.HashMap;
import java.util.Map;

public class Bank {
    private final Map<String, Account> accounts = new HashMap<>();

    public Bank() {
        accounts.put("1001", new Account("1001", "1234", "Aarav Kulkarni", 25000.00));
        accounts.put("1002", new Account("1002", "5678", "Priya Nair", 15000.00));
    }

    public Account authenticate(String userId, String pin) {
        Account account = accounts.get(userId);
        if (account != null && account.validatePin(pin)) {
            return account;
        }
        return null;
    }

    public Account findAccount(String userId) {
        return accounts.get(userId);
    }
}