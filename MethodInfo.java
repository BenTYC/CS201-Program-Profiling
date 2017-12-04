import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.ExceptionalBlockGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;

public class MethodInfo {
	
	private SootMethod sootMethod;
	private List<Block> blocks;
	private List<List<Block>> dominatorSets = new ArrayList<List<Block>>();
	private List<List<Block>> backEdges = new ArrayList<List<Block>>();
	private List<List<Block>> loops = new ArrayList<List<Block>>();
	
	public MethodInfo(SootMethod sootMethod){
		this.sootMethod = sootMethod;
		Body body = sootMethod.retrieveActiveBody();
		BlockGraph blockGraph = new ExceptionalBlockGraph(body);		        
		blocks = blockGraph.getBlocks();       
    	setDominators(blockGraph);
    	setBackEdgesAndLoops(blockGraph);
	}
	
	private void setDominators(BlockGraph blockGraph){
		SimpleDominatorsFinder dominatorsFinder = new SimpleDominatorsFinder(blockGraph);
		for(Block block : blockGraph.getBlocks()){
			List<Block> dominators = dominatorsFinder.getDominators(block);
			dominatorSets.add(dominators);
		}
	}
	
	private void setBackEdgesAndLoops(BlockGraph blockGraph){
	    backEdges = findBackEdges(blockGraph);
		loops = findLoops(backEdges);
	}
	
	public void printLoops(){
		System.out.println("Loops:");
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
	
	public SootMethod getMethod(){
		return sootMethod;
	}
	
	public void printMethodName(){
		System.out.println("Method:" + sootMethod);
	}
	
	public void printBlocks(){
		for (Block block : blocks) 
			printBlockInfo(block);
	}
	
	public void printBlockInfo(Block block){
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
	
	public void printDominatorSets(){
		System.out.println("Dominator Sets:");
		int blockNumber = 0;
		for(List<Block> dominators : dominatorSets){			
			System.out.print("Block " + blockNumber++ + " --> ");			
			for(Block dominator : dominators)
				System.out.print("Block " + dominator.getIndexInMethod() + " ");			
			System.out.println("");
		}		
		System.out.println("");
	}
	
	public void loopsAnalysisAndPrint(BlockGraph blockGraph){
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
	
	private ArrayList<List<Block>> findBackEdges(BlockGraph blockGraph){
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
	
	private ArrayList<List<Block>> findLoops(List<List<Block>> backEdges){
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
	
}
