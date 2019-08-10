package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.entity.EntityRobit;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemRobit extends ItemEnergized implements IItemSustainedInventory {

    public ItemRobit() {
        super("robit", 100_000);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(itemstack, world, tooltip, flag);
        tooltip.add(EnumColor.INDIGO + LangUtils.localize("tooltip.name") + ": " + EnumColor.GREY + getName(itemstack));
        tooltip.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + (getInventory(itemstack) != null && !getInventory(itemstack).isEmpty()));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityChargepad) {
            TileEntityChargepad chargepad = (TileEntityChargepad) tileEntity;
            if (!chargepad.getActive()) {
                Hand hand = context.getHand();
                ItemStack itemstack = player.getHeldItem(hand);
                if (!world.isRemote) {
                    EntityRobit robit = new EntityRobit(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                    robit.setHome(Coord4D.get(chargepad));
                    robit.setEnergy(getEnergy(itemstack));
                    robit.setOwnerUUID(player.getUniqueID());
                    robit.setInventory(getInventory(itemstack));
                    robit.setCustomNameTag(getName(itemstack));
                    world.addEntity(robit);
                }
                player.setHeldItem(hand, ItemStack.EMPTY);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    public void setName(ItemStack itemstack, String name) {
        ItemDataUtils.setString(itemstack, "name", name);
    }

    public String getName(ItemStack itemstack) {
        String name = ItemDataUtils.getString(itemstack, "name");
        return name.isEmpty() ? "Robit" : name;
    }
}