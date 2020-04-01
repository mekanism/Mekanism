package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.slot.SlotModuleTweaker;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;

public class ModuleTweakerContainer extends MekanismContainer {

    public ModuleTweakerContainer(int id, PlayerInventory inv) {
        super(MekanismContainerTypes.MODULE_TWEAKER, id, inv);
        addSlotsAndOpen();
    }

    public ModuleTweakerContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv);
    }

    @Override
    protected void addInventorySlots(@Nonnull PlayerInventory inv) {
        for(int index = 0; index < inv.armorInventory.size(); index++) {
            EquipmentSlotType slotType = EquipmentSlotType.values()[2 + inv.armorInventory.size() - index - 1];
            addSlot(new SlotModuleTweaker(inv, 36 + slotType.ordinal() - 2, 8, 8 + index * 18, slotType));
        }
        addSlot(new SlotModuleTweaker(inv, inv.currentItem, 8, 12 + 18 * 4, EquipmentSlotType.MAINHAND));
        addSlot(new SlotModuleTweaker(inv, 40, 8, 14 + 18 * 5, EquipmentSlotType.OFFHAND));
    }
}
