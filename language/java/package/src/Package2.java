import example.vendor.a.Authorization;
import example.vendor.a.Payment;
import example.vendor.a.Refund;

public class Package2 {
    public static void main(String[] args) {

        // (예제 2)
        //패키지 와일드카드(import some.pkg.*)는 그 패키지의 ‘직계’ 클래스만 가져오고,
        // some.pkg.sub 같은 하위 패키지는 포함하지 않는다.

        Payment p = new Payment();
        Authorization a = new Authorization();
        Refund r = new Refund();

        // 컴파일 에러 (a.* 에는 internal 패키지가 포함되지 않음)
        //InternalClass i = new InternalClass(); // ← 주석을 풀면 에러 발생
        example.vendor.a.internal.InternalClass internalClass =
                new example.vendor.a.internal.InternalClass();

    }
}