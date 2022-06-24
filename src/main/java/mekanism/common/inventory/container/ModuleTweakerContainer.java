package mekanism.common.inventory.container;

import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.inventory.container.slot.ArmorSlot;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.OffhandSlot;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.EnumUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleTweakerContainer extends MekanismContainer {

    public ModuleTweakerContainer(int id, Inventory inv) {
        super(MekanismContainerTypes.MODULE_TWEAKER, id, inv);
        addSlotsAndOpen();
    }

    public ModuleTweakerContainer(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv);
    }

    @Override
    protected void addInventorySlots(@NotNull Inventory inv) {
        int armorInventorySize = inv.armor.size();
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
            addSlot(new HotBarSlot(inv, slotY, 43 + slotY * 18, 161) {
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
        addSlot(new OffhandSlot(inv, 40, 8, 16 + 18 * 4) {
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
        return !stack.isEmpty() && stack.getItem() instanceof IModuleContainerItem;
    }

    public static boolean hasTweakableItem(Player player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (isTweakableItem(player.getInventory().items.get(slot))) {
                return true;
            }
        }
        return player.getInventory().armor.stream().anyMatch(ModuleTweakerContainer::isTweakableItem) ||
               player.getInventory().offhand.stream().anyMatch(ModuleTweakerContainer::isTweakableItem);
    }

    @Nullable
    @Override
    public ICapabilityProvider getSecurityObject() {
        return null;
    }
}