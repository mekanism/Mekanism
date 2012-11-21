package universalelectricity.core.implement;

/**
 * Applied to TileEntities that can produces electricity. Of course, you will still need to call
 * ElectricityManager.instance.produce() to actually output the electricity.
 * 
 * @author Calclavia
 */
public interface IElectricityProducer extends IConnector, IDisableable, IVoltage
{

}
