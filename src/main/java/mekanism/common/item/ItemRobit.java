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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemRobit extends ItemEnergized implements IItemSustainedInventory {

    public ItemRobit() {
        super("robit", 100_000);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        super.addInformation(itemstack, world, list, flag);
        list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.name") + ": " + EnumColor.GREY + getName(itemstack));
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY + (getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(PlayerEntity entityplayer, World world, BlockPos pos, Hand hand, Direction side, float posX, float posY, float posZ) {
        TileEntity tileEntity = world.getTileEntity(pos);
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (tileEntity instanceof TileEntityChargepad) {
            TileEntityChargepad chargepad = (TileEntityChargepad) tileEntity;
            if (!chargepad.getActive()) {
                if (!world.isRemote) {
                    EntityRobit robit = new EntityRobit(world, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
                    robit.setHome(Coord4D.get(chargepad));
                    robit.setEnergy(getEnergy(itemstack));
                    robit.setOwnerUUID(entityplayer.getUniqueID());
                    robit.setInventory(getInventory(itemstack));
                    robit.setCustomNameTag(getName(itemstack));
                    world.spawnEntity(robit);
                }
                entityplayer.setHeldItem(hand, ItemStack.EMPTY);
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