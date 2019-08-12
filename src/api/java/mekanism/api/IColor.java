package mekanism.api;

public interface IColor<T extends IColor> extends Comparable<T> {

    String getRegistryPrefix();
}