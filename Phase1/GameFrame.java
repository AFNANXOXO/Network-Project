package newserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class GameFrame extends JFrame {
    private JTextField usernameField;
    private JTextArea textArea;
    private JButton connectButton, playButton;
    private Client client;
    private static boolean inPlayRoom = false;

    public GameFrame() {
        setTitle("غرفة الانتظار - لعبكة");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        // لوحة الإدخال العلوية
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        usernameField = new JTextField(10);
        connectButton = new JButton("اتصال");
        topPanel.add(connectButton);
        topPanel.add(usernameField);
        topPanel.add(new JLabel(":اسم المستخدم"));
        add(topPanel, BorderLayout.NORTH);

        // منطقة عرض قائمة الانتظار أو اللاعبين
        JPanel centerPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // جعل النص من اليمين لليسار
        JScrollPane scrollPane = new JScrollPane(textArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // لوحة الأزرار السفلية
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        playButton = new JButton("ابدأ اللعب");
        playButton.setEnabled(false);
        bottomPanel.add(playButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // أحداث الأزرار
        connectButton.addActionListener(e -> connectToServer());
        playButton.addActionListener(e -> {
            inPlayRoom=true;
            if (client != null) {
                client.sendPlayRequest();  // إرسال طلب بدء اللعبة
                playButton.setEnabled(false); // تعطيل زر اللعب بعد النقر عليه
            }
        });
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        if (!username.isEmpty()) {
            try {
                client = new Client(this);
                client.sendUsername(username);
                client.listenToServer(); // الاستماع للخادم بعد الاتصال
                appendMessage("تم الاتصال بالخادم بنجاح.");
                playButton.setEnabled(true); // تمكين زر اللعب بعد الاتصال بالخادم
            } catch (IOException ex) {
                appendMessage("خطأ: تعذر الاتصال بالخادم.");
            }
        } else {
            appendMessage("يرجى إدخال اسم المستخدم.");
        }
    }

    public void updatePlayerList(ArrayList<String> players) {
        SwingUtilities.invokeLater(() -> {
            // تحديث النص فقط بقائمة اللاعبين الحالية
            if (inPlayRoom) {
                textArea.setText("قائمة اللاعبين:\n");
            } else {
                textArea.setText("قائمة الانتظار:\n");
            }

            // إضافة كل لاعب إلى النص
            for (String player : players) {
                textArea.append(player + "\n");
            }
        });
    }

    

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameFrame());
    }
}
