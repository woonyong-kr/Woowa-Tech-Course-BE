package example.vendor.payment;

public interface PaymentGateway {
    String pay(int amount, String currency);
}