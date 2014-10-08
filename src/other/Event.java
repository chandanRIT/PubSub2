package other;
public class Event {
	static int nextId;
	
	private int pubId;

	private int id;
	//private Topic topic;
	private String topicName;
	private String title;
	private String content;
	
	public Event(int pubId, String topicName, String content){
		this.id = Event.nextId++;
		this.topicName = topicName;
		this.pubId = pubId;
		this.content = content;
	}
	
	public Event(int pubId, String topicName, String title, String content){
		this(pubId, topicName, content);		
		this.title = title;
	}
	
	public String getItsTopic(){
		return topicName;
	}
	
	public int getPubId(){
		return pubId;
	}
	
	public String getContent(){
		return content;
	} 
}