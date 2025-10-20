import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// 운영체제마다 줄바꿈 규약이 다르며, readAllLines()는 \n, \r, \r\n을 모두 줄끝으로 인식한다.
// 플랫폼 차이를 자동으로 처리해주므로 크로스 플랫폼 호환성이 보장된다.
public class Escape10 {
    public static void main(String[] args) throws IOException {
        String sep = System.lineSeparator();
        System.out.println("플랫폼 줄바꿈: " + sep.length() + " 글자");

        Path tmp = Files.createTempFile("test-", ".txt");
        Files.writeString(tmp, "A\r\nB\r\n", StandardCharsets.UTF_8);

        var lines = Files.readAllLines(tmp, StandardCharsets.UTF_8);
        System.out.println("읽은 라인: " + lines);

        Files.deleteIfExists(tmp);
    }
}