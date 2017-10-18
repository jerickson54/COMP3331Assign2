package assign2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import javax.swing.plaf.synth.SynthStyle;

public class RoutingPerformance {
	
	//arguements
	/*
	 * NETWORK SCHEME-type of arguement that is evaluated
	 * ROUTING SCHEME- SHP,SDP,LLP
	 * TOPOLOGY FILE
	 * WORKLOAD FILE-virtual network connection requests
	 * PACKET_RATE- positive integer that shows the number of packets per second 
	 * 
	 */
	
	/*NEED the following statistics
	 * 
	 * total number of virtual network connections
	 * total number of packets
	 * number of successfully routed packets
	 * number of blocked packets
	 * average number of hops
	 * average source to destination cumulatative propagation delay per successfully routed circuit
	 */
	
	private static int totalNumVirtualNetworkConnections = 0;
	private static int numberOfPackets = 0;
	private static int numberSuccessPackets = 0;
	private static int numberBlockedPackets = 0;
	private static double averageNumHops = 0.0;
	private static double cumPropDelay = 0.0;

	private static ArrayList<String> pathsToDest = new ArrayList<String>();

	
	public static void main(String args[]){
		
		//needed for dumb inner class
		RoutingPerformance p = new RoutingPerformance();
		
		//for testing in eclipse
		String routingScheme = "SHP";
		
		
		
	/*	
	if(args.length!= 5){
	
		System.out.println("Missing arguements");
		System.out.println("Should be the following format: java RoutingPerformance networkScheme RoutingScheme TopologyFile WorkloadFile PacketRate");
		return;
	
	}
	
	//either circuit or packet only
	String networkScheme = args[0];
	if(networkScheme.equals("CIRCUIT") || networkScheme.equals("PACKET")){
		System.out.println("Network scheme can only be CIRCUIT or PACKET. Try again.");
		return;
	}
	
	//can only be SHP, SDP, or LLP
	String routingScheme = args[1];
	if(routingScheme.equals("SHP") || routingScheme.equals("SDP") || routingScheme.equals("LLP")){
		System.out.println("Routing scheme can only be SHP,SDP, or LLP. Try again.");
		return;
	}
	
	*/
	//parse the topology File
	String topologyFileName = "topology.txt";
	//String topologyFileName = args[2];
	
	//use a map to store topology data
	Map <String, valueTopology> topologyMap = new HashMap<String, valueTopology>();
	//calculate all the paths
	Map <String,Boolean> allPaths = new HashMap<String, Boolean>();
	
	
	
	try{
		
		
		Scanner sc = new Scanner(new File(topologyFileName));
	
		while(sc.hasNextLine()){
		
			
		String nextLine = sc.nextLine();
		String details[] = nextLine.split(" ");

		String key = details[0] + details[1];
		//String inverseKey = details[1] + details[0];
		
		allPaths.put(key, true);
		
		//allPaths.put(inverseKey,true);

		
		int propD = Integer.parseInt(details[2]);
		int linkCap = Integer.parseInt(details[3]);

		valueTopology temp = p.new valueTopology(propD,linkCap);
		topologyMap.put(key, temp);
		}
		
		
	
			
		}catch(IOException e){
			System.out.println("Failed to find the file.");
		}
	
		
		
		for(Map.Entry<String,Boolean> entry : allPaths.entrySet()){
			//if(entry.getKey().equals("CD"))
				//allPaths.put("CD", false);
			System.out.println("Key: " + entry.getKey());
			System.out.println("value: "+ entry.getValue());
			//for(path s : entry.getValue()){
			//	System.out.print(s.getDest());
			//	System.out.print(s.isOpen());
			//}
			System.out.println();
		}
		
		
	
	
	
	
	
	
	//parse the workloadFile
	//String workloadFile = args[3];
	String workloadFile = "workload.txt";
	
	//use an arrayList to store workload information
	ArrayList<workLoad> allWorkLoad = new ArrayList<workLoad>();
	
	try{
		
		
		Scanner sc = new Scanner(new File(workloadFile));
	
		while(sc.hasNextLine()){
		String nextLine = sc.nextLine();
		String details[] = nextLine.split(" ");

		double connectEsta = Double.parseDouble(details[0]);
		
		String sourceN = details[1];
		String destN = details[2];
		
		double duration = Double.parseDouble(details[3]);
		
		workLoad temp = p.new workLoad(connectEsta,sourceN,destN,duration);
		allWorkLoad.add(temp);
		}
		
	
			
		}catch(IOException e){
			System.out.println("Failed to find the file.");
		}

	totalNumVirtualNetworkConnections = allWorkLoad.size();
	
	int packetRate = 2; 
	/*
	//packet rate is positive integer 
	int packetRate = Integer.parseInt(args[4]);
	if(packetRate < 0){
		System.out.println("Packet rate cannnot be negative. Try again");
		return;
	}
	*/
	
	//timer stuff
	Date currentTime = new Date();
	long currentTimeStart = currentTime.getTime();
	
	long startTime = System.nanoTime();
	
	//dijkstra's algorithm with cost of each link as 1 and no delay or load factor
	if(routingScheme.equals("SHP")){
		
		//find the time when this is complete
		double maxTimeRun = 0.0;
		for(workLoad w: allWorkLoad){
			if((w.getDuration()+w.getTimeConnectionEstablish() > maxTimeRun))
				maxTimeRun = (w.getDuration()+w.getTimeConnectionEstablish());
					
		}
		
		
		
		
		//get first time for connection establish
			while(((double)System.nanoTime() - startTime)/10000000 <= maxTimeRun){
		for(int i = 0; i < allWorkLoad.size(); ++i){
			
			//System.out.println(((double)System.nanoTime() - startTime)/10000000);
			
			
			String sourceToDest = allWorkLoad.get(i).getSourceNode()+allWorkLoad.get(i).getDestinationNode();
			boolean pathIsOpen = false;
			
			//make sure it exists so we arent getting null
			if(allPaths.get(sourceToDest) != null){
				if(allPaths.get(sourceToDest))
					pathIsOpen = true;
			}
				
			
			//nodes are adjacent
			if(allPaths.containsKey(sourceToDest) && pathIsOpen){
				//transmit during the duration of the time the path is used
				numberOfPackets += packetRate * (allWorkLoad.get(i).getDuration() - allWorkLoad.get(i).getTimeConnectionEstablish()); 
			
				//close path
				allPaths.put(sourceToDest, false); 
			}
			
			//not adjacent
			else{
			
				//determine paths
				LinkedList<String>visited = new LinkedList();
				//add the start value
				visited.add(allWorkLoad.get(i).getSourceNode());
				buildAllPaths(buildGraph(allPaths,p), visited, allWorkLoad.get(i).getDestinationNode()); 
				
				//find the shortest path. If the same then get the last one
				int shortest = pathsToDest.get(0).length();
				String desiredPath = "";
				for(String s: pathsToDest){
					if(s.length() <= shortest){
						shortest = s.length();
						desiredPath = s;
					}
				}
					//System.out.println(desiredPath);
				
					//close the path chosen
					for( int c = 0; c < desiredPath.length()-1; ++c)
						allPaths.put(Character.toString(desiredPath.charAt(c))+Character.toString(desiredPath.charAt(c+1)),false);
					
					System.out.println("AFTER OPEN CLOSE");
					for(Map.Entry<String,Boolean> entry : allPaths.entrySet()){
						//if(entry.getKey().equals("CD"))
							//allPaths.put("CD", false);
						System.out.println("Key: " + entry.getKey());
						System.out.println("value: "+ entry.getValue());
						//for(path s : entry.getValue()){
						//	System.out.print(s.getDest());
						//	System.out.print(s.isOpen());
						//}
						System.out.println();
					}
					
					
					numberOfPackets += packetRate * (allWorkLoad.get(i).getDuration() - allWorkLoad.get(i).getTimeConnectionEstablish());
					
					//close the paths
					
				
				}
			
			}
			}
		
		printStatistics();
		
	} //SHP bracket
	//double timeElapsed = (System.nanoTime() - startTime)/10000000;
	
	
	
	
	
	
	
	
	
	
	}
	public static void buildAllPaths(Graph g, LinkedList<String> visited,String end){
		
	     LinkedList<String> nodes = g.adjacentNodes(visited.getLast());
	        // examine adjacent nodes
	        for (String node : nodes) {
	            if (visited.contains(node)) {
	                continue;
	            }
	            if (node.equals(end)) {
	                visited.add(node);
	                buildPathsToDest(visited);
	                visited.removeLast();
	                break;
	            }
	        }
	        for (String node : nodes) {
	            if (visited.contains(node) || node.equals(end)) {
	                continue;
	            }
	            visited.addLast(node);
	            buildAllPaths(g, visited,end);
	            visited.removeLast();
	        }
		
	}
	
	private static void printStatistics(){
		
		System.out.println("Total number of virtual connection requests: " + totalNumVirtualNetworkConnections);
		System.out.println("Total number of packets: " + numberOfPackets);
		System.out.println("Total number of successfully routed packets: " + numberSuccessPackets);
		System.out.println("Percentage of successfully routed packets: " + (double)numberSuccessPackets/numberOfPackets);
		System.out.println("Number of blocked packets: " + numberBlockedPackets);
		System.out.println("Percentage of blocked packets: " + (double)numberBlockedPackets/numberOfPackets );
		System.out.println("Average number of hops per circuit: " + averageNumHops);
		System.out.println("Average cumulative propagation delay per circuit: " + cumPropDelay);
	}
	
	 private static void buildPathsToDest(LinkedList<String> visited) {
		 	String path = "";
	        for (String node : visited)
	            path +=node;
	        
	        pathsToDest.add(path);
	       
	    }
	
	
	public static Graph buildGraph(Map<String, Boolean> allPaths, RoutingPerformance p){
		Graph g = p.new Graph();
		
		for(Map.Entry<String,Boolean> entry : allPaths.entrySet()){
			if(entry.getValue())
			g.addEdge(entry.getKey().substring(0,1), entry.getKey().substring(1,2));
		}
		
		
		return g;
	}
	/**
	
	public static void searchPaths(Map<String, Boolean> allPaths, String sourceChar, String destChar, String toPath){
		for(Map.Entry<String,Boolean> entry : allPaths.entrySet()){
			if(entry.getKey().substring(0,1).equals(sourceChar)){
				toPath += entry.getKey();
				searchPaths(allPaths,entry.getKey().substring(1,2),destChar,toPath);
				
			}
			
			if(entry.getKey().substring(0,1).equals(destChar)){
				paths.add(toPath);
				toPath;
			}
		}
			

		
		return;
		
		
	}

	**/
	
	
	public class valueTopology{
		private int propogationDelay;
		private int linkCapacity;
		
		
		
		public valueTopology(int propogationDelay, int linkCapacity) {
			super();
			this.propogationDelay = propogationDelay;
			this.linkCapacity = linkCapacity;
		}
		
		public int getPropogationDelay() {
			return propogationDelay;
		}
		public void setPropogationDelay(int propogationDelay) {
			this.propogationDelay = propogationDelay;
		}
		public int getLinkCapacity() {
			return linkCapacity;
		}
		public void setLinkCapacity(int linkCapacity) {
			this.linkCapacity = linkCapacity;
		}
		
		
		
	}
	/*
	public class path{
		private String dest;
		private boolean isOpen;
		
		
		public path(String dest, boolean isOpen) {
			super();
			this.dest = dest;
			this.isOpen = isOpen;
		}
		
		public String getDest() {
			return dest;
		}
		public void setDest(String dest) {
			this.dest = dest;
		}
		public boolean isOpen() {
			return isOpen;
		}
		public void setisOpen(boolean isOpen) {
			this.isOpen = isOpen;
		}
		
		
	}
	*/
	
	public class workLoad{
		
		private double timeConnectionEstablish;
		private String sourceNode;
		private String destinationNode;
		private double duration;
		
		public workLoad(double timeConnectionEstablish, String sourceNode, String destinationNode, double duration) {
			super();
			this.timeConnectionEstablish = timeConnectionEstablish;
			this.sourceNode = sourceNode;
			this.destinationNode = destinationNode;
			this.duration = duration;
		}

		public double getTimeConnectionEstablish() {
			return timeConnectionEstablish;
		}

		public void setTimeConnectionEstablish(double timeConnectionEstablish) {
			this.timeConnectionEstablish = timeConnectionEstablish;
		}

		public String getSourceNode() {
			return sourceNode;
		}

		public void setSourceNode(String sourceNode) {
			this.sourceNode = sourceNode;
		}

		public String getDestinationNode() {
			return destinationNode;
		}

		public void setDestinationNode(String destinationNode) {
			this.destinationNode = destinationNode;
		}

		public double getDuration() {
			return duration;
		}

		public void setDuration(double duration) {
			this.duration = duration;
		}
		
		
		
	}
	public class Graph {
	    private Map<String, LinkedHashSet<String>> map = new HashMap();

	    public void addEdge(String node1, String node2) {
	        LinkedHashSet<String> adjacent = map.get(node1);
	        if(adjacent==null) {
	            adjacent = new LinkedHashSet();
	            map.put(node1, adjacent);
	        }
	        adjacent.add(node2);
	    }

	    public void addTwoWayVertex(String node1, String node2) {
	        addEdge(node1, node2);
	        addEdge(node2, node1);
	    }

	    public boolean isConnected(String node1, String node2) {
	        Set adjacent = map.get(node1);
	        if(adjacent==null) {
	            return false;
	        }
	        return adjacent.contains(node2);
	    }

	    public LinkedList<String> adjacentNodes(String last) {
	        LinkedHashSet<String> adjacent = map.get(last);
	        if(adjacent==null) {
	            return new LinkedList();
	        }
	        return new LinkedList<String>(adjacent);
	    }
	
	}
	
}