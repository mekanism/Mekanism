package mekanism.api;

//TODO: Use this in places and add more of an API to here as well as JavaDocs
public interface IDisableableEnum<TYPE extends Enum<TYPE>> {

    boolean isEnabled();
}