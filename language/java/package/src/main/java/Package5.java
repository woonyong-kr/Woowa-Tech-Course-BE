import example.vendor.payment.*;

public class Package5 {
    public static void main(String[] args) {

        // (예제 5)
        // 같은 이름의 클래스를 피치 못해 함께 써야 하고 용도도 유사하다면,
        // 공통 인터페이스와 클래스별 래퍼(어댑터 패턴)로 추상화해 일관되게 사용하는 것이 하나의 방법이다.

        PaymentGateway a = new AGateway();
        PaymentGateway b = new BGateway();


        System.out.println(a.pay(4000, "KRW"));
        System.out.println(b.pay(4000, "KRW"));

    }
}
