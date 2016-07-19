package ic2.api.reactor;

/**
 * Interface implemented nuclear reactor chambers
 */
public interface IReactorChamber {
	/**
	 * Return the {@link IReactor} this chamber is for
	 *
	 * @return The reactor
	 */
	public IReactor getReactorInstance();

	/**
	 * Returns <code>true</code> if the chamber is a reactor wall opposed to an actual chamber of items
	 * <br/>
	 * eg: {@link TileEntityReactorChamberElectric} returns <code>false</code>, but {@link TileEntityReactorVessel} returns <code>true</code>
	 *
	 * @return If the chamber is a wall or not
	 */
	public boolean isWall();
}