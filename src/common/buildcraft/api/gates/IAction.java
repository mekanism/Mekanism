package buildcraft.api.gates;

public interface IAction {

	int getId();
	String getTexture();
	int getIndexInTexture();
	boolean hasParameter();
	String getDescription();

}