import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.infoflow.AbstractDataSource;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.util.Chain;


public class Main {
	
	static boolean addedClass = false;
	static SootClass routeRecordClass;
	static SootMethod routeRecorder, reportRecord, setMehtodInProgress, routeAnalye;  
	
	public static void main(String[] args) {
		
		//Static Analysis (Retrieve Flow Graph)
		staticAnalysis();
		
		//Dynamic Analysis (Instrumentation) 
		dynamicAnalysis();
		
		Scene.v().addBasicClass("MyRouteRecord", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.util.ArrayList", SootClass.SIGNATURES);
		Scene.v().addBasicClass("java.util.List", SootClass.SIGNATURES);
		
		soot.Main.main(args);

	}

	private static void staticAnalysis(){
		configure("C:\\Users\\Ben\\workspace\\CS201Profiling\\Analysis");
		SootClass sootClass = Scene.v().loadClassAndSupport("Test3");
	    sootClass.setApplicationClass();
	    
	    //Static Analysis code
	    System.out.println("");
	    for (SootMethod sootMethod: sootClass.getMethods()){
	    	if(sootMethod.isConcrete()){
	    		System.out.println("\nMethodNumber: " + sootMethod.getNumber() + "\n");
	    		MethodsAnalyzer.addAndAnalye(sootMethod);
	    		MethodsAnalyzer.printMethodInfo(sootMethod);
	    		/*methodAnalysisAndPrint(sootMethod);
		        
	    		Body body = sootMethod.retrieveActiveBody();
	    		BlockGraph blockGraph = new ExceptionalBlockGraph(body);		        
		        
		        for (Block block : blockGraph.getBlocks()) 
		        	blockAnalysisAndPrint(block);
		        
		    	dominatorSetsAnalysisAndPrint(blockGraph);
		    	
		    	loopsAnalysisAndPrint(blockGraph);
		    	*/
	    	}
	    }
	    
	}

	private static void dynamicAnalysis(){
		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {
			
		@Override
		protected void internalTransform(Body arg0, String arg1, Map arg2) {
			
			synchronized(this)
			{
				if(!addedClass){
					routeRecordClass = Scene.v().loadClassAndSupport("MyRouteRecord"); 
				    routeRecorder = routeRecordClass.getMethod("void routeRecorder(int)");
				    setMehtodInProgress = routeRecordClass.getMethod("void setMehtodInProgress(int)");
				    reportRecord = routeRecordClass.getMethod("void reportRecord()"); 
				    routeAnalye = routeRecordClass.getMethod("void routeAnalye()"); 
					addedClass = true;
				}				
			}
			
			
			//Dynamic Analysis (Instrumentation) code
			SootMethod method = arg0.getMethod();  
	        System.out.println("instrumenting method : " + method.getSignature());
	        BriefBlockGraph blockGraph = new BriefBlockGraph(arg0);
	        
	        //put record code
	        
	        for(Block block: blockGraph.getBlocks()){
	        	InvokeExpr incExpr = Jimple.v().newStaticInvokeExpr(setMehtodInProgress.makeRef(), IntConstant.v(method.getNumber()));
	        	Stmt recordStmt = Jimple.v().newInvokeStmt(incExpr);
	        	block.insertBefore(recordStmt, block.getTail());
	        	incExpr = Jimple.v().newStaticInvokeExpr(routeRecorder.makeRef(), IntConstant.v(block.getIndexInMethod()));
	        	recordStmt = Jimple.v().newInvokeStmt(incExpr);
	        	block.insertBefore(recordStmt, block.getTail());
	        }
	        
	        //put analysis code
	        ArrayList<List<Block>> backEdges = findBackEdges(blockGraph);
	        
	        //put report code	        
	        Chain<Unit> units = arg0.getUnits();  
	        Iterator<Unit> stmtIt = units.snapshotIterator(); 
	        String signature = method.getSubSignature();  
	        boolean isMain = signature.equals("void main(java.lang.String[])");  
	        if (isMain) {  
	            stmtIt = units.snapshotIterator();  
	            while (stmtIt.hasNext()) {  
	                Stmt stmt = (Stmt)stmtIt.next();  
	                if ((stmt instanceof ReturnStmt) || (stmt instanceof ReturnVoidStmt)){  
	                	InvokeExpr analyeExpr = Jimple.v().newStaticInvokeExpr(routeAnalye.makeRef());  
	                    Stmt analyeStmt = Jimple.v().newInvokeStmt(analyeExpr);  
	                    units.insertBefore(analyeStmt, stmt); 
	                    InvokeExpr reportExpr = Jimple.v().newStaticInvokeExpr(reportRecord.makeRef());  
	                    Stmt reportStmt = Jimple.v().newInvokeStmt(reportExpr);  
	                    units.insertBefore(reportStmt, stmt);  
	                }  
	            }  
	        } 
	        
	        
	        
		}			
	   }));
	}
	
	public static void configure(String classpath) {		
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_java);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_soot_classpath(classpath);
        Options.v().set_prepend_classpath(true);
        Options.v().setPhaseOption("cg.spark", "on");        
    }
	
	/*
	public static void methodAnalysisAndPrint(SootMethod sootMethod){
		System.out.print("Method:");
		System.out.println(sootMethod);
	}
	
	public static void blockAnalysisAndPrint(Block block){
		System.out.println("Basic Block: " + block.getIndexInMethod());
		
		Iterator<Unit> unitIterator = block.iterator();
		while(unitIterator.hasNext())
			System.out.println(unitIterator.next());
		
		System.out.print("Preds: " );
		for(Block pre: block.getPreds())
			System.out.print( pre.getIndexInMethod() + " ");
		System.out.println("");
		
		System.out.print("Succs: ");
		for(Block suc: block.getSuccs())
			System.out.print( suc.getIndexInMethod() + " ");
		System.out.println("");
		
		System.out.println("");
	}
	
	public static void dominatorSetsAnalysisAndPrint(BlockGraph blockGraph){
		System.out.println("Dominator Sets:");
		
		SimpleDominatorsFinder dominatorsFinder = new SimpleDominatorsFinder(blockGraph);
		for(Block block : blockGraph.getBlocks()){
			
			System.out.print("Block " + block.getIndexInMethod() + " --> ");
			
			List<Block> dominators = dominatorsFinder.getDominators(block);
			for(Block dominator : dominators)
				System.out.print("Block " + dominator.getIndexInMethod() + " ");
			
			System.out.println("");
		}
		
		System.out.println("");
	}
	
	public static void loopsAnalysisAndPrint(BlockGraph blockGraph){
		System.out.println("Loops:");
		
	    ArrayList<List<Block>> backEdges = findBackEdges(blockGraph);
		ArrayList<List<Block>> loops = findLoops(backEdges);
		
		//print loops
		for(List<Block> loop: loops){
			System.out.print("[");
			for(int i = 0; i < loop.size() - 1; i++){
				System.out.print(loop.get(i).getIndexInMethod() + ", ");
			}
			System.out.print(loop.get(loop.size() - 1).getIndexInMethod());
			System.out.println("]");
		}
		System.out.println("");
	}
	*/
	private static ArrayList<List<Block>> findBackEdges(BlockGraph blockGraph){
	    ArrayList<List<Block>> backEdges = new ArrayList<List<Block>>();
	    SimpleDominatorsFinder dominatorsFinder = new SimpleDominatorsFinder(blockGraph);
		for(Block block : blockGraph.getBlocks()){
			List<Block> dominators = dominatorsFinder.getDominators(block);			
			for(Block dominator : dominators){
				for(Block suc: block.getSuccs()){
					if(suc == dominator){
						List<Block> backEdge = new ArrayList<Block>();
						backEdge.add(0,block);
						backEdge.add(1,suc);
						backEdges.add(backEdge);
					}
				}
			}
		}
		return backEdges;
	}
	/*
	private static ArrayList<List<Block>> findLoops(ArrayList<List<Block>> backEdges){
		ArrayList<List<Block>> loops = new ArrayList<List<Block>>();
		Stack<Block> stack = new Stack<Block>();		
		for(List<Block> backEdge: backEdges){
			List<Block> loop = new ArrayList<Block>();
			loop.add(backEdge.get(1));
			loop.add(backEdge.get(0));
			stack.push(backEdge.get(0));
			while(!stack.empty()){
				Block m = stack.pop();
				for(Block p: m.getPreds()){
					if(!loop.contains(p)){
						loop.add(p);
						stack.push(p);
					}
				}
			}			
			loops.add(loop);
		}		
		return loops;
	}
	*/
}
