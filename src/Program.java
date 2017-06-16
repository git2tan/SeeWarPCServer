/**
 * Created by Artem on 07.05.2017.
 */
public class Program {
    public static void main(String []args){
        DBHandler.getInstance();    //для запуска БД
        Server myServer = new Server();
        myServer.work();
    }
}
