import java.nio.charset.StandardCharsets;
import example.escape.util.HexDumps;

// UTF-16BE/LE는 고정된 엔디언 방식을 사용하며, BOM을 추가하면 바이트 순서를 명시적으로 표시할 수 있다.
// BOM(0xFEFF 또는 0xFFFE)은 파일 맨 앞에서 엔디언을 판별하는 데 사용된다.
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