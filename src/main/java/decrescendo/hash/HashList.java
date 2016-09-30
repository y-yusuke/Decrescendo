package decrescendo.hash;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class HashList {
	private List<byte[]> hashList;

	public HashList(List<byte[]> hashList) {
		this.hashList = hashList;
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
				if (!Arrays.equals(this.hashList.get(i), hashList.hashList.get(i)))
					return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(hashList);
	}

}
