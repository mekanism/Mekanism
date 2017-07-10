package buildcraft.api.facades;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.common.event.FMLInterModComms;

public final class FacadeAPI {
    public static final String IMC_MOD_TARGET = "buildcrafttransport";
    public static final String IMC_FACADE_DISABLE = "facade_disable_block";
    public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";
    public static final String NBT_CUSTOM_BLOCK_REG_KEY = "block_registry_name";
    public static final String NBT_CUSTOM_BLOCK_META = "block_meta";
    public static final String NBT_CUSTOM_ITEM_STACK = "item_stack";

    public static IFacadeItem facadeItem;

    private FacadeAPI() {

    }

    public static void disableBlock(Block block) {
        FMLInterModComms.sendMessage(IMC_MOD_TARGET, IMC_FACADE_DISABLE, block.getRegistryName());
    }

    public static void mapStateToStack(IBlockState state, ItemStack stack) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(NBT_CUSTOM_BLOCK_REG_KEY, state.getBlock().getRegistryName().toString());
        nbt.setInteger(NBT_CUSTOM_BLOCK_META, state.getBlock().getMetaFromState(state));
        nbt.setTag(NBT_CUSTOM_ITEM_STACK, stack.serializeNBT());
        FMLInterModComms.sendMessage(IMC_MOD_TARGET, IMC_FACADE_CUSTOM, nbt);
    }
}
