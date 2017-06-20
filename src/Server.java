import java.util.ArrayList;

/**
 * Created by Artem on 07.05.2017.
 */
public class Server {
    private GamersQueryProcessor processor;
    private Connector connector;
    private ArrayList<String> connectedList;
    private Lobby lobby;
    private ServerGames allGames;

    public Server(){
        processor = new GamersQueryProcessor(this);
        connector = new Connector(processor);
        connectedList = new ArrayList<String>();
        lobby = new Lobby();
        allGames = new ServerGames();
    }

    public void work(){
        Thread myThreadConnector = new Thread(connector);
        myThreadConnector.start();
    }

    public void addToConnectedGamers(ServerGamer gamer){
        connectedList.add(gamer.getLogin());
    }
    public void deleteFromConnectedGamers(ServerGamer gamer){
        connectedList.remove(gamer.getLogin());
    }
    public boolean isConnected(String login){
        boolean isConnected = false;
        for(String one : connectedList){
            if(one.equals(login))
            {
                isConnected = true;
                break;
            }
        }
        return isConnected;
    }
    public boolean connectToLobby(ServerGamer gamer){
        return lobby.addGamer(gamer);
    }
    public Lobby getLobby(){
        return lobby;
    }
    public void createGame(ServerGamer gamer1){
        allGames.createGame(gamer1);
        tryDisconnectFromLobby(gamer1);
    }
    public ArrayList<String> getListOfGame(){
        return allGames.getStringListOfGame();
    }
    public boolean tryToConnectToGame(ServerGamer gamer2, int id){
        boolean answer = allGames.tryToConnect(gamer2, id);
        if(answer){
            //отправить первому игроку сообщение о том что к нему подключился игрок
            tryDisconnectFromLobby(gamer2);
        }

        return answer;
    }

    public boolean tryToCoonectToGameObs(ServerGamer gamer, int id){
        boolean answer = allGames.tryToConnectObs(gamer, id);

        if(answer) {
            tryDisconnectFromLobby(gamer);
        }

        return answer;
    }
    public boolean tryDisconnectFromLobby(ServerGamer gamer){
        boolean answer = lobby.removeGamer(gamer);
        if (answer)
            lobby.sendMessageToLobby(new Message (MessageCommand.S_C_MessageToLobbyFromServer, gamer.getLogin() + " вышел из лобби",""));
        return answer;
    }
    public void deleteGame(Game game){
        allGames.deleteGame(game);
    }
}
