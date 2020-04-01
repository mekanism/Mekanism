package mekanism.common.inventory.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
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
            addSlot(new ArmorSlot(inv, 36 + slotType.ordinal() - 2, 8, 8 + index * 18, slotType) {
                @Override
                public boolean canTakeStack(PlayerEntity player) {
                    return false;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }
            });
        }
        addSlot(new InsertableSlot(inv, inv.currentItem, 8, 12 + 18 * 4) {
            @Override
            public boolean canTakeStack(PlayerEntity player) {
                return false;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });
        addSlot(new InsertableSlot(inv, 40, 8, 14 + 18 * 5) {
            @Override
            public boolean canTakeStack(PlayerEntity player) {
                return false;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });
    }
}
