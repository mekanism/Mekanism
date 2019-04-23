package mekanism.common.integration;

import java.util.List;
import javax.annotation.Nonnull;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.tile.TileEntityAdvancedBoundingBlock;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityGasTank;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = MekanismHooks.WAILA_MOD_ID)
public class WailaDataProvider implements IWailaDataProvider {

    @Method(modid = MekanismHooks.WAILA_MOD_ID)
    public static void register(IWailaRegistrar registrar) {
        WailaDataProvider provider = new WailaDataProvider();

        registrar.registerHeadProvider(provider, TileEntityInductionCell.class);
        registrar.registerHeadProvider(provider, TileEntityInductionProvider.class);
        registrar.registerHeadProvider(provider, TileEntityFactory.class);
        registrar.registerHeadProvider(provider, TileEntityBoundingBlock.class);
        registrar.registerHeadProvider(provider, TileEntityAdvancedBoundingBlock.class);
        registrar.registerHeadProvider(provider, TileEntityFluidTank.class);
        registrar.registerHeadProvider(provider, TileEntityGasTank.class);
        registrar.registerHeadProvider(provider, TileEntityBin.class);
        registrar.registerHeadProvider(provider, TileEntityEnergyCube.class);
    }

    @Nonnull
    @Override
    @Method(modid = MekanismHooks.WAILA_MOD_ID)
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    @Method(modid = MekanismHooks.WAILA_MOD_ID)
    public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
          IWailaConfigHandler config) {
        TileEntity tile = accessor.getTileEntity();

        if (tile instanceof TileEntityInductionCell) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityInductionCell) tile).getName());
        } else if (tile instanceof TileEntityInductionProvider) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityInductionProvider) tile).getName());
        } else if (tile instanceof TileEntityFactory) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityFactory) tile).getName());
        } else if (tile instanceof TileEntityFluidTank) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityFluidTank) tile).getName());
        } else if (tile instanceof TileEntityGasTank) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityGasTank) tile).getName());
        } else if (tile instanceof TileEntityBin) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityBin) tile).getName());
        } else if (tile instanceof TileEntityEnergyCube) {
            currenttip.set(0, EnumColor.WHITE + ((TileEntityEnergyCube) tile).getName());
        } else if (tile instanceof TileEntityBoundingBlock) {
            TileEntityBoundingBlock bound = (TileEntityBoundingBlock) tile;
            Coord4D coord = new Coord4D(bound.getPos(), tile.getWorld());

            //TODO: Switch to IItemHandler though there is no good way to get the name of an IItemHandler
            if (bound.receivedCoords && coord.getTileEntity(tile.getWorld()) instanceof IInventory) {
                currenttip.set(0, EnumColor.WHITE + ((IInventory) coord.getTileEntity(tile.getWorld())).getName());
            }
        }

        return currenttip;
    }

    @Nonnull
    @Override
    @Method(modid = MekanismHooks.WAILA_MOD_ID)
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
          IWailaConfigHandler config) {
        return currenttip;
    }

    @Nonnull
    @Override
    @Method(modid = MekanismHooks.WAILA_MOD_ID)
    public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
          IWailaConfigHandler config) {
        return currenttip;
    }

    @Nonnull
    @Override
    @Method(modid = MekanismHooks.WAILA_MOD_ID)
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
          BlockPos pos) {
        return tag;
    }
}
