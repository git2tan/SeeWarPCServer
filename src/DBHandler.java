import org.sqlite.JDBC;
import java.sql.*;
import java.util.ArrayList;

/**
 * Created by Artem on 07.05.2017.
 */
public class DBHandler {

    private static final String DB_PATH = "jdbc:sqlite:Server_DB.db";
    private static DBHandler instance = null;
    private Connection connection;

    public static DBHandler getInstance(){
        if(instance == null)
            instance = new DBHandler();
        return instance;
    }

    private DBHandler(){

        System.out.println("Открываю БД");
        try {
            DriverManager.registerDriver(new JDBC());
            this.connection = DriverManager.getConnection(DB_PATH);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("БД открылась.");
    }

    public boolean isAccountValid(String login, String pass){
        boolean answer = false;
        try (Statement statement = this.connection.createStatement()){
            String query = String.format("SELECT count(*) FROM Accounts " +
                    "WHERE (Login = '%s') AND (Password = '%s')", login, pass);
            ResultSet result = statement.executeQuery(query);
            if(result.getInt("count(*)") > 0){
                answer = true;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return answer;
    }

    public boolean isLoginAlreadyExist(String login){
        boolean answer = false;
        try (Statement statement = this.connection.createStatement()){
            String query = String.format("SELECT count(*) FROM Accounts " +
                    "WHERE (Login = '%s')", login);
            ResultSet result = statement.executeQuery(query);

            if(result.getInt("count(*)") > 0){
                answer = true;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return answer;
    }

    public void addAccount(String login, String pass){
        try (Statement statement = this.connection.createStatement()){
            String query = String.format("INSERT INTO Accounts ('Login', 'Password', 'Games', 'Win') " +
                    "VALUES ('%s', '%s', '0', '0')", login, pass);
            statement.execute(query);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public ArrayList<String> getTop5WithOffset(int offset){
        ArrayList<String> resultString = new ArrayList<String>();
        try (Statement statement = this.connection.createStatement()){
            String query = String.format("SELECT Login, Win FROM Accounts ORDER BY Win DESC LIMIT 5 OFFSET " + offset);
            ResultSet result = statement.executeQuery(query);

            while(result.next()){
                String one = result.getString("Login")+ "   " + result.getString("Win") + " побед";
                System.out.println("Добавил с писок результатов строку: " + "\"" + one + "\"");
                resultString.add(one);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return resultString;
    }

    public ArrayList<String> getInfoForLogin(String login){
        ArrayList<String> resultString = new ArrayList<String>();
        try (Statement statement = this.connection.createStatement()){
            String query = String.format("SELECT Login, Win, Games FROM Accounts WHERE Login = '%s'", login);
            ResultSet result = statement.executeQuery(query);

            while(result.next()){
                String one = result.getString("Login")+ " " + result.getString("Win") + " побед из " + result.getString("Games") + " игр";
                System.out.println("Добавил с писок результатов строку: " + "\"" + one + "\"");
                resultString.add(one);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return resultString;
    }

    public void setWinner(String login){
        try (Statement statement = this.connection.createStatement()){
            String query1 = String.format("UPDATE Accounts SET  Win = (Win + 1) WHERE Login = '%s'", login);
            String query2 = String.format("UPDATE Accounts SET  Games = (Games + 1) WHERE Login = '%s';", login);
            statement.execute(query1);
            statement.execute(query2);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    public void setLoser(String login){
        try (Statement statement = this.connection.createStatement()){
            String query2 = String.format("UPDATE Accounts SET  Games = (Games + 1) WHERE Login = '%s';", login);

            statement.execute(query2);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
