import java.util.ArrayList;

/**
 * Created by Artem on 08.05.2017.
 */
public class Game {
    private int id;
    private ServerGamer gamer1;
    private ServerGamer gamer2;
    private ArrayList<ServerGamer> observers;
    private boolean isGameStarted;
    private boolean isClosed;


    public Game(int id, ServerGamer gamer1){
        this.gamer1 = gamer1;
        this.id = id;
        observers = new ArrayList<ServerGamer>();
        gamer2 = null;
        gamer1.setGame(this);
        isGameStarted = false;
        isClosed = false;
    }

    public boolean tryToConnect(ServerGamer gamer2){
        if (this.gamer2 == null){
            this.gamer2 = gamer2;
            gamer2.setGame(this);
            sendMessageToObs(new Message(MessageCommand.S_C_ShowObserverActivity, this.toString2(), ""));
            return true;
        }
        else{
            return false;
        }

    }

    public boolean tryToConnectObs(ServerGamer gamer){
        observers.add(gamer);
        gamer.setGame(this);
        sendMessageToObs(new Message(MessageCommand.S_C_ShowObserverActivity, this.toString2(), ""));
        // TODO отправить актуальное состояние игровых полей если начали смотреть уже после того как игроки начали стрелять (стартанули)
        if(isGameStarted)
            gamer.handleMessage(new Message(MessageCommand.S_C_ToObs_ActualGameInfo, gamer1.boardToString(), gamer2.boardToString()));
        return true;
    }

    public void disconnectObs(ServerGamer gamer){
        observers.remove(gamer);

        sendMessageToGamers(new Message(MessageCommand.S_C_NewGameInfo,this.toString(),""));
        sendMessage(new Message(MessageCommand.S_C_MessageToLobbyAboutCoonect,"Obs left: " + gamer.getLogin() + "", ""));
    }

    @Override
    public String toString() {
        return String.format("%06d%02d%s%s%04d", id,gamer1.getLogin().length(), gamer1.getLogin(), gamer2 == null?"0":"1",observers.size());

    }
    public String toString2(){
        String tmp = "";
        if(gamer2 != null){
            tmp = String.format("%s%02d%s%s", ("1"), (gamer2.getLogin().length()), (gamer2.getLogin()), (gamer2.isReady()?"1":"0"));
        }
        else{
            tmp = "0";
        }
        String answer = String.format("%06d%02d%s%s%s%04d", id, gamer1.getLogin().length(), gamer1.getLogin(),(gamer1.isReady()?"1":"0"), tmp, observers.size());
        return answer;
    }

    public int getID(){
        return id;
    }
    public ServerGamer getGamer1(){
        return gamer1;
    }

    public ServerGamer getGamer2() {
        return gamer2;
    }

    public void sendMessage(Message message){
        gamer1.handleMessage(message);
        if(gamer2 != null)
            gamer2.handleMessage(message);

        for(ServerGamer gamer :observers)
            gamer.handleMessage(message);
    }

    public int getObserverCount(){
        return observers.size();
    }

    public void startGame(){
        isGameStarted = true;
        gamer1.handleMessage(new Message(MessageCommand.S_C_ToHostGamerStartTheGame,"", ""));
        gamer2.handleMessage(new Message(MessageCommand.S_C_ToGamer2StartTheGame,"",""));
        for(ServerGamer gamer :observers)
            gamer.handleMessage(new Message(MessageCommand.S_C_AllowObserveTheGame,"",""));
    }

    public void shot(Message message, ServerGamer gamer){
        int x = message.getCoordX();
        int y = message.getCoordY();
        // выстреливший геймер это первый геймер (хостовый)
        if(gamer.getLogin() == gamer1.getLogin()){
            if(gamer2.isHit(x, y)){
                // если попал
                gamer1.handleMessage(new Message(MessageCommand.S_C_YouHitToCoord, "" + x,"" + y));  //посылаем ему обратно что он попал
                gamer2.handleMessage(new Message (MessageCommand.S_C_OpponentHitToYou, "" + x,"" + y)); //посылаем второму что по нему попали
                sendMessageToObs(new Message(MessageCommand.S_C_LoginFireToCoordAndHit, gamer1.getLogin(),"" + x + "" + y)); // геймер1 попал по полю с координатами

                if(!gamer2.isShipNotDestroyed(x,y)) {
                    gamer1.handleMessage(new Message(MessageCommand.S_C_YouDestroyTheShipByCoord, "" + x, "" + y));  // послать сообщение об уничтожении корабля
                    gamer2.setDestroyedShipAt(x,y);
                    gamer2.handleMessage(new Message(MessageCommand.S_C_YourShipByCoordIsDestroyed,"" + x, "" + y));
                    sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginDestroyShipByCoord, gamer1.getLogin(),"" + x + "" + y)); // геймер1 убил корабль с координатами
                }

                if(!gamer2.haveShip()){
                    // послать сообщение о выигрише и проигрыше соответственно
                    if (!isClosed) {
                        isClosed = true;
                        gamer2.handleMessage(new Message(MessageCommand.S_C_YouLose, "", ""));   // проиграл
                        gamer2.setLoser();
                        gamer1.handleMessage(new Message(MessageCommand.S_C_YouWin, "", ""));   // выиграл
                        gamer1.setWinner();
                        sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginWin, gamer1.getLogin(), ""));
                    }
                }
            }
            else{
                gamer1.handleMessage(new Message(MessageCommand.S_C_YouMissToCoord, "" + x,"" + y));
                gamer2.handleMessage(new Message (MessageCommand.S_C_OpponentMissToYou, "" + x,"" + y));
                sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginFireToCoordAndMiss, gamer1.getLogin(),"" + x + "" + y)); // геймер1 промазал по полю с координатами
            }
        }
        else if(gamer.getLogin() == gamer2.getLogin()){
            if(gamer1.isHit(x, y)){
                gamer2.handleMessage(new Message(MessageCommand.S_C_YouHitToCoord, "" + x,"" + y));
                gamer1.handleMessage(new Message (MessageCommand.S_C_OpponentHitToYou, "" + x,"" + y));
                sendMessageToObs(new Message(MessageCommand.S_C_LoginFireToCoordAndHit, gamer2.getLogin(),"" + x + "" + y)); // геймер1 попал по полю с координатами

                if(!gamer1.isShipNotDestroyed(x,y)) {
                    gamer2.handleMessage(new Message(MessageCommand.S_C_YouDestroyTheShipByCoord, "" + x, "" + y));  // послать сообщение об уничтожении корабля
                    gamer1.setDestroyedShipAt(x,y);
                    gamer1.handleMessage(new Message(MessageCommand.S_C_YourShipByCoordIsDestroyed,"" + x, "" + y));
                    sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginDestroyShipByCoord, gamer2.getLogin(),"" + x + "" + y)); // геймер1 убил корабль с координатами
                }

                if(!gamer1.haveShip()){
                    // послать сообщение о выигрише и проигрыше соответственно
                    if (!isClosed) {
                        isClosed = true;
                        gamer1.handleMessage(new Message(MessageCommand.S_C_YouLose, "", ""));   // проиграл
                        gamer1.setLoser();
                        gamer2.handleMessage(new Message(MessageCommand.S_C_YouWin, "", ""));   // выиграл
                        gamer2.setWinner();
                        sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginWin, gamer2.getLogin(), ""));
                    }
                }
            }
            else{
                gamer2.handleMessage(new Message(MessageCommand.S_C_YouMissToCoord, "" + x,"" + y));
                gamer1.handleMessage(new Message (MessageCommand.S_C_OpponentMissToYou, "" + x,"" + y));
                sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginFireToCoordAndMiss, gamer2.getLogin(),"" + x + "" + y)); // геймер1 промазал по полю с координатами
            }
        }
    }
    public void deleteGamerFromGame(ServerGamer gamer){

        if(gamer == gamer2){
            //TODO послать сообщение о том что игрока отключили от игры
            gamer2.setGame(null);
            gamer2 = null;

        }
        else if(gamer1 == gamer){
            //TODO послать сообщение о том что хостовый игрок отменил игру и всех отключить от игры
        }
    }
    private void sendMessageToObs(Message message){
        for(ServerGamer one : observers)
            one.handleMessage(message);
    }

    public void handleSurrenderMessage(ServerGamer gamer){
        if(gamer.getLogin().equals(gamer1.getLogin())){
            //пожелал сдаться первый игрок
            if (!isClosed) {
                isClosed = true;
                gamer1.handleMessage(new Message(MessageCommand.S_C_YouLose, "", ""));   // проиграл
                gamer1.setLoser();
                gamer2.handleMessage(new Message(MessageCommand.S_C_YouWin, "", ""));   // выиграл
                gamer2.setWinner();
                sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginWin, gamer2.getLogin(), ""));
            }
        }
        else if(gamer.getLogin().equals(gamer2.getLogin())){
            //пожелал сдаться второй игрок
            if (!isClosed) {
                isClosed = true;
                gamer2.handleMessage(new Message(MessageCommand.S_C_YouLose, "", ""));   // проиграл
                gamer2.setLoser();
                gamer1.handleMessage(new Message(MessageCommand.S_C_YouWin, "", ""));   // выиграл
                gamer1.setWinner();
                sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginWin, gamer1.getLogin(), ""));
            }
        }
    }
    public void handleDisconnect(ServerGamer gamer){
        if(isGameStarted){
            if(gamer.getLogin().equals(gamer1.getLogin())){
                //пожелал сдаться первый игрок
                if (!isClosed) {
                    isClosed = true;
                    gamer1.handleMessage(new Message(MessageCommand.S_C_YouLose, "", ""));   // проиграл
                    gamer1.setLoser();
                    gamer2.handleMessage(new Message(MessageCommand.S_C_YouWin, "", ""));   // выиграл
                    gamer2.setWinner();
                    sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginWin, gamer2.getLogin(), ""));
                }
            }
            else if(gamer.getLogin().equals(gamer2.getLogin())){
                //пожелал сдаться второй игрок
                if (!isClosed) {
                    isClosed = true;
                    gamer2.handleMessage(new Message(MessageCommand.S_C_YouLose, "", ""));   // проиграл
                    gamer2.setWinner();
                    gamer1.handleMessage(new Message(MessageCommand.S_C_YouWin, "", ""));   // выиграл
                    gamer1.setLoser();
                    sendMessageToObs(new Message(MessageCommand.S_C_ToObs_LoginWin, gamer1.getLogin(), ""));
                }
            }
        }
        else{
            armDisconnect(gamer);
        }
    }

    public void armDisconnect(ServerGamer gamer){
        if (gamer2 != null){
            if(gamer.getLogin().equals(gamer2.getLogin())){
                gamer2 = null;
                gamer1.handleMessage(new Message(MessageCommand.S_C_NewGameInfo, this.toString2(),""));
                sendMessageToObs(new Message(MessageCommand.S_C_ShowObserverActivity, this.toString2(), ""));
            }
            else if (gamer.getLogin().equals(gamer1.getLogin())){
                sendMessage(new Message(MessageCommand.S_C_HostLeftTheGame,"",""));
            }
        }
        else if (gamer.getLogin().equals(gamer1.getLogin())){
            sendMessage(new Message(MessageCommand.S_C_HostLeftTheGame,"",""));
        }
        else
            System.err.println("Что-то пошло не так! Сюда мы не должны попадать...");


    }

    public void sendMessageToGamers(Message message){
        if(gamer1 != null)
            gamer1.handleMessage(message);
        if(gamer2 != null)
            gamer2.handleMessage(message);
    }
}
