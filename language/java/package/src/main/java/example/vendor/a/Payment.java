package example.vendor.a;

public class Payment {

    public String charge(int amount, String currency) {
        return "[A] charged " + amount + " " + currency;
    }
}