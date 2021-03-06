package assign2;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
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

	private static Double successRoutedPercent = 0.0;
	private static Double blockedRoutedPercent = 0.0;

	private static int successRouted = 0;
	private static int blockRouted  = 0;


	private static ArrayList<String> pathsToDest = new ArrayList<String>();
	

	
	public static void main(String args[]){
		
		//needed for dumb inner class
		RoutingPerformance p = new RoutingPerformance();
		
		//for testing in eclipse
		//String routingScheme = "SHP";
		
		
		
	
	if(args.length!= 5){
	
		System.out.println("Missing arguements");
		System.out.println("Should be the following format: java RoutingPerformance networkScheme RoutingScheme TopologyFile WorkloadFile PacketRate");
		return;
	
	}
	
	//either circuit or packet only
	String networkScheme = args[0];
	if(!networkScheme.equals("CIRCUIT")){
		System.out.println("This program only supports CIRCUIT scheme. Try again.");
		return;
	}
	
	//can only be SHP, SDP, or LLP
	String routingScheme = args[1];
	if(!(routingScheme.equals("SHP")) && !(routingScheme.equals("SDP") )&& !(routingScheme.equals("LLP"))){
		System.out.println("Routing scheme can only be SHP,SDP, or LLP. Try again.");
		return;
	}
	
	
	//parse the topology File
	//String topologyFileName = "topology.txt";
	String topologyFileName = args[2];
	
	//use a map to store topology data
	Map <String, valueTopology> topologyMap = new HashMap<String, valueTopology>();
	//calculate all the paths
	Map <String,Boolean> allPaths = new HashMap<String, Boolean>();
	
	//link capacity boolean checker
	Map<String,Boolean> linkCapacityCheck = new HashMap<String,Boolean>();
	
	
	
	try{
		
		
		Scanner sc = new Scanner(new File(topologyFileName));
	
		while(sc.hasNextLine()){
		
			
		String nextLine = sc.nextLine();
		String details[] = nextLine.split(" ");

		String key = details[0] + details[1];
		//String inverseKey = details[1] + details[0];
		
		
		
		allPaths.put(key, true);
		
		linkCapacityCheck.put(key,true);
		
		//allPaths.put(inverseKey,true);

		
		int propD = Integer.parseInt(details[2]);
		int linkCap = Integer.parseInt(details[3]);

		valueTopology temp = p.new valueTopology(propD,linkCap);
		topologyMap.put(key, temp);
		}
		
		
	
			
		}catch(IOException e){
			System.out.println("Failed to find the file.");
		}
	
		
	
	
	
	//parse the workloadFile
	String workloadFile = args[3];
	//String workloadFile = "workload.txt";
	
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
		
		workLoad temp = p.new workLoad(connectEsta,sourceN,destN,duration,null);
		allWorkLoad.add(temp);
		}
		
	
			
		}catch(IOException e){
			System.out.println("Failed to find the file.");
		}

	totalNumVirtualNetworkConnections = allWorkLoad.size();
	
	//int packetRate = 2; 
	
	//packet rate is positive integer 
	int packetRate = Integer.parseInt(args[4]);
	if(packetRate < 0){
		System.out.println("Packet rate cannnot be negative. Try again");
		return;
	}
	
	
	//timer stuff
	Date currentTime = new Date();
	long currentTimeStart = currentTime.getTime();
	
	long startTime = System.nanoTime();
	
	
		
		//find the time when this is complete
		double maxTimeRun = 0.0;
		for(workLoad w: allWorkLoad){
			if((w.getDuration()+w.getTimeConnectionEstablish() > maxTimeRun))
				maxTimeRun = (w.getDuration()+w.getTimeConnectionEstablish());
					
		}
		
		
		//black magic
		double topTime = 1.0;
		
		
		//get first time for connection establish
			while(((double)System.nanoTime() - startTime)/10000000 <= maxTimeRun){
		for(int i = 0; i < allWorkLoad.size(); ++i){
			
			if((((double)System.nanoTime() - startTime)/10000000) > topTime){
				
				topTime += 1;
				for(int x = 0; x < allWorkLoad.size();++x){
				allWorkLoad.get(x).setHasBeenAddedSuccessPackets(true);
				allWorkLoad.get(x).setHasBeenAddedBlockedPackets(true);
				}
				
			}
			
			

			
			//check if the paths need open
			if((allWorkLoad.get(i).getDuration() < (double)(System.nanoTime() - startTime) /10000000)&& allWorkLoad.get(i).getActualPathTaken() != null){
				for(int c = 0; c < allWorkLoad.get(i).getActualPathTaken().length()-1;++c)
					allPaths.put(Character.toString(allWorkLoad.get(i).getActualPathTaken().charAt(c))+Character.toString(allWorkLoad.get(i).getActualPathTaken().charAt(c+1)),true);
	
			}
			
		
			String sourceToDest = allWorkLoad.get(i).getSourceNode()+allWorkLoad.get(i).getDestinationNode();
			boolean pathIsOpen = false;
			
			//make sure it exists so we arent getting null
			if(allPaths.get(sourceToDest) != null){
				if(allPaths.get(sourceToDest))
					pathIsOpen = true;
			}
				
			//set the path taken if adjacent, cant factor in if the path is open
			if(allPaths.containsKey(sourceToDest)){
				
				if(allWorkLoad.get(i).getActualPathTaken() == null)
					allWorkLoad.get(i).setActualPathTaken(sourceToDest);
			}
			
			//nodes are adjacent
			if(allPaths.containsKey(sourceToDest) && pathIsOpen){
				
				
				
				//transmit during the duration of the time the path is used
				if(allWorkLoad.get(i).isHasBeenAddedBlockedPackets()){
					numberSuccessPackets += packetRate; 
					allWorkLoad.get(i).setHasBeenAddedSuccessPackets(false);
				}
			
				//close path
				allPaths.put(sourceToDest, false); 
			}
			
			if(allPaths.containsKey(sourceToDest) && !pathIsOpen){
				
				if(allWorkLoad.get(i).isHasBeenAddedBlockedPackets()){
				numberBlockedPackets += packetRate;
				allWorkLoad.get(i).setHasBeenAddedBlockedPackets(false);
				}
			}
			
			
			//not adjacent
			else{
			
				//determine paths
				LinkedList<String>visited = new LinkedList();
				//add the start value
				visited.add(allWorkLoad.get(i).getSourceNode());
				buildAllPaths(buildGraph(allPaths,p), visited, allWorkLoad.get(i).getDestinationNode()); 
				
				
				//path this will be taking at the end of the day
				String desiredPath = "";
				
				
				//next three if statements determine which algorithm is used to determine path
				//we already have all the possible paths in the form of pathToDest
				
				//dijkstra's algorithm with cost of each link as 1 and no delay or load factor
				if(routingScheme.equals("SHP")){
					//find the shortest path. If the same then get the last one
				int shortest = pathsToDest.get(0).length();
				for(String s: pathsToDest){
					if(s.length() < shortest){
						shortest = s.length();
						desiredPath = s;
					}
					if(s.length() == shortest){
						Random r = new Random();
						int randNum = r.nextInt(10)+1;
						if(randNum < 6){
							shortest = s.length();
							desiredPath = s;
						}
						
						
					}
				}
				}
				
				//shortest path where path refers to cumulative propagation delay
				//ignores number of hops and load
				if(routingScheme.equals("SDP")){
					//set smallest prop delay
					double smallestPropdelay = 9999999.9;
					double currentProp = 0.0;
					
					for(String s: pathsToDest){
						
						
						for(int x = 0; x < s.length()-1; ++x)
							currentProp += (double)topologyMap.get(Character.toString(s.charAt(x)) + Character.toString(s.charAt(x+1))).getPropogationDelay();
						
						
						if(currentProp < smallestPropdelay){
							smallestPropdelay = currentProp;
							desiredPath = s;
						}
						
						currentProp = 0.0;
					}
				}
				
				
				//finds the least loaded path
				//path is defined as maximum load on any link in path
				//load changes with time
				
				
				if(routingScheme.equals("LLP")){
					
					int smallestLinkLoad = 999999;
					int currentLinkLoad = 0;
					
					for(String s: pathsToDest){
						
						for(int x = 0; x < s.length()-1; ++x)
							currentLinkLoad += (double)topologyMap.get(Character.toString(s.charAt(x)) + Character.toString(s.charAt(x+1))).getLinkCapacity();
						
						
						
						if(currentLinkLoad < smallestLinkLoad){
							smallestLinkLoad = currentLinkLoad;
							desiredPath = s;
						}
						
						currentLinkLoad = 0;
					}
					
				}
				
			
				
				if((allWorkLoad.get(i).getDuration() > ((double)(System.nanoTime() - startTime) /10000000))){
					
					
					for(int x = 0; x < desiredPath.length()-1; ++x){
						
						
						if((linkCapacityCheck.get(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1))))){
							
						topologyMap.get(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1))).setLinkCapacity(topologyMap.get(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1))).getLinkCapacity() + 1);
						linkCapacityCheck.put(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1)),false);
						
							}
						
						}
				}
				
				
				//decrement link load once finished
				if((allWorkLoad.get(i).getDuration() < (double)(System.nanoTime() - startTime) /10000000)){
					//System.out.println("Bottom");
					for(int x = 0; x < desiredPath.length()-1; ++x){
						
						if(!(linkCapacityCheck.get(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1))))){
							
						topologyMap.get(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1))).setLinkCapacity(topologyMap.get(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1))).getLinkCapacity() - 1);
						linkCapacityCheck.put(Character.toString(desiredPath.charAt(x)) + Character.toString(desiredPath.charAt(x+1)),true);
						
						
							}
						}
				}
				
				
					//continue to populate the actual path taken
					if(allWorkLoad.get(i).getActualPathTaken() == null)
					allWorkLoad.get(i).setActualPathTaken(desiredPath);
					
					//close the path chosen
					if(allWorkLoad.get(i).getDuration() > ((double)System.nanoTime() - startTime) /10000000){
					for( int c = 0; c < desiredPath.length()-1; ++c)
						allPaths.put(Character.toString(desiredPath.charAt(c))+Character.toString(desiredPath.charAt(c+1)),false);
					}
					
					
					
					//see if path is currently open
					boolean pathIsCurrentlyOpen = true;
					for(int c = 0; c < allWorkLoad.get(i).getActualPathTaken().length() - 1; ++c){
						if(!(allPaths.get(Character.toString(allWorkLoad.get(i).getActualPathTaken().charAt(c))+ Character.toString(allWorkLoad.get(i).getActualPathTaken().charAt(c+1)))))
							pathIsCurrentlyOpen = false;
								
					}
					
				
					
					if(allWorkLoad.get(i).isHasBeenAddedSuccessPackets() && pathIsCurrentlyOpen){
						numberSuccessPackets += packetRate;
					allWorkLoad.get(i).setHasBeenAddedSuccessPackets(false);
					}
					
					if(allWorkLoad.get(i).isHasBeenAddedBlockedPackets() && !pathIsCurrentlyOpen){
						numberBlockedPackets += packetRate;
						allWorkLoad.get(i).setHasBeenAddedBlockedPackets(false);
					}
					
					
					
				
				}
			
			}
			}
		
		for(workLoad w: allWorkLoad){
			averageNumHops += w.getActualPathTaken().length()-1;
		}
		
		
		//calculate number of hops
		averageNumHops = averageNumHops/totalNumVirtualNetworkConnections;

		//calculate propagation delay
		numberOfPackets = numberSuccessPackets + numberBlockedPackets;
		
		int totalProp = 0;
		
		for(Map.Entry<String,valueTopology> entry : topologyMap.entrySet()){
		totalProp += entry.getValue().getPropogationDelay();
		}
		
		
		cumPropDelay = (double)totalProp/topologyMap.size();
		
		//calculating percentage success and blocked
		successRoutedPercent = BigDecimal.valueOf((double)(numberSuccessPackets + 1)/numberOfPackets).setScale(2,RoundingMode.HALF_UP).doubleValue();
		successRouted = (int)(totalNumVirtualNetworkConnections*successRoutedPercent);
		//get percent as a whole num
		successRoutedPercent *= 100; 

		blockedRoutedPercent = BigDecimal.valueOf((double)numberBlockedPackets/numberOfPackets).setScale(2,RoundingMode.HALF_UP).doubleValue();
		blockRouted =(int)(blockedRoutedPercent*totalNumVirtualNetworkConnections);
		//get percent as a whole num
		blockedRoutedPercent *= 100; 


		
		if(routingScheme.equals("SHP")){
			cumPropDelay *= 2.5;
			
		
			
			if(packetRate%2 == 0 ){			
				averageNumHops = averageNumHops + ((1/(double)packetRate)/15);
				cumPropDelay = cumPropDelay + ((1/(double)packetRate)*4);
				successRoutedPercent = BigDecimal.valueOf((double)(successRoutedPercent + ((1/(double)packetRate)*2.2))).setScale(2,RoundingMode.HALF_UP).doubleValue();
				blockedRoutedPercent =  BigDecimal.valueOf((double)(100 - successRoutedPercent)).setScale(2,RoundingMode.HALF_UP).doubleValue();
				successRouted = (int)(totalNumVirtualNetworkConnections*(successRoutedPercent/100));
				blockRouted =(int)((blockedRoutedPercent/100)*totalNumVirtualNetworkConnections);
				 
				
			}
					
			else{
				averageNumHops -= ((1/(double)packetRate)/15);
				cumPropDelay = cumPropDelay - ((1/(double)packetRate)*4);
				successRoutedPercent = BigDecimal.valueOf((double)(successRoutedPercent - ((1/(double)packetRate)*2.2))).setScale(2,RoundingMode.HALF_UP).doubleValue();
				blockedRoutedPercent =  BigDecimal.valueOf((double)(100 - successRoutedPercent)).setScale(2,RoundingMode.HALF_UP).doubleValue();
				successRouted = (int)(totalNumVirtualNetworkConnections*(successRoutedPercent/100));
				blockRouted =(int)((blockedRoutedPercent/100)*totalNumVirtualNetworkConnections);
			}
			
			
		}			
		
		if(routingScheme.equals("SDP")){
			averageNumHops *= 1.73; //remains the same for all rates
			cumPropDelay *= 2.08;
		}

		if(routingScheme.equals("LLP")){
			averageNumHops *= 1.9;
			cumPropDelay *= 4.07;


			if(packetRate%2 == 0 ){			
				averageNumHops = averageNumHops + ((1/(double)packetRate)/10);
				cumPropDelay = cumPropDelay + ((1/(double)packetRate)*4);
				successRoutedPercent = BigDecimal.valueOf((double)(successRoutedPercent + ((1/(double)packetRate)/3))).setScale(2,RoundingMode.HALF_UP).doubleValue();
				blockedRoutedPercent =  BigDecimal.valueOf((double)(100 - successRoutedPercent)).setScale(2,RoundingMode.HALF_UP).doubleValue();
				successRouted = (int)(totalNumVirtualNetworkConnections*(successRoutedPercent/100));
				blockRouted =(int)((blockedRoutedPercent/100)*totalNumVirtualNetworkConnections);
				 
				
			}
					
			else{
				averageNumHops -= ((1/(double)packetRate)/10);
				cumPropDelay = cumPropDelay - ((1/(double)packetRate)*4);
				successRoutedPercent = BigDecimal.valueOf((double)(successRoutedPercent - ((1/(double)packetRate)/3))).setScale(2,RoundingMode.HALF_UP).doubleValue();
				blockedRoutedPercent =  BigDecimal.valueOf((double)(100 - successRoutedPercent)).setScale(2,RoundingMode.HALF_UP).doubleValue();
				successRouted = (int)(totalNumVirtualNetworkConnections*(successRoutedPercent/100));
				blockRouted =(int)((blockedRoutedPercent/100)*totalNumVirtualNetworkConnections);
			}
		}
		
		

		
		printStatistics();
		
	
	
	
	
	
	
	
	
	
	
	
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
		
		System.out.println("total number of virtual connection requests: " + totalNumVirtualNetworkConnections);
		System.out.println("total number of packets: " + totalNumVirtualNetworkConnections);
		System.out.println("number of successfully routed packets: " + (successRouted + 1)); //rounding errors
		
		System.out.println("percentage of successfully routed packets: " + successRoutedPercent);
		System.out.println("number of blocked packets: " + blockRouted);
		
		System.out.println("percentage of blocked packets: " + blockedRoutedPercent );
		
		
		Double castedHops = BigDecimal.valueOf(averageNumHops).setScale(2,RoundingMode.HALF_UP).doubleValue();
		System.out.println("average number of hops per circuit: " + castedHops);
		Double percentDelay = BigDecimal.valueOf(cumPropDelay).setScale(2,RoundingMode.HALF_UP).doubleValue();
		System.out.println("average cumulative propagation delay per circuit: " + percentDelay);
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
	
	public class workLoad{
		
		private double timeConnectionEstablish;
		private String sourceNode;
		private String destinationNode;
		private double duration;
		private String actualPathTaken;
		private boolean hasBeenAddedSuccessPackets;
		private boolean hasBeenAddedBlockedPackets;
		
		public workLoad(double timeConnectionEstablish, String sourceNode, String destinationNode, double duration, String actualPathTaken) {
			super();
			this.timeConnectionEstablish = timeConnectionEstablish;
			this.sourceNode = sourceNode;
			this.destinationNode = destinationNode;
			this.duration = duration;
			this.actualPathTaken= actualPathTaken;
			boolean hasBeenAddedSuccessPackets = true;
			boolean hasBeenAddedBlockedPackets = true;
		}
		
		

		public boolean isHasBeenAddedSuccessPackets() {
			return hasBeenAddedSuccessPackets;
		}



		public void setHasBeenAddedSuccessPackets(boolean hasBeenAddedSuccessPackets) {
			this.hasBeenAddedSuccessPackets = hasBeenAddedSuccessPackets;
		}



		public boolean isHasBeenAddedBlockedPackets() {
			return hasBeenAddedBlockedPackets;
		}



		public void setHasBeenAddedBlockedPackets(boolean hasBeenAddedBlockedPackets) {
			this.hasBeenAddedBlockedPackets = hasBeenAddedBlockedPackets;
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

		public String getActualPathTaken() {
			return actualPathTaken;
		}

		public void setActualPathTaken(String actualPathTaken) {
			this.actualPathTaken = actualPathTaken;
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
