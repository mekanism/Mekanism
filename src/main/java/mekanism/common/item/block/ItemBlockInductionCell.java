package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.EnumColor;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.item.ITieredItem;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockInductionCell extends ItemBlockMekanism implements IEnergizedItem, ITieredItem<InductionCellTier> {

    public ItemBlockInductionCell(BlockInductionCell block) {
        super(block);
    }

    @Nullable
    @Override
    public InductionCellTier getTier(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockInductionCell) {
            return ((BlockInductionCell) ((ItemBlockInductionCell) item).block).getTier();
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        if (block instanceof IBlockDescriptive) {
            if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
                InductionCellTier tier = getTier(itemstack);
                if (tier != null) {
                    list.add(tier.getBaseTier().getColor() + LangUtils.localize("tooltip.capacity") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(tier.getMaxEnergy()));
                    list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY + MekanismUtils.getEnergyDisplay(getEnergy(itemstack)));
                }
                list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                         EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
            } else {
                list.addAll(MekanismUtils.splitTooltip(((IBlockDescriptive) block).getDescription(), itemstack));
            }
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityInductionCell tile = (TileEntityInductionCell) world.getTileEntity(pos);
            //Set the energy
            tile.setEnergy(getEnergy(stack));
            return true;
        }
        return false;
    }

    @Override
    public double getEnergy(ItemStack itemStack) {
        return ItemDataUtils.getDouble(itemStack, "energyStored");
    }

    @Override
    public void setEnergy(ItemStack itemStack, double amount) {
        ItemDataUtils.setDouble(itemStack, "energyStored", Math.max(Math.min(amount, getMaxEnergy(itemStack)), 0));
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        InductionCellTier tier = getTier(itemStack);
        return tier == null ? 0 : tier.getMaxEnergy();
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }
}