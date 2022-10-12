import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private static boolean onAir = true;

    @Override
    public void run() {
        try {
            client = new Socket("localhost", 8888);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            ClientInput clientInput = new ClientInput();
            Thread thread = new Thread(clientInput);
            thread.start();

            String inMsg;
            while ((inMsg = in.readLine()) != null){
                System.out.println(inMsg);
            }
        } catch (IOException e){
            e.printStackTrace();
            shutdown();
        }
    }

    public void shutdown(){
        onAir = false;
        try{
            in.close();
            out.close();
            if(!client.isClosed())
                client.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    class ClientInput implements Runnable{
        @Override
        public void run() {
            try{
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                while (onAir){
                    String message = input.readLine();
                    if (message.equals("@quit")){
                        out.println(message);
                        input.close();
                        shutdown();
                    } else if( message.equals("@senduser")){
                        out.println(message);
                    }

                    else {
                        out.println(message);
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }

    }
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

}
