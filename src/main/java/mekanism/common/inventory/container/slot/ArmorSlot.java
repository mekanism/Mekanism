package mekanism.common.inventory.container.slot;

import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ArmorSlot extends InsertableSlot {

    protected static final ResourceLocation[] ARMOR_SLOT_TEXTURES = {InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
                                                                     InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};

    private final EquipmentSlot slotType;

    public ArmorSlot(Inventory inventory, int index, int x, int y, EquipmentSlot slotType) {
        super(inventory, index, x, y);
        this.slotType = slotType;
        setBackground(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES[this.slotType.getIndex()]);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.canEquip(slotType, ((Inventory) container).player);
    }

    @Override
    public boolean mayPickup(@Nonnull Player player) {
        ItemStack stack = getItem();
        return (stack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(stack)) && super.mayPickup(player);
    }
}
