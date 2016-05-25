package buildcraft.api.crops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class CropManager {
    private static List<ICropHandler> handlers = new ArrayList<ICropHandler>();
    private static ICropHandler defaultHandler;

    private CropManager() {

    }

    public static void registerHandler(ICropHandler cropHandler) {
        handlers.add(cropHandler);
    }

    public static void setDefaultHandler(ICropHandler cropHandler) {
        defaultHandler = cropHandler;
    }

    public static ICropHandler getDefaultHandler() {
        return defaultHandler;
    }

    public static boolean isSeed(ItemStack stack) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isSeed(stack)) {
                return true;
            }
        }
        return defaultHandler.isSeed(stack);
    }

    public static boolean canSustainPlant(World world, ItemStack seed, BlockPos pos) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, pos)) {
                return true;
            }
        }
        return defaultHandler.isSeed(seed) && defaultHandler.canSustainPlant(world, seed, pos);
    }

    public static boolean plantCrop(World world, EntityPlayer player, ItemStack seed, BlockPos pos) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isSeed(seed) && cropHandler.canSustainPlant(world, seed, pos) && cropHandler.plantCrop(world, player, seed, pos)) {
                return true;
            }
        }
        return defaultHandler.plantCrop(world, player, seed, pos);
    }

    public static boolean isMature(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isMature(blockAccess, state, pos)) {
                return true;
            }
        }
        return defaultHandler.isMature(blockAccess, state, pos);
    }

    public static boolean harvestCrop(World world, BlockPos pos, List<ItemStack> drops) {
        IBlockState state = world.getBlockState(pos);
        for (ICropHandler cropHandler : handlers) {
            if (cropHandler.isMature(world, state, pos)) {
                return cropHandler.harvestCrop(world, pos, drops);
            }
        }
        return defaultHandler.isMature(world, state, pos) && defaultHandler.harvestCrop(world, pos, drops);
    }

}
