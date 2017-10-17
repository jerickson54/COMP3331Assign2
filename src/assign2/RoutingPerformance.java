package assign2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
	

	
	public static void main(String args[]){
		
		//needed for dumb inner class
		RoutingPerformance p = new RoutingPerformance();
		
		
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
	
	
	//parse the topology File
	//String topologyFileName = "topology.txt";
	String topologyFileName = args[2];
	
	//use a map to store topology data
	Map <String, valueTopology> topologyMap = new HashMap<String, valueTopology>();
	
	
	
	try{
		
		
		Scanner sc = new Scanner(new File(topologyFileName));
	
		while(sc.hasNextLine()){
		String nextLine = sc.nextLine();
		String details[] = nextLine.split(" ");

		String key = details[0] + details[1];
		
		int propD = Integer.parseInt(details[2]);
		int linkCap = Integer.parseInt(details[3]);

		valueTopology temp = p.new valueTopology(propD,linkCap);
		topologyMap.put(key, temp);
		}
		
	
			
		}catch(IOException e){
			System.out.println("Failed to find the file.");
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
	
	//double timeElapsed = (System.nanoTime() - startTime)/10000000;
	
	
	
	
	
	
	
	
	
	
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

	
}


