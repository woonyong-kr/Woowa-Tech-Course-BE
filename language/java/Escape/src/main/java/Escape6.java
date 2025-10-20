// String.length()는 UTF-16 코드 유닛 개수를 반환하지만, 실제 문자 수는 코드포인트 기준이다.
// 이모지 같은 보충 평면 문자는 length=2지만, codePointCount()로 세면 실제 문자 수는 1이다.
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