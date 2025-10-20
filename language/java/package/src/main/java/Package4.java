import example.vendor.a.Payment;
import example.vendor.b.*;

public class Package4 {
    public static void main(String[] args) {

        // (예제 4)
        // 서로 다른 패키지에 같은 이름의 클래스가 있을 때,
        // 단일 import와 와일드카드 import를 함께 선언하면 단일 import가 우선된다.
        // 이 경우 단일 import된 클래스는 단순 이름으로 사용할 수 있지만,
        // 와일드카드로 가져온 쪽은 정규 패키지명(FQCN)으로만 참조할 수 있다.

        // example.vendor.a와 example.vendor.b 모두 Payment가 있지만,
        // 단일 import가 우선이라 여기서 'Payment'는 a의 Payment만 가리킨다.
        // b의 Payment는 FQCN으로만 참조해야 한다.
        Payment a = new Payment();
        Payment b = new Payment();

        System.out.println(a.charge(4000, "KRW"));
        System.out.println(b.charge(4000, "KRW"));

    }
}
