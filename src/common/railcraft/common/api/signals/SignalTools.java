package railcraft.common.api.signals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import railcraft.common.api.core.WorldCoordinate;
import railcraft.common.api.tracks.RailTools;

/**
 * This is not documented and needs some reworking to simplify usage.
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public abstract class SignalTools
{

    private static Map<PairingKey, WorldCoordinate> signalBlockPairingMap = new HashMap<PairingKey, WorldCoordinate>();
    private static Map<PairingKey, WorldCoordinate> controllerReceiverPairingMap = new HashMap<PairingKey, WorldCoordinate>();

    public static boolean isSignalBlockSectionValid(World world, IBlockSignal first, IBlockSignal second) {
        return RailTools.areDistantRailsConnectedAlongAxis(world, first.getRailX(), first.getRailY(), first.getRailZ(), second.getRailX(), second.getRailY(), second.getRailZ());
    }

    public static boolean isControllerInRangeOfReceiver(ISignalController c, ISignalReceiver r, int range) {
        int distX = c.getX() - r.getX();
        int distY = c.getY() - r.getY();
        int distZ = c.getZ() - r.getZ();
        int distance = (int)Math.sqrt(distX * distX + distY * distY + distZ * distZ);
        return distance <= range;
    }

    public static void startSignalBlockPairing(EntityPlayer player, ItemStack device, IBlockSignal first) {
        endSignalBlockPairing(player, device);
        int id = new Random().nextInt(Short.MAX_VALUE);
        device.setItemDamage(id);
        first.startSignalBlockPairing();
        signalBlockPairingMap.put(new PairingKey(player.username, device.getItemDamage()), new WorldCoordinate(first.getDimension(), first.getX(), first.getY(), first.getZ()));
    }

    public static WorldCoordinate getSignalBlockPair(EntityPlayer player, ItemStack device) {
        return signalBlockPairingMap.get(new PairingKey(player.username, device.getItemDamage()));
    }

    public static void endSignalBlockPairing(EntityPlayer player, ItemStack device) {
        WorldCoordinate pos = signalBlockPairingMap.remove(new PairingKey(player.username, device.getItemDamage()));
        if(pos != null) {
            TileEntity t = player.worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
            if(t instanceof IBlockSignal) {
                ((IBlockSignal)t).endSignalBlockPairing();
            }
        }
    }

    public static void startControllerReceiverPairing(EntityPlayer player, ItemStack device, ISignalController controller) {
        endControllerReceiverPairing(player, device);
        int id = new Random().nextInt(Short.MAX_VALUE);
        device.setItemDamage(id);
        controller.startReceiverPairing();
        controllerReceiverPairingMap.put(new PairingKey(player.username, device.getItemDamage()), new WorldCoordinate(controller.getDimension(), controller.getX(), controller.getY(), controller.getZ()));
    }

    public static WorldCoordinate getSavedController(EntityPlayer player, ItemStack device) {
        return controllerReceiverPairingMap.get(new PairingKey(player.username, device.getItemDamage()));
    }

    public static void endControllerReceiverPairing(EntityPlayer player, ItemStack device) {
        WorldCoordinate pos = controllerReceiverPairingMap.remove(new PairingKey(player.username, device.getItemDamage()));
        if(pos != null) {
            TileEntity t = player.worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
            if(t instanceof ISignalController) {
                ((ISignalController)t).endReceiverPairing();
            }
        }
    }

    public static ISignalReceiver getReceiverFor(ISignalController con) {
        World world = con.getWorld();
        if(world == null || con.getReceiverY() < 0) {
            return null;
        }
        int i = con.getReceiverX();
        int j = con.getReceiverY();
        int k = con.getReceiverZ();
        if(!world.blockExists(i, j, k)) {
            return null;
        }
        TileEntity pair = world.getBlockTileEntity(i, j, k);
        if(pair instanceof ISignalReceiver) {
            return (ISignalReceiver)pair;
        } else {
            con.clearPairedReceiver();
        }
        return null;
    }

    public static ISignalController getControllerFor(ISignalReceiver rec) {
        if(rec.getControllerY() < 0) {
            return null;
        }
        World world = rec.getWorld();
        if(world == null) {
            return null;
        }
        int i = rec.getControllerX();
        int j = rec.getControllerY();
        int k = rec.getControllerZ();
        if(!world.blockExists(i, j, k)) {
            return null;
        }
        TileEntity pair = world.getBlockTileEntity(i, j, k);
        if(pair instanceof ISignalController) {
            return (ISignalController)pair;
        } else {
            rec.clearPairedController();
        }
        return null;
    }

    private static class PairingKey
    {

        protected String username;
        protected int id;

        public PairingKey(String username, int id) {
            this.username = username;
            this.id = id;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.username != null ? this.username.hashCode() : 0);
            hash = 59 * hash + this.id;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            final PairingKey other = (PairingKey)obj;
            if((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
                return false;
            }
            if(this.id != other.id) {
                return false;
            }
            return true;
        }
    }
}
