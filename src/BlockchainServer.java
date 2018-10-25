import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

public class BlockchainServer {

    public static void main(String[] args) {

        if (args.length != 3) {
            return;
        }

        int localPort = 0;
        int remotePort = 0;
        String remoteHost = null;

        try {
            localPort = Integer.parseInt(args[0]);
            remoteHost = args[1];
            remotePort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return;
        }

        Blockchain blockchain = new Blockchain();
        
        
        HashMap<ServerInfo, Date> serverStatus = new HashMap<ServerInfo, Date>();
        serverStatus.put(new ServerInfo(remoteHost, remotePort), new Date());
        ServerInfo remote = new ServerInfo(remoteHost, remotePort);
        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();
        new Thread(new PeriodicHeartBeatRunnable(serverStatus, localPort)).start();
        //new Thread(new CatchUpClientRunnable(remote, "cu")).start();
        //System.out.println(remotePort);
        //System.out.println(remoteHost);
        new Thread(new CatchUp(remote, "initial-cu", blockchain)).start();
        int length = blockchain.getLength();
        if (blockchain.getHead() == null) {
        	new Thread(new LatestBlockClientRunnable(serverStatus, null, length, localPort)).start();
        } else {
        	byte[] hash = Base64.getEncoder().encode(blockchain.getHead().getCurrentHash());
            new Thread(new LatestBlockClientRunnable(serverStatus, hash, length, localPort)).start();
        }
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localPort);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new BlockchainServerRunnable(clientSocket, blockchain, serverStatus, remote)).start();
            }
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        } finally {
            try {
                pcr.setRunning(false);
                pct.join();
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
    }
}
