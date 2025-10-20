import java.util.regex.Pattern;
import java.util.regex.Matcher;

// 정규식에서 '\'는 메타문자이므로 자바 문자열 리터럴 안에서는 한 번 더 이스케이프해야 한다.
// 외부에서 받은 입력값을 안전하게 정규식으로 사용할 때는 Pattern.quote()로 전체를 리터럴 처리해야 한다.
public class Escape3 {
    public static void main(String[] args) {

        Pattern digits = Pattern.compile("\\d+");
        System.out.println("[\\d+] \"123\" matches = " + digits.matcher("123").matches()); // true
        System.out.println("[\\d+] \"12a\" matches = " + digits.matcher("12a").matches()); // false

        Pattern backslash = Pattern.compile("\\\\");
        System.out.println("[\\\\] \"\\\\\" matches = " + backslash.matcher("\\").matches());

        String userInput = "C:\\temp\\file.txt";
        Pattern safe = Pattern.compile(Pattern.quote(userInput));
        System.out.println("[quote] match = " + safe.matcher("C:\\temp\\file.txt").matches());

        String text = "email: admin@example.com";
        Pattern email = Pattern.compile("[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}");
        Matcher m = email.matcher(text);
        while (m.find()) System.out.println("found: " + m.group());
    }
}
