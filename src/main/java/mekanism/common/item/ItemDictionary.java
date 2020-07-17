package mekanism.common.item;

import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.DictionaryContainer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemDictionary extends Item {

    public ItemDictionary(Properties properties) {
        super(properties.maxStackSize(1).rarity(Rarity.UNCOMMON));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && !player.isSneaking()) {
            World world = context.getWorld();
            if (world.isRemote) {
                Block block = world.getBlockState(context.getPos()).getBlock();
                //TODO - V11: List fluid tags it the item is of that type
                Set<ResourceLocation> tags = block.getTags();
                if (tags.isEmpty()) {
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, MekanismLang.DICTIONARY_NO_KEY.translateColored(EnumColor.GRAY)), Util.field_240973_b_);
                } else {
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, MekanismLang.DICTIONARY_KEYS_FOUND.translateColored(EnumColor.GRAY)), Util.field_240973_b_);
                    for (ResourceLocation tag : tags) {
                        player.sendMessage(MekanismLang.DICTIONARY_KEY.translateColored(EnumColor.DARK_GREEN, tag), Util.field_240973_b_);
                    }
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
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