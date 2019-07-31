package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.machine.BlockLaser;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.forgeenergy.ForgeEnergyItemWrapper;
import mekanism.common.integration.tesla.TeslaItemWrapper;
import mekanism.common.item.IItemEnergized;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.tile.TileEntityLaser;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockLaser extends ItemBlockMekanism implements IItemEnergized, ISustainedInventory {

    public ItemBlockLaser(BlockLaser block) {
        super(block);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.INDIGO + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails") + ".");
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.and") + " " + EnumColor.AQUA +
                     GameSettings.getKeyDisplayString(MekanismKeyHandler.modeSwitchKey.getKeyCode()) + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDesc") + ".");
        } else if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey)) {
            list.add(EnumColor.BRIGHT_GREEN + LangUtils.localize("tooltip.storedEnergy") + ": " + EnumColor.GREY
                     + MekanismUtils.getEnergyDisplay(getEnergy(itemstack), getMaxEnergy(itemstack)));
            list.add(EnumColor.AQUA + LangUtils.localize("tooltip.inventory") + ": " + EnumColor.GREY +
                     LangUtils.transYesNo(getInventory(itemstack) != null && getInventory(itemstack).tagCount() != 0));
        } else {
            list.addAll(MekanismUtils.splitTooltip(((IBlockDescriptive) block).getDescription(), itemstack));
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityLaser tile = (TileEntityLaser) world.getTileEntity(pos);
            if (tile != null) {
                //Sustained Inventory
                tile.setInventory(getInventory(stack));
                //Electric
                tile.electricityStored = getEnergy(stack);
            }
            return true;
        }
        return false;
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
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
        Item item = itemStack.getItem();
        if (item instanceof ItemBlockLaser) {
            return MekanismUtils.getMaxEnergy(itemStack, ((BlockLaser) (((ItemBlockLaser) item).block)).getStorage());
        }
        return 0;
    }

    @Override
    public double getMaxTransfer(ItemStack itemStack) {
        return getMaxEnergy(itemStack) * 0.005;
    }

    @Override
    public boolean canReceive(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean canSend(ItemStack itemStack) {
        return false;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
        return new ItemCapabilityWrapper(stack, new TeslaItemWrapper(), new ForgeEnergyItemWrapper());
    }
}