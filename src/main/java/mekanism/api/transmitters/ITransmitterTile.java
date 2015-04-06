package mekanism.api.transmitters;

public interface ITransmitterTile<A, N extends DynamicNetwork<A, N>>
{
	public IGridTransmitter<A, N> getTransmitter();
}
