package io.manbang.easybytecoder.clientbootstrap.Loder.gui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import io.manbang.easybytecoder.clientbootstrap.Loder.AgentLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author xujie
 */
public class ViewMenu {


    public void SelectPid(HashMap<String, String> pids) throws IOException {


        // Setup terminal and screen layers
        final Terminal terminal = new DefaultTerminalFactory().createTerminal();
        final Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        final WindowBasedTextGUI messageDialog = new MultiWindowTextGUI(screen);

        new MessageDialogBuilder()
                .setTitle("easyByteCoder")
                .setText("请选择进行动态字节码注入的进程")
                .addButton(MessageDialogButton.OK)
                .build()
                .showDialog(messageDialog);

        // Create panel to hold components
        final Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(10));


        final Table<String> table = new Table<String>("进程名", "进程Pid");

        for (String s : pids.keySet()) {
            table.getTableModel().addRow(s, pids.get(s));
        }
        table.setSelectAction(new Runnable() {
            @Override
            public void run() {
                List<String> data = table.getTableModel().getRow(table.getSelectedRow());
                final WindowBasedTextGUI confirmGui = new MultiWindowTextGUI(screen);
                MessageDialog confirmButton = new MessageDialogBuilder()
                        .setTitle("确认注入")
                        .setText("应用名为:" + data.get(0) + "   pid为:" + data.get(1))
                        .addButton(MessageDialogButton.OK)
                        .addButton(MessageDialogButton.Cancel)
                        .build();

                MessageDialogButton messageDialogButton = confirmButton.showDialog(confirmGui);

                if ("OK".equals(messageDialogButton.name())) {

                    try {
                        screen.close();
                        terminal.close();
                        AgentLoader.start(data.get(1));
                        return;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        panel.addComponent(table);


        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setComponent(panel);

        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);


    }
}
