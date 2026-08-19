package core.ui.component.dialog;

import core.OperationAuditLog;
import core.OperationLogRuntime;
import core.persist.OperationLogStore;
import core.ui.component.OperationLogHistoryView;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

public class OperationAuditLogDialog extends JDialog {
    private static OperationAuditLogDialog shown;
    private final OperationLogHistoryView historyView;
    private final OperationLogRuntime.ChunkListener chunkListener;

    public OperationAuditLogDialog(java.awt.Window owner) {
        super(owner, "\u64cd\u4f5c\u5ba1\u8ba1\u65e5\u5fd7", java.awt.Dialog.ModalityType.MODELESS);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.historyView = new OperationLogHistoryView();
        this.historyView.setPreferredSize(new Dimension(920, 520));
        this.chunkListener = (sessionId, chunk) -> {
            if (this.historyView != null && sessionId != null && sessionId.equals(this.historyView.getSelectedSessionId())) {
                javax.swing.SwingUtilities.invokeLater(
                        () -> this.historyView.appendToDetailIfViewing(sessionId, chunk));
            }
        };
        OperationLogRuntime.addChunkListener(this.chunkListener);
        JButton refreshBtn = new JButton("\u5237\u65b0");
        refreshBtn.addActionListener(e -> this.reload());
        JButton copyBtn = new JButton("\u590d\u5236\u5f53\u524d\u8be6\u60c5");
        copyBtn.addActionListener(e -> this.copyDetail());
        JButton clearBtn = new JButton("\u6e05\u7a7a\u5f53\u524d\u4f1a\u8bdd\u8bb0\u5f55");
        clearBtn.addActionListener(e -> {
            OperationAuditLog.clear();
            this.reload();
        });
        JButton closeBtn = new JButton("\u5173\u95ed");
        closeBtn.addActionListener(e -> this.setVisible(false));
        JPanel south = new JPanel();
        south.add(refreshBtn);
        south.add(copyBtn);
        south.add(clearBtn);
        south.add(closeBtn);
        this.setLayout(new BorderLayout(6, 6));
        this.add(this.historyView, BorderLayout.CENTER);
        this.add(south, BorderLayout.SOUTH);
        this.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        InputMap im = this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        am.put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OperationAuditLogDialog.this.setVisible(false);
            }
        });
        Font global = core.ApplicationContext.getFont();
        this.historyView.applyDetailFont(global != null ? global.deriveFont(Font.PLAIN, 12) : new Font(Font.MONOSPACED, Font.PLAIN, 12));
        Font ui = javax.swing.UIManager.getFont("Label.font");
        if (ui != null) {
            this.historyView.applyTreeFont(ui.deriveFont(Font.PLAIN, Math.max(11f, ui.getSize2D())));
        }
        this.pack();
        this.setLocationRelativeTo(owner);
        this.reload();
    }

    private void reload() {
        if (OperationLogStore.isAvailable()) {
            String cur = OperationLogRuntime.getCurrentSessionId();
            this.historyView.setCurrentSessionMarker(cur);
            this.historyView.reloadTree();
            if (cur != null) {
                this.historyView.selectSessionInTree(cur);
            }
        } else {
            this.historyView.reloadTree();
            this.historyView.setFallbackDetailText(OperationAuditLog.getFullText());
        }
    }

    private void copyDetail() {
        String sid = this.historyView.getSelectedSessionId();
        String t = sid != null && OperationLogStore.isAvailable()
                ? OperationLogStore.loadSessionText(sid)
                : OperationAuditLog.getFullText();
        if (t != null && !t.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(t), null);
        }
    }

    public static void showDialog(java.awt.Window owner) {
        java.awt.Window o = owner != null ? owner : null;
        if (shown == null || !shown.isDisplayable()) {
            shown = new OperationAuditLogDialog(o);
        } else {
            shown.reload();
        }
        shown.setVisible(true);
        shown.toFront();
    }
}
