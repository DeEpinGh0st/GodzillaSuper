package core.shellprocessor;

import core.ApplicationContext;
import core.annotation.GenerateProcessor;
import core.imp.ShellProcessor;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class StartProcessor {
    public static final HashMap<String, LinkedHashSet<Class>> processors;

    // MCP/headless 自动选择处理器：设置后 process() 不再弹 ChooseProcessor 对话框。
    // 值: null=GUI 弹窗选择; "none"=不处理; 类简单名(如 JspEscapesProcessor)=自动用该处理器
    private static final ThreadLocal<String> AUTO_PROCESSOR = new ThreadLocal<>();

    public static void setAutoProcessor(String name) {
        AUTO_PROCESSOR.set(name);
    }

    public static void clearAutoProcessor() {
        AUTO_PROCESSOR.remove();
    }

    public static byte[] process(byte[] bytes, String suffix) {
        LinkedHashSet<Class> classes = processors.get(suffix);
        if (classes != null) {
            String autoName = AUTO_PROCESSOR.get();
            Class selected = null;
            if (autoName != null) {
                if ("none".equalsIgnoreCase(autoName)) {
                    return bytes;
                }
                for (Class c : classes) {
                    if (c.getSimpleName().equalsIgnoreCase(autoName)) {
                        selected = c;
                        break;
                    }
                }
            } else {
                selected = ChooseProcessor.chooseProcessor((Class[]) classes.toArray(new Class[0]));
            }
            if (selected != null) {
                try {
                    ShellProcessor sp = (ShellProcessor) selected.newInstance();
                    return sp.doProcessor(bytes, suffix);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        return bytes;
    }

    static {
        processors = new HashMap<>();
        List<Class> list = ApplicationContext.scanClass(
                ApplicationContext.class.getResource("/core/shellprocessor/"),
                "core.shellprocessor", ShellProcessor.class, GenerateProcessor.class);
        for (Class clazz : list) {
            GenerateProcessor gp = (GenerateProcessor) clazz.getAnnotation(GenerateProcessor.class);
            for (String s : gp.superTemplate()) {
                LinkedHashSet<Class> set = processors.get(s);
                if (set == null) {
                    set = new LinkedHashSet<>();
                    processors.put(s, set);
                }
                set.add(clazz);
            }
        }
    }
}
