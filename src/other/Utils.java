package other;
import com.google.gson.Gson;

public class Utils {
	public final static int DEF_PORT = 2579;
	public final static String DEF_HNAME = "localhost";
	
	public static final int CLIENT_NOTIF_PORT = 8765;
	
	public static final boolean DEBUG = true;
	
	public static final Gson gson = new Gson();
	
	//client Constants
	public static final String 
			CMD_TOPIC = "TOPIC", 
			CMD_EVENT = "EVENT",
			CMD_SUB = "SUB",
			CMD_UNSUB = "UNSUB",
			CMD_UNSUBALL = "UNSUBALL",		
			CMD_SUBKW = "SUBKW",
			CMD_UNSUBKW = "UNSUBKW",
			CMD_LISTSUBTOPICS = "LISTSUBTOPICS",
			CMD_EXIT = "EXIT";
			
	public static final String STATUS_OKAY = ":)", STATUS_NOKAY = ":(";
	
	public static void debug(String str){
		if(DEBUG)
			System.out.println(str);
	}
}
