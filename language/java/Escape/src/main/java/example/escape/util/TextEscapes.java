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
