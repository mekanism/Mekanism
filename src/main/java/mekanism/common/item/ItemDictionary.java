package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.OreDictCache;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.util.text.TextComponentUtil;
import mekanism.common.util.text.Translation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemDictionary extends Item {

    public ItemDictionary(Properties properties) {
        super(properties.maxStackSize(1));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (player != null && !player.isSneaking()) {
            BlockPos pos = context.getPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (world.isRemote) {
                ItemStack testStack = new ItemStack(block);
                List<String> names = OreDictCache.getOreDictName(testStack);
                if (!names.isEmpty()) {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                          Translation.of("tooltip.mekanism.keysFound"), ":"));
                    for (String name : names) {
                        player.sendMessage(TextComponentUtil.build(EnumColor.DARK_GREEN, " - " + name));
                    }
                } else {
                    player.sendMessage(TextComponentUtil.build(EnumColor.DARK_BLUE, Mekanism.LOG_TAG + " ", EnumColor.GRAY,
                          Translation.of("tooltip.mekanism.noKey"), "."));
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && !world.isRemote()) {
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new DictionaryContainer(i, inv, hand, stack)),
                  buf -> {
                      buf.writeEnumValue(hand);
                      buf.writeItemStack(stack);
                  });
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }
}