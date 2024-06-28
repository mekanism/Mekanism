package mekanism.common.inventory.container.slot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

public class ArmorSlot extends InsertableSlot {

    protected static final ResourceLocation[] ARMOR_SLOT_TEXTURES = {InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
                                                                     InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET};

    private final EquipmentSlot slotType;
    private final Player owner;

    public ArmorSlot(Inventory inventory, int index, int x, int y, EquipmentSlot slotType) {
        super(inventory, index, x, y);
        this.slotType = slotType;
        this.owner = inventory.player;
        setBackground(InventoryMenu.BLOCK_ATLAS, ARMOR_SLOT_TEXTURES[this.slotType.getIndex()]);
    }

    @Override
    public void setByPlayer(@NotNull ItemStack newStack, @NotNull ItemStack oldStack) {
        this.owner.onEquipItem(slotType, oldStack, newStack);
        super.setByPlayer(newStack, oldStack);
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
    public boolean mayPickup(@NotNull Player player) {
        ItemStack stack = getItem();
        return (stack.isEmpty() || player.isCreative() || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) && super.mayPickup(player);
    }
}
