import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Artem on 07.05.2017.
 */
public class GamerReceiver implements Runnable{
    private ServerGamer gamer;
    private Decoder decoder;
    private Socket socket;
    public GamerReceiver(ServerGamer gamer, Socket socket){
        this.gamer = gamer;
        this.socket = socket;
        decoder = new Decoder();
    }

    @Override
    public void run() {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String fromClient;
            while(true){
                fromClient = r.readLine();
                if(fromClient != null){
                    System.out.println("Получил (" + gamer.getLogin() + "): " + fromClient);//при получении сообщения обращаемся к геймеру с просьбой обработать сообщения
                    Message message = decoder.decodeString(fromClient);

                    if(!message.isEmpty())
                    {
                        if(message.getNumberOfCommand() == 300)
                        {
                            gamer.handleMessage(message);
                            System.err.println("ПОЛУЧИЛ СООБЩЕНИЕ ОБ ОТКЛЮЧЕНИИ ОТ: " + gamer.getLogin());

                            // завершаем поток?
                            break;
                        }
                        else
                            gamer.handleMessage(message);//если сообщение удачно расшифрованно - отправляем его клиенту (то есть геймеру) вызывая обработчик
                    }

                }else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            gamer.handleDisconnect();
        }
    }
}
