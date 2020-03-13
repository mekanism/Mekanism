package mekanism.common.frequency;

public enum FrequencyType {
    BASE,
    INVENTORY,
    SECURITY
    ;

    private static final FrequencyType[] TYPES = values();

    public static FrequencyType byIndexStatic(int index) {
        //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
        return TYPES[Math.floorMod(index, TYPES.length)];
    }
}