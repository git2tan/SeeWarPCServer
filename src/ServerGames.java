import java.util.ArrayList;

/**
 * Created by Artem on 13.05.2017.
 */
public class ServerGames {
    private ArrayList<Game> listOfGames;
    private static int id = 1;
    public ServerGames(){
        listOfGames = new ArrayList<Game>();
    }

    public ArrayList<String> getStringListOfGame(){
        ArrayList<String> result = new ArrayList<String>();
        for(Game one : listOfGames){
            result.add(one.toString());
        }
        return result;
    }

    public void createGame(ServerGamer gamer1){
        listOfGames.add(new Game(id++,gamer1));
    }

    public boolean tryToConnect(ServerGamer gamer2, int id){
        Game tmp = null;
        for(Game one : listOfGames){
            if (one.getID() == id)
                tmp = one;
        }
        if(tmp != null){
            return tmp.tryToConnect(gamer2);
        }
        else{
            return false;
        }
    }
    public boolean tryToConnectObs(ServerGamer gamer, int id){
        Game tmp = null;
        for(Game one : listOfGames){
            if (one.getID() == id)
                tmp = one;
        }
        if(tmp != null){
            return tmp.tryToConnectObs(gamer);
        }
        else{
            return false;
        }
    }
    public void deleteGame(Game game){
        listOfGames.remove(game);
    }

}
