// 서로게이트 쌍(high/low surrogate)을 코드포인트로 결합하거나 다시 분리하는 과정을 보여준다.
// toCodePoint()와 toChars()를 이용해 서로게이트 기반의 왕복 변환을 확인할 수 있다.
public class Escape8 {
    public static void main(String[] args) {
        char hi = '\uD83D';  // high surrogate
        char lo = '\uDE00';  // low surrogate

        int cp = Character.toCodePoint(hi, lo);
        System.out.printf("toCodePoint: U+%X%n", cp);

        char[] pair = Character.toChars(cp);
        System.out.printf("toChars: [U+%04X, U+%04X] → '%s'%n",
                (int)pair[0], (int)pair[1], new String(pair));
    }
}