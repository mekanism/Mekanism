package mekanism.generators.common.network.to_server;

import mekanism.api.functions.TriConsumer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorLogicAdapter.FissionReactorLogic;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorLogicAdapter.FusionReactorLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Used for informing the server that an action happened in a GUI
 */
public class PacketGeneratorsGuiInteract implements IMekanismPacket {

    private final GeneratorsGuiInteraction interaction;
    private final BlockPos tilePosition;
    private final double extra;

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockEntity tile) {
        this(interaction, tile.getBlockPos());
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockEntity tile, double extra) {
        this(interaction, tile.getBlockPos(), extra);
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockPos tilePosition) {
        this(interaction, tilePosition, 0);
    }

    public PacketGeneratorsGuiInteract(GeneratorsGuiInteraction interaction, BlockPos tilePosition, double extra) {
        this.interaction = interaction;
        this.tilePosition = tilePosition;
        this.extra = extra;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        Player player = context.getSender();
        if (player != null) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.level, tilePosition);
            if (tile != null) {
                interaction.consume(tile, player, extra);
            }
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(interaction);
        buffer.writeBlockPos(tilePosition);
        buffer.writeDouble(extra);
    }

    public static PacketGeneratorsGuiInteract decode(FriendlyByteBuf buffer) {
        return new PacketGeneratorsGuiInteract(buffer.readEnum(GeneratorsGuiInteraction.class), buffer.readBlockPos(), buffer.readDouble());
    }

    public enum GeneratorsGuiInteraction {
        INJECTION_RATE((tile, player, extra) -> {
            if (tile instanceof TileEntityFusionReactorBlock reactorBlock) {
                reactorBlock.setInjectionRateFromPacket((int) Math.round(extra));
            } else if (tile instanceof TileEntityFissionReactorCasing reactorCasing) {
                reactorCasing.setRateLimitFromPacket(extra);
            }
        }),
        LOGIC_TYPE((tile, player, extra) -> {
            if (tile instanceof TileEntityFissionReactorLogicAdapter logicAdapter) {
                logicAdapter.setLogicTypeFromPacket(FissionReactorLogic.byIndexStatic((int) Math.round(extra)));
            } else if (tile instanceof TileEntityFusionReactorLogicAdapter logicAdapter) {
                logicAdapter.setLogicTypeFromPacket(FusionReactorLogic.byIndexStatic((int) Math.round(extra)));
            }
        }),
        FISSION_ACTIVE((tile, player, extra) -> {
            if (tile instanceof TileEntityFissionReactorCasing reactorCasing) {
                reactorCasing.setReactorActive(Math.round(extra) == 1);
            }
        });

        private final TriConsumer<TileEntityMekanism, Player, Double> consumerForTile;

        GeneratorsGuiInteraction(TriConsumer<TileEntityMekanism, Player, Double> consumerForTile) {
            this.consumerForTile = consumerForTile;
        }

        public void consume(TileEntityMekanism tile, Player player, double extra) {
            consumerForTile.accept(tile, player, extra);
        }
    }
}