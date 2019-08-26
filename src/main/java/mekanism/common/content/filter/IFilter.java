package mekanism.common.content.filter;

public interface IFilter<FILTER extends IFilter<FILTER>> {

    FILTER clone();
}