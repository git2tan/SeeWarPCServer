import java.net.Socket;

/**
 * Created by Artem on 07.05.2017.
 */
public class ServerGamer {
    private boolean isAnonim;
    private GamersQueryProcessor processor;
    private String login;
    private String password;
    private Socket socket;
    private GamerSender sender;
    private GamerReceiver gamerReceiver;
    private Game game;
    private boolean isReady;
    private int[][] board;
    public ServerGamer(Socket socket, GamersQueryProcessor processor){
        isAnonim = true;
        this.socket = socket;
        this.processor = processor;
        isReady = false;
        game = null;
        sender = new GamerSender(socket, this);

        gamerReceiver = new GamerReceiver(this, socket);

        Thread gameReceiverThread = new Thread(gamerReceiver);
        gameReceiverThread.start();
    }

    public void handleMessage(Message message){
//        processor.processMessage(this, message);
        switch(message.getNumberOfCommand()){
            case MessageCommand.C_S_TryLogin :{
                //Геймер пытается залогиниться
                //Проверяем зарегистрирован ли геймер с такими логином и паролем на сервере
                if(!isAnonim){
                    if(message.getLogin().equals(login) && password.equals(message.getPass())){
                        sender.sendMessage(new Message(MessageCommand.S_C_SuccessLogin, login, password));
                    }
                    else
                        handleDisconnect();
                }

                if (isAnonim){
                    if((processor.handleIsGamerExistInDB(message.getLogin(),message.getPass()) && !(processor.isLoginConnected(message.getLogin())))){
                        sendMessage(new Message(MessageCommand.S_C_SuccessLogin,message.getLogin(),message.getPass()));
                        isAnonim = false;
                        login = message.getLogin();
                        password = message.getPass();
                        processor.connectTheGamer(this);
                    }
                    else
                        sendMessage(new Message(MessageCommand.S_C_InValidLogin,"",""));
                }
//                else if(message.getLogin().equals(login) && password.equals(message.getPass())){
//                    sendMessage(new Message(MessageCommand.S_C_SuccessLogin, login, password));
//                }
//                else if (!isAnonim){
//                    isAnonim = true;
//                    sendMessage(new Message(MessageCommand.S_C_InValidLogin,"",""));
//                    handleDisconnect();
//                }

            }break;
            case MessageCommand.S_C_SuccessLogin:{
                //пропускаем так как это только от сервера клиенту может такое приходить
            }break;
            case MessageCommand.S_C_InValidLogin:{
                //аналогично
            }break;
            case MessageCommand.C_S_TryConnectToLobby:{
                //клиент пытается зайти в лобби
                processor.handleConnectToLobby(this);
                //послылаем ответ на запрос входа в лобби (положительный)
                sendMessage(new Message(MessageCommand.S_C_YouAllowConnectToLobby,"",""));
                //посылаем всем в лобби сообщение о том что был подключен такой то игрок
                //processor.handleMessageToLobby(new Message(MessageCommand.S_C_MessageToLobbyFromServer, "В лобби подключился новый игрок - " + login,""));
            }break;
            case MessageCommand.S_C_YouAllowConnectToLobby:{
                //пропускаем так как это от сервера клиенту может быть такое сообщение
            }break;
            case MessageCommand.C_S_MessageToLobby:{
                //сообщение в лобби чат
                processor.handleMessageToLobby(new Message(MessageCommand.S_C_MessageToLobbyFromLogin,message.getLogin(),message.getMessage()));
            }break;
            case MessageCommand.S_C_MessageToLobbyFromLogin:{
                sendMessage(message);
            }break;
            case MessageCommand.S_C_MessageToLobbyFromServer:{
                sendMessage(message);
            }break;
            case MessageCommand.C_S_TryToRegisterNewLogin:{
                //запрос от клиента о регистрации нового пользователя
                if(processor.isLoginExist(message.getLogin())){
                    sendMessage(new Message(MessageCommand.S_C_RegistrationNotSuccess,"",""));
                }else{
                    processor.registrationNewAccount(message.getLogin(), message.getPass());
                    sendMessage(new Message(MessageCommand.S_C_RegistrationSuccess,message.getLogin(),message.getPass()));
                }
            }break;
            case MessageCommand.S_C_RegistrationSuccess:{
                //пропускаем
            }break;
            case MessageCommand.S_C_RegistrationNotSuccess:{
                //пропускаем
            }break;
            case MessageCommand.C_S_WantToCreateGame:{
                //запрос на создание игры
                processor.createGameQueryHandler(this);
                sendMessage(new Message(MessageCommand.S_C_AllowToCreateGame,"",""));
            }break;
            case MessageCommand.S_C_AllowToCreateGame:{
                //пропускаем т.к. это положительный ответ от сервера на создание игры (генерится в 111)
            }break;
            case MessageCommand.C_S_WantToConnectToGame:{
                //запрос на подключение к игре с указанным ID
                if(processor.handleTryToConnectToGame(this, message.getGameID())){
                    //удалось подключиться, значит пользователь уже удален из лобби, необходимо послать всем в чат уведомление об этом
                    Message tmpmessage = new Message(MessageCommand.S_C_SuccessConnectToGame, game.toString2(),"");
                    sendMessage(tmpmessage);
                    game.getGamer1().sendMessage(new Message(MessageCommand.S_C_ToHostGamer_NewGamerConnect,login,""));
                }else{
                    sendMessage(new Message(MessageCommand.S_C_NotAllowConnectToGame,"",""));
                }

            }break;
            case MessageCommand.C_S_ArmLeftTheLobby:{
                // запрос от игрока на выход из лобби
                processor.handleDisconnectFromLobby(this);
                //
            }break;
            case MessageCommand.S_C_ToHostGamer_NewGamerConnect:{
                //пропускаем т.к.сам сервер его генерит
                sendMessage(message);
            }break;
            case MessageCommand.S_C_RequesttoArmDisconnectFromLobby:{
                //должны сами генерировать
                sendMessage(message);
            }break;
            case MessageCommand.S_C_NewGameInfo:{
                sendMessage(message);
            }break;
            case MessageCommand.C_S_GamerReadyAndSendBoard:{
                //пришло сообщение что пользователь готов и послал свое игровое поле
                isReady = true;
                this.board = message.getBoard();
                game.sendMessage(new Message(MessageCommand.S_C_NewGameInfo, game.toString2(),""));
            }break;
            case MessageCommand.C_S_GamerNotReady:{
                //пришло сообщение от пользователя что он не готов
                isReady = false;
                if(game != null)
                    game.sendMessage(new Message(MessageCommand.S_C_NewGameInfo, game.toString2(),""));
            }break;
            case MessageCommand.C_S_HostGamerStartTheGame:{
                // пришло сообщение от пользователя что он хочет стартануть игру
                game.startGame();
            } break;
            case MessageCommand.S_C_ToHostGamerStartTheGame:{
                //от сервера пришол запрос отправить игроку ответ на старт игры в качестве первого игрока
                sender .sendMessage(message);
            } break;
            case MessageCommand.S_C_ToGamer2StartTheGame:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_AllowObserveTheGame:{
                sendMessage(message);
            } break;
            case MessageCommand.C_S_FireToCoord:{
                game.shot(message,this);
            } break;
            case MessageCommand.S_C_YouHitToCoord:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_YouMissToCoord:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_OpponentHitToYou:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_OpponentMissToYou:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_YouDestroyTheShipByCoord:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_YourShipByCoordIsDestroyed:{
                sendMessage(message);
            } break;
            case MessageCommand.S_C_YouWin:{
                // обработать выигрыш
                setWinner();
                sendMessage(message);
            }break;
            case MessageCommand.S_C_YouLose:{
                //обработать проигрыш
                setLoser();
                sendMessage(message);
            }break;
            case MessageCommand.C_S_NeedStatisticFromNumber:{
                sendMessage(new Message(MessageCommand.S_C_ShowStatActivity,"",""));
            }break;
            case MessageCommand.S_C_ShowStatActivity:{

            }break;
            case MessageCommand.C_S_NeedRefreshStatistic:{
                //запрос статистики
                sendMessage(new Message(MessageCommand.S_C_Statistic, processor.getStatisticHandler(message.getVariableOne())));
            }break;
            case MessageCommand.C_S_MessageToLobbyFromlogin:{
                // сообщение от клиента серверу в чат игры
                game.sendMessage(new Message(MessageCommand.S_C_MessageToLobbyFromlogin,message.getLogin(),message.getMessage()));
            }break;
            case MessageCommand.S_C_MessageToLobbyFromlogin:{
                // сообщение от сервера клиенту в чат игры
                sendMessage(message);
            }break;
            case MessageCommand.S_C_MessageToLobbyAboutCoonect:{
                // служебное сообщение в чат игры (от сервера)
                sendMessage(message);
            }break;
            case MessageCommand.C_S_WantToObserverToGame:{
                // сообщение о намерении подключиться к игре в качестве обсервера

                //запрос на подключение к игре с указанным ID
                if(processor.handleTryToConnectToGameObs(this, message.getGameID())){
                    game.sendMessage(new Message(MessageCommand.S_C_MessageToLobbyAboutCoonect,"Obs connected: " + login + "", ""));
                }else{
                    sendMessage(new Message(MessageCommand.S_C_MessageToLobbyAboutCoonect,"Error",""));
                }
            }break;
            case MessageCommand.S_C_ShowObserverActivity:{
                // сообщение от сервера о подключении к игре в качестве обсервера
                sendMessage(message);
            }break;
            case MessageCommand.S_C_LoginFireToCoordAndHit:{
                //
                sendMessage(message);
            }break;
            case MessageCommand.S_C_ToObs_LoginFireToCoordAndMiss:{
                sendMessage(message);
            }break;
            case MessageCommand.S_C_ToObs_LoginDestroyShipByCoord:{
                sendMessage(message);
            }break;
            case MessageCommand.S_C_ToObs_LoginWin:{
                sendMessage(message);
            }break;
            case MessageCommand.S_C_ToObs_ActualGameInfo:{
                sendMessage(message);
            }break;
            case MessageCommand.C_S_GamerWantToLose:{
                // пришло сообщение о том что игрок пожелал сдаться...
                game.handleSurrenderMessage(this);
            }break;
            case MessageCommand.C_S_WantStatAboutlogin:{
                // пришел запрос на статистику по конкретному игроку
                sendMessage(new Message(MessageCommand.S_C_Statistic, processor.getLoginStatisticHandler(message.getLogin())));
            }break;
            case MessageCommand.C_S_LeftFromTheGame:{
                // к->c ручной дисконнект от игры
                if (game != null)
                    game.handleDisconnect(this);

                if (game != null && game.getGamer1().getLogin().equals(login))
                    processor.deleteGame(game);

                //
                processor.handleConnectToLobby(this);
                game = null;
            }break;
            case MessageCommand.S_C_HostLeftTheGame:{
                // хостовый игрок прервал создание игры
                if (sender != null)
                sendMessage(message);

                // если мы не хостовый игрок то отключаемся от игры и затираем ссылку на игру

                if (game != null && game.getGamer1().getLogin().equals(login))
                    processor.deleteGame(game);

                if(game != null && !login.equals(game.getGamer1().getLogin()))
                    game = null;
            }break;
            case MessageCommand.C_S_StopObserveTheGame:{
                //пришло сообщение о желании перестать наблюдать за игрой
                if(game != null)
                    game.disconnectObs(this);
                game = null;
                processor.handleConnectToLobby(this);
            }break;
            case MessageCommand.S_C_ListOfLobbyGame:{
                //исходящее сообщение от процессора со списком игр
                sendMessage(message);
            }break;
            case MessageCommand.C_S_DisconnectFromServer:{
                // сообщение об отключении клиента...
                if (!isAnonim){
                    sendMessage(new Message(MessageCommand.S_C_SystemMessageStopTheThread,"",""));
                    sender = null;
                    processor.disconnectTheGamer(this);
                    if(game != null){
                        game.handleDisconnect(this);
                    }
                }
                else
                    sendMessage(new Message(MessageCommand.S_C_SystemMessageStopTheThread,"",""));
            }break;
            case MessageCommand.S_C_DisconnectFromServer:{
                // исходящее сообщение об отключении
            }break;
        }
    }
    public String getLogin(){
        return login;
    }
    public void handleDisconnect(){
        if(!isAnonim) {
            processor.disconnectTheGamer(this);
        }

        if(game != null)
            handleMessage(new Message(MessageCommand.C_S_LeftFromTheGame, "", ""));

        System.err.println("Error:connect is broken. " + (isAnonim ? "Anonim" : login) + " is disconnect from server");
        processor.handleDisconnectFromLobby(this);
        login = null;
        password = null;
        isAnonim = true;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
    public boolean isReady(){
        return isReady;
    }

    public boolean isHit(int coordX, int coordY){
        if (board[coordX][coordY] == 1) {
            board[coordX][coordY] = 2;
            return true;
        }
        else if (board[coordX][coordY] == 2){
            return true;
        }
        else{
            board[coordX][coordY] = 3;
            return false;
        }

    }
    public boolean haveShip(){
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(board[i][j] == 1){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isShipNotDestroyed(int x, int y){
        //если левее или выше или превее или ниже нет 1 а только 2 то гооврим что уничтожен
        boolean answer = false;
        int cx = x;
        int cy = y;
        while(true)
        {
            cx--;
            if(cx < 0)
                break;
            if(board[cx][y] == 0)
                break;

            if(board[cx][y] == 1)
                return true;
        }
        while(true)
        {
            cx++;
            if(cx > 9)
                break;
            if(board[cx][y] == 0)
                break;

            if(board[cx][y] == 1)
                return true;
        }
        while(true)
        {
            cy--;
            if(cy < 0)
                break;
            if(board[x][cy] == 0)
                break;

            if(board[x][cy] == 1)
                return true;
        }
        while(true)
        {
            cy++;
            if(cy > 9)
                break;
            if(board[x][cy] == 0)
                break;

            if(board[x][cy] == 1)
                return true;
        }



        return answer;
    }
    public void setWinner(){
        processor.setWinner(this);
    }
    public void setLoser(){
        processor.setLoser(this);
    }

    public String boardToString(){
        String tmp = "";
        for (int i = 0; i < 10; i++){
            for (int j = 0; j < 10; j++){
                if(board[i][j] != 1)
                    tmp += "" + board[i][j];
                else
                    tmp += "" + 0;
            }
        }
        return tmp;
    }

    public void setDestroyedShipAt(int coordX, int coordY){
        // заполняем метками "мимо"

        int topLeftX = coordX;
        int topLeftY = coordY;
        int botRightX = coordX;
        int botRightY = coordY;


        while(true){
            if (topLeftX - 1 < 0) {
                break;
            }
            else {
                if ((board[topLeftX - 1][coordY] == 0) || (board[topLeftX - 1][coordY] == 1) || (board[topLeftX - 1][coordY] == 3)){
                    topLeftX = topLeftX - 1;
                    break;
                }
            }
            --topLeftX;
        }

        while(true){
            if (topLeftY - 1 < 0) {
                break;
            }
            else {
                if ((board[coordX][topLeftY - 1] == 0) || (board[coordX][topLeftY - 1] == 1) || (board[coordX][topLeftY - 1] == 3)){
                    topLeftY = topLeftY - 1;
                    break;
                }
            }
            --topLeftY;
        }

        while(true){
            if (botRightX + 1 > 9) {
                break;
            }
            else {
                if ((board[botRightX + 1][coordY] == 0) || (board[botRightX + 1][coordY] == 1) || (board[botRightX + 1][coordY] == 3)){
                    botRightX = botRightX + 1;
                    break;
                }
            }
            ++botRightX;
        }

        while(true){
            if (botRightY + 1 > 9) {
                break;
            }
            else {
                if ((board[coordX][botRightY + 1] == 0) || (board[coordX][botRightY + 1] == 1) || (board[coordX][botRightY + 1] == 3)){
                    botRightY = botRightY + 1;
                    break;
                }
            }
            ++botRightY;
        }

        for (int i = topLeftX; i <= botRightX; i++){
            for (int j = topLeftY; j <= botRightY; j++){
                autoShot(i,j);
            }
        }
    }

    private void autoShot(int x, int y){
        if(board[x][y] != 2)
            board[x][y] = 3;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;

     /* obj ссылается на null */

        if(obj == null)
            return false;

     /* Удостоверимся, что ссылки имеют тот же самый тип */

        if(!(getClass() == obj.getClass()))
            return false;
        else
        {
            ServerGamer tmp = (ServerGamer)obj;
            String login1 = tmp.getLogin();
            if(login1.equals(login))
                return true;
            else
                return false;
        }
    }

    private void sendMessage(Message message){
        if (sender != null)
            sender.sendMessage(message);
    }

}
