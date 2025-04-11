package newserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    private static final int PORT = 9090;
    // قائمة اللاعبين المتصلين في اللوبي
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    // قائمة اللاعبين الموجودين في غرفة اللعب (pair request)
    public static ArrayList<ClientHandler> playRoom = new ArrayList<>();
    private static final int MAX_PLAYERS = 4; // الحد الأقصى للاعبين في غرفة اللعب
    private static final int TIMER_SECONDS = 30; // مؤقت بدء اللعبة (يمكن تعديله)

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                synchronized (clients) {
                    clients.add(clientHandler);
                }
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    // بث رسالة لجميع اللاعبين في اللوبي
    public static void broadcastToLobby(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }
    
    // تحديث قائمة اللاعبين في اللوبي وإرسالها لجميع العملاء
    public static void updateLobbyPlayerList() {
        StringBuilder playerList = new StringBuilder("players:");
        synchronized (clients) {
            for (ClientHandler client : clients) {
                playerList.append(client.getUsername()).append(",");
            }
        }
        broadcastToLobby(playerList.toString());
    }
    
    // بث رسالة لجميع اللاعبين في غرفة اللعب
    public static void broadcastToPlayRoom(String message) {
        synchronized (playRoom) {
            for (ClientHandler player : playRoom) {
                player.sendMessage(message);
            }
        }
    }
    
    // تحديث قائمة اللاعبين في غرفة اللعب وإرسالها لكل لاعب بالغرفة
    public static void updatePlayRoomPlayerList() {
        StringBuilder playerList = new StringBuilder("players:");
        synchronized (playRoom) {
            for (ClientHandler player : playRoom) {
                playerList.append(player.getUsername()).append(",");
            }
        }
        broadcastToPlayRoom(playerList.toString());
    }
    
    // إزالة العميل من اللوبي أو غرفة اللعب عند قطع الاتصال
    public static void removeClient(ClientHandler clientHandler) {
        synchronized (clients) {
            clients.remove(clientHandler);
            updateLobbyPlayerList();
        }
        synchronized (playRoom) {
            if (playRoom.remove(clientHandler)) {
                broadcastToPlayRoom(clientHandler.getUsername() + " has left the play room.");
                updatePlayRoomPlayerList();
            }
        }
    }
    
    // الكلاس الداخلي الذي يتولى معالجة اتصال كل عميل
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // --- Connect: تسجيل اسم اللاعب وإعلامه بالاتصال ---
                username = in.readLine(); // استقبال اسم المستخدم
                Server.updateLobbyPlayerList(); // تحديث وإرسال قائمة اللاعبين في اللوبي

                String message;
                while ((message = in.readLine()) != null) {
                    // --- Pair Request: عند ضغط اللاعب على زر "Play" ---
                    if (message.startsWith("play")) {
                        moveToPlayRoom();
                    } else {
                        // يمكن هنا استقبال رسائل محادثة أو أحداث أخرى في اللوبي
                        Server.broadcastToLobby(username + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection error with client: " + e.getMessage());
            } finally {
                Server.removeClient(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
        
        // عند استقبال طلب اللعب من العميل (Pair Request)
        private void moveToPlayRoom() {
            synchronized (playRoom) {
                if (playRoom.size() >= MAX_PLAYERS) {
                    sendMessage("غرفة اللعب ممتلئة، فضلاُ الانتظار");
                } else {
                    // إزالة العميل من قائمة اللوبي وإضافته لغرفة اللعب
                    synchronized (clients) {
                        clients.remove(this);
                    }
                    playRoom.add(this);
                    // --- Player Joined: إعلام اللاعبين في اللوبي بغرفة اللعب ---
                    Server.broadcastToLobby(username + " انضمت لغرفة اللعب");
                   sendMessage("أهلا في غرفة اللعب");
                    updatePlayRoomPlayerList();
                    // يمكن إضافة منطق بدء اللعبة عند اكتمال عدد اللاعبين المطلوب
                }
            }
        }
    }
}
