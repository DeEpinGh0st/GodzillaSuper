//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package core.shellprocessor.phpInclude;

import core.annotation.GenerateProcessor;
import core.imp.ShellProcessor;

@GenerateProcessor(
    DisplayName = "无",
    superTemplate = {"php"}
)
public class PhpNone implements ShellProcessor {
    public PhpNone() {
    }

    public byte[] doProcessor(byte[] shell, String suffix) {
        return shell;
    }
}
