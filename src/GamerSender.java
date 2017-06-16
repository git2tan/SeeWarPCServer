import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Artem on 07.05.2017.
 */
public class GamerSender {
    private Socket socket;
    private ServerGamer myGamer;
    private PrintWriter toServer;
    private Decoder decoder;
    public GamerSender(Socket socket, ServerGamer gamer){
        this.socket = socket;
        this.myGamer = gamer;
        decoder = new Decoder();

        try {
            toServer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO Обработать ошибку и отключить игрока
        }
    }

    public void sendMessage(Message message){
        String tmp = decoder.codeMessage(message);
        System.out.println("Отправил игроку" + myGamer.getLogin() + ": " + tmp);
        toServer.println(tmp);
    }
}
