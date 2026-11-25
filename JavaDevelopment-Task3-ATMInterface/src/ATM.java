import java.util.Scanner;

public class ATM {
    private final Bank bank;
    private final Scanner scanner;

    public ATM(Bank bank) {
        this.bank = bank;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=================================");
        System.out.println("      WELCOME TO OIBSIP ATM      ");
        System.out.println("=================================");
        System.out.println("Demo Logins -> ID: 1001 | PIN: 1234  OR  ID: 1002 | PIN: 5678");

        while (true) {
            System.out.print("\nEnter User ID (or 'exit' to close): ");
            String userId = scanner.nextLine().trim();
            if (userId.equalsIgnoreCase("exit")) {
                System.out.println("ATM session closed.");
                break;
            }

            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine().trim();

            Account currentAccount = bank.authenticate(userId, pin);
            if (currentAccount != null) {
                System.out.println("\nLogin successful. Welcome, " + currentAccount.getHolderName() + "!");
                showMenu(currentAccount);
            } else {
                System.out.println("Invalid User ID or PIN. Try again.");
            }
        }
    }

   private void showMenu(Account account) {
        while (true) {
            System.out.println("\n--- ATM MENU ---");
            System.out.println("1. Transactions History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Quit");
            System.out.print("Choose an option (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    account.printTransactionHistory();
                    break;
                case "2":
                    handleWithdraw(account);
                    break;
                case "3":
                    handleDeposit(account);
                    break;
                case "4":
                    handleTransfer(account);
                    break;
                case "5":
                    System.out.println("Logged out. Thank you for using OIBSIP ATM!");
                    return;
                default:
                    System.out.println("Invalid choice. Enter a number between 1 and 5.");
            }
        }
    }
private void handleWithdraw(Account account) {
        double amount = readAmount("Enter amount to withdraw: Rs. ");
        if (amount <= 0) return;

        if (account.withdraw(amount)) {
            System.out.printf("Withdrawn Rs. %.2f | Remaining Balance: Rs. %.2f%n", amount, account.getBalance());
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    private void handleDeposit(Account account) {
        double amount = readAmount("Enter amount to deposit: Rs. ");
        if (amount <= 0) return;

        account.deposit(amount);
        System.out.printf("Deposited Rs. %.2f | Updated Balance: Rs. %.2f%n", amount, account.getBalance());
    }

    private void handleTransfer(Account account) {
        System.out.print("Enter recipient User ID: ");
        String targetId = scanner.nextLine().trim();

        if (targetId.equals(account.getUserId())) {
            System.out.println("Cannot transfer to your own account.");
            return;
        }

        Account recipient = bank.findAccount(targetId);
        if (recipient == null) {
            System.out.println("Recipient User ID not found.");
            return;
        }

        double amount = readAmount("Enter amount to transfer to " + recipient.getHolderName() + ": Rs. ");
        if (amount <= 0) return;

        if (account.transfer(recipient, amount)) {
            System.out.printf("Transferred Rs. %.2f to %s | Remaining Balance: Rs. %.2f%n",
                    amount, recipient.getHolderName(), account.getBalance());
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    private double readAmount(String prompt) {
        System.out.print(prompt);
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
            return -1;
        }
    }
   
}