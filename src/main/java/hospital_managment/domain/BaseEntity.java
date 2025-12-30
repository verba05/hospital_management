package hospital_managment.domain;

import java.io.Serializable;
import java.util.Objects;

public abstract class BaseEntity implements Serializable {

	protected int id;
	protected long version = 0L;
	private transient long loadedVersion = -1L;

	public BaseEntity() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void incrementVersion() {
		version++;
	}

	public void setLoadedVersion(long v) {
		this.loadedVersion = v;
	}

	public long getLoadedVersion() {
		return loadedVersion;
	}


	public boolean hasChangedSinceLoad() {
		if (loadedVersion == -1L) {
			return true;
		}
		return this.version != this.loadedVersion;
	}

	public void markSaved() {
		this.loadedVersion = this.version;
	}

	public boolean checkVersion(long expected) {
		return this.version == expected;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BaseEntity that = (BaseEntity) o;
		if (this.id == 0 || that.id == 0) return super.equals(o);
		return id == that.id;
	}

	@Override
	public int hashCode() {
		if (id == 0) return System.identityHashCode(this);
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return String.format("%s{id=%d,version=%d,loadedVersion=%d}", getClass().getSimpleName(), id, version, loadedVersion);
	}
}