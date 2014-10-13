package other;

/*
 * This class represents an Event that is published in the PubSubSystem.
 * @author kchandan 
 */
public class Event {
	static int nextId; // Used to generate new ID on every new event.  
	
	private int pubId; //The id of the publisher of the event
	private int id; // Event's Id
	private String topicName; // TopicName of the Topic, the Event belongs to.   
	private String title; // Event's title
	private String content; // Event's content
	private String[] kwArr; // Optional list of keywords associated with the Event. Can be null.
	
	public Event(int pubId, String topicName, String title, String content, String[] kwArr){
		this.id = Event.nextId++;
		this.pubId = pubId;
		this.topicName = topicName;
		this.title = title;
		this.content = content;
		this.kwArr = kwArr;
	}
	
	//Constructors below 
	public Event(int pubId, String topicName, String title, String content){
		this(pubId, topicName, title, content, null);		
	}
	
	public Event(int pubId, String topicName, String content){
		this(pubId, topicName, null, content, null);		
	}
	
	//Below are the Getters for the private attributes of the event
	public int getId(){
		return id;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getTopicName(){
		return topicName;
	}
	
	public String[] getKeywords(){
		return kwArr;
	}
	
	public int getPubId(){
		return pubId;
	}
	
	public String getContent(){
		return content;
	} 
}