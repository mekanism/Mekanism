package mekanism.common.content.filter;

public interface IOreDictFilter<FILTER extends IOreDictFilter<FILTER>> extends IFilter<FILTER> {

    void setOreDictName(String name);

    String getOreDictName();
}