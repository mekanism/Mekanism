package mekanism.common.tile.component;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import mekanism.api.AutomationType;
import mekanism.api.SerializationConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.Mekanism;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableStreamCodec;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.HolderSet.Named;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

//TODO: Clean this up as a lot of the code can probably be reduced due to the slot knowing some of that information
public class TileComponentUpgrade implements ITileComponent, ISpecificContainerTracker {

    /// How long it takes this machine to install an upgrade.
    private static final int UPGRADE_TICKS_REQUIRED = SharedConstants.TICKS_PER_SECOND;
    //TODO - 26.2: Make sure this is lenient so if there are invalid amounts or unknown upgrades then it skips them. Maybe just LenientUnboundedMapCodec ?
    private static final Codec<Object2IntMap<Holder<Upgrade>>> UPGRADE_MAP_CODEC = Codec.unboundedMap(Upgrade.CODEC, ExtraCodecs.POSITIVE_INT).xmap(
          Object2IntOpenHashMap::new,
          Function.identity()
    );

    /// TileEntity implementing this component.
    private final TileEntityMekanism tile;
    private final TagKey<Upgrade> supported;
    /// The inventory slot the upgrade slot of this component occupies.
    private final UpgradeInventorySlot upgradeSlot;
    private final UpgradeInventorySlot upgradeOutputSlot;

    @SyntheticComputerMethod(getter = "getInstalledUpgrades")
    private Object2IntMap<Holder<Upgrade>> upgrades = new Object2IntOpenHashMap<>();
    /// How many upgrade ticks have progressed.
    private int upgradeTicks;
    private boolean canCheckUpgrades = true;

    public TileComponentUpgrade(TileEntityMekanism tile) {
        this.tile = tile;
        supported = Objects.requireNonNull(this.tile.getSupportedUpgrade(), "Tile supports upgrades, but isn't returning a supported upgrade tag?");
        upgradeSlot = UpgradeInventorySlot.input(() -> {
            this.tile.onContentsChanged();
            canCheckUpgrades = true;
        }, supported);
        upgradeOutputSlot = UpgradeInventorySlot.output(this.tile);
        this.tile.addComponent(this);
    }

    public void tickServer(HolderLookup.Provider registries, @Nullable TransactionContext transaction) {
        if (canCheckUpgrades) {
            ItemResource itemType = upgradeSlot.resource();
            if (!itemType.isEmpty()) {
                Holder<Upgrade> upgradeType = itemType.get(IUpgradeHelper.INSTANCE.dataComponent());
                if (upgradeType != null && upgradeType.is(supported)) {
                    int upgrades = getUpgrades(upgradeType);
                    if (upgrades < upgradeType.value().max()) {
                        if (upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                            upgradeTicks++;
                            return;
                        } else if (upgradeTicks == UPGRADE_TICKS_REQUIRED) {
                            int toAdd = getUpgradesToAdd(upgradeType, upgrades, upgradeSlot.amountAsInt());
                            if (toAdd > 0) {
                                try (Transaction subTransaction = Transaction.open(transaction)) {
                                    int extracted = upgradeSlot.extract(itemType, toAdd, subTransaction, AutomationType.INTERNAL);
                                    if (extracted > 0) {//Note: This will always be <= toAdd
                                        //If we added any upgrades (even if it was less than the amount we expected to be able to add)
                                        // increment how many upgrades added, and commit the transaction to actually consume them from the slot
                                        setUpgrades(registries, upgradeType, upgrades + extracted);
                                        subTransaction.commit();
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

    public float getScaledUpgradeProgress() {
        return upgradeTicks / (float) UPGRADE_TICKS_REQUIRED;
    }

    public int getUpgrades(Holder<Upgrade> upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    /// Assumes that it has been checked if the given upgrade is supported or not
    ///
    /// @param upgrade      Upgrade type.
    /// @param maxAvailable Max number of upgrades to install.
    ///
    /// @return Actual number of upgrades installed.
    ///
    /// @apiNote Call from the server
    public int addUpgrades(HolderLookup.Provider registries, Holder<Upgrade> upgrade, int maxAvailable) {
        int installed = getUpgrades(upgrade);
        int toAdd = getUpgradesToAdd(upgrade, installed, maxAvailable);
        if (toAdd > 0) {
            setUpgrades(registries, upgrade, installed + toAdd);
            //Note: We don't need to check if we can add upgrades if we get added to by interacting with the block
            // as if we couldn't add from the slot then we already caught it, otherwise it was likely a different type
            return toAdd;
        }
        return 0;
    }

    private void setUpgrades(HolderLookup.Provider registries, Holder<Upgrade> upgrade, int upgrades) {
        this.upgrades.put(upgrade, upgrades);
        tile.recalculateUpgrades(registries, upgrade, upgrades);
        if (upgrade.is(UpgradeIds.MUFFLING)) {
            //Send an update packet to the client to update the number of muffling upgrades installed
            tile.sendUpdatePacket();
        }
        tile.markForSave();
    }

    private int getUpgradesToAdd(Holder<Upgrade> upgrade, int installed, int maxAvailable) {
        int max = upgrade.value().max();
        if (installed < max) {
            return Math.min(max - installed, maxAvailable);
        }
        return 0;
    }

    public void removeUpgrade(HolderLookup.Provider registries, Holder<Upgrade> upgrade, boolean removeAll) {
        int installed = getUpgrades(upgrade);
        if (installed > 0) {
            try (Transaction transaction = Transaction.openRoot()) {
                int removed = upgradeOutputSlot.insert(IUpgradeHelper.INSTANCE.asResource(upgrade), removeAll ? installed : 1, transaction, AutomationType.INTERNAL);
                if (removed > 0) {
                    //We can fit at least one in the output slot
                    //Actually remove them and put them in the output slot
                    transaction.commit();
                    if (installed == removed) {
                        upgrades.removeInt(upgrade);
                        tile.recalculateUpgrades(registries, upgrade, 0);
                    } else {
                        int totalInstalled = installed - removed;
                        upgrades.put(upgrade, totalInstalled);
                        tile.recalculateUpgrades(registries, upgrade, totalInstalled);
                    }
                    //If we have some upgrades in the input slot, mark that we should check if they can be transferred
                    canCheckUpgrades = !upgradeSlot.isEmpty();
                }
            }
        }
    }

    public boolean supports(Holder<Upgrade> upgrade) {
        return upgrade.is(getSupportedTypes());
    }

    public Set<Holder<Upgrade>> getInstalledTypes() {
        return upgrades.keySet();
    }

    public TagKey<Upgrade> getSupportedTypes() {
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
    public void applyImplicitComponents(DataComponentGetter input) {
        UpgradeAware upgradeAware = input.get(MekanismDataComponents.UPGRADES);
        if (upgradeAware != null) {
            upgrades = new Object2IntOpenHashMap<>(upgradeAware.upgrades());
            upgradeSlot.setContents(upgradeAware.inputSlot(), null);
            upgradeOutputSlot.setContents(upgradeAware.outputSlot(), null);
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        //Note: UpgradeAware will copy the stacks
        builder.set(MekanismDataComponents.UPGRADES, new UpgradeAware(new Object2IntOpenHashMap<>(upgrades), upgradeSlot.asStack(), upgradeOutputSlot.asStack()));
    }

    @Override
    public void deserialize(ValueInput upgradeInput) {
        Optional<Object2IntMap<Holder<Upgrade>>> storedUpgrades = upgradeInput.read(SerializationConstants.UPGRADES, UPGRADE_MAP_CODEC);
        if (storedUpgrades.isPresent()) {
            upgrades = storedUpgrades.get();
        } else if (!upgrades.isEmpty()) {
            upgrades.clear();
        }
        HolderLookup.Provider lookup = upgradeInput.lookup();
        Optional<Named<Upgrade>> tag = lookup.get(supported);
        if (tag.isPresent()) {
            for (Holder<Upgrade> upgrade : tag.get()) {
                tile.recalculateUpgrades(lookup, upgrade, getUpgrades(upgrade));
            }
        } else {//Best effort, and just recalculate the upgrades that are currently stored
            Mekanism.logger.warn("Unable to find supported upgrades: #{}. Recalculating upgrades for installed upgrades.", supported.location());
            for (ObjectIterator<Entry<Holder<Upgrade>>> iterator = Object2IntMaps.fastIterator(upgrades); iterator.hasNext(); ) {
                Object2IntMap.Entry<Holder<Upgrade>> entry = iterator.next();
                tile.recalculateUpgrades(lookup, entry.getKey(), entry.getIntValue());
            }
        }
        //Load the inventory
        ContainerType.ITEM.readFrom(upgradeInput, getSlots());
    }

    @Override
    public void serialize(ValueOutput upgradeOutput) {
        if (!upgrades.isEmpty()) {
            upgradeOutput.store(SerializationConstants.UPGRADES, UPGRADE_MAP_CODEC, upgrades);
        }
        //Save the inventory
        ContainerType.ITEM.saveTo(upgradeOutput, getSlots());
    }

    @Override
    public void addToUpdateTag(ValueOutput output) {
        //Note: We only bother to sync how many muffling upgrades we have installed as that is the only thing the client cares about
        for (ObjectIterator<Entry<Holder<Upgrade>>> iterator = Object2IntMaps.fastIterator(upgrades); iterator.hasNext(); ) {
            Object2IntMap.Entry<Holder<Upgrade>> entry = iterator.next();
            if (entry.getKey().is(UpgradeIds.MUFFLING)) {
                output.putInt(SerializationConstants.MUFFLING_COUNT, entry.getIntValue());
            }
        }
    }

    @Override
    public void readFromUpdateTag(ValueInput input) {
        Holder.Reference<Upgrade> mufflingUpgrade = input.lookup().get(UpgradeIds.MUFFLING).orElse(null);
        if (mufflingUpgrade != null && supports(mufflingUpgrade)) {
            int mufflingCount = input.getIntOr(SerializationConstants.MUFFLING_COUNT, 0);
            if (mufflingCount == 0) {
                upgrades.removeInt(mufflingUpgrade);
            } else {
                upgrades.put(mufflingUpgrade, mufflingCount);
            }
        }
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData(Level level) {
        List<ISyncableData> list = new ArrayList<>();
        list.add(SyncableInt.create(() -> upgradeTicks, value -> upgradeTicks = value));
        list.add(SyncableStreamCodec.upgradeMap(() -> upgrades, value -> upgrades = value));
        return list;
    }

    @ComputerMethod
    List<Holder<Upgrade>> getSupportedUpgrades() throws ComputerException {
        return tile.validateLevel().registryAccess().get(supported).stream().flatMap(HolderSet::stream).toList();
    }
}