package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

//TODO: Clean this up as a lot of the code can probably be reduced due to the slot knowing some of that information
public class TileComponentUpgrade implements ITileComponent, ISpecificContainerTracker {

    /**
     * How long it takes this machine to install an upgrade.
     */
    private static final int UPGRADE_TICKS_REQUIRED = 20;
    /**
     * How many upgrade ticks have progressed.
     */
    private int upgradeTicks;
    /**
     * TileEntity implementing this component.
     */
    private final TileEntityMekanism tile;
    @SyntheticComputerMethod(getter = "getInstalledUpgrades")
    private Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
    private final Set<Upgrade> supported = EnumSet.noneOf(Upgrade.class);
    /**
     * The inventory slot the upgrade slot of this component occupies.
     */
    private final UpgradeInventorySlot upgradeSlot;
    private final UpgradeInventorySlot upgradeOutputSlot;

    public TileComponentUpgrade(TileEntityMekanism tile) {
        this.tile = tile;
        tile.getSupportedUpgrade().forEach(this::setSupported);
        upgradeSlot = UpgradeInventorySlot.input(tile, supported);
        upgradeOutputSlot = UpgradeInventorySlot.output(tile);
        tile.addComponent(this);
    }

    public void tickServer() {
        ItemStack stack = upgradeSlot.getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem upgradeItem) {
            Upgrade type = upgradeItem.getUpgradeType(stack);
            if (supports(type) && getUpgrades(type) < type.getMax()) {
                if (upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                    upgradeTicks++;
                    return;
                } else if (upgradeTicks == UPGRADE_TICKS_REQUIRED) {
                    int added = addUpgrades(type, upgradeSlot.getCount());
                    if (added > 0) {
                        MekanismUtils.logMismatchedStackSize(upgradeSlot.shrinkStack(added, Action.EXECUTE), added);
                    }
                }
            }
        }
        upgradeTicks = 0;
    }

    public UpgradeInventorySlot getUpgradeSlot() {
        return upgradeSlot;
    }

    public UpgradeInventorySlot getUpgradeOutputSlot() {
        return upgradeOutputSlot;
    }

    public double getScaledUpgradeProgress() {
        return upgradeTicks / (double) UPGRADE_TICKS_REQUIRED;
    }

    public int getUpgrades(Upgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    /**
     * Assumes that it has been checked if the given upgrade is supported or not
     *
     * @param upgrade      Upgrade type.
     * @param maxAvailable Max number of upgrades to install.
     *
     * @return Actual number of upgrades installed.
     *
     * @apiNote Call from the server
     */
    public int addUpgrades(Upgrade upgrade, int maxAvailable) {
        int installed = getUpgrades(upgrade);
        if (installed < upgrade.getMax()) {
            int toAdd = Math.min(upgrade.getMax() - installed, maxAvailable);
            if (toAdd > 0) {
                this.upgrades.put(upgrade, installed + toAdd);
                tile.recalculateUpgrades(upgrade);
                if (upgrade == Upgrade.MUFFLING) {
                    //Send an update packet to the client to update the number of muffling upgrades installed
                    tile.sendUpdatePacket();
                }
                tile.markForSave();
                return toAdd;
            }
        }
        return 0;
    }

    public void removeUpgrade(Upgrade upgrade, boolean removeAll) {
        int installed = getUpgrades(upgrade);
        if (installed > 0) {
            int toRemove = removeAll ? installed : 1;
            ItemStack simulatedRemainder = upgradeOutputSlot.insertItem(UpgradeUtils.getStack(upgrade, toRemove), Action.SIMULATE, AutomationType.INTERNAL);
            if (simulatedRemainder.getCount() < toRemove) {
                //We can fit at least one in the output slot
                //Actually remove them and put them in the output slot
                toRemove -= simulatedRemainder.getCount();
                if (installed == toRemove) {
                    upgrades.remove(upgrade);
                } else {
                    upgrades.put(upgrade, installed - toRemove);
                }
                tile.recalculateUpgrades(upgrade);
                upgradeOutputSlot.insertItem(UpgradeUtils.getStack(upgrade, toRemove), Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
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

    @ComputerMethod(nameOverride = "getSupportedUpgrades")
    public Set<Upgrade> getSupportedTypes() {
        return supported;
    }

    private List<IInventorySlot> getSlots() {
        return List.of(upgradeSlot, upgradeOutputSlot);
    }

    @Override
    public void read(CompoundTag nbtTags) {
        NBTUtils.setCompoundIfPresent(nbtTags, NBTConstants.COMPONENT_UPGRADE, upgradeNBT -> {
            upgrades = Upgrade.buildMap(upgradeNBT);
            for (Upgrade upgrade : getSupportedTypes()) {
                tile.recalculateUpgrades(upgrade);
            }
            //Load the inventory
            NBTUtils.setListIfPresent(upgradeNBT, NBTConstants.ITEMS, Tag.TAG_COMPOUND, list -> DataHandlerUtils.readContainers(getSlots(), list));
        });
    }

    @Override
    public void write(CompoundTag nbtTags) {
        CompoundTag upgradeNBT = new CompoundTag();
        Upgrade.saveMap(upgrades, upgradeNBT);
        //Save the inventory
        upgradeNBT.put(NBTConstants.ITEMS, DataHandlerUtils.writeContainers(getSlots()));
        nbtTags.put(NBTConstants.COMPONENT_UPGRADE, upgradeNBT);
    }

    @Override
    public void addToUpdateTag(CompoundTag updateTag) {
        //Note: We only bother to sync how many muffling upgrades we have installed as that is the only thing the client cares about
        if (supports(Upgrade.MUFFLING)) {
            updateTag.putInt(NBTConstants.MUFFLING_COUNT, upgrades.getOrDefault(Upgrade.MUFFLING, 0));
        }
    }

    @Override
    public void readFromUpdateTag(CompoundTag updateTag) {
        if (supports(Upgrade.MUFFLING)) {
            NBTUtils.setIntIfPresent(updateTag, NBTConstants.MUFFLING_COUNT, amount -> {
                if (amount == 0) {
                    upgrades.remove(Upgrade.MUFFLING);
                } else {
                    upgrades.put(Upgrade.MUFFLING, amount);
                }
            });
        }
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        List<ISyncableData> list = new ArrayList<>();
        list.add(SyncableInt.create(() -> upgradeTicks, value -> upgradeTicks = value));
        //We want to make sure the client and server have the upgrades in the same order,
        // so we just do it based on their ordinal
        for (Upgrade upgrade : EnumUtils.UPGRADES) {
            if (supports(upgrade)) {
                list.add(SyncableInt.create(() -> upgrades.getOrDefault(upgrade, 0), value -> {
                    if (value == 0) {
                        upgrades.remove(upgrade);
                    } else if (value > 0) {
                        upgrades.put(upgrade, value);
                    }
                }));
            }
        }
        return list;
    }
}