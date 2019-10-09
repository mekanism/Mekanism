package mekanism.common.integration.computer;

/**
 * Created by aidancbrady on 7/20/15.
 */
public interface IComputerIntegration {

    String[] getMethods();

    Object[] invoke(int method, Object[] args) throws NoSuchMethodException;
}