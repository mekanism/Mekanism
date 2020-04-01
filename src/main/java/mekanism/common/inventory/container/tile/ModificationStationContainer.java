package mekanism.common.inventory.container.tile;

import javax.annotation.Nonnull;
import com.mojang.datafixers.util.Pair;
import mekanism.common.inventory.container.slot.InsertableSlot;
import mekanism.common.inventory.container.slot.SlotModuleTweaker;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

        for(int index = 0; index < inv.armorInventory.size(); index++) {
            final EquipmentSlotType slotType = EquipmentSlotType.values()[2 + inv.armorInventory.size() - index - 1];

            addSlot(new Slot(inv, 36 + inv.armorInventory.size() - index - 1, 8, 8 + index * 18) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack.canEquip(slotType, inv.player);
                }

                @Override
                public boolean canTakeStack(PlayerEntity playerIn) {
                    ItemStack itemstack = getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
                }

                @Override
                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
                    return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, SlotModuleTweaker.ARMOR_SLOT_TEXTURES[slotType.getIndex()]);
                }
            });
        }
        addSlot(new InsertableSlot(inv, inv.currentItem, 8, 12 + 18 * 4));
        addSlot(new InsertableSlot(inv, 40, 8, 14 + 18 * 5));
    }

    @Override
    protected int getInventoryYOffset() {
        return 148;
    }
}
