/**
 * Created by Artem on 07.05.2017.
 */
public class Program {
    public static void main(String []args){
        int port = 4444;
        if (args.length>0)
            port = tryParsePort(args[0]);
        DBHandler.getInstance();    //для запуска БД
        Server myServer = new Server(port);
        myServer.work();
    }
    public static int tryParsePort(String string){
        int port = 4444;
        try{
            int tmp = Integer.parseInt(string);
            if (tmp >= 1024 && tmp <= 49151)
                port = tmp;
        }
        catch (Exception e){

        }
        return port;
    }
}
