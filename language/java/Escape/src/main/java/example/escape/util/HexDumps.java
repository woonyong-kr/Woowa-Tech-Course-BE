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