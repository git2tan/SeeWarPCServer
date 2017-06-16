import java.util.ArrayList;

/**
 * Created by Artem on 07.05.2017.
 */
public class GamersQueryProcessor {
    private Server server;
    public GamersQueryProcessor(Server server){
        this.server = server;
    }

    public boolean handleIsGamerExistInDB(String login, String pass){
        return DBHandler.getInstance().isAccountValid(login,pass);
    }
    public void connectTheGamer(ServerGamer gamer){
        server.addToConnectedGamers(gamer);
    }
    public void disconnectTheGamer(ServerGamer gamer){
        server.deleteFromConnectedGamers(gamer);
    }
    public boolean isLoginConnected(String login){
        return server.isConnected(login);
    }
    public void handleConnectToLobby(ServerGamer gamer){
        server.connectToLobby(gamer);

        handleMessageToLobby(new Message(107, "В лобби подключился новый игрок - " + gamer.getLogin(),""));
        ArrayList<String> list = server.getListOfGame();
        if (!list.isEmpty()) {
            gamer.handleMessage(new Message(201, list));
        }
    }
    public void handleMessageToLobby(Message message){
        server.getLobby().sendMessageToLobby(message);
    }
    public boolean isLoginExist(String login){
        return DBHandler.getInstance().isLoginAlreadyExist(login);
    }
    public void registrationNewAccount(String login, String pass){
        if(!(DBHandler.getInstance().isLoginAlreadyExist(login)))
            DBHandler.getInstance().addAccount(login,pass);
    }
    public void createGameQueryHandler(ServerGamer gamer1){
        server.createGame(gamer1);
        handleMessageToLobby(new Message(201, server.getListOfGame()));
    }
    public ArrayList<String> getStatisticHandler(int offset){
        return DBHandler.getInstance().getTop5WithOffset(offset);
    }
    public ArrayList<String> getLoginStatisticHandler(String login){
        return DBHandler.getInstance().getInfoForLogin(login);
    }
    public boolean handleTryToConnectToGame(ServerGamer gamer2, int id){
        boolean answer = server.tryToConnectToGame(gamer2, id);
        if (answer)
            handleMessageToLobby(new Message(201, server.getListOfGame()));
        return answer;
    }
    public boolean handleTryToConnectToGameObs(ServerGamer gamer, int id){
        boolean answer = server.tryToCoonectToGameObs(gamer, id);
            if(answer)
                handleMessageToLobby(new Message(201, server.getListOfGame()));

        return answer;
    }
    public void setWinner(ServerGamer gamer){
        DBHandler.getInstance().setWinner(gamer.getLogin());
    }
    public void setLoser(ServerGamer gamer){
        DBHandler.getInstance().setLoser(gamer.getLogin());
    }

    public void deleteGame(Game game){
        server.deleteGame(game);
        handleMessageToLobby(new Message(201, server.getListOfGame()));
    }
}
