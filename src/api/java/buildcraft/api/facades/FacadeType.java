package buildcraft.api.facades;

public enum FacadeType {
	Basic, Phased;

	public static FacadeType fromOrdinal(int ordinal) {
		return ordinal == 1 ? Phased : Basic;
	}
}