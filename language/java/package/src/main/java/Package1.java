import example.vendor.a.Payment;

public class Package1 {
    public static void main(String[] args) {

        // (예제 1)
        // 패키지는 import를 사용해 클래스를 선택적으로 사용하거나
        // import 없이 FQCN(정규 패키지명)으로 사용할 수 있다.

        // import: 패키지명을 생략하고 클래스명만 사용
        Payment a = new Payment();

        // FQCN: import 없이 '패키지.클래스'를 그대로 사용
        example.vendor.a.Payment b = new example.vendor.a.Payment();

        System.out.println(a.charge(1000, "KRW"));
        System.out.println(b.charge(1000, "KRW"));
    }
}