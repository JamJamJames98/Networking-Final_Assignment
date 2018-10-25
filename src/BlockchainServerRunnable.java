import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
 
public class BlockchainServerRunnable implements Runnable{
 
    private Socket clientSocket;
    private Blockchain blockchain;
    private HashMap<ServerInfo, Date> serverStatus;
    private ServerInfo remote;
 
    public BlockchainServerRunnable(Socket clientSocket, Blockchain blockchain, HashMap<ServerInfo, Date> serverStatus, ServerInfo remote) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
        this.serverStatus = serverStatus;
        this.remote = remote;
    }
 
    public void run() {
        try {
            serverHandler(clientSocket.getInputStream(), clientSocket.getOutputStream());
            clientSocket.close();
        } catch (IOException e) {
        }
    }
 
    public void serverHandler(InputStream clientInputStream, OutputStream clientOutputStream) {
 
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(clientInputStream));
        PrintWriter outWriter = new PrintWriter(clientOutputStream, true);
        String remoteIP = (((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
        String localIP = (((InetSocketAddress)clientSocket.getLocalSocketAddress()).getAddress()).toString().replace("/", "");
        int localPort = (((InetSocketAddress)clientSocket.getLocalSocketAddress()).getPort());
                
        try {
            while (true) {
                String inputLine = inputReader.readLine();
                if (inputLine == null) {
                    break;
                }
 
                String[] tokens = inputLine.split("\\|");
                Date temp_date = new Date();
                switch (tokens[0]) {
                    case "tx":
                        if (blockchain.addTransaction(inputLine))
                            outWriter.print("Accepted\n\n");
                        else
                            outWriter.print("Rejected\n\n");
                        outWriter.flush();
                        break;
                    case "pb":
                        outWriter.print(blockchain.toString() + "\n");
                        outWriter.flush();
                        break;
                    case "cc":
                        return;
                    case "hb":
                        int port_number = Integer.parseInt(tokens[1]);
                        int sequence_number = Integer.parseInt(tokens[2]);
                        temp_date = new Date();
                        ServerInfo new_server = new ServerInfo(remoteIP,port_number);
                        ServerInfo local_server = new ServerInfo(localIP, localPort);
                        //broadcast
                        serverStatus.put(new_server, temp_date);
                        if (sequence_number == 0) {
                            ArrayList<Thread> threads = new ArrayList<>();
                            for (ServerInfo server_list : serverStatus.keySet()) {
                                if (server_list.equals(new_server) || server_list.equals(local_server)) {
                                    continue;
                                }
                                Thread new_thread = new Thread(new HeartBeatClientRunnable(server_list, "si|" + localPort + "|" + remoteIP + "|" + port_number));
                                threads.add(new_thread);
                                new_thread.start();
                            }
                            for (Thread new_thread: threads) {
                                try {
                                    new_thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }   
                        }
                        break;
                    case "si":
                        int local_port = Integer.parseInt(tokens[1]);
                        String remote_host = tokens[2];
                        int remote_port = Integer.parseInt(tokens[3]);
                        temp_date = new Date();
                        new_server = new ServerInfo(remote_host,remote_port);
                        ServerInfo remote_server = new ServerInfo(remoteIP, local_port);
                        if (serverStatus.keySet().contains(new_server) == false) {  
                            serverStatus.put(new_server, temp_date);
                            ArrayList<Thread> threads = new ArrayList<>();
                            //relay
                            for (ServerInfo server_list : serverStatus.keySet()) {
                                if (server_list.equals(new_server) || server_list.equals(remote_server)) {
                                    continue;
                                }
                                Thread new_thread = new Thread(new HeartBeatClientRunnable(server_list, "si|" + localPort + "|" + remote_host + "|" + remote_port));
                                threads.add(new_thread);
                                new_thread.start();
                            }
                            for (Thread new_thread: threads) {
                                try {
                                    new_thread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }                                   
                        break;
                    case "lb":
                        break;
                    case "cu":
                        Block head_block = blockchain.getHead();
                        System.out.println("Sending over head block...");
                        //System.out.println("Blockchain length currently is " + blockchain.getLength());
                        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        new Thread(new CatchUp(remote, "initial-cu", blockchain)).start();
                        oos.writeObject(head_block);
                        oos.close();
                        break;
                    case "initial-cu":
                        Block head_initial = blockchain.getHead();
                        System.out.println("Sending over blockchain for catchup...");
                        //System.out.println("Blockchain length currently is " + blockchain.getLength());
                        ObjectOutputStream ooos = new ObjectOutputStream(clientSocket.getOutputStream());
                        new Thread(new CatchUp(remote, "initial-cu", blockchain));
                        ooos.writeObject(blockchain.getLength());
                        for (int i = 0; i < blockchain.getLength(); i++) {
                            //System.out.println("Iteration is " + i);
                            ooos.writeObject(head_initial);
                            //System.out.println(head_block.toString());
                            head_initial = head_initial.getPreviousBlock();
                        }
                        ooos.close();
                        System.out.println("All blocks sent");
                        break;
                    default:
                        outWriter.print("Error\n\n");
                        outWriter.flush();
                }
            }
        } catch (IOException e) {
        }
    }
}