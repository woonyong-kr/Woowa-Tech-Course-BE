# Language/Java/Escape

## 이스케이프(Escape Sequence)

> 문자열은 문자들의 순서를 나타내는 리터럴이고, 이스케이프 시퀀스는 문자열 안에서 **보이지 않는 제어 문자**나 **특수한 의미를 갖는 문자**를 표현하기 위한 기호 체계입니다.

자바의 문자열 처리는 생각보다 복잡합니다.
단순해 보이는 `\n` 하나가 **언제, 어디서, 어떻게** 처리되는가에 따라 완전히 다른 결과를 만듭니다.

**소스에 적은 리터럴은 컴파일 타임에 해석**되고, **사용자가 입력한 문자열은 런타임에 그대로** 들어온다는 사실이 핵심입니다.
이 차이를 구분해서 이해하지 않으면 파일 경로, 정규식, JSON 직렬화, 로그 출력 등에서 쉽게 오류가 발생합니다.

### 자바의 이스케이프 문자 표
| 의미 | 리터럴 | 실제 동작 | 유니코드 | 비고 |
|---|---|---|---|---|
| 줄바꿈 | `\n` | LF(Line Feed, 개행) | U+000A | Unix 계열 기본 줄바꿈 |
| 캐리지 리턴 | `\r` | CR(Carriage Return) | U+000D | 커서를 줄 맨 앞으로, Windows는 `\r\n` 조합 사용 |
| 수평 탭 | `\t` | HT(Horizontal Tab) | U+0009 | 일반적으로 8칸 간격, 환경에 따라 다름 |
| 백스페이스 | `\b` | BS(Backspace) | U+0008 | 이전 문자 삭제(콘솔 환경 의존적) |
| 폼 피드 | `\f` | FF(Form Feed) | U+000C | 페이지 넘김(프린터 제어용, 현대에는 거의 사용 안 함) |
| 백슬래시 | `\\` | `\` 출력 | U+005C | 이스케이프 문자 자체를 출력 |
| 큰따옴표 | `\"` | `"` 출력 | U+0022 | 문자열 리터럴 내부에서 사용 |
| 작은따옴표 | `\'` | `'` 출력 | U+0027 | 문자 리터럼(`char`)에서 필수 |
| 유니코드 | `\uXXXX` | 코드포인트 문자 | 가변 | 예: `\uAC00` → '가' (4자리 16진수) |
| 널 문자 | `\0` | NUL | U+0000 | C/C++와 달리 자바에서는 문자열 종료 의미 없음 |

## 예제에 사용된 클래스

예제를 효과적으로 학습하기 위해 만들어진 유틸리티 클래스입니다. 문자열이 실제로 메모리에 어떻게 저장되는지 확인하고, 각 단계의 변환을 직접 눈으로 볼 수 있도록 작성했습니다.

<details>
<summary><strong>HexDumps</strong></summary>

### [HexDumps.java](src/main/java/example/escape/util/HexDumps.java)

문자열을 16진수 바이트로 덤프해 실제 메모리 구조를 확인하기 위해 작성했습니다.

```java
package example.escape.util;

// 바이트 배열을 공백 구분 대문자 헥스 문자열로 변환
public final class HexDumps {
    // 바이트 배열 → "AA BB CC" 형태로 포맷(용량: N*3, 부호 정규화: b & 0xFF)
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length == 0 ? 0 : (bytes.length * 3));
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b& 0xFF));
        }
        return sb.toString();
    }
}
```
</details>
<details>
<summary><strong>TextEscapes</strong></summary>

### [TextEscapes.java](src/main/java/example/escape/util/TextEscapes.java)

설정 파일이나 런타임 입력에서 받은 `\n`, `\t` 같은 이스케이프를 실제 제어 문자로 변환해 보기 위해 작성했습니다.

```java
package example.escape.util;

import java.util.LinkedHashMap;
import java.util.Map;

//문자열 내 이스케이프(\\n, \\t, "\\uXXXX" 등)를 실제 문자로 변환
public final class TextEscapes {

    // 백슬래시(\\) 임시 대체용 비가시 문자(U+E000)
    private static final String MARKER = "\uE000";
    // 긴 패턴 → 단일 이스케이프 → 마커 복원(순서 보장용 LinkedHashMap)
    private static final Map<String, String> SIMPLE_ESCAPES = new LinkedHashMap<>();
    static {
        // "\\\\" → MARKER (먼저 보호)
        SIMPLE_ESCAPES.put("\\\\", MARKER);
        // 이스케이프 치환
        SIMPLE_ESCAPES.put("\\n", "\n");
        SIMPLE_ESCAPES.put("\\r", "\r");
        SIMPLE_ESCAPES.put("\\t", "\t");
        SIMPLE_ESCAPES.put("\\b", "\b");
        SIMPLE_ESCAPES.put("\\f", "\f");
        SIMPLE_ESCAPES.put("\\\"", "\"");
        SIMPLE_ESCAPES.put("\\'", "'");
        // MARKER → "\" (마지막에 백슬래시 복원)
        SIMPLE_ESCAPES.put(MARKER, "\\");
    }

    // 유니코드 이스케이프 먼저 해석 → 단순 이스케이프 적용 → 백슬래시 복원
    public static String unescapeAll(String raw) {
        String out = unescapeUnicode(raw);
        for (Map.Entry<String, String> e : SIMPLE_ESCAPES.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }

    // 유니코드 시퀀스를 코드포인트로 변환(서로게이트 쌍 결합 지원, 리터럴 \\ 보호)
    public static String unescapeUnicode(String raw) {
        int len = raw.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; ) {
            char c = raw.charAt(i);
            if (c == '\\' && i + 5 < len && raw.charAt(i + 1) == 'u' && !isBackslashEscaped(raw, i)) {
                String hex = raw.substring(i + 2, i + 6);
                if (is4Hex(hex)) {
                    int cu1 = Integer.parseInt(hex, 16);
                    int next = i + 6;

                    if (isHighSurrogate(cu1)) {
                        if (next + 5 < len && raw.charAt(next) == '\\' && raw.charAt(next + 1) == 'u') {
                            String hex2 = raw.substring(next + 2, next + 6);
                            if (is4Hex(hex2)) {
                                int cu2 = Integer.parseInt(hex2, 16);
                                if (isLowSurrogate(cu2)) {
                                    int cp = Character.toCodePoint((char) cu1, (char) cu2);
                                    // "\\uXXXX\\uXXXX", 서로게이트 쌍 결합 → 코드포인트 1개(UTF-16 코드 유닛 2개)
                                    sb.append(Character.toChars(cp));
                                    i = next + 6;
                                    continue;
                                }
                            }
                        }
                    }
                    // BMP 단일 코드 유닛 또는 고립된 서로게이트는 그대로 추가
                    sb.append(Character.toChars(cu1));
                    i += 6;
                    continue;
                }
            }
            // 일반 문자 그대로 복사
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    // High Surrogate (U+D800–DBFF) 검사
    private static boolean isHighSurrogate(int cu) {
        return cu >= 0xD800 && cu <= 0xDBFF;
    }

    // Low Surrogate (U+DC00–DFFF) 검사
    private static boolean isLowSurrogate(int cu)  {
        return cu >= 0xDC00 && cu <= 0xDFFF;
    }

    // 위치 i의 '\' 앞에 연속 '\' 개수가 홀수면 현재 '\'는 리터럴로 간주(치환 금지)
    private static boolean isBackslashEscaped(String s, int i) {
        int count = 0, p = i - 1;
        while (p >= 0 && s.charAt(p) == '\\') {
            count++; p--;
        }
        return (count & 1) == 1;
    }

    // 길이 4, 16진수만(0-9, a-f/A-F)인지 체크(대소문자 무시: ch|0x20)
    private static boolean is4Hex(String s) {
        if (s.length() != 4) return false;
        for (int k = 0; k < 4; k++) {
            char ch = s.charAt(k);
            boolean hex = (ch >= '0' && ch <= '9') || ((ch | 0x20) >= 'a' && (ch | 0x20) <= 'f');
            if (!hex) return false;
        }
        return true;
    }
}
```
</details>
<details>
<summary><strong>JsonEscapes</strong></summary>

### [JsonEscapes.java](src/main/java/example/escape/util/JsonEscapes.java)

JSON 규칙에 따라 특수 문자를 이스케이프하고 JSON 문자열을 구성하기 위해 작성했습니다.

```java
package example.escape.util;

// JSON용 이스케이프/간단한 조립
public final class JsonEscapes {

    // 문자열을 JSON 문자열 값으로 안전하게 이스케이프
    public static String escapeJson(String text) {
        if (text == null) return "null";
        StringBuilder sb = new StringBuilder(Math.max(16, text.length() + 16));
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;   // 백슬래시
                case '\"': sb.append("\\\""); break;   // 큰따옴표
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    // 제어문자(U+0000~U+001F)는 4자리 16진 유니코드 이스케이프(백슬래시 + 'u' + 4hex)로 출력
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04X", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // JSON 문자열 리터럴 한 개 만들기: "..." 형태로 감싸기
    public static String quote(String text) {
        return "\"" + escapeJson(text) + "\"";
    }

    // JSON 객체 생성
    public static String buildJsonManually(String name, String message) {
        return String.format("{\"name\":%s,\"message\":%s}",
                quote(name), quote(message));
    }
}

```
</details>

## 리터럴과 런타임 입력의 차이

같은 `"A\nB"`를 작성했을 때 어디서 작성했는지에 따라 완전히 다른 결과가 나타납니다.
소스코드에 작성한 리터럴은 **컴파일 시점**에 이미 처리되지만, 사용자가 입력한 문자열은 **런타임에 그대로** 들어옵니다.
이 차이를 모르면 문자열 처리에서 많은 오류를 겪게 됩니다.

### [리터럴과 런타임 입력의 차이](src/main/java/Escape1.java)
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import example.escape.util.HexDumps;

public class Escape1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String literal = "A\nB";
        System.out.println("[리터럴] 내용:\n" + literal);
        System.out.println("[리터럴] length: " + literal.length());
        System.out.println("[리터럴] UTF-8: " + 
                HexDumps.bytesToHex(literal.getBytes(StandardCharsets.UTF_8)));

        System.out.print("\n[입력] A\\nB 를 그대로 입력 > ");
        String input = sc.nextLine();
        System.out.println("[입력] 내용: " + input);
        System.out.println("[입력] length: " + input.length());
        System.out.println("[입력] UTF-8: " + 
                HexDumps.bytesToHex(input.getBytes(StandardCharsets.UTF_8)));
    }
}
```

### 실행 결과
```
[리터럴] 내용:
A
B
[리터럴] length: 3
[리터럴] UTF-8: 41 0A 42 

[입력] A\nB 를 그대로 입력 > A\nB
[입력] 내용: A\nB
[입력] length: 4
[입력] UTF-8: 41 5C 6E 42 
```

### 처리 흐름
```
소스코드 리터럴: "A\nB"
         ↓
    [컴파일 단계]
    컴파일러가 \n을 개행(U+000A)으로 변환
         ↓
   메모리 저장: [A(0x41), LF(0x0A), B(0x42)]
   길이: 3글자
   UTF-8: 41 0A 42


사용자 입력: A\nB (키보드 타이핑)
         ↓
    [런타임 단계]
    JVM이 바이트 그대로 수신
    (이스케이프 해석 없음)
         ↓
   메모리 저장: [A(0x41), \(0x5C), n(0x6E), B(0x42)]
   길이: 4글자
   UTF-8: 41 5C 6E 42
```

소스코드의 리터럴 `"A\nB"`는 메모리에 3글자(`41 0A 42`)로 저장되며, 0x0A는 실제 개행 문자입니다.
따라서 출력할 때 A와 B 사이에서 줄이 바뀝니다.

런타임 입력 `"A\nB"`는 메모리에 4글자(`41 5C 6E 42`)로 저장되며, 0x5C는 백슬래시, 0x6E은 문자 n입니다.
따라서 출력할 때 줄이 바뀌지 않고 그냥 `A\nB`로 표시됩니다.

컴파일러는 소스코드의 `\n`을 개행 문자로 인식해 변환하지만, JVM은 입력 스트림에서 받은 `\`와 `n`을 일반 문자로 취급합니다.

## 내부 작동 원리

리터럴과 입력이 다르게 처리되는 것을 확인했습니다. 이 차이가 왜 발생하는지 이해하려면 컴파일러와 JVM의 처리 방식을 알아야 합니다.

### 컴파일 단계 (javac)

컴파일러가 소스코드를 읽을 때, 문자열 리터럴 내부의 이스케이프는 즉시 실제 문자로 해석됩니다.
이 과정은 컴파일 단계에서 완료되므로, 바이트코드에는 이미 해석된 결과가 저장됩니다.

예를 들어 소스코드에 "Hello\nWorld"라고 작성하면:
```
소스코드 작성
  ↓
렉서(Lexer): STRING_LITERAL 토큰으로 인식
  ↓
파서(Parser): 이스케이프 해석
  - 'H', 'e', 'l', 'l', 'o'
  - \n → U+000A (실제 개행 문자)
  - 'W', 'o', 'r', 'l', 'd'
  ↓
바이트코드 생성: Constant Pool에 [48 65 6C 6C 6F 0A 57 6F 72 6C 64] 저장
  ↓
.class 파일: 이미 해석이 완료된 상태로 저장
```
컴파일된 .class 파일을 javap -v 명령으로 확인하면 상수 풀에 이미 해석된 문자열이 저장되어 있는 것을 볼 수 있습니다.
이는 런타임에 추가 해석이 필요 없다는 의미입니다.

### 런타임 단계 (JVM)

런타임에 사용자 입력을 받을 때는 완전히 다른 과정을 거칩니다:
```
사용자가 "A\nB" 타이핑
  ↓
System.in (InputStream): 바이트 스트림으로 수신
  - 0x41 (A)
  - 0x5C (\)
  - 0x6E (n)
  - 0x42 (B)
  ↓
InputStreamReader: Charset 적용해 바이트 → 문자 변환
  (이스케이프 해석 없음!)
  ↓
Scanner.nextLine() / BufferedReader: 문자 시퀀스 읽기
  ↓
String 객체 생성: [A(0x41), \(0x5C), n(0x6E), B(0x42)]
```

중요한 점은 이 전체 과정에서 이스케이프 해석이 일어나지 않는다는 것입니다.
사용자가 키보드로 \와 n을 순서대로 타이핑하면, 바이트 스트림은 0x5C 0x6E(백슬래시와 n의 ASCII)이 되고, 이것이 그대로 문자열 "\\n"(길이 2)으로 만들어집니다.

이스케이프 해석은 오직 컴파일 타임에만 일어나며, JVM은 런타임에 문자열을 다시 파싱하지 않습니다.
만약 런타임 입력에서 이스케이프를 해석하고 싶다면, 다음에 볼 Escape2에서처럼 명시적으로 변환 메서드를 호출해야 합니다.

## 설정 파일의 이스케이프 처리

가끔 설정 파일에 `log_format="%date%\n%level%"` 형태로 이스케이프가 저장되어 있습니다.
프로그램이 이를 읽을 때 `\n`은 단순한 두 글자일 뿐 개행이 아닙니다.
의도대로 작동하려면 명시적으로 변환해야 합니다.

### [설정 파일의 이스케이프 처리](src/main/java/Escape2.java)
```java
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import example.escape.util.HexDumps;
import example.escape.util.TextEscapes;

public class Escape2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("[입력] hello\\nworld\\t\\uAC00 > ");
        String raw = sc.nextLine();

        String applied = TextEscapes.unescapeAll(raw);

        System.out.println("원본:   " + raw);
        System.out.println("변환:   " + applied);
        System.out.println("[UTF-8] 원본: " + 
                HexDumps.bytesToHex(raw.getBytes(StandardCharsets.UTF_8)));
        System.out.println("[UTF-8] 변환: " + 
                HexDumps.bytesToHex(applied.getBytes(StandardCharsets.UTF_8)));
    }
}
```

### 실행 결과
```
[입력] hello\nworld\t\uAC00 > hello\nworld\t\uAC00
원본:   hello\nworld\t\uAC00
변환:   hello
world	가
[UTF-8] 원본: 68 65 6C 6C 6F 5C 6E 77 6F 72 6C 64 5C 74 5C 75 41 43 30 30 
[UTF-8] 변환: 68 65 6C 6C 6F 0A 77 6F 72 6C 64 09 EA B0 80 
```

### 변환 과정
```
입력 문자열: "hello\nworld\t\uAC00" (가시적 이스케이프)
         ↓
  UTF-8 분석:
  5C 6E (백슬래시 + n) ← 개행이 아니라 2개 글자
  5C 74 (백슬래시 + t) ← 탭이 아니라 2개 글자
  5C 75 41 43 30 30 ← 유니코드가 아니라 6개 글자
         ↓
     명시적 변환 (예제에 작성한 TextEscapes.unescapeAll() 사용)
         ↓
  변환 결과:
  5C 6E → 0A (실제 개행)
  5C 74 → 09 (실제 탭)
  5C 75 41 43 30 30 → EA B0 80 (한글 '가')
```

입력받은 문자열의 UTF-8 바이트를 보면 모두 가시적인 문자 조합입니다. [TextEscapes.unescapeAll()](src/main/java/example/escape/util/TextEscapes.java)을 호출하면:

- `5C 6E` → `0A` (실제 개행)
- `5C 74` → `09` (실제 탭)
- `5C 75 41 43 30 30` → `EA B0 80` (한글 '가'의 UTF-8 표현)

중요한 점은 이 변환이 **자동으로 일어나지 않는다**는 것입니다. 설정 파일에서 읽은 문자열에 `\n`이 있어도 자바는 이를 개행으로 처리하지 않습니다. 개발자가 명시적으로 호출해야 합니다.

## 정규식의 이중 이스케이프

정규식도 백슬래시를 메타문자로 사용합니다.
예를 들어 `\d`는 "숫자"를 의미하는데, 문제는 자바 문자열도 백슬래시를 이스케이프 문자로 사용한다는 점입니다.
따라서 **두 단계의 처리**가 일어납니다.

코드에서 `Pattern.compile("\\d+")`라고 쓸 때:
1. 컴파일러가 `"\\d+"` 를 보고 `\d+`로 변환
2. 정규식 엔진이 `\d+`를 받아 "숫자 1개 이상"으로 해석

```
소스코드: Pattern.compile("\\d+")
         ↓
    [컴파일 단계]
    컴파일러가 "\\d+" 해석
         ↓
    런타임 String: "\d+"
         ↓
    [정규식 엔진]
    "\d+" → "숫자 1개 이상" 의미
         ↓
    "123" 매칭 성공 ✓
    "12a" 매칭 실패 ✗
```

만약 `"\d+"`라고만 쓰면 컴파일러가 `\d`를 모르는 이스케이프로 본다고 해서 에러는 아니지만, 정규식 엔진이 문자 d를 리터럴로 매칭하게 되어 의도와 다르게 해석됩니다.

### [정규식의 이중 이스케이프](src/main/java/Escape3.java)
```java
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Escape3 {
    public static void main(String[] args) {
        Pattern digits = Pattern.compile("\\d+");
        System.out.println("[\\d+] \"123\" matches = " + digits.matcher("123").matches());
        System.out.println("[\\d+] \"12a\" matches = " + digits.matcher("12a").matches());

        Pattern backslash = Pattern.compile("\\\\");
        System.out.println("[\\\\] \"\\\\\" matches = " + backslash.matcher("\\").matches());

        String userInput = "C:\\temp\\file.txt";
        Pattern safe = Pattern.compile(Pattern.quote(userInput));
        System.out.println("[quote] match = " + safe.matcher("C:\\temp\\file.txt").matches());

        String text = "email: admin@example.com";
        Pattern email = Pattern.compile("[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}");
        Matcher m = email.matcher(text);
        while (m.find()) System.out.println("found: " + m.group());
    }
}
```

### 실행 결과
```
[\d+] "123" matches = true
[\d+] "12a" matches = false
[\\] "\\" matches = true
[quote] match = true
found: admin@example.com
```

## 제어 문자의 정체

`\n`은 보이지 않지만 실제 문자입니다.
유니코드 코드포인트 U+000A에 해당하며, 메모리에 실제 바이트로 저장되고, 다른 문자들과 똑같이 처리됩니다.

### [제어 문자의 정체](src/main/java/Escape4.java)
```java
import example.escape.util.HexDumps;

public class Escape4 {
    public static void main(String[] args) {
        String s = "A\nB\tC";
        System.out.println("[가시화] " + s.replace("\n", "\\n").replace("\t", "\\t"));
        
        System.out.println("[문자 분석]");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            String name = (c == '\n' ? "\\n" : (c == '\t' ? "\\t" : "" + c));
            System.out.printf("  [%d] '%s' U+%04X%n", i, name, (int)c);
        }
        
        System.out.println("[UTF-8 바이트] " + 
                HexDumps.bytesToHex(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
```

### 실행 결과
```
[가시화] A\nB\tC
[문자 분석]
  [0] 'A' U+0041
  [1] '\n' U+000A
  [2] 'B' U+0042
  [3] '\t' U+0009
  [4] 'C' U+0043
[UTF-8 바이트] 41 0A 42 09 43 
```

### 메모리 구조
```
메모리 저장 (문자별):
┌────────────────────────────────────┐
│ A    │  \n   │  B   │  \t   │  C─  │
│ 0x41 │ 0x0A  │ 0x42 │ 0x09  │ 0x43 │
└────────────────────────────────────┘

바이트 표현 (UTF-8):
41 0A 42 09 43
```
제어 문자는 메모리에 실제로 존재합니다.
실제로 `A`와 `B` 사이에서 줄이 바뀌고 `B`와 `C` 사이에 공백이 생기지만, 제어 문자 때문인지 파악하기 어렵습니다.
하지만 각 문자를 코드포인트로 출력하거나 바이트로 덤프하면 U+000A, U+0009 같은 실제 값을 확인할 수 있습니다.

## UTF-16과 코드 유닛

자바의 `String.length()`는 문자 개수가 아니라 **UTF-16 코드 유닛의 개수**를 반환합니다.
대부분의 문자는 1개 유닛이지만, 이모지 같은 보충 평면 문자는 2개 유닛으로 표현됩니다.

### [인코딩별 바이트 길이 비교](src/main/java/Escape5.java)
```java
import java.nio.charset.StandardCharsets;
import example.escape.util.HexDumps;

public class Escape5 {
    public static void main(String[] args) {
        sample("ABC");
        sample("가");
        sample("\uD83D\uDE00"); // 😀
    }

    private static void sample(String s) {
        byte[] u8 = s.getBytes(StandardCharsets.UTF_8);
        byte[] be = s.getBytes(StandardCharsets.UTF_16BE);
        System.out.printf("[\"%s\"] length=%d%n", s, s.length());
        System.out.println("  UTF-8:    " + HexDumps.bytesToHex(u8));
        System.out.println("  UTF-16BE: " + HexDumps.bytesToHex(be));
    }
}
```

### 실행 결과
```
["ABC"] length=3
  UTF-8:    41 42 43 
  UTF-16BE: 00 41 00 42 00 43 

["가"] length=1
  UTF-8:    EA B0 80 
  UTF-16BE: AC 00 

["😀"] length=2
  UTF-8:    F0 9F 98 80 
  UTF-16BE: D8 3D DE 00 
```

### 인코딩별 바이트 길이
```
"ABC"
├─ 메모리 (UTF-16): 3개 char
├─ UTF-8: 3바이트 (1+1+1)
└─ UTF-16BE: 6바이트 (2+2+2)

"가" (U+AC00)
├─ 메모리 (UTF-16): 1개 char
├─ UTF-8: 3바이트
└─ UTF-16BE: 2바이트

"😀" (U+1F600)
├─ 메모리 (UTF-16): 2개 char
├─ UTF-8: 4바이트
└─ UTF-16BE: 4바이트 (2+2, 서로게이트 쌍)
```

각 문자는 인코딩에 따라 다른 바이트 수를 사용합니다. 하지만 이모지는 `length()`가 2를 반환하는데, 이유는 내부적으로 두 개의 UTF-16 코드 유닛으로 표현되기 때문입니다.

### [코드포인트와 코드 유닛의 차이](src/main/java/Escape6.java)
```java
public class Escape6 {
    public static void main(String[] args) {
        String bmp = "가";
        String sup = "\uD83D\uDE00"; // 😀

        System.out.println("BMP  length=" + bmp.length() + 
                ", codePoints=" + bmp.codePointCount(0, bmp.length()));
        System.out.println("SUPP length=" + sup.length() + 
                ", codePoints=" + sup.codePointCount(0, sup.length()));

        int cp = sup.codePointAt(0);
        System.out.printf("sup.codePointAt(0)=U+%X%n", cp);
    }
}
```

### 실행 결과
```
BMP  length=1, codePoints=1
SUPP length=2, codePoints=1
sup.codePointAt(0)=U+1F600
```

### 코드포인트 vs 코드 유닛
```
한글 "가" (BMP 범위)
├─ char 배열: [가]
├─ length(): 1 (char 개수)
└─ codePointCount(): 1 (코드포인트 개수)
   → 둘이 일치

이모지 "😀" (보충 평면)
├─ char 배열: [D83D, DE00]
├─ length(): 2 (char 개수)
└─ codePointCount(): 1 (코드포인트 개수)
   → 다름!
```

한글 "가"는 `length()`와 `codePointCount()`가 모두 1입니다.
하지만 이모지는 `length()` = 2, `codePointCount()` = 1입니다.
이모지는 실제로 1개 문자이지만 자바 내부에서는 2개 유닛으로 표현되기 때문입니다.

## 서로게이트 쌍(Surrogate Pair)

보충 평면 문자(이모지)는 **서로게이트 쌍**이라는 방식으로 인코딩됩니다.
두 개의 UTF-16 코드 유닛을 조합해 하나의 코드포인트를 나타냅니다.

### [서로게이트 쌍의 길이 계산](src/main/java/Escape7.java)
```java
public class Escape7 {
    public static void main(String[] args) {
        String s = "😀A";
        
        System.out.println("length() = " + s.length());
        System.out.println("codePointCount() = " + s.codePointCount(0, s.length()));
    }
}
```

### 실행 결과
```
length() = 3
codePointCount() = 2
```

### [서로게이트 쌍의 변환](src/main/java/Escape8.java)
```java
public class Escape8 {
    public static void main(String[] args) {
        char hi = '\uD83D';  // high surrogate
        char lo = '\uDE00';  // low surrogate

        int cp = Character.toCodePoint(hi, lo);
        System.out.printf("toCodePoint: U+%X%n", cp);

        char[] pair = Character.toChars(cp);
        System.out.printf("toChars: [U+%04X, U+%04X] → '%s'%n",
                (int)pair[0], (int)pair[1], new String(pair));
    }
}
```

### 실행 결과
```
toCodePoint: U+1F600
toChars: [U+D83D, U+DE00] → '😀'
```

### 실행 결과
```
코드포인트 U+1F600 (😀)
         ↓
   UTF-16 분해
  (보충 평면이므로)
         ↓
┌─ High Surrogate: U+D83D (범위: D800~DBFF)
└─ Low Surrogate:  U+DE00 (범위: DC00~DFFF)

메모리 저장: [D83D, DE00]
         ↓
Character.toCodePoint(D83D, DE00)
         ↓
원래 코드포인트 U+1F600 복원
```

이모지 😀(U+1F600)는 자바 내부에서 high surrogate `D83D`와 low surrogate `DE00`의 조합으로 저장됩니다.
`Character.toCodePoint()`는 이 두 코드 유닛을 원래 코드포인트로 복원하고, `Character.toChars()`는 반대로 분해합니다.

## 인코딩과 플랫폼

같은 문자도 인코딩 방식과 플랫폼에 따라 다르게 저장됩니다.

### [엔디언에 따른 바이트 순서](src/main/java/Escape9.java)
```java
import java.nio.charset.StandardCharsets;
import example.escape.util.HexDumps;

public class Escape9 {
    public static void main(String[] args) {
        String s = "가";
        byte[] be = s.getBytes(StandardCharsets.UTF_16BE);
        byte[] le = s.getBytes(StandardCharsets.UTF_16LE);
        
        System.out.println("UTF-16BE: " + HexDumps.bytesToHex(be));
        System.out.println("UTF-16LE: " + HexDumps.bytesToHex(le));
        
        byte[] bomBE = new byte[]{(byte)0xFE, (byte)0xFF, be[0], be[1]};
        byte[] bomLE = new byte[]{(byte)0xFF, (byte)0xFE, le[0], le[1]};
        System.out.println("BOM+BE:   " + HexDumps.bytesToHex(bomBE));
        System.out.println("BOM+LE:   " + HexDumps.bytesToHex(bomLE));
    }
}
```

### 실행 결과
```
UTF-16BE: AC 00 
UTF-16LE: 00 AC 
BOM+BE:   FE FF AC 00 
BOM+LE:   FF FE 00 AC 
```

### 바이트 순서(엔디언)
```
같은 문자 "가" (U+AC00)

Big-Endian (상위 바이트 먼저):
AC 00 ← 상위 AC, 하위 00

Little-Endian (하위 바이트 먼저):
00 AC ← 하위 AC, 상위 00

BOM (Byte Order Mark):
FE FF ← Big-Endian 표시
FF FE ← Little-Endian 표시
```

같은 "가"(U+AC00)도 Big-Endian과 Little-Endian에서 바이트 순서가 바뀝니다.
BOM(Byte Order Mark)을 추가하면 파일 맨 앞의 `FE FF` 또는 `FF FE`로 엔디언을 표시할 수 있어,
파일을 읽을 때 올바른 순서를 자동으로 판별할 수 있습니다.

### [플랫폼별 줄바꿈 처리](src/main/java/Escape10.java)
```java
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Escape10 {
    public static void main(String[] args) throws IOException {
        String sep = System.lineSeparator();
        System.out.println("플랫폼 줄바꿈: " + sep.length() + " 글자");

        Path tmp = Files.createTempFile("test-", ".txt");
        Files.writeString(tmp, "A\r\nB\r\n", StandardCharsets.UTF_8);
        
        var lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        System.out.println("읽은 라인: " + lines);

        Files.deleteIfExists(tmp);
    }
}
```

### 실행 결과
```
플랫폼 줄바꿈: 1 글자
읽은 라인: [A, B]
```

### 플랫폼별 줄바꿈
```
플랫폼 줄바꿈: 1 글자
읽은 라인: [A, B]
```

Windows는 CRLF(`\r\n`)를 줄바꿈으로 사용하고, Unix/Linux는 LF(`\n`)만 사용합니다.
하지만 자바의 `readAllLines()`는 모든 형식을 자동으로 인식합니다.

## 정규식

### [외부 입력과 정규식 메타문자](src/main/java/Escape11.java)
```java
import java.util.regex.Pattern;

public class Escape11 {
    public static void main(String[] args) {
        String userInput = "C:\\temp\\file.txt";
        
        Pattern p = Pattern.compile("prefix-" + Pattern.quote(userInput) + "-suffix");
        String target = "prefix-C:\\temp\\file.txt-suffix";
        
        System.out.println("match = " + p.matcher(target).matches());
    }
}
```

### 실행 결과
```
match = true
```

사용자가 입력한 문자열에 백슬래시나 점 같은 메타문자가 포함되어 있으면, 이를 정규식에 바로 포함시키면 위험합니다.
`Pattern.quote()`로 감싸면 모든 메타문자를 무력화해 리터럴 매칭이 가능합니다.

### [정규식 메타문자 이스케이프](src/main/java/Escape12.java)
```java
import java.util.regex.Pattern;

public class Escape12 {
    public static void main(String[] args) {
        Pattern version = Pattern.compile("v\\d+\\.\\d+");
        
        System.out.println("v10.3 matches = " + version.matcher("v10.3").matches());
        System.out.println("v10x3 matches = " + version.matcher("v10x3").matches());
        System.out.println("v10a3 matches = " + version.matcher("v10a3").matches());
    }
}
```

### 실행 결과
```
v10.3 matches = true
v10x3 matches = false
v10a3 matches = false
```

### 정규식 메타문자
```
패턴: v\\d+\\.\\d+

컴파일러 해석:
"v\\d+\\.\\d+" → "\v\d+\.\d+"

정규식 엔진 해석:
v     → 문자 v 리터럴
\d+   → 숫자 1개 이상
\.    → 점(.) 리터럴 (메타문자 무력화)
\d+   → 숫자 1개 이상

매칭:
v10.3 ✓ (v + 숫자 + 점 + 숫자)
v10x3 ✗ (x는 숫자가 아님)
```

정규식에서 점(`.`)은 "모든 문자"를 의미하는 메타문자입니다.
리터럴 점으로 매칭하려면 `\.`으로 이스케이프해야 하며, 자바 문자열에서는 이를 `"\\."` 형태로 작성합니다.
이스케이프 없이 `.`만 사용하면 모든 문자를 매칭하므로, `v10x3`처럼 의도하지 않은 패턴도 매칭됩니다.


## 정리하며

### 첫 번째: 처리 시점이 결과를 결정한다

소스코드에 작성한 `\n`은 컴파일러가 개행 문자(0x0A)로 변환하지만, 사용자가 입력한 `\n`은 백슬래시(0x5C)와 n(0x6E) 두 바이트로 메모리에 저장됩니다.
같은 기호지만 메모리의 실제 바이트 값이 다르므로 완전히 다른 결과를 만듭니다.
컴파일러가 소스코드 단계에서 이스케이프를 해석하고, JVM이 런타임 입력을 있는 그대로 받아들이는데, 이 차이를 이해하는 것이 중요합니다.

### 두 번째: 변환은 자동으로 일어나지 않는다

설정 파일이나 텍스트 입력에서 받은 문자열의 `\n`은 백슬래시+n(0x5C 0x6E) 상태로 메모리에 저장되고, 이 상태로 영구히 남습니다.
문자를 처리해야 한다면 그 과정을 명시적으로 코드에 구현하여 0x0A로 변환해야 합니다.

### 세 번째: 메타문자는 문맥에 따라 의미가 다르다

정규식에 사용자 입력을 직접 포함시키면 점(`.`)은 모든 문자를 의미하는 메타문자로 해석되고, JSON 직렬화에 개행을 포함시키면 `\n`이 제어 문자로 해석됩니다.
같은 기호라도 어디에서 사용되느냐에 따라 다르게 해석되며, 외부 입력을 다루는 코드에서는 이 차이를 명확히 인식하고, 필요하면 안전한 변환 메커니즘을 사용해야 합니다.

---
문자열이 메모리에 실제로 어떤 바이트 값으로 저장되었는지 보면, 이스케이프 문제인지 인코딩 문제인지 메타문자 해석 문제인지 빠르게 구분할 수 있습니다.
이 세 가지 원리와 바이트 진단 방법을 기억한다면, 복잡해 보이는 문자열 처리 문제도 체계적으로 접근할 수 있습니다.
