package mekanism.api.reactor;

@Deprecated
public interface IReactorBlock
{
	public boolean isFrame();

	public void setReactor(IFusionReactor reactor);

	public IFusionReactor getReactor();
}
