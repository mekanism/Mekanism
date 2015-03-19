package mekanism.common.multiblock;

public interface IMultiblock<T extends SynchronizedData<T>>
{
	public T getSynchronizedData();
}
