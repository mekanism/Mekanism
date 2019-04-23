package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.base.ITileComponent;
import mekanism.common.base.IUpgradeItem;
import mekanism.api.TileNetworkList;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.nbt.NBTTagCompound;

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
    public TileEntityContainerBlock tileEntity;
    private Map<Upgrade, Integer> upgrades = new HashMap<>();
    private Set<Upgrade> supported = new HashSet<>();
    /**
     * The inventory slot the upgrade slot of this component occupies.
     */
    private int upgradeSlot;

    public TileComponentUpgrade(TileEntityContainerBlock tile, int slot) {
        tileEntity = tile;
        upgradeSlot = slot;

        setSupported(Upgrade.SPEED);
        setSupported(Upgrade.ENERGY);

        tile.components.add(this);
    }

    public void readFrom(TileComponentUpgrade upgrade) {
        upgrades = upgrade.upgrades;
        supported = upgrade.supported;
        upgradeSlot = upgrade.upgradeSlot;
        upgradeTicks = upgrade.upgradeTicks;
    }

    // This SHOULD continue to directly use te.inventory, as it is needed for Entangleporter upgrades, since it messes with IInventory.
    @Override
    public void tick() {
        if (!tileEntity.getWorld().isRemote) {
            if (!tileEntity.inventory.get(upgradeSlot).isEmpty() && tileEntity.inventory.get(upgradeSlot)
                  .getItem() instanceof IUpgradeItem) {
                Upgrade type = ((IUpgradeItem) tileEntity.inventory.get(upgradeSlot).getItem())
                      .getUpgradeType(tileEntity.inventory.get(upgradeSlot));

                if (supports(type) && getUpgrades(type) < type.getMax()) {
                    if (upgradeTicks < UPGRADE_TICKS_REQUIRED) {
                        upgradeTicks++;
                    } else if (upgradeTicks == UPGRADE_TICKS_REQUIRED) {
                        upgradeTicks = 0;
                        addUpgrade(type);

                        tileEntity.inventory.get(upgradeSlot).shrink(1);

                        Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                                    tileEntity.getNetworkedData(new TileNetworkList())),
                              new Range4D(Coord4D.get(tileEntity)));
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

    public int getUpgradeSlot() {
        return upgradeSlot;
    }

    public void setUpgradeSlot(int i) {
        upgradeSlot = i;
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

        tileEntity.recalculateUpgradables(upgrade);
    }

    public void removeUpgrade(Upgrade upgrade) {
        upgrades.put(upgrade, Math.max(0, getUpgrades(upgrade) - 1));

        if (upgrades.get(upgrade) == 0) {
            upgrades.remove(upgrade);
        }

        tileEntity.recalculateUpgradables(upgrade);
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
    public void read(ByteBuf dataStream) {
        upgrades.clear();

        int amount = dataStream.readInt();

        for (int i = 0; i < amount; i++) {
            upgrades.put(Upgrade.values()[dataStream.readInt()], dataStream.readInt());
        }

        upgradeTicks = dataStream.readInt();

        for (Upgrade upgrade : getSupportedTypes()) {
            tileEntity.recalculateUpgradables(upgrade);
        }
    }

    @Override
    public void write(TileNetworkList data) {
        data.add(upgrades.size());

        for (Map.Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
            data.add(entry.getKey().ordinal());
            data.add(entry.getValue());
        }

        data.add(upgradeTicks);
    }

    @Override
    public void read(NBTTagCompound nbtTags) {
        upgrades = Upgrade.buildMap(nbtTags);

        for (Upgrade upgrade : getSupportedTypes()) {
            tileEntity.recalculateUpgradables(upgrade);
        }
    }

    @Override
    public void write(NBTTagCompound nbtTags) {
        Upgrade.saveMap(upgrades, nbtTags);
    }

    @Override
    public void invalidate() {
    }
}
