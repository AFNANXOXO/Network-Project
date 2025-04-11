package newserver;

import javax.swing.*;
import java.awt.*;

public class GameRoom extends JFrame {
    private JTextArea roomTextArea;
    
    public GameRoom() {
        setTitle("Play Room");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        roomTextArea = new JTextArea();
        roomTextArea.setEditable(false);
        add(new JScrollPane(roomTextArea), BorderLayout.CENTER);
    }
    
    // تحديث قائمة اللاعبين في غرفة اللعب (Player Joined)
    public void updateRoomPlayers(String playersList) {
        SwingUtilities.invokeLater(() -> roomTextArea.setText(playersList));
    }
    
    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> roomTextArea.append(message + "\n"));
    }
}
