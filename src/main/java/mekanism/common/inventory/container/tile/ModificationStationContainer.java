package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.util.EnumUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;

public class ModificationStationContainer extends MekanismTileContainer<TileEntityModificationStation> {

    public ModificationStationContainer(int id, PlayerInventory inv, TileEntityModificationStation tile) {
        super(MekanismContainerTypes.MODIFICATION_STATION, id, inv, tile);
    }

    public ModificationStationContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getTileFromBuf(buf, TileEntityModificationStation.class));
    }

    @Override
    protected void addInventorySlots(@Nonnull PlayerInventory inv) {
        super.addInventorySlots(inv);

        for (int index = 0; index < inv.armorInventory.size(); index++) {
            final EquipmentSlotType slotType = EnumUtils.EQUIPMENT_SLOT_TYPES[2 + inv.armorInventory.size() - index - 1];
            addSlot(new ArmorSlot(inv, 36 + inv.armorInventory.size() - index - 1, 8, 8 + index * 18, slotType));
        }
        // offhand
        addSlot(new OffhandSlot(inv, 40, 8, 16 + 18 * 4));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}
