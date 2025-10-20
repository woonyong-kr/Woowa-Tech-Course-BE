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
