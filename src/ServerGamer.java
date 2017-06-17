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
            case 100 :{
                //Геймер пытается залогиниться
                //Проверяем зарегистрирован ли геймер с такими логином и паролем на сервере
                if((processor.handleIsGamerExistInDB(message.getLogin(),message.getPass()) && !(processor.isLoginConnected(message.getLogin())))){
                    sender.sendMessage(new Message(101,message.getLogin(),message.getPass()));
                    isAnonim = false;
                    login = message.getLogin();
                    password = message.getPass();
                    processor.connectTheGamer(this);
                }
                else if(message.getLogin().equals(login) && password.equals(message.getPass())){
                    sender.sendMessage(new Message(101, login, password));
                }
                else
                    sender.sendMessage(new Message(102,"",""));
            }break;
            case 101:{
                //пропускаем так как это только от сервера клиенту может такое приходить
            }break;
            case 102:{
                //аналогично
            }break;
            case 103:{
                //клиент пытается зайти в лобби
                processor.handleConnectToLobby(this);
                //послылаем ответ на запрос входа в лобби (положительный)
                sender.sendMessage(new Message(104,"",""));
                //посылаем всем в лобби сообщение о том что был подключен такой то игрок
                //processor.handleMessageToLobby(new Message(107, "В лобби подключился новый игрок - " + login,""));
            }break;
            case 104:{
                //пропускаем так как это от сервера клиенту может быть такое сообщение
            }break;
            case 105:{
                //сообщение в лобби чат
                processor.handleMessageToLobby(new Message(106,message.getLogin(),message.getMessage()));
            }break;
            case 106:{
                sender.sendMessage(message);
            }break;
            case 107:{
                sender.sendMessage(message);
            }break;
            case 108:{
                //запрос от клиента о регистрации нового пользователя
                if(processor.isLoginExist(message.getLogin())){
                    sender.sendMessage(new Message(110,"",""));
                }else{
                    processor.registrationNewAccount(message.getLogin(), message.getPass());
                    sender.sendMessage(new Message(109,message.getLogin(),message.getPass()));
                }
            }break;
            case 109:{
                //пропускаем
            }break;
            case 110:{
                //пропускаем
            }break;
            case 111:{
                //запрос на создание игры
                processor.createGameQueryHandler(this);
                sender.sendMessage(new Message(112,"",""));
            }break;
            case 112:{
                //пропускаем т.к. это положительный ответ от сервера на создание игры (генерится в 111)
            }break;
            case 113:{
                //запрос на подключение к игре с указанным ID
                if(processor.handleTryToConnectToGame(this, message.getGameID())){
                    //удалось подключиться, значит пользователь уже удален из лобби, необходимо послать всем в чат уведомление об этом
                    processor.handleMessageToLobby(new Message(107, "Отключился игрок - " + login,""));
                    //Message tmpmessage = new Message(114, game.getGamer1().getLogin(), "" + game.getObserverCount());
                    Message tmpmessage = new Message(114, game.toString2(),"");
                    sender.sendMessage(tmpmessage);
                    game.getGamer1().sender.sendMessage(new Message(117,login,""));
                }else{
                    sender.sendMessage(new Message(115,"",""));
                }

            }break;
            case 116:{
                // запрос от игрока на прекращение создания игры
                // TODO послать всем уведомление о том что игра была прервана
                game.sendMessage(new Message(118,"",""));
                // TODO удалить игру
                // TODO отправить в лобби новый список игр
                //
            }break;
            case 117:{
                //пропускаем т.к.сам сервер его генерит
                sender.sendMessage(message);
            }break;
            case 118:{
                //должны сами генерировать в 116
                sender.sendMessage(message);
            }break;
            case 119:{
                sender.sendMessage(message);
            }break;
            case 120:{
                //пришло сообщение что пользователь готов и послал свое игровое поле
                isReady = true;
                this.board = message.getBoard();
                game.sendMessage(new Message(119, game.toString2(),""));
            }break;
            case 121:{
                //пришло сообщение от пользователя что он не готов
                isReady = false;
                if(game != null)
                    game.sendMessage(new Message(119, game.toString2(),""));
            }break;
            case 122:{
                // пришло сообщение от пользователя что он хочет стартануть игру
                game.startGame();
            } break;
            case 123:{
                //от сервера пришол запрос отправить игроку ответ на старт игры в качестве первого игрока
                sender .sendMessage(message);
            } break;
            case 124:{
                sender.sendMessage(message);
            } break;
            case 125:{
                sender.sendMessage(message);
            } break;
            case 126:{
                game.shot(message,this);
            } break;
            case 127:{
                sender.sendMessage(message);
            } break;
            case 128:{
                sender.sendMessage(message);
            } break;
            case 129:{
                sender.sendMessage(message);
            } break;
            case 130:{
                sender.sendMessage(message);
            } break;
            case 131:{
                sender.sendMessage(message);
            } break;
            case 132:{
                sender.sendMessage(message);
            } break;
            case 133:{
                // обработать выигрыш
                setWinner();
                sender.sendMessage(message);
            }break;
            case 134:{
                //обработать проигрыш
                setLoser();
                sender.sendMessage(message);
            }break;
            case 135:{
                sender.sendMessage(new Message(136,"",""));
            }break;
            case 136:{

            }break;
            case 137:{
                //запрос статистики
                sender.sendMessage(new Message(202, processor.getStatisticHandler(message.getVariableOne())));
            }break;
            case 138:{
                // сообщение от клиента серверу в чат игры
                game.sendMessage(new Message(139,message.getLogin(),message.getMessage()));
            }break;
            case 139:{
                // сообщение от сервера клиенту в чат игры
                sender.sendMessage(message);
            }break;
            case 140:{
                // служебное сообщение в чат игры (от сервера)
                sender.sendMessage(message);
            }break;
            case 141:{
                // сообщение о намерении подключиться к игре в качестве обсервера

                //запрос на подключение к игре с указанным ID
                if(processor.handleTryToConnectToGameObs(this, message.getGameID())){
                    // удалось подключиться, значит пользователь уже удален из лобби, необходимо послать всем в чат уведомление об этом
                    processor.handleMessageToLobby(new Message(107, "Отключился игрок - " + login,""));
                    // Message tmpmessage = new Message(114, game.getGamer1().getLogin(), "" + game.getObserverCount());
//                    Message tmpmessage = new Message(142, game.toString2(),"");
//                    sender.sendMessage(tmpmessage);

                    game.sendMessage(new Message(140,"Obs connected: " + login + "", ""));
                }else{
                    sender.sendMessage(new Message(140,"Error",""));
                }
            }break;
            case 142:{
                // сообщение от сервера о подключении к игре в качестве обсервера
                sender.sendMessage(message);
            }break;
            case 143:{
                //
                sender.sendMessage(message);
            }break;
            case 144:{
                sender.sendMessage(message);
            }break;
            case 145:{
                sender.sendMessage(message);
            }break;
            case 146:{
                sender.sendMessage(message);
            }break;
            case 147:{
                sender.sendMessage(message);
            }break;
            case 148:{
                // пришло сообщение о том что игрок пожелал сдаться...
                game.handleSurrenderMessage(this);
            }break;
            case 149:{
                // пришел запрос на статистику по конкретному игроку
                sender.sendMessage(new Message(202, processor.getLoginStatisticHandler(message.getLogin())));
            }break;
            case 150:{
                // к->c ручной дисконнект от игры
                if (game != null)
                    game.handleDisconnect(this);

                if (game != null && game.getGamer1().getLogin().equals(login))
                    processor.deleteGame(game);

                game = null;
            }break;
            case 151:{
                // хостовый игрок прервал создание игры
                sender.sendMessage(message);

                // если мы не хостовый игрок то отключаемся от игры и затираем ссылку на игру
                if(!login.equals(game.getGamer1().getLogin()))
                    game = null;
            }break;
            case 152:{
                //пришло сообщение о желании перестать наблюдать за игрой
                if(game != null)
                    game.disconnectObs(this);
                game = null;
            }break;
            case 201:{
                //исходящее сообщение от процессора со списком игр
                sender.sendMessage(message);
            }break;
            case 300:{
                // сообщение об отключении клиента...
                if (!isAnonim){
                    sender.sendMessage(new Message(301,"",""));
                    processor.disconnectTheGamer(this);
                    if(game != null){
                        game.handleDisconnect(this);
                    }
                }
                else
                    sender.sendMessage(new Message(301,"",""));
            }break;
            case 301:{
                // исходящее сообщение об отключении
            }break;
        }
    }
    public String getLogin(){
        return login;
    }
    public void handleDisconnect(){
        if(!isAnonim)
            processor.disconnectTheGamer(this);

        if(game != null)
            handleMessage(new Message(150, "", ""));

        System.err.println("Error:connect is broken. " + (isAnonim ? "Anonim" : login) + " is disconnect from server");
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
            if(tmp.getLogin().equals(login))
                return true;
            else
                return false;
        }
    }

}
