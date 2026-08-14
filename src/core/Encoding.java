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

    /**
     * Auto 模式下最近一次含 CJK 探测命中的字符集。
     * 编码方向据此回推：目录列表按 GBK 解码出中文后，进入该目录时也必须按 GBK 编码回发，
     * 否则目标端按自身字符集解析路径会「不存在」。纯 ASCII 响应不改变该状态。
     */
    private volatile String lastDecodedCharset;

    private static final String[] ENCODING_TYPES = new String[]{"Auto", "UTF-8", "GBK", "GB2312", "BIG5", "GB18030", "ISO-8859-1", "latin1", "UTF16", "ascii", "cp850"};

    private Encoding(String charsetString) {
        this.charsetString = charsetString;
    }

    public static String[] getAllEncodingTypes() {
        return ENCODING_TYPES;
    }

    public byte[] Encoding(String text) {
        String charset = this.encodeCharset();
        try {
            return charset != null ? text.getBytes(charset) : text.getBytes();
        } catch (UnsupportedEncodingException e) {
            util.Log.error(e);
            return text.getBytes();
        }
    }

    // Auto: use the charset that won the last CJK decode; before any detection fall back to
    // platform default (same as the legacy empty-encoding behavior).
    private String encodeCharset() {
        if (!AUTO.equals(this.charsetString)) {
            return this.charsetString;
        }
        return this.lastDecodedCharset;
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
    private String autoDecoding(byte[] bytes) {
        String utf8 = strictDecode("UTF-8", bytes);
        if (utf8 != null) {
            if (containsCjk(utf8)) {
                this.lastDecodedCharset = "UTF-8";
                return utf8;
            }
            String gbk = strictDecode("GBK", bytes);
            if (gbk != null && containsCjk(gbk)) {
                this.lastDecodedCharset = "GBK";
                return gbk;
            }
            return utf8; // pure ASCII: keep previous detection
        }
        String gbk = strictDecode("GBK", bytes);
        if (gbk != null) {
            this.lastDecodedCharset = "GBK";
            return gbk;
        }
        String gb18030 = strictDecode("GB18030", bytes);
        if (gb18030 != null) {
            this.lastDecodedCharset = "GB18030";
            return gb18030;
        }
        String big5 = strictDecode("BIG5", bytes);
        if (big5 != null) {
            this.lastDecodedCharset = "BIG5";
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
