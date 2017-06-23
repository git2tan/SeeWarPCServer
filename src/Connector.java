import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Artem on 07.05.2017.
 */
public class Connector implements Runnable {
    GamersQueryProcessor processor;
    int port;
    public Connector(GamersQueryProcessor processor, int port){
        this.processor = processor;
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
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