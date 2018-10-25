import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CatchUpClientRunnable implements Runnable{

    private ServerInfo server_list;
    private String message;

    public CatchUpClientRunnable(ServerInfo server_list, String message) {
        this.server_list = server_list;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            // create socket with a timeout of 2 seconds
            Socket toServer = new Socket();
            int port = server_list.getPort();
            String host = server_list.getHost();
            toServer.connect(new InetSocketAddress(host, port), 2000);
            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);

            // send the message forward
            printWriter.println(message);
            printWriter.flush();
            System.out.println(message);
            // close printWriter and socket
            printWriter.close();
            toServer.close();
        } catch (IOException e) {
        }
    }
}
