package decrescendo.suffixtree;

import decrescendo.hash.Hash;

class CloneIndex {
	private int id1;
	private int id2;
	private int index1;
	private int index2;
	public int size;
	private Hash commonHash;

	CloneIndex(int id1, int id2, int index1, int index2, int size, Hash commonHash) {
		this.id1 = id1;
		this.id2 = id2;
		this.index1 = index1;
		this.index2 = index2;
		this.size = size;
		this.commonHash = commonHash;
	}

	public int getId1() {
		return id1;
	}

	public void setId1(int id1) {
		this.id1 = id1;
	}

	public int getId2() {
		return id2;
	}

	public void setId2(int id2) {
		this.id2 = id2;
	}

	public int getIndex1() {
		return index1;
	}

	public void setIndex1(int index1) {
		this.index1 = index1;
	}

	public int getIndex2() {
		return index2;
	}

	public void setIndex2(int index2) {
		this.index2 = index2;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Hash getCommonHash() {
		return commonHash;
	}

	public void setCommonHash(Hash commonHash) {
		this.commonHash = commonHash;
	}
}
