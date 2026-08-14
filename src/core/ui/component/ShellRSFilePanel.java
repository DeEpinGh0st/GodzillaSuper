package core.ui.component;

import core.ApplicationContext;
import core.EasyI18N;
import core.Encoding;
import core.imp.Payload;
import core.shell.ShellEntity;
import core.ui.component.dialog.GOptionPane;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import org.fife.ui.rtextarea.RTextScrollPane;
import util.Log;
import util.UiFunction;
import util.automaticBindClick;
import util.functions;

public class ShellRSFilePanel extends JPanel {
    private JComboBox<String> encodingComboBox;
    private JButton saveButton;
    private JButton refreshButton;
    private JButton backButton;
    private JTextField readFileTextField;
    private RTextArea fileDataTextArea;
    private JPanel parentPanel;
    private Payload payload;
    private CardLayout cardLayout;
    private JSplitPane splitPane;
    private ShellEntity shellContext;
    private byte[] fileData;
    private String containerName;
    private JPanel topPanel;
    private RTextScrollPane scrollPane;
    private String encodingTypeString;
    private Encoding encodingModule;
    private String currentFile;

    public ShellRSFilePanel(ShellEntity shellContext, JPanel parentPanel, String containerName) {
        super(new java.awt.BorderLayout());
        this.cardLayout = null;
        this.containerName = null;
        this.parentPanel = parentPanel;
        if (parentPanel != null) {
            this.cardLayout = (CardLayout)parentPanel.getLayout();
            this.containerName = containerName;
        }

        this.payload = shellContext.getPayloadModule();
        this.shellContext = shellContext;
        this.topPanel = new JPanel();
        this.encodingComboBox = new JComboBox(ApplicationContext.getAllEncodingTypes());
        this.encodingModule = Encoding.getEncoding("Auto");
        this.saveButton = new JButton("保存");
        this.refreshButton = new JButton("刷新");
        this.backButton = new JButton("返回");
        this.splitPane = new JSplitPane();
        this.readFileTextField = new JTextField(80);
        this.fileDataTextArea = new RTextArea();
        this.scrollPane = new RTextScrollPane(this.fileDataTextArea, true);
        this.scrollPane.setIconRowHeaderEnabled(true);
        this.scrollPane.getGutter().setBookmarkingEnabled(true);

        this.topPanel.add(this.readFileTextField, (new GBC(1, 1)).setFill(2).setWeight(1.0D, 0.0D));
        this.topPanel.add(this.encodingComboBox);
        this.topPanel.add(this.saveButton);
        this.topPanel.add(this.refreshButton);
        this.topPanel.add(this.backButton);
        this.splitPane.setOrientation(0);
        this.splitPane.setTopComponent(this.topPanel);
        this.splitPane.setBottomComponent(this.scrollPane);
        this.encodingComboBox.addActionListener((e) -> {
            Object item = this.encodingComboBox.getSelectedItem();
            this.encodingTypeString = item instanceof String ? (String)item : null;
            this.encodingModule.setCharsetString(this.encodingTypeString == null ? "UTF-8" : this.encodingTypeString);
            this.refreshData();
        });
        automaticBindClick.bindJButtonClick(this, this);
        Object selected = this.encodingComboBox.getSelectedItem();
        this.encodingTypeString = selected instanceof String ? (String)selected : null;
        this.add(this.splitPane);
        EasyI18N.installObject(this);
    }

    public void rsFile(String file) {
        if (!this.payload.isAlive() && !this.shellContext.isUseCache()) {
            this.readFileTextField.setText(this.currentFile);
            GOptionPane.showMessageDialog(this, "刷新失败 有效载荷已经销毁", "提示", 2);
            return;
        }

        this.currentFile = file;
        this.readFileTextField.setText(file);
        this.fileData = this.payload.downloadFile(file);
        UiFunction.setSyntaxEditingStyle(this.fileDataTextArea, file);
        this.refreshData();
    }

    private void refreshData() {
        byte[] data = this.fileData == null ? new byte[0] : this.fileData;
        this.fileDataTextArea.setText(this.encodingModule.Decoding(data));
    }

    public void saveButtonClick(ActionEvent actionEvent) {
        if (this.payload.isAlive()) {
            String file = this.readFileTextField.getText();
            boolean ok = this.payload.uploadFile(file, this.encodingModule.Encoding(this.fileDataTextArea.getText()));
            if (ok) {
                GOptionPane.showMessageDialog(this, "保存成功", "提示", 1);
            } else {
                GOptionPane.showMessageDialog(this, "保存失败", "提示", 2);
            }
        } else {
            GOptionPane.showMessageDialog(this, "保存失败 有效载荷已经销毁", "提示", 2);
        }
    }

    public void refreshButtonClick(ActionEvent actionEvent) {
        this.rsFile(this.readFileTextField.getText());
    }

    public void backButtonClick(ActionEvent actionEvent) {
        if (this.cardLayout != null) {
            this.fileData = null;
            this.fileDataTextArea.setText("");
            this.readFileTextField.setText("");
            this.cardLayout.show(this.parentPanel, this.containerName);
        }
    }

    public String getFile() {
        return this.currentFile;
    }

    public String getShellId() {
        return this.shellContext.getId();
    }

    public void openThisToEditFileFrame() {
        core.ui.component.frame.EditFileFrame.OpenNewEdit(this);
    }
}

