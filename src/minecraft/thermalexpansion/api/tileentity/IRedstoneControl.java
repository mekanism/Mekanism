
package thermalexpansion.api.tileentity;

public interface IRedstoneControl {

    public boolean getRedstoneDisable();

    public boolean getRedstoneState();

    public boolean redstoneControl();

    public boolean redstoneControlOrDisable();

    public boolean setRedstoneDisable(boolean disable);

    public boolean setRedstoneState(boolean state);

    public boolean setRedstoneInfo(boolean disable, boolean state);
}
