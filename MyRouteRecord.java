import java.util.ArrayList;
import java.util.List;

public class MyRouteRecord {
	
	private static List<ArrayList<Integer>> routs = new ArrayList<ArrayList<Integer>>(); 	
	private static int mehtodInProgress;
	
	public static synchronized void setMehtodInProgress(int mehtodNumber){
		mehtodInProgress = mehtodNumber;
	}
	
	public static synchronized void routeRecorder(int blockNumber){
    	if(routs.size() < mehtodInProgress)
    		for(int i = 0; i < mehtodInProgress - routs.size();i++)
    			routs.add(new ArrayList<Integer>());
    	routs.get(mehtodInProgress - 1).add(blockNumber);    	
    }
	
    public static synchronized void routeAnalye(){
    	for(List<Integer> rout: routs){
    		
    		
    	} 
    }
    
    public static synchronized void reportProfiling(){
    	
    }
    
    public static synchronized void reportRecord(){
    	int count = 0;
    	for(List<Integer> rout: routs){
    		System.out.print("Method" + count++ + ": ");
    		for(int n : rout){
    			System.out.print( n + " ");
    		}
    		System.out.println("");
    	}    	
    }

	
    /*
    public static synchronized void routeRecorder(int blockNumber, int mehtodNumber){
    	if(mehtodNumber > routs.size())
    		for(int i = 0; i < mehtodNumber - routs.size();i++)
    			routs.add(new ArrayList<Integer>());
    	routs.get(mehtodNumber).add(blockNumber);    	
    }
    */	
}
