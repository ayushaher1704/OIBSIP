
import java.util.ArrayList;
import java.util.List;

public class Account {

    private String userId;
    private String pin;
    private String holderName;
    private double balance;

    private List<Transaction> transactions;
    public static final double MAX_BALANCE = 5_000_000.00;   // Rs. 50 Lakh max account limit
    public static final double MAX_SINGLE_TX = 500_000.00;   // Rs. 5 Lakh max per transaction

public Account(String userId, String pin, String holderName, double initialBalance) {

    this.userId = userId;
    this.pin = pin;
    this.holderName = holderName;
    this.balance = initialBalance;

    this.transactions = new ArrayList<>();

    if (initialBalance > 0) {
        transactions.add(
            new Transaction("OPENING", initialBalance, "Initial account balance")
        );
    }
}
public String getUserId() {
        return userId;
    }

    public String getHolderName() {
        return holderName;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

   public boolean deposit(double amount) {
        if (amount <= 0 || amount > MAX_SINGLE_TX || (balance + amount) > MAX_BALANCE) {
            return false;
        }
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount, "Cash deposit"));
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > MAX_SINGLE_TX || amount > balance) {
            return false;
        }
        balance -= amount;
        transactions.add(new Transaction("WITHDRAW", amount, "Cash withdrawal"));
        return true;
    }

    public boolean transfer(Account recipient, double amount) {
        if (amount <= 0 || amount > MAX_SINGLE_TX || amount > balance) {
            return false;
        }
        if ((recipient.balance + amount) > MAX_BALANCE) {
            return false;
        }
        this.balance -= amount;
        recipient.balance += amount;
        this.transactions.add(new Transaction("TRANSFER OUT", amount, "To User ID: " + recipient.getUserId()));
        recipient.transactions.add(new Transaction("TRANSFER IN", amount, "From User ID: " + this.userId));
        return true;
    }
    public void printTransactionHistory() {
        System.out.println("\n--- Transaction History (" + holderName + ") ---");
        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded.");
        } else {
            for (Transaction tx : transactions) {
                System.out.println(tx);
            }
        }
        System.out.printf("Current Balance: Rs. %.2f%n", balance);
    }

}


