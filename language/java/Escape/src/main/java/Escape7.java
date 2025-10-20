// 이모지 같은 보충 평면 문자는 length()가 2를 반환하지만, 실제로는 1개 문자다.
// 서로게이트 쌍(high/low surrogate)으로 표현되기 때문에 코드 유닛이 2개인 것이다.
public class Escape7 {
    public static void main(String[] args) {
        String s = "😀A";

        System.out.println("length() = " + s.length());
        System.out.println("codePointCount() = " + s.codePointCount(0, s.length()));
    }
}