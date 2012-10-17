package railcraft.common.api.signals;

import net.minecraft.src.World;

/**
 * This is not documented and needs some reworking to simplify usage.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface ISignalController
{

    public EnumSignalAspect getSignalAspect();

    public boolean attemptToPairWithReceiver(ISignalReceiver receiver);

    public void startReceiverPairing();

    public void endReceiverPairing();

    public void clearPairedReceiver();

    public boolean isPairedWithReceiver();

    public ISignalReceiver getReceiver();

    public int getReceiverX();

    public int getReceiverY();

    public int getReceiverZ();

    public int getX();

    public int getY();

    public int getZ();

    public int getDimension();

    public World getWorld();

    public String getDescription();

    public boolean isInvalid();
}
