package other;

public class Event {
	static int nextId;
	
	private int pubId;

	private int id;
	//private Topic topic;
	private String topicName;
	private String title;
	private String content;
	private String[] kwArr;
	
	public Event(int pubId, String topicName, String title, String content, String[] kwArr){
		this.id = Event.nextId++;
		this.pubId = pubId;
		this.topicName = topicName;
		this.title = title;
		this.content = content;
		this.kwArr = kwArr;
	}
	
	public Event(int pubId, String topicName, String title, String content){
		this(pubId, topicName, title, content, null);		
	}
	
	public Event(int pubId, String topicName, String content){
		this(pubId, topicName, null, content, null);		
	}
	
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