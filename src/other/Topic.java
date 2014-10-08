package other;
import java.util.List;

public class Topic {
	static int nextId;
	
	private int id;
	private int pubId;
	private List<String> keywords;
	private String name;
	
	public Topic(int pubId, String name){
		this.id = Topic.nextId++;
		this.pubId = pubId;
		this.name = name;
	}
	
	public Topic(int pubId, String name, List<String> keywords){
		this(pubId, name);
		this.keywords=keywords;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	
}
