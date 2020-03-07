package mekanism.common.tile.component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.inventory.container.ITrackableContainer;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants.NBT;

//TODO: Clean this up as a lot of the code can probably be reduced due to the slot knowing some of that information
public class TileComponentUpgrade implements ITileComponent, ITrackableContainer {

    /**
     * How long it takes this machine to install an upgrade.
     */
    private static int UPGRADE_TICKS_REQUIRED = 40;
    /**
     * How many upgrade ticks have progressed.
     */
    private int upgradeTicks;
    /**
     * TileEntity implementing this component.
     */
    private TileEntityMekanism tile;
    private Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
    private Set<Upgrade> supported = EnumSet.noneOf(Upgrade.class);
    /**
     * The inventory slot the upgrade slot of this component occupies.
     */
    private final UpgradeInventorySlot upgradeSlot;

    public TileComponentUpgrade(TileEntityMekanism tile, @Nonnull UpgradeInventorySlot slot) {
        this.tile = tile;
        upgradeSlot = slot;
        slot.getSupportedUpgrade().forEach(this::setSupported);
        tile.addComponent(this);
    }

    @Override
    public void tick() {
        if (!tile.isRemote()) {
            ItemStack stack = upgradeSlot.getStack();
            if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem) {
                Upgrade type = ((IUpgradeItem) stack.getItem()).getUpgradeType(stack);

                if (supports(type) && getUpgrades(type) < type.getMax()) {
                    if (upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                        upgradeTicks++;
                    } else if (upgradeTicks == UPGRADE_TICKS_REQUIRED) {
                        upgradeTicks = 0;
                        addUpgrade(type);
                        if (upgradeSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                            //TODO: Print warning about failing to shrink size of stack
                        }
                        if (type == Upgrade.MUFFLING) {
                            Mekanism.packetHandler.sendUpdatePacket(tile);
                        }
                        tile.markDirty();
                    }
                } else {
                    upgradeTicks = 0;
                }
            } else {
                upgradeTicks = 0;
            }
        }
    }

    public UpgradeInventorySlot getUpgradeSlot() {
        return upgradeSlot;
    }

    public double getScaledUpgradeProgress() {
        return upgradeTicks / (double) UPGRADE_TICKS_REQUIRED;
    }

    public int getUpgrades(Upgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public void addUpgrade(Upgrade upgrade) {
        upgrades.put(upgrade, Math.min(upgrade.getMax(), getUpgrades(upgrade) + 1));
        tile.recalculateUpgrades(upgrade);
    }

    public void removeUpgrade(Upgrade upgrade) {
        upgrades.put(upgrade, Math.max(0, getUpgrades(upgrade) - 1));
        if (upgrades.get(upgrade) == 0) {
            upgrades.remove(upgrade);
        }
        tile.recalculateUpgrades(upgrade);
    }

    public void setSupported(Upgrade upgrade) {
        setSupported(upgrade, true);
    }

    public void setSupported(Upgrade upgrade, boolean isSupported) {
        if (isSupported) {
            supported.add(upgrade);
        } else {
            supported.remove(upgrade);
        }
    }

    public boolean supports(Upgrade upgrade) {
        return supported.contains(upgrade);
    }

    public boolean isUpgradeInstalled(Upgrade upgrade) {
        return upgrades.containsKey(upgrade);
    }

    public Set<Upgrade> getInstalledTypes() {
        return upgrades.keySet();
    }

    public Set<Upgrade> getSupportedTypes() {
        return supported;
    }

    //TODO: Evaluate if there is a better way to do this, as the only type that the client actually cares about
    // is the muffling level (and only really cares about it if it changes)
    @Override
    public void read(PacketBuffer dataStream) {
        if (supports(Upgrade.MUFFLING)) {
            int amount = dataStream.readInt();
            if (amount == 0) {
                upgrades.remove(Upgrade.MUFFLING);
            } else {
                upgrades.put(Upgrade.MUFFLING, amount);
            }
        }
    }

    @Override
    public void write(TileNetworkList data) {
        if (supports(Upgrade.MUFFLING)) {
            data.add(upgrades.getOrDefault(Upgrade.MUFFLING, 0));
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        if (nbtTags.contains(NBTConstants.COMPONENT_UPGRADE, NBT.TAG_COMPOUND)) {
            CompoundNBT upgradeNBT = nbtTags.getCompound(NBTConstants.COMPONENT_UPGRADE);
            upgrades = Upgrade.buildMap(upgradeNBT);
            for (Upgrade upgrade : getSupportedTypes()) {
                tile.recalculateUpgrades(upgrade);
            }
            //Load the inventory
            NBTUtils.setCompoundIfPresent(upgradeNBT, NBTConstants.SLOT, upgradeSlot::deserializeNBT);
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        CompoundNBT upgradeNBT = new CompoundNBT();
        Upgrade.saveMap(upgrades, upgradeNBT);
        //Save the inventory
        CompoundNBT compoundNBT = upgradeSlot.serializeNBT();
        if (!compoundNBT.isEmpty()) {
            upgradeNBT.put(NBTConstants.SLOT, compoundNBT);
        }
        nbtTags.put(NBTConstants.COMPONENT_UPGRADE, upgradeNBT);
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(() -> upgradeTicks, value -> upgradeTicks = value));
        //We want to make sure the client and server have the upgrades in the same order
        // so we just do it based on their ordinal
        for (Upgrade upgrade : EnumUtils.UPGRADES) {
            if (supports(upgrade)) {
                container.track(SyncableInt.create(() -> upgrades.getOrDefault(upgrade, 0), value -> {
                    if (value == 0) {
                        upgrades.remove(upgrade);
                    } else if (value > 0) {
                        upgrades.put(upgrade, value);
                    }
                }));
            }
        }
    }
}