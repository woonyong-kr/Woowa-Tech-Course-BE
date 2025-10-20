import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import example.escape.util.HexDumps;

// 문자열 리터럴은 컴파일 시점에 \n이 실제 개행 문자(U+000A)로 해석되어 상수 풀에 저장된다.
// 반면 런타임 입력으로 받은 문자열은 '\'와 'n' 두 문자가 그대로 포함되어 다르게 동작한다.
public class Escape1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 리터럴: 컴파일 타임에 \n → LF(U+000A)로 해석됨
        String literal = "A\nB";
        System.out.println("[리터럴] 내용:\n" + literal);
        System.out.println("[리터럴] length: " + literal.length()); // 3
        System.out.println("[리터럴] UTF-8: " +
                HexDumps.bytesToHex(literal.getBytes(StandardCharsets.UTF_8)));

        // 입력: 사용자가 친 글자 그대로 ('\' 'n')
        System.out.print("\n[입력] A\\nB 를 그대로 입력 > ");
        String input = sc.nextLine();
        System.out.println("[입력] 내용: " + input);
        System.out.println("[입력] length: " + input.length()); // 4
        System.out.println("[입력] UTF-8: " +
                HexDumps.bytesToHex(input.getBytes(StandardCharsets.UTF_8)));
    }
}
