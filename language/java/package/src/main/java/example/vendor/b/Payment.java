package example.vendor.b;

public class Payment {

    public String charge(int amount, String currency) {
        return "[B] charged " + amount + " " + currency;
    }
}