package core;

import core.shell.ShellEntity;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

public class Encoding {
    private String charsetString;

    public static final String AUTO = "Auto";

    private static final String[] ENCODING_TYPES = new String[]{"Auto", "UTF-8", "GBK", "GB2312", "BIG5", "GB18030", "ISO-8859-1", "latin1", "UTF16", "ascii", "cp850"};

    private Encoding(String charsetString) {
        this.charsetString = charsetString;
    }

    public static String[] getAllEncodingTypes() {
        return ENCODING_TYPES;
    }

    public byte[] Encoding(String text) {
        // Auto: encode direction defaults to UTF-8 (payloads are UTF-8 native)
        String charset = AUTO.equals(this.charsetString) ? "UTF-8" : this.charsetString;
        try {
            return text.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            util.Log.error(e);
            return text.getBytes();
        }
    }

    public String Decoding(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        if (AUTO.equals(this.charsetString)) {
            return autoDecoding(bytes);
        }
        try {
            return new String(bytes, this.charsetString);
        } catch (UnsupportedEncodingException e) {
            util.Log.error(e);
            return new String(bytes);
        }
    }

    // Auto: strict UTF-8 first. GBK bytes can occasionally form valid UTF-8
    // (e.g. CJK GBK pairs decode as Hebrew/Cyrillic), so when strict UTF-8
    // succeeds but contains no CJK while strict GBK decode does contain CJK,
    // treat the content as GBK. Never uses isMessyCode (false-positives on
    // pure Chinese+digit strings).
    private static String autoDecoding(byte[] bytes) {
        String utf8 = strictDecode("UTF-8", bytes);
        if (utf8 != null) {
            if (containsCjk(utf8)) {
                return utf8;
            }
            String gbk = strictDecode("GBK", bytes);
            if (gbk != null && containsCjk(gbk)) {
                return gbk;
            }
            return utf8;
        }
        String gbk = strictDecode("GBK", bytes);
        if (gbk != null) {
            return gbk;
        }
        String gb18030 = strictDecode("GB18030", bytes);
        if (gb18030 != null) {
            return gb18030;
        }
        String big5 = strictDecode("BIG5", bytes);
        if (big5 != null) {
            return big5;
        }
        return new String(bytes);
    }

    // Returns null when the byte sequence is not valid in the charset.
    private static String strictDecode(String charset, byte[] bytes) {
        try {
            CharsetDecoder decoder = Charset.forName(charset).newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean containsCjk(String text) {
        for (int i = 0; i < text.length(); ++i) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(text.charAt(i));
            if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
                return true;
            }
        }
        return false;
    }

    public void setCharsetString(String charsetString) {
        this.charsetString = charsetString;
    }

    public String getCharsetString() {
        return this.charsetString;
    }

    public static Encoding getEncoding(ShellEntity shellEntity) {
        return shellEntity.getEncodingModule();
    }

    public static Encoding getEncoding(String charsetString) {
        return new Encoding(charsetString);
    }

    @Override
    public String toString() {
        return this.charsetString;
    }
}
