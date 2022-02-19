package Chat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

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


            //new Thread(() -> {
            ExecutorService service = Executors.newFixedThreadPool(2);
            service.submit(() -> {
                try {
                    authentification();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGGER.info("[ОШИБКА]: " + e);
                } finally {
                    closeConnection();
                }
            });

            // убить через 120 сек, если не авторизовался
            service.submit(() -> {
                try{
                    TimeUnit.SECONDS.sleep(120);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    LOGGER.info("[ОШИБКА]: " + e);
                }
                if(!isAuth) {
                    try{
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        LOGGER.info("[ОШИБКА]: " + e);
                    }
                }
            });
            service.shutdown();

        } catch (IOException ex) {
            LOGGER.info("Проблема при создании клиента");
        }
    }

    /**
     * Чтение сообщения сервером от клиента
     */
    private void readMessages() throws IOException {
        while (true) {
            String messageFromClient = inputStream.readUTF();// читаем сообщение от клиента
            LOGGER.info("[СООБЩЕНИЕ] от " + name + ": " + messageFromClient);
            if (messageFromClient.equals(ChatConstants.STOP_WORD)) {
                LOGGER.info("[ОТКЛЮЧЕНИЕ ПОЛЬЗОВАТЕЛЯ]: "+ name);
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
                        LOGGER.info("[АВТОРИЗАЦИЯ ПОЛЬЗОВАТЕЛЯ]: " + name + " вошел в чат");
                        isAuth = true;
                        return;
                    } else {
                        sendMsg("Ник уже используется");
                        LOGGER.info("Ник уже используется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                    LOGGER.info("Неверные логин/пароль");
                }
            }
        }
    }
    public void sendMsg(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + e);
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
            LOGGER.error("[ОШИБКА]: " + e);

        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("[ОШИБКА]: " + e);
        }
    }
}
