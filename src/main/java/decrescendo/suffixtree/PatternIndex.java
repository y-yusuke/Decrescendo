package decrescendo.suffixtree;

class PatternIndex {
	private int id;
	private int index;

	PatternIndex(int id, int index) {
		this.id = id;
		this.index = index;
	}

	public int getId() {
		return id;
	}

	public int getIndex() {
		return index;
	}
}
