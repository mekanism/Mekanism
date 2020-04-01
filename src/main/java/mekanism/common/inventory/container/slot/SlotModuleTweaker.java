package mekanism.common.inventory.container.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SlotModuleTweaker extends Slot {

    public static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[] {PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET};
    private EquipmentSlotType slotType;

    public SlotModuleTweaker(IInventory inventory, int index, int x, int y, EquipmentSlotType slotType) {
        super(inventory, index, x, y);
        this.slotType = slotType;
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
        if (slotType.getSlotType() == EquipmentSlotType.Group.ARMOR) {
            return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, SlotModuleTweaker.ARMOR_SLOT_TEXTURES[slotType.getIndex()]);
        }
        return super.func_225517_c_();
    }
}
