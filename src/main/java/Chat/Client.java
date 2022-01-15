package Chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Client extends JFrame {

    private Socket socket; //соединение с сервером

    private JTextArea chatArea; //текстовое поле для вывода сообщений

    private JTextField inputField; // текстовое поле для ввода сообщений

    private DataInputStream inputStream; //поток ввода
    private DataOutputStream outputStream;  //поток вывода

    private DataOutputStream fileOutputStream; // поток для записи в файл
    private DataInputStream fileInputStream;
    private String nameFile;
    private File file;

    public String getNameFile() {
        return nameFile;
    }

    public Client() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initGUI();
    }
    //открытие соединения
    private void openConnection() throws IOException {
        socket = new Socket(ChatConstants.HOST, ChatConstants.PORT); //открытие сокета
        inputStream = new DataInputStream(socket.getInputStream()); //доступ к исходящему потоку сервера
        outputStream = new DataOutputStream(socket.getOutputStream()); //доступ к входящему потоку сервера

        //входящие сообщения
        new Thread(() -> {
            try {
                //auth
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    chatArea.append(strFromServer + "\n");
                    if (strFromServer.startsWith(ChatConstants.AUTH_OK)) {
                        break;
                    }
                }

                // сообщение nick вошел в чат
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    chatArea.append(strFromServer + "\n");
                    if (strFromServer.endsWith("вошел в чат")) {
                        // создать файл
                        String[] messageFull = strFromServer.split("\\s+");
                        nameFile = "history_" + messageFull[0] + ".txt";
                        file = new File(getNameFile());
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        // добавить историю в чат
                        chatArea.append("Начало истории чата: \n");
                        chatArea.append(sendHistory());
                        chatArea.append("Конец истории чата.\n");
                        chatArea.append("\n");
                        break;
                    }
                }


                //read
                fileOutputStream = new DataOutputStream(new FileOutputStream(getNameFile(), true));
                saveHistory(LocalDateTime.now().toString());
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    saveHistory(strFromServer);
                    if (strFromServer.equals(ChatConstants.STOP_WORD)) {
                        break;
                    } else if (strFromServer.startsWith(ChatConstants.CLIENTS_LIST)) {
                        chatArea.append("Сейчас онлайн " + strFromServer);
                    } else {
                        chatArea.append(strFromServer);
                    }
                    chatArea.append("\n");
                }
            } catch (IOException ex) {
                System.out.println("Fail!");
                Runtime.getRuntime().exit(0);
            }
        }).start();
    }

    private void saveHistory(String text) throws IOException {
        fileOutputStream.writeUTF(text + "\n");
    }

    public String sendHistory() {
        List<String> list = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        try {
            fileInputStream = new DataInputStream(new FileInputStream(getNameFile()));
            String line;
            while(fileInputStream.available()>0) {
                line = fileInputStream.readUTF();
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // поставить условие, если list.size() >= 100
        if(list.size() >= 100) {
            for(int i = list.size() - 100; i < list.size(); i++) {
                text.append(list.get(i));
            }
        } else {
            for (String s : list) {
                text.append(s);
            }
        }
        return text.toString();
    }

    public void closeConnection() {
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

    public void initGUI() {
        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Message area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        //down pannel
        JPanel panel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        // inputField.setBounds(100, 100, 150, 30);
        panel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        panel.add(sendButton, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());

        inputField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    outputStream.writeUTF(ChatConstants.STOP_WORD);
                    closeConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);

    }

    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            try {
                outputStream.writeUTF(inputField.getText());
                inputField.setText("");
                inputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Send error occured");
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
