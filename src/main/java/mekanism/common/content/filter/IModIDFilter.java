package mekanism.common.content.filter;

public interface IModIDFilter<FILTER extends IModIDFilter<FILTER>> extends IFilter<FILTER> {

    void setModID(String id);

    String getModID();

    @Override
    default boolean hasFilter() {
        return getModID() != null && !getModID().isEmpty();
    }
}