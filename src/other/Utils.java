package other;
import com.google.gson.Gson;

/*
 * This class stores the constants used through out the project. 
 */
public class Utils {
	public final static int DEF_EM_PORT = 2589; //Default port number of Event Manager
	public final static String DEF_HNAME = "localhost"; // Default host Name
	public static final int DEF_CLIENT_NOTIF_PORT = 8765; //Default port on the agents to receive notifications
	
	public static final boolean DEBUG = true; // Flag to turn on/off debug messages (mostly logs) on the server
	
	public static final Gson gson = new Gson(); // Reference to a reusable GSON object that's used in parsing to / from GSON
	
	//Command constants used in PubSubAgent and WorkerThread
	public static final String 
			CMD_TOPIC = "TOPIC", 
			CMD_EVENT = "EVENT",
			CMD_SUB = "SUB",
			CMD_UNSUB = "UNSUB",
			CMD_UNSUBALL = "UNSUBALL",		
			CMD_SUBKW = "SUBKW",
			CMD_UNSUBKW = "UNSUBKW",
			CMD_LISTSUBTOPICS = "LISTSUBTOPICS",
			CMD_LISTTOPICS = "CMD_LISTTOPICS",
			CMD_LISTSUBKWS = "CMD_LISTSUBKWS",
			CMD_PULLNOTIFS = "PULLNOTIFS",
			CMD_EXIT = "EXIT";
			
	public static final String STATUS_OKAY = ":)", STATUS_NOKAY = ":(";
	
	/*
	 * This method prints messages on to the console if DEBUG flag is on.
	 */
	public static void debug(String str){
		if(DEBUG)
			System.out.println(str);
	}
}
