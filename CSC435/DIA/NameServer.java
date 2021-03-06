/*--------------------------------------------------------
1. Name / Date:
Rong Zhuang
2 Nov, 2015

2. Java version used:
build 1.8.0_60

3. Precise command-line compilation examples / instructions:
> Run DIA.bat

4. Precise examples / instructions to run this program:
In separate shell windows:
> Run DIA.bat

5. Notes:
 a. Web Server monitors the http request at port: 2540.
 b. Web Server is capable of handling requests simultaneously.
 c. The logs in console will be saved to file MyWebServerLogs.txt. The requests
    for icons will not be displayed, since there are too many such logs when you
    are exploreing the direcotry.
 d. Main functions:
  1) Explore files/directories recursively
  2) Display content of a single file;
  3) Handle dynamical script execution, eg. fake-cgi request.
	4) Logs will be saved each time after a new request is handled.
----------------------------------------------------------*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Model class for server
 */
class Server implements Serializable {
	private String ip = "";
	private int port = 0;
	private List<Agent> agents = new CopyOnWriteArrayList<Agent>();

	public Server(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

  public String getIp() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getCounter() {
		return this.agents.size();
	}

	public void addAgent(Agent ag) {
		this.agents.add(ag);
	}

	public void removeAgent(String name) {
		for(Agent ag : this.agents) {
			if (ag.getName().equals(name)) {
				this.agents.remove(ag);
			}
		}
	}
}

/**
 * Model class for agent
 */
class Agent implements Serializable {
	private int id;
	private String name;
	private String ip;
	private int port;
	private Server server;
	private Group group;

	public Agent(int id, String name, String ip, int port, Server server, Group group) {
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.server = server;
		this.group = group;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Server getServer() {
		return this.server;
	}

	public Group getGroup() {
		return this.group;
	}

	public String toString() {
		return this.name + ":" + this.ip + ":" + this.port;
	}
}

/**
 * Model class for group
 */
class Group implements Serializable {
	private int id;
	private String name;
	private String color;
	private List<Agent> agents = new CopyOnWriteArrayList<Agent>();

	public Group(int id, String name, String color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getColor() {
		return this.color;
	}

	public int getCounter() {
		return this.agents.size();
	}

	public void addAgent(Agent ag) {
		this.agents.add(ag);
	}

	public void removeAgent(int id) {
		for(Agent ag : this.agents) {
			if (ag.getId() == id) {
				this.agents.remove(ag);
				break;
			}
		}
	}
}

class LogHelper {
	// Path for the log file
	private String filePath = "";
	// Use an ArrayList to store the logs from server
	private ArrayList<String> alLogs = new ArrayList<String>();

	public LogHelper(String file) {
		this.filePath = file;
	}

	/**
	 * Clear all log
	 */
	public void clear() {
			alLogs.clear();
	}


	/**
	 * Write log to local storage list
	 * @param log, the content of the log
	 */
	public void write(String log) {
		write(log, true);
	}

	/**
	 * Write log to local storage list
	 * @param log, the content of the log
	 * @param print, print to screen
	 */
	public void write(String log, boolean print) {
		// Store new log
		alLogs.add(log);

		if(print) {
			// Print the log
			System.out.println(log);
		}
	}
	/**
	 * Save log to file
	 */
	public void save() {
		save(true);
	}
	/**
	 * Save logs to the specified file
	 * @param append, ture is append, false is override
	 */
	public void save(boolean append) {
		try {
			if (alLogs!=null && alLogs.size()>0) {
				// Convert the ArrayList to string array
				String[] arrLogs = new String[alLogs.size()];
				arrLogs = alLogs.toArray(arrLogs);

				// Open the log
				FileWriter fileWriterLog = new FileWriter(filePath, append);

				// User BufferedWriter to add new line
				BufferedWriter bufferedWriterLog = new BufferedWriter(fileWriterLog);

				// Go through all the content and write them to the file
				for(int ix=0; ix < arrLogs.length; ix++) {
					// Write the current log
					bufferedWriterLog.write(arrLogs[ix]);
					// One log one line
					bufferedWriterLog.newLine();
				}

				// Always close files.
				bufferedWriterLog.close();
				// Clear after saving
				alLogs.clear();
			}
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + filePath + "'");
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}
}

class UdpHelper {
	/**
	 * Send data to specified address and port via UDP, and receive the feedback
	 * @param udpdata, data
	 * @param server, server name
	 * @param port, port number
	 */
	public static String sendAndReceiveUDP(String udpdata, String server, int port) throws IOException {
		// Define package length
		int len = 1024;
		// Define byte array to store original data
		byte[] receiveData = new byte[len];
		// Data from UDP message with actual length
		byte[] data;
		// Define UDP socket
		DatagramSocket clientSocket = new DatagramSocket();
		// Get ip address for destination
		InetAddress ipAddress = InetAddress.getByName(server);
		// Define default package length
		len = udpdata.getBytes().length;
		// Define byte array for sending data
		byte[] sendData = new byte[len];
		sendData = udpdata.getBytes();
		// Define upd package
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
		// Send to client
		clientSocket.send(sendPacket);
		//System.out.println("Registration request has been sent to NameServer.");
		// Define receive package
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		// Wait for receiving data from server
		clientSocket.receive(receivePacket);
		// Create array with actual length
		data = new byte[receivePacket.getLength()];
		// Convert original package data with actual length, the blankspace are removed in the tail
		System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
		// Update the public attribute, so the main process can get the value.
		String feedback = new String(data);
		// Close connection after sending
		clientSocket.close();

		return feedback;
	}

	/**
	 * Send data to specified address and port via UDP
	 * @param udpdata, data
	 * @param server, server name
	 * @param port, port number
	 */
	public static void sendUDP(String udpdata, String server, int port) throws IOException {
		// Define UDP socket
		DatagramSocket clientSocket = new DatagramSocket();
		// Get ip address for destination
		InetAddress ipAddress = InetAddress.getByName(server);
		// Define default package length
		int len = udpdata.getBytes().length;
		// Define byte array for sending data
		byte[] sendData = new byte[len];
		sendData = udpdata.getBytes();
		// Define upd package
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
		// Send to client
		clientSocket.send(sendPacket);
		// Close connection after sending
		clientSocket.close();
	}

	/**
	 * Send data to specified address and port via UDP
	 * @param udpdata, data
	 * @param ipAddress, address of the destination
	 * @param port, port number
	 */
	public static void sendUDP(String udpdata, InetAddress ipAddress, int port) throws IOException {
		// Define UDP socket
		DatagramSocket clientSocket = new DatagramSocket();
		// Define default package length
		int len = udpdata.getBytes().length;
		// Define byte array for sending data
		byte[] sentData = new byte[len];
		sentData = udpdata.getBytes();
		// Define upd package
		DatagramPacket sendPacket = new DatagramPacket(sentData, sentData.length, ipAddress, port);
		// Send to client
		clientSocket.send(sendPacket);
		//System.out.println("Send data:" + udpdata + " to hostserver.");
		// Close connection after sending
		clientSocket.close();
	}
}

/**
 * The communication listener monitors the communication port 48050 for the name
 * server. The messages come from host servers or agents. This listener provide
 * the services for registering new server and new agent. Moreover, it updates
 * the states according to the inforamtion from host servers or agents.
 */
class CommunicationListener extends Thread {
	// logger
	LogHelper logger;
	// Server list
	List<Server> lServer;
	// Agent list
	List<Agent> lAgent;
	// Group list
	List<Group> lGroup;
	// Name list
	List<String> lName = new ArrayList<String>();

	/**
	 * Constructor
	 * @param s, the socket which is to be monitored
	 * @param port, the port used to start a new server socket
	 */
	CommunicationListener(LogHelper lgr, List<Server> ls, List<Agent> la, List<Group> lg) {
		this.logger = lgr;
		this.lServer = ls;
		this.lAgent = la;
		this.lGroup = lg;
	}

	/**
	 * Start to work, receive udp message and provide feedback if necessary
	 */
	public void run() {
		// UDP message
		String updMessage = "";
		// Flag indicates whether the listener needs to continue work
		boolean hostRunning = true;
		// Communication port
		int port = 48050;
		// Define package length
		int len = 1024;
		// Define byte array to store original data
		byte[] receiveData = new byte[len];
		// Data from UDP message with actual length
		byte[] data;

		try{
			//System.out.println("Communication listener is starting up, listening at port " + port + ".");
			logger.write("Communication listener is starting up, listening at port " + port + ".\n");
			logger.save();
			// Define UDP socket with specific port
			DatagramSocket serverSocket = new DatagramSocket(port);
			// Define UDP package
			DatagramPacket receivePacket = new DatagramPacket(receiveData, len);

			// Build name list
			lName = buildAgentNameList();

			while(hostRunning) {
				// Clear log
				logger.clear();
				// Wait for receiving data from server
				serverSocket.receive(receivePacket);
				// Create array with actual length
				data = new byte[receivePacket.getLength()];
				// Convert original package data with actual length, the blankspace are removed in the tail
				System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), data, 0, receivePacket.getLength());
				// Update the public attribute, so the main process can get the value.
				updMessage = new String(data);

				if (updMessage.startsWith("[NewHostServer]")) { // Register host server
					// Extract the information of the new server
					String hostserver = updMessage.substring(15, updMessage.length()).trim();
					//System.out.println(hostserver);
					String[] info = hostserver.split("#");
					if (info.length == 2) { // ip + port
						// Add to the server list
						lServer.add(new Server(info[0], Integer.parseInt(info[1])));
						//System.out.println("New Host Server(" + info[0] + ":" + info[1] + ") has been registered.");
						logger.write("New Host Server(" + info[0] + ":" + info[1] + ") has been registered.");
					}
				}
				else if (updMessage.startsWith("[NewAgent]")) { // Register agent
					// Extract the information of the agent
					String agent = updMessage.substring(10, updMessage.length()).trim();
					//System.out.println(agent);
					String[] info = agent.split("#");
					if (info.length == 4) { // host ip + host port + agent ip + agent port
						// Server where the agent is being held
						Server server = new Server(info[0], Integer.parseInt(info[1]));
						// Get a unused name for the new agent
						String agentName = getNewAgentName(lName);
						//System.out.println("\nNew Agent is found, IP: " + info[2] + " Port:" + info[3]);
						//System.out.println("Assigned with name: " + agentName);
						logger.write("\nNew Agent is found, IP: " + info[2] + " Port:" + info[3]);
						logger.write("Assigned with name: " + agentName);
						// Create a new agent instance
						Agent agt = new Agent(lAgent.size() + 1, agentName, info[2], Integer.parseInt(info[3]), server, getRandomGroup(lGroup));
						// Add agent to server
						for(Server srv: lServer) {
							if (srv.getIp().equals(agt.getServer().getIp())
									&& srv.getPort() == agt.getServer().getPort()) {
								srv.addAgent(agt);
								//System.out.println("Agent "+agt.getName()+" has been added to server "+srv.getPort());
								logger.write("Agent "+agt.getName()+" has been added to server "+srv.getPort());
								break;
							}
						}
						// Add agent to group
						for(Group grp: lGroup) {
							if (grp.getId() == agt.getGroup().getId()) {
								grp.addAgent(agt);
								//System.out.println("Agent "+agt.getName()+" has been added to group "+grp.getName());
								logger.write("Agent "+agt.getName()+" has been added to group "+grp.getName());
								break;
							}
						}
						// Add to the agent list
						lAgent.add(agt);
						//System.out.println("New Agent(" + agt.toString() + ") has been registered.");
						logger.write("New Agent(" + agt.toString() + ") has been registered.");
						// send name back to agent
						UdpHelper.sendUDP(agentName, receivePacket.getAddress(), receivePacket.getPort());
					}
				}
				else if (updMessage.startsWith("[RequireHostServer]")) { // Ask host server for migration
					// Extract the information of the request
					String requirehost = updMessage.substring(19, updMessage.length()).trim();
					//System.out.println(requirehost);
					String[] info = requirehost.split("#");
					if (info.length == 3) { // agent ip + agent port
						//System.out.println("\nAgent(" + info[0] + "-" + info[1] + ":" + info[2]+") is requesting for new HostServer to migrate.");
						logger.write("\nAgent(" + info[0] + "-" + info[1] + ":" + info[2]+") is requesting for new HostServer to migrate.");
						// Get an available server
						Server server = getRandomServer(lServer);
						String udpData = server.getIp() + "#" + server.getPort();
						// Send new host server back to requestor
						UdpHelper.sendUDP(udpData, receivePacket.getAddress(), receivePacket.getPort());
					}
				}
				else if (updMessage.startsWith("[AgentMigration]")) { // Inform name server that migration finished
					// Extract the information of the migration
					String migration = updMessage.substring(16, updMessage.length()).trim();
					//System.out.println(migration);
					String[] info = migration.split("#");
					if (info.length == 4) { // agent name + server port + new ip + new port
						for(Agent agt: lAgent) {
							// Look for the agent and update it accordingly
							if (agt.getName().equals(info[0])) {
								agt.setIp(info[2]);
								agt.setPort(Integer.parseInt(info[3]));
								agt.getServer().setPort(Integer.parseInt(info[1]));
								break;
							}
						}
						logger.write("Agent(" + info[0] + ") has migrated to " + info[2]+": "+info[3]);
					}
				}
				else if (updMessage.startsWith("[AgentKilled]")) { // Inform name server that migration finished
					// Extract the information of the migration
					String agent = updMessage.substring(13, updMessage.length()).trim();
					System.out.println(agent);
					String[] info = agent.split("#");
					if (info.length == 3) { // host ip + host port + agent name
						// Remove agent from list
						for(int i=0; i<lAgent.size(); i++) {
							// Look for the agent and remove it
							if(lAgent.get(i).getName().equals(info[2])) {
								lAgent.remove(i);
								break;
							}
						}
						// Remove agent from server
						for(int i=0; i<lServer.size(); i++) {
							// Look for the agent and remove it
							if(lServer.get(i).getIp().equals(info[0]) &&
							   lServer.get(i).getPort() == Integer.parseInt(info[1])) {
								lServer.get(i).removeAgent(info[2]);
								break;
							}
						}
						logger.write("\nAgent(" + info[0] + "-" + info[1] + ": " + info[2] + ") has been removed from name server");
					}
				}

				// Save log to file
				logger.save();
			}
			// Close connetion
			serverSocket.close();
		}
		catch (IOException ioe) {
			System.out.println(ioe);
		}
		//catch (InterruptedException ire) {
		//	System.out.println(ire);
		//}
		finally {
			//System.out.println("UDP Service is stopped!");
		}
	}	

	/**
	 * Build list with 20 names
	 * @return, name list
	 */
	private List<String> buildAgentNameList() {
		List<String> nameList = new ArrayList<String>();
		nameList.add("Alice");
		nameList.add("Bob");
		nameList.add("Carold");
		nameList.add("Dave");
		nameList.add("Frank");
		nameList.add("Johnny");
		nameList.add("Ted");
		nameList.add("Amei");
		nameList.add("Clark");
		nameList.add("Adam");
		nameList.add("Wade");
		nameList.add("Calvin");
		nameList.add("Eric");
		nameList.add("Galvin");
		nameList.add("Henry");
		nameList.add("Jack");
		nameList.add("Morgan");
		nameList.add("Paul");
		nameList.add("Ross");
		nameList.add("Riley");
		return nameList;
	}

	/**
	 * Get name by random
	 * @param name, name list
	 * @return, randomly determined name
	 */
	private String getNewAgentName(List<String> names) {
		if (names==null||names.size()==0) {
			return "";
		}
		else {
			int random = (int)(Math.random() * names.size() + 1);
			String name = names.get(random - 1);
			names.remove(random - 1);
			return name;
		}
	}

	/**
	 * Get a group by random
	 * @param groups, group list
	 * @return, randomly determined group
	 */
	private Group getRandomGroup(List<Group> groups) {
		int random = (int)(Math.random() * groups.size() + 1);
		return groups.get(random - 1);
	}

	/**
	 * Get a server by random
	 * @param servers, server list
	 * @return, randomly determined server
	 */
	private Server getRandomServer(List<Server> servers) {
		int random = (int)(Math.random() * servers.size() + 1);
		return servers.get(random - 1);
	}
}

/**
 * This name server maintains the whole status of the DIA system. Itself can
 * handle the general requests which are asking for the current states. Besides,
 * a communication listener will be setup at the very beginning to handle other
 * udp requests.
 */
public class NameServer {
	// Define log file name server
	static String	FILE_LOG_NAME_SERVER = "Log_NameServer.txt";
	// Define thread-safe list to share data between multiple threads
	static List<Server> servers = Collections.synchronizedList(new CopyOnWriteArrayList<Server>());
	static List<Agent> agents = Collections.synchronizedList(new CopyOnWriteArrayList<Agent>());
	static List<Group> groups = Collections.synchronizedList(new CopyOnWriteArrayList<Group>());

	/**
	 * Start a Server Socket to monitor client requests and display the current status
	 */
	public static void main(String args[]){
		// The maximum queue length for incoming connection
		int queue_len = 6;
		// Port number for http request
		int port = 48060;
		// A reference of the client socket
		Socket socket;
		// Local IP address
		String localIP = "";

		try{
			// Create log instance
			LogHelper logger = new LogHelper(FILE_LOG_NAME_SERVER);
			logger.clear();
			// Setup the server socket
			ServerSocket servsocket = new ServerSocket(port, queue_len);
			//System.out.println("Rong Zhuang's Name Server is starting up, listening at port " + port + ".");
			logger.write("Rong Zhuang's Name Server is starting up, listening at port " + port + ".");

			// Get ip address of local host for name server
			localIP = InetAddress.getLocalHost().getHostAddress();
			// Build initial group list
			groups = buildGroups();
			// Creating BlockingQueue of size 10
			CommunicationListener listener = new CommunicationListener(logger, servers, agents, groups);
			// Start listener to handle communication requests
			listener.start();
			// Sleep for a while to make sure it begins work before other thread
			Thread.sleep(3000);

			while(true){
				// Make the server socket wait for the next client request
				socket = servsocket.accept();
				// Output stream to the client
				PrintStream printer = new PrintStream(socket.getOutputStream());
				// Send http page to client, which contains the current states
				logger.clear();
				logger.write("\nClient is requesting for current states...");
				sendResponseToClient(localIP, port, agents, servers, printer);
				logger.write("The latest states have been sent back to client.");
				logger.save();
			}
		}
		catch(IOException ex){
			//Handle the exception
			System.out.println(ex);
		}
		catch(InterruptedException ex){
			//Handle the exception
			System.out.println(ex);
		}
		finally {
			System.out.println("Name Server has been shutdown!");
		}
	}

	/**
	 * Create three groups with name and color
	 * @return, group list
	 */
	static List<Group> buildGroups() {
		List<Group> grps = new ArrayList<Group>();
		grps.add(new Group(1, "A", "#842DCE"));
		grps.add(new Group(2, "B", "#00FF00"));
		grps.add(new Group(3, "C", "#FF0000"));
		return grps;
	}

	/**
	 * Build content in html format, and send to client
	 * @param host, host server, eg. localhost
	 * @param port, port number for the current connection
	 * @param agents, agent list
	 * @param servers, server list
	 * @param printer, output printer
	 */
	static void sendResponseToClient(String host, int port, List<Agent> agents, List<Server> servers, PrintStream printer) {
		String htmlPage = buildHtmlPage(host, port, agents, servers);
		String htmlHeader = buildHttpHeader(htmlPage.length());
		printer.println(htmlHeader);
		printer.println(htmlPage);
	}

	/**
	 * Build html page
	 * @param host, host server, eg. localhost
	 * @param port, port number
	 * @param agents, agent list
	 * @param servers, server list
	 * @return, page text
	 */
	static String buildHtmlPage(String host, int port, List<Agent> agents, List<Server> servers) {
		StringBuilder sbHtml = new StringBuilder();
		sbHtml.append("<!DOCTYPE html>\n");
		sbHtml.append("<html>\n");
		sbHtml.append("  <head>\n");
		sbHtml.append("    <title>Name Server</title>\n");
		sbHtml.append("    <link rel=\"icon\" href=\"http://www.google.com/favicon.ico\" type=\"image/x-icon\" />");
		sbHtml.append("  </head>\n");
		sbHtml.append("  <body>\n");
		sbHtml.append("    <center> <h1> Agent Name Server </h1> </center>\n");
		sbHtml.append("    <h2> Agents </h2>\n");
		sbHtml.append("    <table style=\"background-color:white\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\" width=\"100%\"> \n");
		sbHtml.append("      <tr>\n");
		sbHtml.append("          <th width=\"15%\"> Agent Name and Hot Link </th>\n");
		sbHtml.append("          <th width=\"15%\"> Current Location </th>\n");
		sbHtml.append("          <th width=\"15%\"> Host Server </th>\n");
		sbHtml.append("          <th width=\"10%\"> Group </th>\n");
		sbHtml.append("          <th width=\"10%\"> Group ID </th>\n");
		sbHtml.append("          <th width=\"10%\"> Kill the Agent </th>\n");
		sbHtml.append("      </tr>\n");
		for (Agent agt: agents) {
			sbHtml.append("      <tr>\n");
			sbHtml.append("        <td> <a href=\"http://" + agt.getIp() + ":" + agt.getPort() + "\">" + agt.getName() + "</a> </td>\n");
			sbHtml.append("        <td> <a href=\"http://" + agt.getIp() + ":" + agt.getPort() + "\"> http://" + agt.getIp() + ":" + agt.getPort() + " </a> </td>\n");
			sbHtml.append("        <td> <a href=\"http://" + agt.getServer().getIp() + ":" + agt.getServer().getPort() + "\"> IP: " + agt.getServer().getIp() +" Port: " + agt.getServer().getPort() + " </a> </td>\n");
			sbHtml.append("        <td bgcolor=\"" + agt.getGroup().getColor() + "\"></td>\n");
			sbHtml.append("        <td>" + agt.getGroup().getName() + "</td>\n");
			sbHtml.append("        <td> <a href=\"http://" + agt.getIp() + ":" + agt.getPort() + "/action/kill\"> Kill! </a> </td>\n");
			sbHtml.append("      </tr>\n");
		}
		sbHtml.append("    </table>\n");
		sbHtml.append("    <h2> HostServers </h2>\n");
		sbHtml.append("    <table style=\"background-color:white\" cellspacing=\"0\" cellpadding=\"3\" border=\"1\" width=\"100%\"> \n");
		sbHtml.append("      <tr>\n");
		sbHtml.append("          <th width=\"40%\"> HostServer Location </th>\n");
		sbHtml.append("          <th width=\"40%\"> Agents on the server </th>\n");
		sbHtml.append("          <th width=\"20%\"> Operation </th>\n");
		sbHtml.append("      </tr>\n");
		for (Server svr: servers) {
			sbHtml.append("      <tr>\n");
			sbHtml.append("        <td> <a href=\"http://" + svr.getIp() + ":" + svr.getPort() + "\"> IP: " + svr.getIp() +" Port: " + svr.getPort() + " </a> </td>\n");
			sbHtml.append("        <td> Now hosting: " + svr.getCounter() + " agents </td>\n");
			sbHtml.append("        <td> <a href=\"http://" + svr.getIp() + ":" + svr.getPort() + "\"> Create New Agent </a> </td>\n");
			sbHtml.append("      </tr>\n");
		}
		sbHtml.append("    </table>\n");
		sbHtml.append("    <hr>\n");
		sbHtml.append("    <p>*This page is returned by localhost(" + host + ") at port " + port + ".</p>\n");
		sbHtml.append("  </body>\n");
		sbHtml.append("</html>\n");
		return sbHtml.toString();
	}

	/**
	 * Build http header
	 * @param length, length of the content
	 * @return, header text
	 */
	static String buildHttpHeader(long length) {
		StringBuilder sbHtml = new StringBuilder();
		sbHtml.append("HTTP/1.1 200 OK\r\n");
		sbHtml.append("Content-Length: " + length + "\r\n");
		sbHtml.append("Content-Type: text/html\r\n");
		return sbHtml.toString();
	}
}
