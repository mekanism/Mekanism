package mekanism.common.inventory.container;

import mekanism.api.gear.IModuleHelper;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModuleTweakerContainer extends MekanismContainer {

    public ModuleTweakerContainer(int id, Inventory inv) {
        super(MekanismContainerTypes.MODULE_TWEAKER, id, inv);
        addSlotsAndOpen();
    }

    @Override
    protected void addInventorySlots(@NotNull Inventory inv) {
        int armorInventorySize = 4;
        for (int index = 0; index < armorInventorySize; index++) {
            EquipmentSlot slotType = EnumUtils.EQUIPMENT_SLOT_TYPES[2 + armorInventorySize - index - 1];
            addSlot(new ArmorSlot(inv, 36 + slotType.ordinal() - 2, 8, 8 + index * 18, slotType) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }
        for (int slotY = 0; slotY < Inventory.getSelectionSize(); slotY++) {
            addSlot(new HotBarSlot(inv, slotY, 58 + slotY * 18, 161) {
                @Override
                public boolean mayPickup(@NotNull Player player) {
                    return false;
                }

                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }
        addSlot(new OffhandSlot(inv, Inventory.SLOT_OFFHAND, 8, 16 + 18 * 4, inv.player) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
    }

    public static boolean isTweakableItem(ItemStack stack) {
        return IModuleHelper.INSTANCE.getModuleContainer(stack) != null;
    }

    public static boolean hasTweakableItem(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (isTweakableItem(inventory.getItem(slot))) {
                return true;
            }
        }
        for (EquipmentSlot equipmentslot : EquipmentSlotGroup.ARMOR) {
            if (equipmentslot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack armorStack = player.getItemBySlot(equipmentslot);
                if (!armorStack.isEmpty() && isTweakableItem(armorStack)) {
                    return true;
                }
            }
        }
        return isTweakableItem(player.getOffhandItem());
    }

    @Override
    public boolean canPlayerAccess(@NotNull Player player) {
        return true;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;//opened from hotkey
    }
}
