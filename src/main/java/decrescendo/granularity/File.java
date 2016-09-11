package decrescendo.granularity;

public class File extends Granularity {
	private String source;

	public File() {
		super();
		setOrder(0);
		setName("");
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
