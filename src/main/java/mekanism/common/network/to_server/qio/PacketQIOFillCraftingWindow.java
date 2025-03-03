package mekanism.common.network.to_server.qio;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.content.qio.QIOServerCraftingTransferHandler;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

//Note: While our logic is not dependent on knowing about transferMultiple, we make use of it for encoding and decoding
// as when it is false we can reduce how many bytes the packet is by a good amount by making assumptions about the sizes of things
@NothingNullByDefault
public record PacketQIOFillCraftingWindow(ResourceLocation recipeID, boolean transferMultiple, boolean rejectToInventory,
                                          Byte2ObjectMap<List<SingularHashedItemSource>> sources) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketQIOFillCraftingWindow> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("fill_qio"));
    public static final StreamCodec<ByteBuf, PacketQIOFillCraftingWindow> STREAM_CODEC = StreamCodec.ofMember(PacketQIOFillCraftingWindow::write, PacketQIOFillCraftingWindow::decode);

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketQIOFillCraftingWindow> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof QIOItemViewerContainer container) {
            byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.getUUID());
            if (selectedCraftingGrid == -1) {
                Mekanism.logger.warn("Received transfer request from: {}, but they do not currently have a crafting window open.", player);
            } else {
                Optional<RecipeHolder<?>> optionalRecipe = MekanismRecipeType.byKey(player.level(), recipeID);
                if (optionalRecipe.isPresent()) {
                    Recipe<?> recipe = optionalRecipe.get().value();
                    if (recipe instanceof CraftingRecipe craftingRecipe) {
                        QIOServerCraftingTransferHandler.tryTransfer(container, selectedCraftingGrid, rejectToInventory, player, recipeID, craftingRecipe, sources);
                    } else {
                        Mekanism.logger.warn("Received transfer request from: {}, but the type ({}) of the specified recipe was not a crafting recipe.",
                              player, recipe.getClass());
                    }
                } else {
                    Mekanism.logger.warn("Received transfer request from: {}, but could not find specified recipe.", player);
                }
            }
        }
    }

    private void write(@NotNull ByteBuf buffer) {
        ResourceLocation.STREAM_CODEC.encode(buffer, recipeID);
        buffer.writeBoolean(transferMultiple);
        buffer.writeBoolean(rejectToInventory);
        //Cast to byte as this should always be at most 9
        buffer.writeByte((byte) sources.size());
        for (ObjectIterator<Byte2ObjectMap.Entry<List<SingularHashedItemSource>>> iterator = Byte2ObjectMaps.fastIterator(sources); iterator.hasNext(); ) {
            Byte2ObjectMap.Entry<List<SingularHashedItemSource>> entry = iterator.next();
            //Target Slot
            buffer.writeByte(entry.getByteKey());
            //Source slot
            List<SingularHashedItemSource> slotSources = entry.getValue();
            if (transferMultiple) {
                //We "cheat" by only writing the list size if we are transferring as many items as possible as
                // the list will always be of size one
                ByteBufCodecs.VAR_INT.encode(buffer, slotSources.size());
            }
            for (SingularHashedItemSource source : slotSources) {
                byte sourceSlot = source.getSlot();
                //We "cheat" here by just writing the source slot regardless of if we are in the crafting window, main inventory, or QIO
                // as then we can use the not a valid value as indication that we have a UUID following for QIO source, and otherwise we
                // get away with not having to write some sort of identifier for which type of data we are transferring
                buffer.writeByte(sourceSlot);
                if (transferMultiple) {
                    //We "cheat" by only writing the amount used if we are transferring as many items as possible as
                    // this will always just be one
                    ByteBufCodecs.VAR_INT.encode(buffer, source.getUsed());
                }
                if (sourceSlot == -1) {
                    //If we don't actually have a source slot, that means we need to write the UUID
                    // as it is being transferred out of the QIO
                    UUID qioSource = source.getQioSource();
                    if (qioSource == null) {
                        throw new IllegalStateException("Invalid QIO crafting window transfer source.");
                    }
                    UUIDUtil.STREAM_CODEC.encode(buffer, qioSource);
                }
            }
        }
    }

    private static PacketQIOFillCraftingWindow decode(ByteBuf buffer) {
        ResourceLocation recipeID = ResourceLocation.STREAM_CODEC.decode(buffer);
        boolean transferMultiple = buffer.readBoolean();
        boolean rejectToInventory = buffer.readBoolean();
        byte slotCount = buffer.readByte();
        Byte2ObjectMap<List<SingularHashedItemSource>> sources = new Byte2ObjectArrayMap<>(slotCount);
        for (byte slot = 0; slot < slotCount; slot++) {
            byte targetSlot = buffer.readByte();
            int subSourceCount = transferMultiple ? ByteBufCodecs.VAR_INT.decode(buffer) : 1;
            List<SingularHashedItemSource> slotSources = new ArrayList<>(subSourceCount);
            sources.put(targetSlot, slotSources);
            for (int i = 0; i < subSourceCount; i++) {
                byte sourceSlot = buffer.readByte();
                int count = transferMultiple ? ByteBufCodecs.VAR_INT.decode(buffer) : 1;
                if (sourceSlot == -1) {
                    slotSources.add(new SingularHashedItemSource(UUIDUtil.STREAM_CODEC.decode(buffer), count));
                } else {
                    slotSources.add(new SingularHashedItemSource(sourceSlot, count));
                }
            }
        }
        return new PacketQIOFillCraftingWindow(recipeID, transferMultiple, rejectToInventory, sources);
    }
}