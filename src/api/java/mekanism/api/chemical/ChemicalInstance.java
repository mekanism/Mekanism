package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemInstance;

///@since 10.8.0
public interface ChemicalInstance extends TypedInstance<Chemical> {

    String FIELD_ID = ItemInstance.FIELD_ID;
    String FIELD_AMOUNT = SerializationConstants.AMOUNT;

    /// A standard codec for non-empty Chemical holders.
    Codec<Holder<Chemical>> CHEMICAL_HOLDER_CODEC = Chemical.CODEC.validate(chemical -> chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)
                                                                                        ? DataResult.error(() -> "Chemical must not be mekanism:empty")
                                                                                        : DataResult.success(chemical));

    /// A stream codec which can be used to encode and decode chemical holders over the network.
    StreamCodec<RegistryFriendlyByteBuf, Holder<Chemical>> CHEMICAL_HOLDER_STREAM_CODEC = Chemical.STREAM_CODEC;

    /// Gets the size of this chemical instance.
    ///
    /// @return The size of this chemical instance or zero if it is empty
    int amount();
}