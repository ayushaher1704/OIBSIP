
import java.util.ArrayList;
import java.util.List;

public class Account {

    private String userId;
    private String pin;
    private String holderName;
    private double balance;

    private List<Transaction> transactions;

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

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public void deposit(double amount) {
        balance += amount;
        transactions.add(new Transaction("DEPOSIT", amount, "Cash deposit"));
    }

    public boolean withdraw(double amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        transactions.add(new Transaction("WITHDRAW", amount, "Cash withdrawal"));
        return true;
    }

    public boolean transfer(Account recipient, double amount) {
        if (amount > balance) {
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


