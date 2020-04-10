package mekanism.common.content.filter;

public interface IModIDFilter<FILTER extends IModIDFilter<FILTER>> extends IFilter<FILTER> {

    void setModID(String id);

    String getModID();
}