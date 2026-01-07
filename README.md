# حل الواجب العملي – GL

## ① تحليل الكود الحالي (Code Analysis)
النظام الحالي يقوم بالوظائف التالية:  

- فتح حساب (Open Account)  
- إيداع / سحب / تحويل الأموال (Deposit / Withdraw / Transfer)  
- إرسال إشعارات (Notification)  
- تسجيل العمليات (Logging)  
- تكرار الحساب (Duplicate Account)  

**ملاحظة:** كل هذه الوظائف موجودة داخل **صنف واحد فقط (BankSystem)**، مما يؤدي إلى صعوبة الصيانة والمشاكل التصميمية.

---

## ② تحديد المشاكل التصميمية (Design Problems)

1. **انتهاك مبدأ المسؤولية الواحدة (SRP)**  

BankSystem مسؤول عن:  
- إدارة الحسابات  
- منطق العمليات البنكية  
- الإشعارات  
- التسجيل (Logging)  
- إنشاء الحسابات  
- تكرار الحساب  

**الحل:** فصل المسؤوليات إلى أصناف مستقلة.

2. **تمثيل سيئ للبيانات**  

استخدام القوائم المتعددة:
```java
List<String> accountOwners;
List<Double> balances;
List<String> accountTypes;
```

يؤدي إلى أخطاء تزامن بين القوائم

صعوبة الصيانة

الحل: استخدام كائن Account لكل حساب.

 3.Singleton غير آمن
```java
public static BankSystem instance;
```


غير محمي من التعددية (Thread unsafe)


4. شروط if/else كثيرة (Violation OCP) 
```java

if (type.equals("SMS")) ...
else if (type.equals("EMAIL")) ...
```


عند إضافة نوع جديد يجب تعديل الكود
الحل: استخدام Strategy Pattern للإشعارات.

5.تكرار الحساب بدون نمط واضح

النسخ اليدوي للحسابات
الحل: استخدام Prototype Pattern.

③ إعادة التصميم باستعمال المبادئ والأنماط :

الانماط المستعملة النمط وسببه: 

Singleton	 نظام بنكي واحد

Factory Method  	إنشاء الحسابات

Strategy	  الإشعارات

Prototype	  تكرار الحساب

SOLID (SRP)  فصل المسؤوليات

④ النسخة المحسّنة من الكود
🔹 Account (نمط Prototype)
```java
public abstract class Account implements Cloneable {
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
        if (balance >= amount) balance -= amount;
        else System.out.println("Insufficient balance");
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
```

🔹 حسابات مختلفة
```java
public class StandardAccount extends Account {
    public StandardAccount(String owner, double balance) {
        super(owner, balance);
    }
}

public class PremiumAccount extends Account {
    public PremiumAccount(String owner, double balance) {
        super(owner, balance);
    }
}
```

🔹 Factory Method لإنشاء الحسابات
```java
public class AccountFactory {
    public static Account createAccount(String owner, double balance, boolean premium) {
        return premium ?
                new PremiumAccount(owner, balance) :
                new StandardAccount(owner, balance);
    }
}
```

🔹 Strategy للإشعارات
```java
public interface NotificationStrategy {
    void send(String message);
}

public class SMSNotification implements NotificationStrategy {
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}

public class EmailNotification implements NotificationStrategy {
    public void send(String message) {
        System.out.println("EMAIL: " + message);
    }
}
```

🔹 Logger (مسؤولية مستقلة)
```java
public class Logger {
    public static void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
}
```

🔹 BankSystem (Singleton + منطق بنكي فقط)
```java
import java.util.*;

public class BankSystem {

    private static BankSystem instance;
    private Map<String, Account> accounts = new HashMap<>();

    private BankSystem() {}

    public static BankSystem getInstance() {
        if (instance == null)
            instance = new BankSystem();
        return instance;
    }

    public void openAccount(String owner, double balance, boolean premium) {
        Account acc = AccountFactory.createAccount(owner, balance, premium);
        accounts.put(owner, acc);
        Logger.log("Account opened for " + owner);
    }

    public void deposit(String owner, double amount, NotificationStrategy notif) {
        Account acc = accounts.get(owner);
        if (acc != null) {
            acc.deposit(amount);
            notif.send(owner + " deposited " + amount);
        }
    }

    public void withdraw(String owner, double amount, NotificationStrategy notif) {
        Account acc = accounts.get(owner);
        if (acc != null) {
            acc.withdraw(amount);
            notif.send(owner + " withdrew " + amount);
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
            Account copy = acc.clone();
            accounts.put(owner + "_copy", copy);
        }
    }

    public void printAccounts() {
        accounts.forEach((k,v) ->
                System.out.println(k + " | " + v.getBalance()));
    }
}
```

🔹 Main
```java
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
```

