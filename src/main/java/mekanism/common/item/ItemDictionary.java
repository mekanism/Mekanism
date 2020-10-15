package mekanism.common.item;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.DictionaryContainer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemDictionary extends Item {

    public ItemDictionary(Properties properties) {
        super(properties.maxStackSize(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(MekanismLang.DESCRIPTION_DICTIONARY.translate());
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.func_238171_j_()));
        }
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null) {
            World world = context.getWorld();
            BlockPos pos = context.getPos();
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile != null || !player.isSneaking()) {
                //If there is a tile at the position or the player is not sneaking
                // grab the tags of the block and the tile
                if (!world.isRemote) {
                    Set<ResourceLocation> blockTags = world.getBlockState(pos).getBlock().getTags();
                    Set<ResourceLocation> tileTags = tile == null ? Collections.emptySet() : tile.getType().getTags();
                    if (blockTags.isEmpty() && tileTags.isEmpty()) {
                        player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY, MekanismLang.DICTIONARY_NO_KEY),
                              Util.DUMMY_UUID);
                    } else {
                        //Note: We handle checking they are not empty in sendTagsToPlayer, so that we only display one if one is empty
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_BLOCK_TAGS_FOUND, blockTags);
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_TILE_ENTITY_TYPE_TAGS_FOUND, tileTags);
                    }
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResultType itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull PlayerEntity player, @Nonnull LivingEntity entity, @Nonnull Hand hand) {
        if (!player.isSneaking()) {
            if (!player.world.isRemote) {
                sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_ENTITY_TYPE_TAGS_FOUND, entity.getType().getTags());
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote()) {
                NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new DictionaryContainer(i, inv, hand, stack)),
                      buf -> {
                          buf.writeEnumValue(hand);
                          buf.writeItemStack(stack);
                      });
            }
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        } else {
            BlockRayTraceResult result = MekanismUtils.rayTrace(player, FluidMode.ANY);
            if (result.getType() != Type.MISS) {
                Block block = world.getBlockState(result.getPos()).getBlock();
                if (block instanceof FlowingFluidBlock) {
                    if (!world.isRemote()) {
                        sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, ((FlowingFluidBlock) block).getFluid().getTags());
                    }
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private void sendTagsOrEmptyToPlayer(PlayerEntity player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
        if (tags.isEmpty()) {
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY, MekanismLang.DICTIONARY_NO_KEY),
                  Util.DUMMY_UUID);
        } else {
            sendTagsToPlayer(player, tagsFoundEntry, tags);
        }
    }

    private void sendTagsToPlayer(PlayerEntity player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
        if (!tags.isEmpty()) {
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, EnumColor.GRAY, tagsFoundEntry), Util.DUMMY_UUID);
            for (ResourceLocation tag : tags) {
                player.sendMessage(MekanismLang.DICTIONARY_KEY.translateColored(EnumColor.DARK_GREEN, tag), Util.DUMMY_UUID);
            }
        }
    }
}