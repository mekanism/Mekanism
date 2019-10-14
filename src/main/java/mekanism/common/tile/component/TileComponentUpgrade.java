package mekanism.common.tile.component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

//TODO: Clean this up as a lot of the code can probably be reduced due to the slot knowing some of that information
public class TileComponentUpgrade implements ITileComponent {

    /**
     * How long it takes this machine to install an upgrade.
     */
    public static int UPGRADE_TICKS_REQUIRED = 40;
    /**
     * How many upgrade ticks have progressed.
     */
    public int upgradeTicks;
    /**
     * TileEntity implementing this component.
     */
    private TileEntityMekanism tileEntity;
    private Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
    private Set<Upgrade> supported = EnumSet.noneOf(Upgrade.class);
    /**
     * The inventory slot the upgrade slot of this component occupies.
     */
    private UpgradeInventorySlot upgradeSlot;

    public TileComponentUpgrade(TileEntityMekanism tile, @Nonnull UpgradeInventorySlot slot) {
        tileEntity = tile;
        upgradeSlot = slot;
        slot.getSupportedUpgrade().forEach(this::setSupported);
        tile.addComponent(this);
        //TODO: Store and load slot contents when saving/writing to disk
    }

    @Override
    public void tick() {
        if (!tileEntity.isRemote()) {
            ItemStack stack = upgradeSlot.getStack();
            if (!stack.isEmpty() && stack.getItem() instanceof IUpgradeItem) {
                Upgrade type = ((IUpgradeItem) stack.getItem()).getUpgradeType(stack);

                if (supports(type) && getUpgrades(type) < type.getMax()) {
                    if (upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                        upgradeTicks++;
                    } else if (upgradeTicks == UPGRADE_TICKS_REQUIRED) {
                        upgradeTicks = 0;
                        addUpgrade(type);
                        //TODO: We are unable to shrink here as
                        inventory.get(upgradeSlot).shrink(1);
                        Mekanism.packetHandler.sendUpdatePacket(tileEntity);
                        tileEntity.markDirty();
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

    public int getScaledUpgradeProgress(int i) {
        return upgradeTicks * i / UPGRADE_TICKS_REQUIRED;
    }

    public int getUpgrades(Upgrade upgrade) {
        if (upgrades.get(upgrade) == null) {
            return 0;
        }
        return upgrades.get(upgrade);
    }

    public void addUpgrade(Upgrade upgrade) {
        upgrades.put(upgrade, Math.min(upgrade.getMax(), getUpgrades(upgrade) + 1));
        tileEntity.recalculateUpgrades(upgrade);
    }

    public void removeUpgrade(Upgrade upgrade) {
        upgrades.put(upgrade, Math.max(0, getUpgrades(upgrade) - 1));
        if (upgrades.get(upgrade) == 0) {
            upgrades.remove(upgrade);
        }
        tileEntity.recalculateUpgrades(upgrade);
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

    public void clearSupportedTypes() {
        supported.clear();
    }

    @Override
    public void read(PacketBuffer dataStream) {
        upgrades.clear();
        int amount = dataStream.readInt();

        for (int i = 0; i < amount; i++) {
            upgrades.put(dataStream.readEnumValue(Upgrade.class), dataStream.readInt());
        }
        upgradeTicks = dataStream.readInt();
        for (Upgrade upgrade : getSupportedTypes()) {
            tileEntity.recalculateUpgrades(upgrade);
        }
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(upgrades.size());
        for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            data.add(entry.getKey());
            data.add(entry.getValue());
        }
        data.add(upgradeTicks);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        upgrades = Upgrade.buildMap(nbtTags);
        for (Upgrade upgrade : getSupportedTypes()) {
            tileEntity.recalculateUpgrades(upgrade);
        }
    }

    @Override
    public void write(CompoundNBT nbtTags) {
        Upgrade.saveMap(upgrades, nbtTags);
    }

    @Override
    public void invalidate() {
    }
}