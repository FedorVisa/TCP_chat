import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connectionHandlers;
    private ServerSocket server;
    private ExecutorService pool;
    private  static boolean onAir;
    public Server(){
        connectionHandlers = new ArrayList<>();
        onAir = true;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket( 8888);
            pool = Executors.newCachedThreadPool();
            while (onAir) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connectionHandlers.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void broadcast (String msg){
        for( ConnectionHandler persons : connectionHandlers){
            if( persons!= null){
                persons.sendMsg( msg);
            }
        }
        return;
    }

    public void shutdown()  {
        try {
        onAir = false;
        pool.shutdown();
        if(!server.isClosed()){
            server.close();
        }
        for(ConnectionHandler persons : connectionHandlers) {
            persons.kick();
        }
            }catch (IOException e){
                e.printStackTrace();
        }
    }



    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private  String name;

        public ConnectionHandler( Socket client){
            this.client = client;
        }

        public void changenick(String msg ){
            String[] nickname = msg.split(" ", 2);
            if( nickname.length == 2){
                out.println("your new nickname is: " + nickname[1]);
                broadcast( name + " has changed his nickname to: " + nickname[1]);
                name = nickname[1];
            } else out.println(" wrong format of setting a nickname");
            return;
        }

        public void sendToSomeone( String msg){
            String[] nickname = msg.split(" ");
            for( ConnectionHandler hand : connectionHandlers){
                if(hand.name.equals(nickname[1]))
                    hand.sendMsg(nickname[2]);
            }
        }

        @Override
        public void run() {
            try{
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader( new InputStreamReader(client.getInputStream()));
                out.println("Enter a nickname:");
                name = in.readLine();
                System.out.println( name+ " connected to the chat :)" );
                broadcast(name+ " has joined");
                String message;
                while ((message = in.readLine()) != null){
                    if (message.startsWith("@nick")) {
                        changenick( message);
                    } else if ( message.startsWith("@quit")){
                        broadcast(name + " has quit the chat ");
                    } else if (message.startsWith("@senduser")){
                        sendToSomeone( message);
                    }
                    else {
                        broadcast( name + ":" + message);
                    }

                }

            } catch( IOException e){
                e.printStackTrace();
                kick();
            }
        }

        public void sendMsg ( String msg){
            out.println(msg);
        }

        public void kick() {
            try {

                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }


    }
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
