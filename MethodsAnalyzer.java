
import java.util.ArrayList;
import java.util.List;
import soot.SootMethod;
import soot.toolkits.graph.Block;


public class MethodsAnalyzer {

	private static List<MethodInfo> methodInfoList = new ArrayList<MethodInfo>();
	
	public static void addAndAnalye(SootMethod sootMethod){
		methodInfoList.add(new MethodInfo(sootMethod));
	}
	
	public static void printMethodInfo(SootMethod sootMethod){
		for(MethodInfo methodInfo: methodInfoList){
			if(sootMethod == methodInfo.getMethod()){
				methodInfo.printMethodName();
				methodInfo.printBlocks();
				methodInfo.printDominatorSets();
				methodInfo.printLoops();
			}
		}
	}
	
	public static void printMethodInfo(){
		System.out.println("\n***********\n");
		for(MethodInfo methodInfo: methodInfoList){
			methodInfo.printMethodName();
			methodInfo.printBlocks();
			methodInfo.printDominatorSets();
			methodInfo.printLoops();
		}
	}
	
	public static void insertRouteRecorder(Block block){
		
	}
	
}
