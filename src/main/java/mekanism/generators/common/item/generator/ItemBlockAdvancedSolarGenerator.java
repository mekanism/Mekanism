package mekanism.generators.common.item.generator;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.IItemSustainedInventory;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.block.BlockAdvancedSolarGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockAdvancedSolarGenerator extends ItemBlockAdvancedTooltip implements IItemEnergized, IItemSustainedInventory, ISecurityItem {

    public ItemBlockAdvancedSolarGenerator(BlockAdvancedSolarGenerator block) {
        super(block);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
            list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY
                 + MekanismUtils.getEnergyDisplay(getEnergy(itemstack), getMaxEnergy(itemstack)));
        list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                 LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        Block block = world.getBlockState(pos).getBlock();
        if (!(block.isReplaceable(world, pos) && world.isAirBlock(pos.up()))) {
            return false;
        }
        for (int xPos = -1; xPos <= 1; xPos++) {
            for (int zPos = -1; zPos <= 1; zPos++) {
                BlockPos toCheck = pos.add(xPos, 2, zPos);
                if (world.isOutsideBuildHeight(toCheck) || !world.getBlockState(toCheck).getBlock().isReplaceable(world, toCheck)) {
                    //If there is not enough room, fail
                    return false;
                }
            }
        }
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);
    }

    @Override
    public double getMaxEnergy(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item instanceof ItemBlockAdvancedSolarGenerator) {
            return MekanismUtils.getMaxEnergy(itemStack, ((BlockAdvancedSolarGenerator) (((ItemBlockAdvancedSolarGenerator) item).block)).getStorage());
        }
        return 0;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return true;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityWrapper(stack, new ForgeEnergyItemWrapper());
    }
}