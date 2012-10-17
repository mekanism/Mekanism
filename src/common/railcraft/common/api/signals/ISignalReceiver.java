package railcraft.common.api.signals;

import net.minecraft.src.World;

/**
 * This is not documented and needs some reworking to simplify usage.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ISignalReceiver
{

    public boolean doesActionOnAspect(EnumSignalAspect aspect);

    public void doActionOnAspect(EnumSignalAspect aspect, boolean trigger);

    public boolean attemptToPairWithController(ISignalController controller);

    public void clearPairedController();

    public boolean isPairedWithController();

    public ISignalController getController();

    public int getControllerX();

    public int getControllerY();

    public int getControllerZ();

    public int getX();

    public int getY();

    public int getZ();

    public int getDimension();

    public World getWorld();

    public boolean isInvalid();

    public String getDescription();
}
