package other;

/**
 * This class represents a Topic which is advertised in the PubSub System
 * @author kchandan
 */
public class Topic {
	static int nextId; // Used to generate new ID for every new Topic that's created.
	
	private int id; //Topic's ids
	private int pubId; //Publisher's Id which advertised the topic
	//private List<String> keywords;  
	private String name; // Topic's name
	
	// Constructor 
	public Topic(int pubId, String name){
		this.id = Topic.nextId++;
		this.pubId = pubId;
		this.name = name;
	}
	
	/*public Topic(int pubId, String name, List<String> keywords){
		this(pubId, name);
		//this.keywords=keywords;
	}*/
	
	//Below are the Getters for the private attributes of the Topic class  
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	/*
	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	*/
	
}
