package Chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Обслуживает клиента (отвечает за связь между клиентом и сервером)
 */
public class ClientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private volatile boolean isAuth;
    private String name; // ник пользователя
    public String getName() {
        return name;
    }
    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    authentification();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();

            // убить через 120 сек, если не авторизовался
            Thread killThread = new Thread(() -> {
                try{
                    TimeUnit.SECONDS.sleep(120);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!isAuth) {
                    try{
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            killThread.start();

        } catch (IOException ex) {
            System.out.println("Проблема при создании клиента");
        }
    }
    private void readMessages() throws IOException {
        while (true) {
            String messageFromClient = inputStream.readUTF();
            System.out.println("от " + name + ": " + messageFromClient);
            if (messageFromClient.equals(ChatConstants.STOP_WORD)) {
                return;
            } else if (messageFromClient.startsWith(ChatConstants.SEND_TO_LIST)||(messageFromClient.startsWith(ChatConstants.SEND_TO_ONE_CLIENT))) {
                String[] splittedStr = messageFromClient.split("\\s+");
                List<String> nicknames = new ArrayList<>();
                for (int i = 1; i < splittedStr.length - 1; i++) {
                    nicknames.add(splittedStr[i]);}
                nicknames.add(this.name);

                server.broadcastMessageToClients(messageFromClient,nicknames);
            }
            else if (messageFromClient.startsWith(ChatConstants.CHANGE_NICK)) {
                // получить новый ник
                String[] messageList = messageFromClient.split("\\s+");
                String newNick = messageList[1];

                // проверить, что данный ник можно использовать
                String messageFull;
                if(server.getAuthService().isNickFree(newNick)) {
                    String temp = name;
                    server.getAuthService().updateNick(newNick, name);
                    name = newNick;
                    messageFull = temp + " сменил ник на " + name;
                    server.broadcastMessage(messageFull); // отправить всем авторизованным пользователем, сообщение о смене ника пользователя
                } else {
                    messageFull = "Данный ник занят";
                    server.broadcastMessageToClients(messageFull, Collections.singletonList(this.name));
                }
            }

            else {
                server.broadcastMessage("[" + name + "]: " + messageFromClient);
            }
        }
    }
    // /auth login pass
    private void authentification() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(ChatConstants.AUTH_COMMAND)) {
                String[] parts = message.split("\\s+");
                String nick = server.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick!= null) {
                    //проверим, что такого нет
                    if (!server.isNickBusy(nick)) {
                        sendMsg(ChatConstants.AUTH_OK + " " + nick);
                        name = nick;
                        server.subscribe(this);// добавить пользователя в сервис обмена сообщениями
                        server.broadcastMessage(name + " вошел в чат");
                        isAuth = true;
                        return;
                    } else {
                        sendMsg("Ник уже используется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                }
            }
        }
    }
    public void sendMsg(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        server.unsubscribe(this);// закрываем соединение с сервером обмена сообщениями
        server.broadcastMessage(name + " вышел из чата");// сообщение всем авторизованным пользователям: ник вышел из чата
        //закрываем все открытые потоки
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
