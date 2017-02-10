package decrescendo.suffixtree;

class PatternIndex {
	private int id;
	private int index;
	private int size;

	public PatternIndex(int id, int index, int size) {
		this.id = id;
		this.index = index;
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public int getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}
}
