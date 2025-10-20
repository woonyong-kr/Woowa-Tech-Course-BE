import java.nio.charset.StandardCharsets;
import example.escape.util.HexDumps;

// 제어 문자를 코드포인트와 바이트 단위로 시각화해 실제로 어떤 값이 저장되는지 확인한다.
// 인코딩 방식에 따라 같은 문자도 다른 바이트 수를 사용한다.
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
