package mekanism.common.content.filter;

public interface ITagFilter<FILTER extends ITagFilter<FILTER>> extends IFilter<FILTER> {

    void setTagName(String name);

    String getTagName();

    @Override
    default boolean hasFilter() {
        return getTagName() != null && !getTagName().isEmpty();
    }
}