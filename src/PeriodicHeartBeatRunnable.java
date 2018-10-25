import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class PeriodicHeartBeatRunnable implements Runnable {

    private HashMap<ServerInfo, Date> serverStatus;
    private int sequenceNumber;
    private int localPort;

    public PeriodicHeartBeatRunnable(HashMap<ServerInfo, Date> serverStatus, int localPort) {
        this.serverStatus = serverStatus;
        this.sequenceNumber = 0;
        this.localPort = localPort;
    }

    @Override
    public void run() {
        while(true) {
            //broadcast HeartBeat message to all peers from week 9 tute
            ArrayList<Thread> threadArrayList = new ArrayList<>();
            for (ServerInfo server_list : serverStatus.keySet()) {
                Thread thread = new Thread(new HeartBeatClientRunnable(server_list, "hb|" + localPort + "|" + sequenceNumber));
                threadArrayList.add(thread);
                thread.start();
            }

            for (Thread thread : threadArrayList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
            }

            // increment the sequenceNumber
            //System.out.print(sequenceNumber);
            sequenceNumber += 1;

            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            for (Entry<ServerInfo, Date> entry: serverStatus.entrySet()) {
            	Date temp_date = new Date();
            	Date entry_date = entry.getValue();
            	//remove if no response for 4 seconds
            	if (temp_date.getTime() - entry_date.getTime() > 4000) {
            		serverStatus.remove(entry);
            	}
            }
        }
    }
}
