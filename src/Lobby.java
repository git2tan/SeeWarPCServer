import java.util.ArrayList;

/**
 * Created by Artem on 08.05.2017.
 */
public class Lobby {
    private ArrayList<ServerGamer> gamerInLobby;
    public Lobby(){
        this.gamerInLobby = new ArrayList<ServerGamer>();
    }
    public void sendMessageToLobby(Message message){
        for(ServerGamer one : gamerInLobby){
            one.handleMessage(message);
        }
    }
    public boolean addGamer(ServerGamer gamer){
        if(!gamerInLobby.contains(gamer)){
            gamerInLobby.add(gamer);
            return true;
        }
        else
            return false;

    }

    public boolean removeGamer(ServerGamer gamer){
        boolean answer = false;
        if(gamerInLobby.contains(gamer))
            answer =true;
        gamerInLobby.remove(gamer);
        return answer;
    }
}
