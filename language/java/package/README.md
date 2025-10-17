# Language/Java/Package

---

## 패키지(Package)

> 패키지(package)는 관련 있는 타입(클래스·인터페이스· enum ·애너 테이션)을 하나의 논리적 이름 공간으로 묶어, 파일/JAR의 디렉터리 구조와 1:1로 대응시키고, 이름 충돌을 방지하며 접근 제어의 경계를 제공하기 위한 체계입니다.<br>
> 우리가 파일을 주제·성격별로 폴더에 정리하듯, 자바에서는 클래스를 패키지로 분류해 관리하고, 같은 이름의 클래스라도 패키지가 다르면 충돌 없이 함께 사용할 수 있습니다.

### 핵심 규칙

- **단일 import:** `import a.b.C;` → 단순 이름 `C`만 바로 사용할 수 있음.
- **와일드카드:** `import a.b.*;` → `a.b` 바로 아래 클래스만 포함, `a.b.c.*`는 포함하지 않음.
- **자동 가시성:** 동일 패키지와 `java.lang.*`는 import 없이 사용 가능.
- **모호성:** 서로 다른 패키지의 같은 단순명이 동시에 가시하면 컴파일 에러.
  한쪽만 단일 import 하고 다른 쪽은 FQCN으로 쓰거나, 둘 다 FQCN으로 사용.
- **static import:** 정적 멤버는 클래스명 없이 쓰긴 하지만, 가독성 위해 최소 사용 권장.

---

## 패키지 사용하기

---

인텔리제이에서 `소스 루트(src) 우클릭 → New → Package`로 패키지를 만듭니다.

생성된 패키지는 실제 파일 시스템에도 동일 경로의 디렉터리로 만들어지고, 컴파일러/JVM은 이 경로를 패키지 이름으로 해석합니다.

#### 예약어(키워드)와 패키지 이름

자바 키워드와 리터럴(true/false/null)은 식별자로 사용할 수 없습니다.

- 예) `package`, `import`, `class`, `int`, `null`, `true` 등 사용 불가.
  <br>
  <br>

### 패키지와 파일/JAR 매핑

---

예제는 여러 결제 벤더를 동시에 연동하는 상황을 가정하고, 아래와 같은 구조를 사용합니다.

예제 전제: 벤더 결제 모듈 시나리오(최소 트리)
```
src/
└─ example/
   └─ vendor/
      ├─ a/                 
      │  ├─ Payment.java         
      │  ├─ Authorization.java  
      │  ├─ Refund.java     
      │  └─ internal/          
      │     └─ InternalClass.java
      └─ b/                      
         └─ Payment.java
```

- `example.vendor.a.Payment`, `example.vendor.b.Payment`: 벤더 A/B의 결제 클라이언트.
- `example.vendor.payment.PaymentGateway`, `AGateway`, `BGateway`: 공통 인터페이스와 어댑터.

이 설정은 실무에서 흔한 이름 충돌(Payment, Client, Request 등)을 안전하게 다루고, import/FQCN/어댑터 선택이 패키지 해석과 클래스패스에 미치는 영향을 학습하기 위해 의도적으로 구성했습니다.

[예제 전제: 벤더 결제 모듈 패키지 예시](./src/example/vendor/a/Payment.java)
```java  
package example.vendor.a;

public class Payment {
    public String charge(int amount, String currency) {
        return "[A] charged " + amount + " " + currency;
    }
}
```

### 단일 import vs FQCN

---
`import example.vendor.a.Payment`를 쓰면 `Payment`를 단순 이름으로 쓸 수 있고, 동시에 FQCN(`example.vendor.a.Payment`)으로도 명시적으로 지정해 사용할 수 있습니다.

접근 대상 패키지에 동일 단순명이 있어도 한쪽은 단일 import, 다른 쪽은 FQCN을 병행하면 충돌 없이 공존시킬 수 있습니다.

[예제 1](./src/Package1.java)
```java  
import example.vendor.a.Payment;

public class Example {
    public static void main(String[] args) {
        // import: 패키지명을 생략하고 클래스명만 사용
        Payment a = new Payment();
        // FQCN: import 없이 '패키지.클래스'를 그대로 사용
        example.vendor.a.Payment b = new example.vendor.a.Payment();
    }
}
```

### 와일드카드 import의 범위

---
`import example.vendor.a.*`를 사용하면 `a` 패키지 “바로 아래” 클래스들을 단순 이름으로 접근할 수 있습니다.

이때 접근 가능한 범위는 바로 아래 클래스로 제한되며, `a.internal.*` 같은 하위 패키지에 접근하려면 별도의 import나 FQCN이 필요합니다.

필수 트리(요약)
```
src/example/vendor/a/{Payment.java, Authorization.java, Refund.java}
src/example/vendor/a/internal/InternalClass.java
```
[예제 2](./src/Package2.java)
```java  
import example.vendor.a.Payment;

public class Example {
    public static void main(String[] args) {
        Payment p = new Payment();
        Authorization a = new Authorization();
        Refund r = new Refund();

        // 컴파일 에러 (a.* 에는 internal 패키지가 포함되지 않음)
        //InternalClass i = new InternalClass(); // ← 주석을 풀면 에러 발생
        example.vendor.a.internal.InternalClass internalClass =
                new example.vendor.a.internal.InternalClass();
    }
}
```

### 동일 단순명 단일 import 충돌

---
`import example.vendor.a.Payment`와 `import example.vendor.b.Payment`를 동시에 선언하면, 동일 단순명에 대한 중복 단일 import로 즉시 컴파일 에러가 발생합니다.

동일한 단순명을 가진 클래스가 둘 이상일 때는 한쪽만 단일 import로 노출하고, 나머지는 FQCN으로 명시해 사용해야 합니다.

[예제 3](./src/Package3.java)
```java  
import example.vendor.a.Payment;
// 컴파일 에러 'example.vendor.a.Payment' is already defined in a single-type import
//import example.vendor.b.Payment; // ← 주석을 풀면 에러 발생

public class Example {
    public static void main(String[] args) {
        Payment a = new Payment();
        Payment b = new Payment();
    }
}

```

### 단일 import 우선순위 + 와일드카드

---
`import example.vendor.a.Payment` + `import example.vendor.b.*`를 함께 선언하면, 단일 import가 와일드카드보다 우선되어 단순 이름 `Payment`는 항상 `a.Payment`로 해석됩니다.

컴파일 에러는 없으며, 이는 컴파일러의 이름 해석 규칙상 단일 import에 대한 우선순위가 있다는 것을 알 수 있습니다.

[예제 4](./src/Package4.java)
```java  
import example.vendor.a.Payment;
import example.vendor.b.*;

public class Example {
    public static void main(String[] args) {
        // example.vendor.a와 example.vendor.b 모두 Payment가 있지만,
        // 단일 import가 우선이라 여기서 'Payment'는 a의 Payment만 가리킨다.
        // b의 Payment는 FQCN으로만 참조해야 한다.
        Payment a = new Payment();
        Payment b = new Payment();

        System.out.println(a.charge(4000, "KRW"));
        System.out.println(b.charge(4000, "KRW"));
    }
}
```

### 어댑터 패턴으로 이름 충돌 숨기기

---
동일한 단순명을 가진 클래스를 피치 못해 함께 써야 하고 용도도 유사하다면, 공통 인터페이스와 클래스별 래퍼(어댑터 패턴)로 추상화해 일관되게 사용하는 것도 하나의 방법입니다.

예제처럼 PaymentGateway(공통 인터페이스)와 AGateway/BGateway(어댑터)를 쓰면 벤더 구현 세부는 내부에 캡슐화되므로, 같은 이름의 타입이 공존해도 교체가 쉬워지고 이름/패키지 충돌이 호출부로 새지 않습니다.

[예제 5](./src/Package5.java)
```java  
import example.vendor.payment.*;

public class Example {
    public static void main(String[] args) {
        PaymentGateway a = new AGateway();
        PaymentGateway b = new BGateway();
        
        System.out.println(a.pay(4000, "KRW"));
        System.out.println(b.pay(4000, "KRW"));
    }
}
```

### FQCN ↔ 파일 경로 매핑과 로딩 확인

---
자바에는 별도의 ‘패키지 메타파일’이 없으며, 패키지는 클래스의 정규 이름(FQCN)과 파일/JAR 내부의 경로 규칙으로 표현됩니다.

#### 자바가 패키지를 읽는 과정

1. 컴파일 단계(javac) : FQCN(예: example.vendor.a.Payment)의 점(.)을 디렉터리 구분자(/)로 치환해 example/vendor/a/Payment.class를 생성한다.
2. .class 내부 구조: 바이트코드의 상수 풀(Constant Pool)에 내부 이름(internal name)이 슬래시 표기(example/vendor/a/Payment)로 기록된다.
3. 로딩 단계(JVM/클래스로더) : 실행 시 요청된 FQCN을 슬래시 경로 + .class로 변환해 클래스패스/모듈패스/JAR에서 해당 리소스를 탐색·로드한다.

#### 런타임에서 검증하기

`getName()`은 해당 클래스의 정규 이름(FQCN, 예: `example.vendor.a.Payment`)을, `getPackage().getName()`은 그 클래스가 속한 패키지 이름(예: `example.vendor.a`)을 문자열로 반환합니다.

두 값을 출력해 보면 런타임에 FQCN과 패키지명이 그대로 드러나며, 별도의 패키지 메타파일 없이도 클래스 이름 체계만으로 패키지가 식별됨을 확인할 수 있습니다.

이어서 리소스 조회 메서드로 실제 파일 경로 매핑을 확인할 수 있습니다.

예제의 두 호출이 반환하는 URL(예: `file://…`, 또는 JAR 안이면 `jar:file://…!/`)을 출력해 보면 FQCN의 점 표기가 실제로 슬래시 경로 + `.class`로 대응됨이 드러나며, 현재 클래스패스 설정이 올바르게 적용됐는지도 함께 확인할 수 있습니다.

[예제 6](./src/Package6.java)
```java  
import example.vendor.a.Payment;

public class Example {
    public static void main(String[] args) throws Exception {

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
```

## 패키지를 사용하는 이유

---
패키지는 코드를 주제별로 묶어 이름 충돌을 막고 공개/비공개 경계를 세우며 교체·테스트·배포 단위를 단순화해, 단순한 폴더 정리를 넘어 변경 영향 범위를 줄이고 협업을 안정화합니다.

### 핵심 포인트

- **이름 충돌 제어:** 외부 라이브러리에서 흔한 단순명(Client, Request, Payment 등)이 겹치더라도 패키지로 구분하면 함께 사용할 수 있습니다.

- **내부 구현 은닉과 경계:** 공개 API는 `api.*`, 내부 세부는 `internal.*`로 분리하고 package-private과 결합하면 민감한 구현을 패키지 내부에 캡슐화한 채 외부에는 공개된 API로만 사용하도록 강제할 수 있습니다.

- **테스트 전략 단순화:** 같은 패키지에 테스트를 두면 package-private을 직접 검증하고, 외부 패키지에서 테스트하면 공개 API만 보이게 됩니다.

- **도메인 중심 구조화:** 패키지 루트는 관례적으로 역순 도메인(io.github.woonyongkr 등)을 사용하는데, 패키지를 쓰면 전역 이름 충돌을 피하고 소유 경계가 분명해지며 리팩터링·모듈화가 수월해지고 변경 영향 범위가 줄어 협업과 배포가 수월해집니다.

## 기타 정보

---

- **unnamed package(패키지 선언 없음):** 패키지 선언이 없으면 클래스 경계·클래스패스 통합이 꼬여 충돌/의존 추적이 어려우므로, 학습·단발 테스트 외에는 사용을 지양합니다.

- **package-info.java:** 패키지 수준 설명(문서 주석)과 공통 애너테이션을 부여해 API 경계·정책(nullability 등)을 한곳에서 관리할 수 있습니다.

- **static import:** 정적 상수/함수(Math.max, Assertions.assertEquals 등)를 접두어 없이 간결히 쓰게 해주지만, 출처가 흐려져 충돌·가독성 저하가 생길 수 있으므로 필요한 것만 선택적으로 사용합니다.

- **java.lang 자동 import:** String, System, Math 등은 java.lang.*가 항상 가시하여 별도 import 없이 바로 쓰이지만, 그 외 표준 패키지(java.util, java.time 등)는 명시 import가 필요합니다.

---
배포 가능한 프로그램을 염두에 둔다면, 클래스를 명확한 패키지 구조(역순 도메인 루트·도메인 중심 하위 패키지)로 조직하고 공개 API와 내부 구현을 구분하는 습관이 유지보수성과 배포 측면에 도움이 될 것 같습니다.
