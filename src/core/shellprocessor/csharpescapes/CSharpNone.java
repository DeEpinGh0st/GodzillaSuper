//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package core.shellprocessor.csharpescapes;

import core.annotation.GenerateProcessor;
import core.imp.ShellProcessor;

@GenerateProcessor(
    DisplayName = "无",
    superTemplate = {"aspx", "ashx", "asmx", "soap"}
)
public class CSharpNone implements ShellProcessor {
    public CSharpNone() {
    }

    public byte[] doProcessor(byte[] shell, String suffix) {
        return shell;
    }

    public byte[] doProcessor(byte[] shell, String suffix, String pass) {
        return shell;
    }
}
