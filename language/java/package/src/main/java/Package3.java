import example.vendor.a.Payment;
// 컴파일 에러 'example.vendor.a.Payment' is already defined in a single-type import
//import example.vendor.b.Payment; // ← 주석을 풀면 에러 발생

public class Package3 {
    public static void main(String[] args) {

        // (예제 3)
        // 서로 패키지가 다르더라도 같은 이름의 단일 클래스를 임포트하면 에러가 발생한다.

        Payment a = new Payment();
        Payment b = new Payment();

        System.out.println(a.charge(3000, "KRW"));
        System.out.println(b.charge(3000, "KRW"));
    }
}
