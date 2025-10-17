package example.vendor.payment;

import example.vendor.a.Payment;

public class AGateway implements PaymentGateway {

    private final Payment client = new Payment();

    @Override
    public String pay(int amount, String currency) {
        return client.charge(amount, currency);
    }

}