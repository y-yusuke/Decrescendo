package decrescendo.hash;

import java.util.List;
import java.util.Objects;

public class HashList {
	public final List<Hash> hashList;
	public final int hashCode;

	public HashList(List<Hash> hashList) {
		this.hashList = hashList;
		this.hashCode = Objects.hash(hashList);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass())
			return false;

		HashList hashList = (HashList) o;
		if (Objects.equals(this.hashList, hashList.hashList))
			return true;
		else if (this.hashList.size() != hashList.hashList.size())
			return false;
		else {
			for (int i = 0; i < this.hashList.size(); i++) {
				if (!this.hashList.get(i).equals(hashList.hashList.get(i)))
					return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

}
