package mekanism.common.tile.component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

//TODO: Clean this up as a lot of the code can probably be reduced due to the slot knowing some of that information
public class TileComponentUpgrade implements ITileComponent, ISpecificContainerTracker {

    /**
     * How long it takes this machine to install an upgrade.
     */
    private static final int UPGRADE_TICKS_REQUIRED = SharedConstants.TICKS_PER_SECOND;
    /**
     * How many upgrade ticks have progressed.
     */
    private int upgradeTicks;
    /**
     * TileEntity implementing this component.
     */
    private final TileEntityMekanism tile;
    @SyntheticComputerMethod(getter = "getInstalledUpgrades")
    private final Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
    private final Set<Upgrade> supported;
    /**
     * The inventory slot the upgrade slot of this component occupies.
     */
    private final UpgradeInventorySlot upgradeSlot;
    private final UpgradeInventorySlot upgradeOutputSlot;
    private boolean canCheckUpgrades = true;

    public TileComponentUpgrade(TileEntityMekanism tile) {
        this.tile = tile;
        supported = this.tile.getSupportedUpgrade();
        upgradeSlot = UpgradeInventorySlot.input(() -> {
            this.tile.onContentsChanged();
            canCheckUpgrades = true;
        }, supported);
        upgradeOutputSlot = UpgradeInventorySlot.output(this.tile);
        this.tile.addComponent(this);
    }

    public void tickServer() {
        if (canCheckUpgrades) {
            ItemResource itemType = upgradeSlot.getResource();
            if (!itemType.isEmpty() && itemType.getItem() instanceof IUpgradeItem upgradeItem) {
                Upgrade type = upgradeItem.getUpgradeType();
                if (supports(type)) {
                    int upgrades = getUpgrades(type);
                    if (upgrades < type.getMax()) {
                        if (upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                            upgradeTicks++;
                            return;
                        } else if (upgradeTicks == UPGRADE_TICKS_REQUIRED) {
                            int toAdd = getUpgradesToAdd(type, upgrades, upgradeSlot.amountAsInt());
                            if (toAdd > 0) {
                                try (Transaction transaction = Transaction.openRoot()) {
                                    int extracted = upgradeSlot.extract(itemType, toAdd, transaction, AutomationType.INTERNAL);
                                    if (extracted > 0) {//Note: This will always be <= toAdd
                                        //If we added any upgrades (even if it was less than the amount we expected to be able to add)
                                        // increment how many upgrades added, and commit the transaction to actually consume them from the slot
                                        setUpgrades(type, upgrades + extracted);
                                        transaction.commit();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            upgradeTicks = 0;
            //We can skip checking for upgrades until the input upgrade slot changes
            canCheckUpgrades = false;
        }
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
        int toAdd = getUpgradesToAdd(upgrade, installed, maxAvailable);
        if (toAdd > 0) {
            setUpgrades(upgrade, installed + toAdd);
            //Note: We don't need to check if we can add upgrades if we get added to by interacting with the block
            // as if we couldn't add from the slot then we already caught it, otherwise it was likely a different type
            return toAdd;
        }
        return 0;
    }

    private void setUpgrades(Upgrade upgrade, int upgrades) {
        this.upgrades.put(upgrade, upgrades);
        tile.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.MUFFLING) {
            //Send an update packet to the client to update the number of muffling upgrades installed
            tile.sendUpdatePacket();
        }
        tile.markForSave();
    }

    private int getUpgradesToAdd(Upgrade upgrade, int installed, int maxAvailable) {
        if (installed < upgrade.getMax()) {
            return Math.min(upgrade.getMax() - installed, maxAvailable);
        }
        return 0;
    }

    public void removeUpgrade(Upgrade upgrade, boolean removeAll) {
        int installed = getUpgrades(upgrade);
        if (installed > 0) {
            try (Transaction transaction = Transaction.openRoot()) {
                int removed = upgradeOutputSlot.insert(UpgradeUtils.getResource(upgrade), removeAll ? installed : 1, transaction, AutomationType.INTERNAL);
                if (removed > 0) {
                    //We can fit at least one in the output slot
                    transaction.commit();
                    //Actually remove them and put them in the output slot
                    if (installed == removed) {
                        upgrades.remove(upgrade);
                    } else {
                        upgrades.put(upgrade, installed - removed);
                    }
                    tile.recalculateUpgrades(upgrade);
                    //If we have some upgrades in the input slot, mark that we should check if they can be transferred
                    canCheckUpgrades = !upgradeSlot.isEmpty();
                }
            }
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
    public String getComponentKey() {
        return SerializationConstants.COMPONENT_UPGRADE;
    }

    @Override
    public void applyImplicitComponents(@NotNull DataComponentGetter input) {
        UpgradeAware upgradeAware = input.get(MekanismDataComponents.UPGRADES);
        if (upgradeAware != null) {
            upgrades.clear();
            upgrades.putAll(upgradeAware.upgrades());
            upgradeSlot.setContents(upgradeAware.inputSlot());
            upgradeOutputSlot.setContents(upgradeAware.outputSlot());
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        //Note: UpgradeAware will copy the stacks
        builder.set(MekanismDataComponents.UPGRADES, new UpgradeAware(new EnumMap<>(upgrades), upgradeSlot.asStack(), upgradeOutputSlot.asStack()));
    }

    @Override
    public void deserialize(@NotNull ValueInput upgradeInput) {
        upgrades.clear();
        upgrades.putAll(Upgrade.buildMap(upgradeInput));
        for (Upgrade upgrade : getSupportedTypes()) {
            tile.recalculateUpgrades(upgrade);
        }
        //Load the inventory
        ContainerType.ITEM.readFrom(upgradeInput, getSlots());
    }

    @Override
    public void serialize(@NotNull ValueOutput upgradeOutput) {
        if (!upgrades.isEmpty()) {
            Upgrade.saveMap(upgrades, upgradeOutput);
        }
        //Save the inventory
        ContainerType.ITEM.saveTo(upgradeOutput, getSlots());
    }

    @Override
    public void addToUpdateTag(@NotNull ValueOutput output) {
        //Note: We only bother to sync how many muffling upgrades we have installed as that is the only thing the client cares about
        if (supports(Upgrade.MUFFLING)) {
            output.putInt(SerializationConstants.MUFFLING_COUNT, upgrades.getOrDefault(Upgrade.MUFFLING, 0));
        }
    }

    @Override
    public void readFromUpdateTag(@NotNull ValueInput input) {
        if (supports(Upgrade.MUFFLING)) {
            input.getInt(SerializationConstants.MUFFLING_COUNT).ifPresent(amount -> {
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
                list.add(SyncableInt.create(() -> getUpgrades(upgrade), value -> {
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