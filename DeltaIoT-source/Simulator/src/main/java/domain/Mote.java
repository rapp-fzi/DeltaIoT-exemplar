package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mote extends Node {

	private double batteryCapacity;
	private double batteryRemaining;
	
	private int load; // = number of packets to send in a turn
	private Profile<Double> activationProbability = new Constant<>(1.0); // = chance this mote will send packets
	
	private List<Link> links = new ArrayList<>();
	private List<Packet> packetQueue = new ArrayList<>();
	
	private int lastPacketNumber = 0;
	
	
	public Mote(int id, double batteryCapacity, int load, Position position) {
		super(id, position);
		this.batteryCapacity = batteryCapacity;
		this.batteryRemaining = batteryCapacity;
		this.load = load;
	}
	
	public Mote(int id, double batteryCapacity, int load) {
		this(id, batteryCapacity, load, null);
	}
		
	public double getBatteryCapacity() {
		return batteryCapacity;
	}
	public double getBatteryRemaining() {
		return batteryRemaining;
	}
	public void setBatteryRemaining(double batteryRemaining) {
		this.batteryRemaining = batteryRemaining;
	}
	public int getLoad() {
		return load;
	}
	public Profile<Double> getActivationProbability() {
		return activationProbability;
	}
	public void setActivationProbability(Profile<Double> actProbProf) {
		this.activationProbability = actProbProf;
	}
	
	public void addLinkTo(Node to, Gateway direction, int power, int distribution) {
		Link link = new Link(this, to, direction, power, distribution);
		links.add(link);
	}
	
	public Link getLinkTo(Node to) {
		for(Link link: links) {
			if(link.getTo() == to) {
				return link;
			}
		}
		return null;
	}
	
	public List<Link> getLinks() {
		return Collections.unmodifiableList(links);
	}

	/**
	 * Handles the turn of this mote for the emulation
	 * 
	 * 1. It might send some packets of it's own (based on load and activation probability)
	 * 		a. It will increment the expected packets for the chosen destination gateway (to calculate packetloss easily)
	 * 2. It will send any packets is has queued up from other motes
	 * 3. It will reduce its battery based on the number of packets send
	 */
	public void handleTurn(RunInfo runInfo) {
		// Create your own packets
		List<Packet> myPackets = new ArrayList<>(); //empty if none send
		boolean shouldSend = Math.random() < activationProbability.get(runInfo.getRunNumber());
		if (shouldSend) {
			// Create your own packets
			for (int i = 0; i < load; i++) {
				lastPacketNumber ++;
				myPackets.add(new Packet(this, null, lastPacketNumber));
			}
		}
		
		// Decide what direction to send the packets
		// 1. Calculate total distribution
		{
			int totalDistribution = 0;
			for(Link link: links) {
				totalDistribution += link.getDistribution();
			}
			// 2. For each packet choose a destination
			for(Packet packet: myPackets) {
				int rand = (int)Math.round(Math.random() * totalDistribution);
				int countDistribution = 0;
		        for (Link link : links) {
		        	countDistribution += link.getDistribution();
		            if (countDistribution >= rand) {
		                packet.setDestination(link.getDirection());
		            	break;
		            }
		        }
			}
		}
		// 3. Notify the gateways they can expect packages, so they can easily calculate packet loss
		for(Packet packet: myPackets) {
			packet.getDestination().addPacketToExpect();
		}
		
		// Send all packets in queue
		packetQueue.addAll(myPackets);
		for(Packet packet: packetQueue) {
			// Gather possible destination links
			List<Link> possibleLinks = new ArrayList<>();
			for(Link link: links) {
				if(link.getDirection() == packet.getDestination()) {
					possibleLinks.add(link);
				}
			}
			// Calculate total distribution
			int totalDistribution = 0;
			for(Link link: possibleLinks) {
				totalDistribution += link.getDistribution();
			}
			// Send packets
			//   if there are no possible links, the packet won't be send.
			if (possibleLinks.isEmpty()) {
				// This if isn't really necessary, if possibleLinks is empty, 
				//		the last else will happen, but the for-loop won't do anything
				// This if just makes it explicit.
				// TODO perhaps do something else than nothing?
			}
			//   if distribution is > 100 I assume packets are duplicated over all possible links
			else if (totalDistribution > 100) {
				for(Link link: possibleLinks) {
					sendPacketOver(link, packet, runInfo);
				}
			}
			//   else the distribution is handled like weights
			else {
				int rand = (int)Math.round(Math.random() * totalDistribution);
				int countDistribution = 0;
		        for (Link link : possibleLinks) {
		        	countDistribution += link.getDistribution();
		            if (countDistribution >= rand) {
		            	sendPacketOver(link, packet, runInfo);
		            	break;
		            }
		        }
			}
		}
		
		// Clear packet queue since they are all send now
		packetQueue.clear();
	}
	
	void sendPacketOver(Link link, Packet packet, RunInfo runInfo) {
		assert links.contains(link);
		
		// Send packet over a link
		link.sendPacket(packet, runInfo);

		// Subtract battery usage
		double batteryUsage = link.getSfTime() * (link.getPowerConsumptionRate() / DomainConstants.coulomb);
		batteryRemaining -= batteryUsage;
		packet.getDestination().reportPowerConsumed(batteryUsage);
	}

	@Override
	void receivePacket(Packet packet) {
		packetQueue.add(packet);
		// Subtract battery life
		double batteryUsage = DomainConstants.receptionTime * (DomainConstants.receptionCost / DomainConstants.coulomb);
		batteryRemaining -= batteryUsage;
		packet.getDestination().reportPowerConsumed(batteryUsage);
	}

	@Override
	public String toString() {
		return "Mote " + String.format("%2d", getId()) + " [battery=" + String.format("%5.1f", batteryRemaining) + "/" + String.format("%5.1f", batteryCapacity) 
		+ ", load=" + load + ", queue=" + packetQueue.size() + "]";
	}
	
}
