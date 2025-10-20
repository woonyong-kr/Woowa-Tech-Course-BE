import java.util.regex.Pattern;

// 정규식에서 '.'은 임의의 한 문자를 의미하므로 리터럴 점을 매칭하려면 '\.'로 이스케이프해야 한다.
// 자바 문자열 리터럴에서는 역슬래시를 한 번 더 써야 하므로 "\\."로 작성한다.
public class Escape12 {
    public static void main(String[] args) {
        Pattern version = Pattern.compile("v\\d+\\.\\d+");

        System.out.println("v10.3 matches = " + version.matcher("v10.3").matches());
        System.out.println("v10x3 matches = " + version.matcher("v10x3").matches());
        System.out.println("v10a3 matches = " + version.matcher("v10a3").matches());
    }
}