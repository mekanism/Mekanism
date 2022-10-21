package mekanism.common.item;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemDictionary extends Item {

    public ItemDictionary(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
            tooltip.add(MekanismLang.DESCRIPTION_DICTIONARY.translate());
        } else {
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.descriptionKey.getTranslatedKeyMessage()));
        }
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            Level world = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile != null || !player.isShiftKeyDown()) {
                //If there is a tile at the position or the player is not sneaking
                // grab the tags of the block and the tile
                if (!world.isClientSide) {
                    BlockState blockState = world.getBlockState(pos);
                    FluidState fluidState = blockState.getFluidState();
                    Set<ResourceLocation> blockTags = TagUtils.tagNames(blockState.getTags());
                    Set<ResourceLocation> fluidTags = fluidState.isEmpty() ? Collections.emptySet() : TagUtils.tagNames(fluidState.getTags());
                    Set<ResourceLocation> tileTags = tile == null ? Collections.emptySet() : TagUtils.tagNames(ForgeRegistries.BLOCK_ENTITY_TYPES, tile.getType());
                    if (blockTags.isEmpty() && fluidTags.isEmpty() && tileTags.isEmpty()) {
                        player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.DICTIONARY_NO_KEY));
                    } else {
                        //Note: We handle checking they are not empty in sendTagsToPlayer, so that we only display one if one is empty
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_BLOCK_TAGS_FOUND, blockTags);
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, fluidTags);
                        sendTagsToPlayer(player, MekanismLang.DICTIONARY_BLOCK_ENTITY_TYPE_TAGS_FOUND, tileTags);
                    }
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            if (!player.level.isClientSide) {
                sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_ENTITY_TYPE_TAGS_FOUND, entity.getType().getTags());
            }
            return InteractionResult.sidedSuccess(player.level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!world.isClientSide()) {
                MekanismContainerTypes.DICTIONARY.tryOpenGui((ServerPlayer) player, hand, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        } else {
            BlockHitResult result = MekanismUtils.rayTrace(player, ClipContext.Fluid.ANY);
            if (result.getType() != Type.MISS) {
                FluidState fluidState = world.getFluidState(result.getBlockPos());
                if (!fluidState.isEmpty()) {
                    if (!world.isClientSide()) {
                        sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, fluidState.getTags());
                    }
                    return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    private void sendTagsOrEmptyToPlayer(Player player, ILangEntry tagsFoundEntry, Stream<? extends TagKey<?>> tags) {
        sendTagsOrEmptyToPlayer(player, tagsFoundEntry, TagUtils.tagNames(tags));
    }

    private void sendTagsOrEmptyToPlayer(Player player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
        if (tags.isEmpty()) {
            player.sendSystemMessage(MekanismUtils.logFormat(MekanismLang.DICTIONARY_NO_KEY));
        } else {
            sendTagsToPlayer(player, tagsFoundEntry, tags);
        }
    }

    private void sendTagsToPlayer(Player player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
        if (!tags.isEmpty()) {
            player.sendSystemMessage(MekanismUtils.logFormat(tagsFoundEntry));
            for (ResourceLocation tag : tags) {
                player.sendSystemMessage(MekanismLang.DICTIONARY_KEY.translateColored(EnumColor.DARK_GREEN, tag));
            }
        }
    }
}