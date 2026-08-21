//
// Source code recreated from a .class file (javap), faithful to the original
// JOptionPane wrapper, with one addition: SUPPRESS_UI (MCP/headless mode) routes
// all dialogs to util.Log instead of popping UI.
//

package core.ui.component.dialog;

import core.EasyI18N;
import core.ui.component.RTextArea;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.fife.ui.rtextarea.RTextScrollPane;
import util.Log;
import util.UiFunction;

public class GOptionPane {
    public static final Object UNINITIALIZED_VALUE = "uninitializedValue";
    public static final int DEFAULT_OPTION = -1;
    public static final int YES_NO_OPTION = 0;
    public static final int YES_NO_CANCEL_OPTION = 1;
    public static final int OK_CANCEL_OPTION = 2;
    public static final int YES_OPTION = 0;
    public static final int NO_OPTION = 1;
    public static final int CANCEL_OPTION = 2;
    public static final int OK_OPTION = 0;
    public static final int CLOSED_OPTION = -1;
    public static final int ERROR_MESSAGE = 0;
    public static final int INFORMATION_MESSAGE = 1;
    public static final int WARNING_MESSAGE = 2;
    public static final int QUESTION_MESSAGE = 3;
    public static final int PLAIN_MESSAGE = -1;
    public static final String ICON_PROPERTY = "icon";
    public static final String MESSAGE_PROPERTY = "message";
    public static final String VALUE_PROPERTY = "value";
    public static final String OPTIONS_PROPERTY = "options";
    public static final String INITIAL_VALUE_PROPERTY = "initialValue";
    public static final String MESSAGE_TYPE_PROPERTY = "messageType";
    public static final String OPTION_TYPE_PROPERTY = "optionType";
    public static final String SELECTION_VALUES_PROPERTY = "selectionValues";
    public static final String INITIAL_SELECTION_VALUE_PROPERTY = "initialSelectionValue";
    public static final String INPUT_VALUE_PROPERTY = "inputValue";
    public static final String WANTS_INPUT_PROPERTY = "wantsInput";

    /**
     * MCP/headless mode: suppress ALL dialogs, log instead.
     * Set by McpService.startHeadless so MCP tools never block on UI.
     */
    public static volatile boolean SUPPRESS_UI = false;

    public GOptionPane() {
    }

    public static String showInputDialog(Object message) throws java.awt.HeadlessException {
        return showInputDialog((Component)null, message);
    }

    public static String showInputDialog(Object message, Object initialSelectionValue) {
        return showInputDialog((Component)null, message, initialSelectionValue);
    }

    public static String showInputDialog(Component parentComponent, Object message) throws java.awt.HeadlessException {
        return showInputDialog(parentComponent, message, getString("OptionPane.inputDialogTitle", parentComponent), QUESTION_MESSAGE);
    }

    public static String showInputDialog(Component parentComponent, Object message, Object initialSelectionValue) {
        return (String)showInputDialog(parentComponent, message, getString("OptionPane.inputDialogTitle", parentComponent), QUESTION_MESSAGE, (Icon)null, (Object[])null, initialSelectionValue);
    }

    public static String showInputDialog(Component parentComponent, Object message, String title, int messageType) throws java.awt.HeadlessException {
        return (String)showInputDialog(parentComponent, message, title, messageType, (Icon)null, (Object[])null, (Object)null);
    }

    public static Object showInputDialog(Component parentComponent, Object message, String title, int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue) throws java.awt.HeadlessException {
        if (SUPPRESS_UI) {
            Log.error("GOptionPane.showInputDialog (suppressed): " + message);
            return null;
        }
        return JOptionPane.showInputDialog(parentComponent, message, title, messageType, icon, selectionValues, initialSelectionValue);
    }

    public static void showThrowableMessageDialog(Component parentComponent, String message, Throwable throwable) throws java.awt.HeadlessException {
        if (SUPPRESS_UI) {
            Log.error(message);
            Log.error(throwable);
            return;
        }
        RTextArea textArea = new RTextArea();
        RTextScrollPane scrollPane = new RTextScrollPane(textArea, true);
        scrollPane.setLineNumbersEnabled(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        throwable.printStackTrace(printStream);
        printStream.flush();
        printStream.close();
        textArea.setText(out.toString() + "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        scrollPane.setHorizontalScrollBarPolicy(32);
        scrollPane.setPreferredSize(new Dimension(699, 333));
        showMessageDialog((Component)null, scrollPane, message, ERROR_MESSAGE);
    }

    public static void showMessageDialog(Component parentComponent, Object message) throws java.awt.HeadlessException {
        showMessageDialog(parentComponent, message, getString("OptionPane.messageDialogTitle", parentComponent), INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType) throws java.awt.HeadlessException {
        showMessageDialog(parentComponent, message, title, messageType, (Icon)null);
    }

    public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType, Icon icon) throws java.awt.HeadlessException {
        showOptionDialog(parentComponent, message, title, DEFAULT_OPTION, messageType, icon, (Object[])null, (Object)null);
    }

    public static int showConfirmDialog(Component parentComponent, Object message) throws java.awt.HeadlessException {
        return showConfirmDialog(parentComponent, message, getString("OptionPane.titleText", parentComponent), YES_NO_CANCEL_OPTION);
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) throws java.awt.HeadlessException {
        return showConfirmDialog(parentComponent, message, title, optionType, QUESTION_MESSAGE);
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType) throws java.awt.HeadlessException {
        return showConfirmDialog(parentComponent, message, title, optionType, messageType, (Icon)null);
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon) throws java.awt.HeadlessException {
        return showOptionDialog(parentComponent, message, title, optionType, messageType, icon, (Object[])null, (Object)null);
    }

    public static int showOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) throws java.awt.HeadlessException {
        if (SUPPRESS_UI) {
            Log.error("GOptionPane.showOptionDialog (suppressed): " + message);
            return CLOSED_OPTION;
        }
        title = EasyI18N.getI18nString(title);
        if (message instanceof String) {
            message = EasyI18N.getI18nString(message.toString());
        }
        if (initialValue instanceof String) {
            initialValue = EasyI18N.getI18nString(initialValue.toString());
        }
        if (options != null && options.length > 0) {
            for (int i = 0; i < options.length; i++) {
                Object option = options[i];
                if (option != null && option instanceof String) {
                    options[i] = EasyI18N.getI18nString(option.toString());
                }
            }
        }
        Component parent = parentComponent;
        if (parent != null && !(parent instanceof java.awt.Window)) {
            java.awt.Window parentWindow = UiFunction.getParentWindow(parent);
            if (parentWindow != null) {
                parent = parentWindow;
            }
        }
        return JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, icon, options, initialValue);
    }

    public static void showInternalMessageDialog(Component parentComponent, Object message) {
        showInternalMessageDialog(parentComponent, message, getString("OptionPane.messageDialogTitle", parentComponent), INFORMATION_MESSAGE);
    }

    public static void showInternalMessageDialog(Component parentComponent, Object message, String title, int messageType) {
        showInternalMessageDialog(parentComponent, message, title, messageType, (Icon)null);
    }

    public static void showInternalMessageDialog(Component parentComponent, Object message, String title, int messageType, Icon icon) {
        showInternalOptionDialog(parentComponent, message, title, DEFAULT_OPTION, messageType, icon, (Object[])null, (Object)null);
    }

    public static int showInternalConfirmDialog(Component parentComponent, Object message) {
        return showInternalConfirmDialog(parentComponent, message, getString("OptionPane.titleText", parentComponent), YES_NO_CANCEL_OPTION);
    }

    public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
        return showInternalConfirmDialog(parentComponent, message, title, optionType, QUESTION_MESSAGE);
    }

    public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType) {
        return showInternalConfirmDialog(parentComponent, message, title, optionType, messageType, (Icon)null);
    }

    public static int showInternalConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon) {
        return showInternalOptionDialog(parentComponent, message, title, optionType, messageType, icon, (Object[])null, (Object)null);
    }

    public static int showInternalOptionDialog(Component parentComponent, Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
        if (SUPPRESS_UI) {
            Log.error("GOptionPane.showInternalOptionDialog (suppressed): " + message);
            return CLOSED_OPTION;
        }
        title = EasyI18N.getI18nString(title);
        if (message instanceof String) {
            message = EasyI18N.getI18nString(message.toString());
        }
        return JOptionPane.showInternalOptionDialog(parentComponent, message, title, optionType, messageType, icon, options, initialValue);
    }

    public static String showInternalInputDialog(Component parentComponent, Object message) {
        return showInternalInputDialog(parentComponent, message, getString("OptionPane.inputDialogTitle", parentComponent), QUESTION_MESSAGE);
    }

    public static String showInternalInputDialog(Component parentComponent, Object message, String title, int messageType) {
        return (String)showInternalInputDialog(parentComponent, message, title, messageType, (Icon)null, (Object[])null, (Object)null);
    }

    public static Object showInternalInputDialog(Component parentComponent, Object message, String title, int messageType, Icon icon, Object[] selectionValues, Object initialSelectionValue) {
        if (SUPPRESS_UI) {
            Log.error("GOptionPane.showInternalInputDialog (suppressed): " + message);
            return null;
        }
        return JOptionPane.showInternalInputDialog(parentComponent, message, title, messageType, icon, selectionValues, initialSelectionValue);
    }

    public static Frame getFrameForComponent(Component parentComponent) throws java.awt.HeadlessException {
        return JOptionPane.getFrameForComponent(parentComponent);
    }

    public static String getString(Object key, Component c) {
        Locale l = c == null ? Locale.getDefault() : c.getLocale();
        return UIManager.getString(key, l);
    }

    public static JDesktopPane getDesktopPaneForComponent(Component parentComponent) {
        return JOptionPane.getDesktopPaneForComponent(parentComponent);
    }

    public static void setRootFrame(Frame newRootFrame) {
        JOptionPane.setRootFrame(newRootFrame);
    }

    public static Frame getRootFrame() throws java.awt.HeadlessException {
        return JOptionPane.getRootFrame();
    }

    public static void showUpdateObjectPropertyDialog(Object object) {
        // Original was an IntelliJ-forms reflection property editor (GUI-niche).
        if (SUPPRESS_UI) {
            Log.error("GOptionPane.showUpdateObjectPropertyDialog (suppressed): " + object);
            return;
        }
        Log.log("GOptionPane.showUpdateObjectPropertyDialog: %s", object);
    }
}
