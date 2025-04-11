package newserver;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class Client implements Runnable {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 9090;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private GameFrame gui;
    
    public Client(GameFrame gui) throws IOException {
        this.gui = gui;
        socket = new Socket(SERVER_IP, SERVER_PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    // إرسال اسم المستخدم للسيرفر (Connect)
    public void sendUsername(String username) {
        out.println(username);
    }
    
    // إرسال طلب اللعب (Pair Request)
    public void sendPlayRequest() {
        out.println("play");
    }
    
    // إرسال رسائل أخرى للسيرفر (مثل إجابات أو محادثة)
    public void sendMessage(String message) {
        out.println(message);
    }
    
    // إعلام السيرفر بالخروج
    public void quit() {
        out.println("quit");
        close();
    }
    
    // الاستماع للرسائل القادمة من السيرفر وتحديث واجهة المستخدم
    public void listenToServer() {
        new Thread(() -> {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.startsWith("players:")) {
                        String[] players = response.replace("players:", "").split(",");
                        gui.updatePlayerList(new ArrayList<>(List.of(players)));
                    } else {
                        gui.appendMessage(response);
                    }
                }
            } catch (IOException e) {
                gui.appendMessage("Connection lost.");
            }
        }).start();
    }
    
    public void close() {
        try {
            in.close();
            socket.close();
        } catch (IOException e) {
            gui.appendMessage("Error closing connection.");
        }
    }
    
    @Override
    public void run() {
        // يمكن توسيعه إذا احتجنا لتشغيل عمليات إضافية على العميل
    }
}
