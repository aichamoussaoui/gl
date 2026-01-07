import java.util.*;

abstract class Account implements Cloneable {
    protected String owner;
    protected double balance;

    public Account(String owner, double balance) {
        this.owner = owner;
        this.balance = balance;
    }

    public String getOwner() { return owner; }
    public double getBalance() { return balance; }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        if (balance >= amount)
            balance -= amount;
        else
            System.out.println("Insufficient balance");
    }

    @Override
    public Account clone() {
        try {
            return (Account) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

class StandardAccount extends Account {
    public StandardAccount(String owner, double balance) {
        super(owner, balance);
    }
}

class PremiumAccount extends Account {
    public PremiumAccount(String owner, double balance) {
        super(owner, balance);
    }
}

class AccountFactory {
    public static Account create(String owner, double balance, boolean premium) {
        return premium ?
                new PremiumAccount(owner, balance) :
                new StandardAccount(owner, balance);
    }
}

interface NotificationStrategy {
    void send(String message);
}

class SMSNotification implements NotificationStrategy {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

class EmailNotification implements NotificationStrategy {
    public void send(String message) {
        System.out.println("EMAIL: " + message);
    }
}

class Logger {
    public static void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}

class BankSystem {

    private static BankSystem instance;
    private Map<String, Account> accounts = new HashMap<>();

    private BankSystem() {
        System.out.println("Bank System Started");
    }

    public static BankSystem getInstance() {
        if (instance == null)
            instance = new BankSystem();
        return instance;
    }

    public void openAccount(String owner, double balance, boolean premium) {
        accounts.put(owner, AccountFactory.create(owner, balance, premium));
        Logger.log("Account opened for " + owner);
    }

    public void deposit(String owner, double amount, NotificationStrategy n) {
        Account acc = accounts.get(owner);
        if (acc != null) {
            acc.deposit(amount);
            n.send(owner + " deposited " + amount);
        }
    }

    public void withdraw(String owner, double amount, NotificationStrategy n) {
        Account acc = accounts.get(owner);
        if (acc != null) {
            acc.withdraw(amount);
            n.send(owner + " withdrew " + amount);
        }
    }

    public void transfer(String from, String to, double amount) {
        Account a = accounts.get(from);
        Account b = accounts.get(to);
        if (a != null && b != null && a.getBalance() >= amount) {
            a.withdraw(amount);
            b.deposit(amount);
            Logger.log("Transfer done");
        }
    }

    public void duplicateAccount(String owner) {
        Account acc = accounts.get(owner);
        if (acc != null) {
            accounts.put(owner + "_copy", acc.clone());
            Logger.log("Account duplicated for " + owner);
        }
    }

    public void printAccounts() {
        System.out.println("=== Accounts ===");
        accounts.forEach((k, v) ->
                System.out.println(k + " | " + v.getBalance()));
    }
}

// MAIN 
public class MainApp {
    public static void main(String[] args) {

        BankSystem bank = BankSystem.getInstance();

        bank.openAccount("Amine", 1000, false);
        bank.openAccount("Sara", 2000, true);

        bank.deposit("Amine", 300, new SMSNotification());
        bank.withdraw("Sara", 400, new EmailNotification());

        bank.transfer("Amine", "Sara", 200);
        bank.duplicateAccount("Sara");

        bank.printAccounts();
    }
}
