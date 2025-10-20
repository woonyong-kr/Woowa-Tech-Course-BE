import example.vendor.a.Payment;

public class Package6 {
    public static void main(String[] args) throws Exception {

        // (예제 6)
        // 자바에는 '패키지 전용 메타파일'이 없다.
        // 패키지는 클래스의 이름(FQCN)과 파일/JAR 경로 규칙으로 표현된다.
        // 1) 컴파일 단계(javac): FQCN(예: example.vendor.a.Payment)의 점(.)을 디렉터리 슬래시(/)로 바꿔
        //    해당 경로(example/vendor/a/Payment.class)에 .class를 생성한다.
        // 2) .class 내부: 이 클래스의 '내부 이름(internal name)'으로
        //    example/vendor/a/Payment 처럼 '슬래시 표기'가 Constant Pool에 기록된다.
        // 3) 로딩 단계(JVM/클래스로더): 요청된 FQCN을 슬래시 경로 + ".class"로 변환해
        //    클래스패스/모듈패스/JAR에서 리소스를 찾는다.

        Payment a = new Payment();

        // 런타임에서 이름/패키지를 직접 확인
        System.out.println("Name:     " + a.getClass().getName());
        System.out.println("Package:  " + a.getClass().getPackage().getName());

        // 실제 .class 리소스 경로가 FQCN ↔ 슬래시 경로로 매핑되는지 확인
        // (상대) Class 기준: 같은 패키지 내 Payment.class를 찾음
        System.out.println("Resource: " + a.getClass().getResource("Payment.class"));
        // (절대) 클래스패스 루트 기준 경로
        System.out.println("CL Res : " + ClassLoader.getSystemResource("example/vendor/a/Payment.class"));
    }
}
