package mekanism.common.integration.computer;

/**
 * Created by aidancbrady on 7/20/15.
 */
//TODO: Look into replacing how this is done, to have it be a map of String (method names), to lambda's that point at the corresponding java method.
public interface IComputerIntegration {

    String[] getMethods();

    Object[] invoke(int method, Object[] args) throws NoSuchMethodException;
}