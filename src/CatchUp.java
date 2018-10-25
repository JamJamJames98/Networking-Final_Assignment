import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
 
public class CatchUp implements Runnable {
 
    private ServerInfo server_list;
    private String message;
    private Blockchain blockchain;
 
    public CatchUp(ServerInfo server_list, String message, Blockchain blockchain) {
        this.server_list = server_list;
        this.message = message;
        this.blockchain = blockchain;
    }
 
    @Override
    public void run() {
        try {
            //this is a full catchup
            if (message.equals("initial-cu")) {
                System.out.println("Initiating full catchup...");
                Socket toServer = new Socket();
                int port = server_list.getPort();
                String host = server_list.getHost();
                toServer.connect(new InetSocketAddress(host, port), 2000);
                PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);
                // send the message forward
                printWriter.println(message);
                //printWriter.flush();
                ObjectInputStream ois = new ObjectInputStream(toServer.getInputStream());
                int length = (Integer) ois.readObject();
                //System.out.println("Length is : " + length);
                Block new_block = null;
                Block temp = null;
                Block prev = null;
                for (int i = 0; i < length; i++) {
                    if (i == 0) {
                        new_block = (Block) ois.readObject();
                        //System.out.println("new_block");
                        //System.out.println(new_block.toString());
                        blockchain.setHead(new_block);
                        prev = new_block;
                    }
                    else {
                        temp = (Block) ois.readObject();
                        //System.out.println("temp");
                        //System.out.println(temp.toString());
                        prev.setPreviousBlock(temp);
                        //System.out.println(new_block.getPreviousBlock());
                        prev = temp;
                    }
                }
                ois.close();
                // System.out.println(message);
                // close printWriter and socket
                printWriter.close();
                toServer.close();
                System.out.println("Full catchup completed");
            } else if (message.equals("cu")) {
                System.out.println("Initiating head block catchup...");
                Socket toServer = new Socket();
                int port = server_list.getPort();
                String host = server_list.getHost();
                toServer.connect(new InetSocketAddress(host, port), 2000);
                PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);
                // send the message forward
                printWriter.println(message);
                //printWriter.flush();
                ObjectInputStream ois = new ObjectInputStream(toServer.getInputStream());
                //int x = ois.readInt();
                Block new_block = (Block) ois.readObject();
                blockchain.setHead(new_block);
                ois.close();
                // System.out.println(message);
                // close printWriter and socket
                printWriter.close();
                toServer.close();
                System.out.println("Head block catchup completed"); 
            } else {
                /*Socket toServer = new Socket();
                int port = server_list.getPort();
                String host = server_list.getHost();
                toServer.connect(new InetSocketAddress(host, port), 2000);
                PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);
                // send the message forward
                System.out.println("beforeELSE");
                ObjectInputStream ois = new ObjectInputStream(toServer.getInputStream());
                System.out.println("afterELSE");
                printWriter.println(message);
                printWriter.flush();
                // System.out.println(message);
                // close printWriter and socket
                printWriter.close();

                String[] tokens = message.split("\\|");
                byte[] my_hash = Base64.getDecoder().decode(tokens[1]);
                Block head_block = blockchain.getHead();
                
                for(Transaction t: head_block.getTransactions()){
                    blockchain.addTransaction(t.toString());
                }
                for (int i = 0; i < blockchain.getLength(); i++) {
                    if (head_block.calculateHash().equals(my_hash)) {
                        Block new_block = (Block) ois.readObject();
                        if (new_block != null) {
                            head_block.setPreviousBlock(new_block);
                        }
                    } else {
                        head_block = head_block.getPreviousBlock();
                    }
                }
                toServer.close();*/
            }
            
        } catch (IOException e) {
            System.out.println("Server Not Up! You Tried To Connect To");
            System.out.println("Port : " + server_list.getPort());
            System.out.println("Host : " + server_list.getHost());
            System.out.println("Catchup failed");
            
        } catch (ClassNotFoundException e) {
           // e.printStackTrace();
        }
    }
}