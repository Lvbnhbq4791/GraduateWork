import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"), new File("stop-ru.txt"));
        GsonBuilder builder = new GsonBuilder();
        try (ServerSocket serverSocket = new ServerSocket(8989)) { // стартуем сервер один(!) раз
            while (true) { // в цикле(!) принимаем подключения
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {
                    Gson gson = builder.setPrettyPrinting()
                            .create();
                    String text = in.readLine();
                    System.out.println("Запрос поиска слово: " + text);
                    List<PageEntry> answer = engine.search(text);
                    if (!answer.isEmpty()) {
                        out.println(gson.toJson(answer));
                    } else {
                        out.println("В тексте слова " + "<<" + text + ">>" + " нет");
                    }
                } catch (IOException e) {
                    System.out.println("Не могу стартовать сервер");
                    e.printStackTrace();
                }
            }
        }
    }
}