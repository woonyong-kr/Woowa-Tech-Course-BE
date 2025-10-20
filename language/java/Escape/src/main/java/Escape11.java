import java.util.regex.Pattern;

// 외부 입력을 정규식 패턴에 삽입할 때는 Pattern.quote()로 전체를 리터럴로 감싸야 안전하다.
// 그렇지 않으면 메타문자가 예기치 않게 정규식으로 해석될 수 있다.
public class Escape11 {
    public static void main(String[] args) {
        String userInput = "C:\\temp\\file.txt"; // 외부 입력 가정

        Pattern p = Pattern.compile("prefix-" + Pattern.quote(userInput) + "-suffix");
        String target = "prefix-C:\\temp\\file.txt-suffix";

        System.out.println("match = " + p.matcher(target).matches()); // true
    }
}