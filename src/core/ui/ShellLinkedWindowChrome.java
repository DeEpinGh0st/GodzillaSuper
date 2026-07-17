package core.ui;

import core.ui.component.WallpaperLayerPanel;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Applies the same wallpaper + window opacity + light transparency to {@link ShellManage}
 * (Shell \u4ea4\u4e92 / \u94fe\u63a5\u7a97\u53e3). Handles async {@code setContentPane} after load.
 */
public final class ShellLinkedWindowChrome {

    private static final String ATTACHED = "gsl5.ShellLinkedWindowChrome.attached";
    /** ShellManage \u5185\u52a0\u8f7d\u63d0\u793a\uff1b\u5bf9\u6240\u6709 JPanel \u7edf\u4e00 setOpaque(false) \u4f1a\u5bfc\u81f4\u906e\u7f69\u5c42\u65e0\u6cd5\u6b63\u5e38\u6d88\u5931 */
    private static final String LOADING_VIS_SUB = "\u52a0\u8f7d\u53ef\u89c6\u5316";

    private ShellLinkedWindowChrome() {
    }

    public static void attach(ShellManage frame) {
        if (frame == null) {
            return;
        }
        if (Boolean.TRUE.equals(frame.getRootPane().getClientProperty(ATTACHED))) {
            return;
        }
        frame.getRootPane().putClientProperty(ATTACHED, Boolean.TRUE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                scheduleChromePass(frame);
            }
        });

        frame.getRootPane().addPropertyChangeListener("contentPane", ev -> {
            Object nv = ev.getNewValue();
            if (nv instanceof WallpaperLayerPanel) {
                ((WallpaperLayerPanel) nv).reloadFromSettings();
                ModernUi.applyWindowOpacityTo(frame);
                return;
            }
            scheduleChromePass(frame);
        });

        scheduleChromePass(frame);
        SwingUtilities.invokeLater(() -> scheduleChromePass(frame));
        Timer late = new Timer(500, ev -> {
            ensureWallpaperWrapped(frame);
            softenInternals(frame);
            ModernUi.applyWindowOpacityTo(frame);
            hideStuckLoadingOverlay(frame);
            ((Timer) ev.getSource()).stop();
        });
        late.setRepeats(false);
        late.start();
        // Poll a few times: load finishes async; wallpaper wrap races with remove(loadLabel).
        final int[] ticks = {0};
        Timer stripStuck = new Timer(400, ev -> {
            hideStuckLoadingOverlay(frame);
            ticks[0]++;
            if (ticks[0] >= 20 || (frame instanceof ShellManage && ((ShellManage) frame).isUiReady())) {
                hideStuckLoadingOverlay(frame);
                ((Timer) ev.getSource()).stop();
            }
        });
        stripStuck.setRepeats(true);
        stripStuck.start();
    }

    private static void scheduleChromePass(JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            ensureWallpaperWrapped(frame);
            softenInternals(frame);
            ModernUi.applyWindowOpacityTo(frame);
        });
    }

    /**
     * \u9690\u85cf\u6b8b\u7559\u7684\u52a0\u8f7d\u63d0\u793a\uff08\u539f\u56e0\u5df2\u4fee\uff1a\u4e0d\u518d\u5bf9\u4efb\u610f JPanel \u7edf\u4e00 opaque=false\uff09\u3002
     */
    private static void hideStuckLoadingOverlay(JFrame frame) {
        if (!(frame instanceof ShellManage)) {
            return;
        }
        ShellManage sm = (ShellManage) frame;
        Container root = sm.getContentPane();
        if (root instanceof WallpaperLayerPanel) {
            for (Component c : root.getComponents()) {
                if (c instanceof Container) {
                    hideLoadingLabelsInTree((Container) c);
                }
            }
        } else if (root instanceof Container) {
            hideLoadingLabelsInTree((Container) root);
        }
    }

    private static void hideLoadingLabelsInTree(Container c) {
        if (c == null) {
            return;
        }
        Component[] kids = c.getComponents();
        for (Component ch : kids) {
            if (ch instanceof JLabel) {
                String t = ((JLabel) ch).getText();
                if (t != null && (t.contains(LOADING_VIS_SUB)
                        || t.contains("正在加载")
                        || t.contains("正在连接")
                        || t.startsWith("loading"))) {
                    // Prefer remove over setVisible: residual labels stay in layout otherwise.
                    c.remove(ch);
                    c.revalidate();
                    c.repaint();
                }
            } else if (ch instanceof Container) {
                hideLoadingLabelsInTree((Container) ch);
            }
        }
    }

    static void ensureWallpaperWrapped(JFrame frame) {
        Container cp = frame.getContentPane();
        if (cp instanceof WallpaperLayerPanel) {
            ((WallpaperLayerPanel) cp).reloadFromSettings();
            hideStuckLoadingOverlay(frame);
            return;
        }
        if (!(cp instanceof JPanel)) {
            return;
        }
        JPanel inner = (JPanel) cp;
        // Snapshot children before wrap so loadLabel/tabbedPane stay under the same panel.
        WallpaperLayerPanel wall = new WallpaperLayerPanel();
        wall.setContentRoot(inner);
        frame.setContentPane(wall);
        inner.setOpaque(false);
        wall.reloadFromSettings();
        ModernUi.applyWindowOpacityTo(frame);
        hideStuckLoadingOverlay(frame);
    }

    private static void softenInternals(JFrame frame) {
        if (!(frame instanceof ShellManage)) {
            return;
        }
        ShellManage sm = (ShellManage) frame;
        JTabbedPane tp = sm.getTabbedPane();
        if (tp != null) {
            tp.setOpaque(false);
        }
        Container root = sm.getContentPane();
        if (root instanceof WallpaperLayerPanel) {
            for (Component c : root.getComponents()) {
                if (c instanceof Container) {
                    descendSoften((Container) c, 0, 10);
                }
            }
        } else if (root instanceof Container) {
            descendSoften((Container) root, 0, 10);
        }
    }

    private static void descendSoften(Container c, int depth, int maxDepth) {
        if (c == null || depth > maxDepth) {
            return;
        }
        if (c instanceof WallpaperLayerPanel) {
            for (Component ch : c.getComponents()) {
                if (ch instanceof Container) {
                    descendSoften((Container) ch, depth + 1, maxDepth);
                }
            }
            return;
        }
        if (c instanceof JTabbedPane) {
            ((JComponent) c).setOpaque(false);
        } else if (c instanceof JSplitPane) {
            ((JSplitPane) c).setOpaque(false);
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
        }
        /* \u4e0d\u5bf9\u666e\u901a JPanel \u7edf\u4e00 setOpaque(false)\uff0c\u5426\u5219\u52a0\u8f7d\u906e\u7f69/\u5361\u7247\u5e03\u5c40\u4f1a\u5f02\u5e38\uff0c\u51fa\u73b0\u201c\u6b63\u5728\u52a0\u8f7d\u53ef\u89c6\u5316\u9875\u9762\u201d\u6b8b\u7559 */
        for (Component ch : c.getComponents()) {
            if (ch instanceof Container) {
                descendSoften((Container) ch, depth + 1, maxDepth);
            }
        }
    }

    public static void reloadWallpaperOnAllShellWindows() {
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w instanceof ShellManage) {
                Container cp = ((ShellManage) w).getContentPane();
                if (cp instanceof WallpaperLayerPanel) {
                    ((WallpaperLayerPanel) cp).reloadFromSettings();
                }
            }
        }
    }

    public static void applyOpacityToAllShellWindows() {
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w instanceof ShellManage) {
                ModernUi.applyWindowOpacityTo(w);
            }
        }
    }
}
