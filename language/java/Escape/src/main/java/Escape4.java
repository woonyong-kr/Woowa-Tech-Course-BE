import example.escape.util.HexDumps;

// 보이지 않는 제어 문자를 코드포인트와 바이트 단위로 시각화해 실제 값을 확인한다.
// \n(U+000A), \t(U+0009) 등의 문자는 눈에 보이지 않아도 메모리에 존재한다.
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