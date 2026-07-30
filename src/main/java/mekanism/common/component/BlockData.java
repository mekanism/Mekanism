package mekanism.common.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Bees;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jspecify.annotations.Nullable;

public record BlockData(BlockState blockState, @Nullable CompoundTag blockEntityTag) implements TooltipProvider {

    public static final BlockData NONE = new BlockData(Blocks.AIR.defaultBlockState(), null);

    public static final Codec<BlockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          BlockState.CODEC.fieldOf(SerializationConstants.STATE).forGetter(BlockData::blockState),
          CompoundTag.CODEC.optionalFieldOf(SerializationConstants.BLOCK_ENTITY_TAG).forGetter(data -> Optional.ofNullable(data.blockEntityTag))
    ).apply(instance, (state, tag) -> new BlockData(state, tag.orElse(null))));
    public static final StreamCodec<ByteBuf, BlockData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), BlockData::blockState,
          ByteBufCodecs.optional(ByteBufCodecs.TRUSTED_COMPOUND_TAG), data -> Optional.ofNullable(data.blockEntityTag()),
          (state, tag) -> new BlockData(state, tag.orElse(null))
    );
    private static final Component UNKNOWN = MekanismLang.UNKNOWN.translate();

    public BlockData(HolderLookup.Provider provider, BlockState state, @Nullable BlockEntity blockEntity) {
        this(state, blockEntity == null ? null : blockEntity.saveWithFullMetadata(provider));
    }

    public boolean tryPlaceIntoWorld(Level level, BlockPos pos, @Nullable Player player) {
        //TODO: Note - this will not allow for rotation of the block based on how it is placed direction wise via the removal of
        // the cardboard box and will instead leave it how it was when the box was initially put on
        //Adjust the state based on neighboring blocks to ensure double chests properly become single chests again
        BlockState adjustedState = Block.updateFromNeighbourShapes(blockState, level, pos);
        if (adjustedState.isAir()) {
            //If the block cannot be unpacked in this position, don't allow it to be unpacked
            return false;
        }

        FluidState fluidState = adjustedState.getFluidState();
        FluidType fluidType = fluidState.getFluidType();
        //Note: Doesn't support nbt
        FluidStack fluid = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
        BucketPickup tryPickup = null;
        //Do our best effort to support to not allow water to be placed into the nether
        if (fluidType.isVaporizedOnPlacement(level, pos, fluid)) {
            if (!MekanismConfig.general.strictUnboxing.get() && adjustedState.getBlock() instanceof BucketPickup pickup) {
                tryPickup = pickup;
            } else {
                //Not a bucket pickup, we don't know how to pick up the block
                return false;
            }
        }

        level.setBlockAndUpdate(pos, adjustedState);
        //TODO: Do we need to call setPlacedBy or not bother given we are setting the blockstate to what it was AND setting any tile data
        //adjustedState.getBlock().setPlacedBy(world, pos, blockState, player, new ItemStack(adjustedState.getBlock()));
        if (blockEntityTag != null) {
            //Update the location
            blockEntityTag.putInt(SerializationConstants.X, pos.getX());
            blockEntityTag.putInt(SerializationConstants.Y, pos.getY());
            blockEntityTag.putInt(SerializationConstants.Z, pos.getZ());
            //And get the block entity and load it from the data
            BlockEntity tile = WorldUtils.getTileEntity(level, pos);
            if (tile != null) {
                try (ProblemReporter.ScopedCollector problemRep = new ProblemReporter.ScopedCollector(tile.problemPath(), Mekanism.logger)) {
                    ValueInput valueInput = TagValueInput.create(problemRep, level.registryAccess(), blockEntityTag);
                    tile.loadWithComponents(valueInput);
                }
            }
        }
        if (tryPickup != null) {
            if (!tryPickup.pickupBlock(player, level, pos, adjustedState).isEmpty()) {
                fluidType.onVaporize(null, level, pos, fluid);
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("OptionalIsPresent")//Capturing lambdas
    public void addToTooltip(TooltipContext context, Consumer<Component> builder, TooltipFlag flag, DataComponentGetter componentGetter) {
        boolean hasData = !blockState.isAir();
        builder.accept(MekanismLang.BLOCK_DATA.translateColored(EnumColor.INDIGO, YesNo.of(hasData, true)));
        if (!hasData) {
            return;
        }
        Block block = blockState.getBlock();
        builder.accept(MekanismLang.BLOCK.translateColored(EnumColor.INDIGO, EnumColor.GRAY, block));
        //TODO: Try to come up with a better way to proxy components from the stored block's BE
        if (blockEntityTag != null) {
            builder.accept(MekanismLang.BLOCK_ENTITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY,
                  RegistryUtils.getHolderById(blockEntityTag, BuiltInRegistries.BLOCK_ENTITY_TYPE)
                        .<Object>map(Holder::getRegisteredName)
                        .orElse(UNKNOWN)
            ));
            //Note: Currently unused by any of the addToTooltip methods we proxy
            DataComponentGetter boxedComponentsGetter = DataComponentMap.EMPTY;
            if (block instanceof SpawnerBlock || block instanceof TrialSpawnerBlock) {
                String key = block instanceof SpawnerBlock ? BaseSpawner.SPAWN_DATA_TAG : TrialSpawnerStateData.TAG_SPAWN_DATA;
                Optional<EntityType<?>> entityType = blockEntityTag.read(key, SpawnData.CODEC)
                      .map(SpawnData::getEntityToSpawn)
                      .flatMap(entity -> entity.read(Entity.TAG_ID, EntityType.CODEC));
                if (entityType.isPresent()) {
                    builder.accept(MekanismLang.BLOCK_ENTITY_SPAWN_TYPE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, entityType.get().getDescription()));
                }
            } else if (block instanceof DecoratedPotBlock) {
                //Based off ItemStack#addToTooltip, but using the values we already have
                PotDecorations decorations = blockEntityTag.read(DecoratedPotBlockEntity.TAG_SHERDS, PotDecorations.CODEC).orElse(PotDecorations.EMPTY);
                if (decorations != PotDecorations.EMPTY) {// && tooltipDisplay.shows(DataComponents.POT_DECORATIONS)
                    Consumer<Component> tooltipListAdder = decoration -> {
                        if (decoration == CommonComponents.EMPTY) {
                            //If it is the blank line being added before the list of decorations, also add our section that displays it as being decorated
                            builder.accept(decoration);
                            builder.accept(MekanismLang.BLOCK_ENTITY_DECORATION.translateColored(EnumColor.INDIGO));
                        } else {
                            // Otherwise, it is one of the decorations, show it in our list format
                            builder.accept(MekanismLang.GENERIC_LIST.translateColored(EnumColor.INDIGO, EnumColor.GRAY, decoration));
                        }
                    };
                    decorations.addToTooltip(context, tooltipListAdder, flag, boxedComponentsGetter);
                }
            } else if (block instanceof BeehiveBlock) {
                //BeehiveBlockEntity#BEES
                Optional<Bees> bees = blockEntityTag.read("bees", Occupant.LIST_CODEC).map(Bees::new);
                if (bees.isPresent()) {
                    bees.get().addToTooltip(context, builder, flag, boxedComponentsGetter);
                }
                Optional<BlockItemStateProperties> properties = blockState.getOptionalValue(BeehiveBlock.HONEY_LEVEL)
                      .map(honey -> new BlockItemStateProperties(Map.of(BeehiveBlock.HONEY_LEVEL.getName(), Integer.toString(honey))));
                if (properties.isPresent()) {
                    properties.get().addToTooltip(context, builder, flag, boxedComponentsGetter);
                }
            }
        }
    }
}