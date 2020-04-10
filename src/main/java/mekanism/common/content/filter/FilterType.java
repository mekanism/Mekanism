package mekanism.common.content.filter;

public enum FilterType {
    MINER_ITEMSTACK_FILTER,
    MINER_MATERIAL_FILTER,
    MINER_MODID_FILTER,
    MINER_TAG_FILTER,
    SORTER_ITEMSTACK_FILTER,
    SORTER_MATERIAL_FILTER,
    SORTER_MODID_FILTER,
    SORTER_TAG_FILTER,
    OREDICTIONIFICATOR;

    private static final FilterType[] FILTERS = values();

    public static FilterType byIndexStatic(int index) {
        //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
        return FILTERS[Math.floorMod(index, FILTERS.length)];
    }
}