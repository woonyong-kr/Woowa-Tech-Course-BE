import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import example.escape.util.HexDumps;
import example.escape.util.TextEscapes;

// 사용자가 입력한 \n, \t, \\uXXXX 같은 문자열 표현을 실제 제어 문자로 변환한다.
// 설정 파일이나 텍스트 기반 DSL에서 이스케이프 문자를 해석할 때 필요하다.
public class Escape2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("[입력] hello\\nworld\\t\\uAC00 > ");
        String raw = sc.nextLine();

        // \n, \t, \", \\, 유니코드 등 화이트리스트 언이스케이프
        String applied = TextEscapes.unescapeAll(raw);

        System.out.println("원본:   " + raw);
        System.out.println("변환:   " + applied);
        System.out.println("[UTF-8] 원본: " +
                HexDumps.bytesToHex(raw.getBytes(StandardCharsets.UTF_8)));
        System.out.println("[UTF-8] 변환: " +
                HexDumps.bytesToHex(applied.getBytes(StandardCharsets.UTF_8)));
    }
}
