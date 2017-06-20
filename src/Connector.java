import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Artem on 07.05.2017.
 */
public class Connector implements Runnable {
    GamersQueryProcessor processor;

    public Connector(GamersQueryProcessor processor){
        this.processor = processor;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(4444)) {
            System.out.println("Server is :" + serverSocket.getLocalSocketAddress());
            while(true){
                Socket tmpSocket = null;
                tmpSocket = serverSocket.accept();
                if(tmpSocket!=null) {
                    //при подключении создает нового анонимного серверного геймер
                    System.out.println("Подключен новый пользователь from " + tmpSocket.getInetAddress().toString());
                    ServerGamer gamer = new ServerGamer(tmpSocket, processor);
                    System.out.println("Жду нового пользователя");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}