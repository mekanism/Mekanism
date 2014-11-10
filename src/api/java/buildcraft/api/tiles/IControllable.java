package buildcraft.api.tiles;

public interface IControllable {
	public enum Mode {
		Unknown, On, Off, Loop
	};
	
	Mode getControlMode();
	void setControlMode(Mode mode);
	boolean acceptsControlMode(Mode mode);
}
