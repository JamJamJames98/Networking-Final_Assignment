import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LatestBlockClientRunnable implements Runnable{

	HashMap<ServerInfo, Date> serverStatus;
    private byte[] hash;
    private int length;
    private int localPort;

    public LatestBlockClientRunnable(HashMap<ServerInfo, Date> serverStatus, byte[] hash, int length, int localPort) {
    	this.serverStatus = serverStatus;
        this.hash = hash;
        this.length = length;
        this.localPort = localPort;
    }

	@Override
    public void run() {
		while(true) {
			if ((length > 0) && (length < 6)) {  	
				//broadcast message to all other peers
	            ArrayList<Thread> threadArrayList = new ArrayList<>();
	            for (ServerInfo server_list : serverStatus.keySet()) {
	                Thread thread = new Thread(new HeartBeatClientRunnable(server_list, "lb|" + localPort + "|" + length + "|" + hash));
	                threadArrayList.add(thread);
	                thread.start();
	            }

	            for (Thread thread : threadArrayList) {
	                try {
	                    thread.join();
	                } catch (InterruptedException e) {
	                }
	            }
	            // sleep for two seconds
	            try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	            }
	        }
			if ((length > 0) && (length >= 6)) {
				//broadcast message to 5 random peers
				List<ServerInfo> keys = new ArrayList<ServerInfo>(serverStatus.keySet());
				Collections.shuffle(keys);
				ArrayList<Thread> threadArrayList = new ArrayList<>();
	            for (ServerInfo server_list : serverStatus.keySet()) {
	                Thread thread = new Thread(new HeartBeatClientRunnable(server_list, "lb|" + localPort + "|" + length + "|" + hash));
	                threadArrayList.add(thread);
	                thread.start();
	            }

	            for (Thread thread : threadArrayList) {
	                try {
	                    thread.join();
	                } catch (InterruptedException e) {
	                }
	            }
	            // sleep for two seconds
	            try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	            }
			}
			if (length == 0){
				//broadcast message to the only other peer (this is when length == 0)
	            ArrayList<Thread> threadArrayList = new ArrayList<>();
	            for (ServerInfo server_list : serverStatus.keySet()) {
	                Thread thread = new Thread(new HeartBeatClientRunnable(server_list, "lb|" + localPort + "|" + length + "|" + null));
	                threadArrayList.add(thread);
	                thread.start();
	            }

	            for (Thread thread : threadArrayList) {
	                try {
	                    thread.join();
	                } catch (InterruptedException e) {
	                }
	            }
	            // sleep for two seconds
	            try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	            }
			}
			
		}
		
    }
}
