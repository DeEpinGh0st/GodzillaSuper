//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package core.shellprocessor.jspescapes;

import core.annotation.GenerateProcessor;
import core.imp.ShellProcessor;

@GenerateProcessor(
    DisplayName = "无",
    superTemplate = {"jsp", "jspx"}
)
public class JspNone implements ShellProcessor {
    public JspNone() {
    }

    public byte[] doProcessor(byte[] shell, String suffix) {
        return shell;
    }

    public byte[] doProcessor(byte[] shell, String suffix, String pass) {
        return shell;
    }
}
