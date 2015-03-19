package mekanism.api.reactor;


public interface IReactorBlock
{
	public boolean isFrame();

	public void setReactor(IFusionReactor reactor);

	public IFusionReactor getReactor();

}
