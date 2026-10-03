package mekanism.common.content.gear.mekatool;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.IntFunction;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketLightningRender.LightningPreset;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.BlockTransformer.BlockTransformData;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public record ModuleFarmingUnit(FarmingRadius farmingRadius) implements ICustomModule<ModuleFarmingUnit> {

    public static final Identifier FARMING_RADIUS = Mekanism.rl("farming_radius");
    private static final List<ToolFunction> TOOL_FUNCTIONS = List.of(
          //First try to use the meka-tool as an axe
          ModuleFarmingUnit::useAxeAOE,
          //Then as a shovel
          ModuleFarmingUnit::flattenAOE,
          //Finally, as a hoe
          ModuleFarmingUnit::tillAOE
    );

    public ModuleFarmingUnit(IModule<ModuleFarmingUnit> module) {
        this(module.<FarmingRadius>getConfigOrThrow(FARMING_RADIUS).get());
    }

    @Override
    public InteractionResult onItemUse(IModule<ModuleFarmingUnit> module, UseOnContext context, TransactionContext transaction) {
        //Start with doing common logic to the module before we get onto specific logic for the different ways the module can be used
        Player player = context.getPlayer();
        if (player == null) {
            //Skip if we don't have a player
            return InteractionResult.PASS;
        } else if (context.getHand() == InteractionHand.MAIN_HAND && player.getOffhandItem().has(DataComponents.BLOCKS_ATTACKS) && !player.isSecondaryUseActive()) {
            //Copied check from the top of BlockTransformer#transformBlock
            return InteractionResult.PASS;
        }
        int diameter = farmingRadius.getRadius();
        if (diameter == 0) {
            //If we don't have any blocks we are going to want to do, then skip it
            return InteractionResult.PASS;
        }
        EnergyHandler energyHandler = module.getEnergyHandler(ItemAccess.forStack(context.getItemInHand()), true);
        if (energyHandler == null) {
            return InteractionResult.FAIL;
        }
        //Lookup the state so we only have to query it once
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());
        for (ToolFunction action : TOOL_FUNCTIONS) {
            try (Transaction subTransaction = Transaction.open(transaction)) {
                InteractionResult result = action.use(context, clickedState, energyHandler, diameter, subTransaction);
                if (result.consumesAction()) {
                    //If we were successful
                    subTransaction.commit();
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> boolean canPerformAction(IModule<ModuleFarmingUnit> module, IModuleContainer moduleContainer,
          ITEM instance, ItemAbility action) {
        return action == ItemAbilities.SHOVEL_DOUSE;
    }

    public enum FarmingRadius implements IHasTextComponent, StringRepresentable {
        OFF(0),
        LOW(1),
        MED(3),
        HIGH(5),
        ULTRA(7);

        public static final Codec<FarmingRadius> CODEC = StringRepresentable.fromEnum(FarmingRadius::values);
        public static final IntFunction<FarmingRadius> BY_ID = ByIdMap.continuous(FarmingRadius::ordinal, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        public static final StreamCodec<ByteBuf, FarmingRadius> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FarmingRadius::ordinal);

        private final String serializedName;
        private final int radius;
        private final Component label;

        FarmingRadius(int radius) {
            this.serializedName = name().toLowerCase(Locale.ROOT);
            this.radius = radius;
            this.label = TextComponentUtil.getString(Integer.toString(radius));
        }

        @Override
        public Component getTextComponent() {
            return label;
        }

        public int getRadius() {
            return radius;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private static InteractionResult tillAOE(UseOnContext context, BlockState clickedState, EnergyHandler energyHandler, int diameter, TransactionContext transaction) {
        return transformAOE(context, clickedState, energyHandler, diameter, transaction, BlockTransformers.HOE, MekanismConfig.gear.mekaToolEnergyUsageHoe.get(), new FlatToolAOEData());
    }

    private static InteractionResult flattenAOE(UseOnContext context, BlockState clickedState, EnergyHandler energyHandler, int diameter, TransactionContext transaction) {
        return transformAOE(context, clickedState, energyHandler, diameter, transaction, BlockTransformers.SHOVEL, MekanismConfig.gear.mekaToolEnergyUsageShovel.get(), new FlatToolAOEData());
    }

    private static InteractionResult useAxeAOE(UseOnContext context, BlockState clickedState, EnergyHandler energyHandler, int diameter, TransactionContext transaction) {
        return transformAOE(context, clickedState, energyHandler, diameter, transaction, BlockTransformers.AXE, MekanismConfig.gear.mekaToolEnergyUsageAxe.get(), new AxeToolAOEData());
    }

    private static InteractionResult transformAOE(UseOnContext context, BlockState clickedState, EnergyHandler energyHandler, int diameter, TransactionContext transaction,
          ResourceKey<BlockTransformer> transformer, int energyUsage, IToolAOEData toolAOEData) {
        Reference<BlockTransformer> holder = context.getLevel().registryAccess().getOrThrow(transformer);
        Iterable<BlockTransformData> transforms = DataMapHooks.getAllTransformers(holder);
        Player player = context.getPlayer();
        if (player != null && player.isCreative()) {
            energyUsage = 0;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        ItemStack itemInHand = context.getItemInHand();
        BlockState modifiedState = null;
        BlockTransformer.BlockTransformData chosenTransform = null;
        int theoreticalAvailableEnergy = -1;
        for (BlockTransformer.BlockTransformData transformData : transforms) {
            if (!transformData.disallowedFaces().contains(clickedFace)) {
                BlockState newBlockState = transformData.blockStateProvider().value().getOptionalState(level, level.getRandom(), pos);
                if (newBlockState != null) {
                    //Scale up (or down) how much energy each action costs based on how much damage is done per usage
                    int scaledEnergyUsage = energyUsage * transformData.itemDamagePerUse();
                    if (scaledEnergyUsage > 0) {
                        if (theoreticalAvailableEnergy != -1 && scaledEnergyUsage > theoreticalAvailableEnergy) {
                            //We need more energy than the max amount we already know can be extracted checked an amount higher than what we have available,
                            // and we know we won't have enough for this either, so skip
                            continue;
                        }
                        try (Transaction subTransaction = Transaction.open(transaction)) {
                            int extracted = energyHandler.extract(scaledEnergyUsage, subTransaction);
                            if (extracted < scaledEnergyUsage) {
                                //We don't have enough energy to extracting from our container, skip to the next valid transform as maybe it costs less energy
                                theoreticalAvailableEnergy = extracted;
                                continue;
                            } else if (!level.isClientSide()) {
                                //Consume energy for primary target block
                                subTransaction.commit();
                            }
                        }
                    }
                    modifiedState = newBlockState;
                    chosenTransform = transformData;
                    //Scale up (or down) how much energy each action costs based on how much damage is done per usage
                    energyUsage = scaledEnergyUsage;
                    break;
                }
            }
        }
        if (modifiedState == null) {
            //Skip modifying the blocks if the one we clicked cannot be modified
            return InteractionResult.PASS;
        } else if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        //Process the block we interacted with initially and play the sound
        applyTransform(level, pos, clickedFace, modifiedState, clickedState, player, itemInHand, chosenTransform, toolAOEData);
        for (BlockPos newPos : toolAOEData.getTargetPositions(pos, clickedFace, (diameter - 1) / 2)) {
            if (pos.equals(newPos)) {
                //Skip the source position as we manually handled it before the loop
                continue;
            }
            try (Transaction subTransaction = Transaction.open(transaction)) {
                if (energyUsage > 0 && energyHandler.extract(energyUsage, subTransaction) < energyUsage) {
                    //We don't have enough energy to continue extracting from our container, break
                    break;
                }
                //Check to make that the result we would get from modifying the other block is the same as the one we got on the initial block we interacted with
                // Also make sure that it is properly valid
                if (modifiedState == chosenTransform.blockStateProvider().value().getOptionalState(level, level.getRandom(), newPos)) {
                    //Some of the below methods don't behave properly when the BlockPos is mutable, so now that we are onto ones where it may actually
                    // matter we make sure to get an immutable instance of newPos
                    newPos = newPos.immutable();
                    //Update energy cost
                    subTransaction.commit();
                    BlockState oldBlockState = level.getBlockState(newPos);
                    applyTransform(level, newPos, clickedFace, modifiedState, oldBlockState, player, itemInHand, chosenTransform, toolAOEData);
                    PacketUtils.sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, 31 * pos.hashCode() + newPos.hashCode(),
                          toolAOEData.getLightningPos(pos, clickedFace), toolAOEData.getLightningPos(newPos, clickedFace), 10), level, pos);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    /// Logic tweaked from [BlockTransformer#transformBlock(UseOnContext)]
    private static void applyTransform(Level level, BlockPos pos, Direction clickedFace, BlockState newBlockState, BlockState oldBlockState, @Nullable Player player,
          ItemStack itemInHand, BlockTransformer.BlockTransformData transformData, IToolAOEData toolAOEData) {
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemInHand);
        }

        if (level instanceof ServerLevel serverLevel) {
            Optional<ResourceKey<LootTable>> loot = transformData.loot();
            //noinspection OptionalIsPresent
            if (loot.isPresent()) {
                Block.dropFromBlockInteractLootTable(serverLevel, loot.get(),
                      pos,
                      oldBlockState,
                      level.getBlockEntity(pos),
                      itemInHand,
                      player,
                      (sl, stack) -> transformData.dropStrategy().pop(sl, pos, clickedFace, stack)
                );
            }
        }

        BlockState updatedShape = transformData.updateFromNeighbors() ? Block.updateFromNeighbourShapes(newBlockState, level, pos) : newBlockState;
        //Replace the block. Note it just directly sets it (in the same way the normal tools do).
        level.setBlock(pos, updatedShape, Block.UPDATE_ALL_IMMEDIATE);
        //Note: Change player to be explicitly null so that the server sends it to the client as we don't run this on the client side
        level.playSound(null, pos, transformData.sound().value(), SoundSource.BLOCKS, 1.0F, 1.0F);
        //Note: Change player to be explicitly null so that the server sends it to the client as we don't run this on the client side
        transformData.particle().send(level, null, pos);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, updatedShape));

        if (transformData.transformType() == BlockTransformer.TransformType.COPPER_CHEST
            && oldBlockState.getBlock() instanceof CopperChestBlock && oldBlockState.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos neighborPos = ChestBlock.getConnectedBlockPos(pos, oldBlockState);
            level.gameEvent(GameEvent.BLOCK_CHANGE, neighborPos, GameEvent.Context.of(player, level.getBlockState(neighborPos)));
            //Note: Change player to be explicitly null so that the server sends it to the client as we don't run this on the client side
            transformData.particle().send(level, null, neighborPos);

            PacketUtils.sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, 31 * pos.hashCode() + neighborPos.hashCode(),
                  toolAOEData.getLightningPos(pos, clickedFace), toolAOEData.getLightningPos(neighborPos, clickedFace), 10), level, pos);
        }
    }

    @FunctionalInterface
    private interface ToolFunction {

        InteractionResult use(UseOnContext context, BlockState clickedState, EnergyHandler energyHandler, int diameter, TransactionContext transaction);
    }

    private interface IToolAOEData {

        Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius);

        Vec3 getLightningPos(BlockPos pos, Direction clickedSide);
    }

    private static class FlatToolAOEData implements IToolAOEData {

        @Override
        public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
            return BlockPos.betweenClosed(pos.offset(-radius, 0, -radius), pos.offset(radius, 0, radius));
        }

        @Override
        public Vec3 getLightningPos(BlockPos pos, Direction clickedSide) {
            return Vec3.upFromBottomCenterOf(pos, 0.94);
        }
    }

    private static class AxeToolAOEData implements IToolAOEData {

        @Override
        public Iterable<BlockPos> getTargetPositions(BlockPos pos, Direction side, int radius) {
            Vec3i adjustment = switch (side) {
                case EAST, WEST -> new Vec3i(0, radius, radius);
                case UP, DOWN -> new Vec3i(radius, 0, radius);
                case SOUTH, NORTH -> new Vec3i(radius, radius, 0);
            };
            BlockPos first = pos.subtract(adjustment);
            BlockPos second = pos.offset(adjustment);
            AABB box = new AABB(first.getX(), first.getY(), first.getZ(), second.getX(), second.getY(), second.getZ());
            return BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ));
        }

        @Override
        public Vec3 getLightningPos(BlockPos pos, Direction clickedSide) {
            return Vec3.atCenterOf(pos)
                  .add(0.5 * clickedSide.getStepX(), 0.5 * clickedSide.getStepY(), 0.5 * clickedSide.getStepZ());
        }
    }
}