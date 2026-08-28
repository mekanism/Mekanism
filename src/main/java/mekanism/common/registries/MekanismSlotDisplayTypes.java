package mekanism.common.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.MekanismRegistries;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.display.slot.ChanceSlotDisplay;
import mekanism.api.recipes.display.slot.WithAmountSlotDisplay;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalSlotDisplay;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalTagSlotDisplay;
import mekanism.common.Mekanism;
import mekanism.common.recipe.display.slot.ChemicalConversionSlotDisplay;
import mekanism.common.recipe.display.slot.ChemicalSolidTagSlotDisplay;
import mekanism.common.recipe.display.slot.ChemicalTankSlotDisplay;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismSlotDisplayTypes {

    public static final DeferredRegister<SlotDisplay.Type<?>> SLOT_DISPLAY_TYPES = DeferredRegister.create(Registries.SLOT_DISPLAY, Mekanism.MODID);

    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalSlotDisplay>> CHEMICAL = SLOT_DISPLAY_TYPES.register("chemical",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryFixedCodec.create(MekanismRegistries.Keys.CHEMICAL).fieldOf(SerializationConstants.CHEMICAL).forGetter(ChemicalSlotDisplay::chemical)
          ).apply(instance, ChemicalSlotDisplay::new)), StreamCodec.composite(
                ByteBufCodecs.holderRegistry(MekanismRegistries.Keys.CHEMICAL), ChemicalSlotDisplay::chemical,
                ChemicalSlotDisplay::new
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalStackSlotDisplay>> CHEMICAL_STACK = SLOT_DISPLAY_TYPES.register("chemical_stack",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.CHEMICAL).forGetter(ChemicalStackSlotDisplay::stack)
          ).apply(instance, ChemicalStackSlotDisplay::new)), StreamCodec.composite(
                ChemicalStackTemplate.STREAM_CODEC, ChemicalStackSlotDisplay::stack,
                ChemicalStackSlotDisplay::new
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalTagSlotDisplay>> CHEMICAL_TAG = SLOT_DISPLAY_TYPES.register("chemical_tag",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.codec(MekanismRegistries.Keys.CHEMICAL).fieldOf(SerializationConstants.TAG).forGetter(ChemicalTagSlotDisplay::tag)
          ).apply(instance, ChemicalTagSlotDisplay::new)), StreamCodec.composite(
                TagKey.streamCodec(MekanismRegistries.Keys.CHEMICAL), ChemicalTagSlotDisplay::tag,
                ChemicalTagSlotDisplay::new
          )));


    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChanceSlotDisplay>> CHANCE = SLOT_DISPLAY_TYPES.register("chance",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.BASE).forGetter(ChanceSlotDisplay::source),
                //Only allow doubles > 0
                Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE).fieldOf(SerializationConstants.CHEMICAL_OUTPUT).forGetter(ChanceSlotDisplay::chance)
          ).apply(instance, ChanceSlotDisplay::new)), StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, ChanceSlotDisplay::source,
                ByteBufCodecs.DOUBLE, ChanceSlotDisplay::chance,
                ChanceSlotDisplay::new
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<WithAmountSlotDisplay>> WITH_AMOUNT = SLOT_DISPLAY_TYPES.register("with_amount",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.BASE).forGetter(WithAmountSlotDisplay::source),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf(SerializationConstants.AMOUNT).forGetter(WithAmountSlotDisplay::amount)
          ).apply(instance, WithAmountSlotDisplay::new)), StreamCodec.composite(
                SlotDisplay.STREAM_CODEC, WithAmountSlotDisplay::source,
                ByteBufCodecs.VAR_INT, WithAmountSlotDisplay::amount,
                WithAmountSlotDisplay::new
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalTankSlotDisplay>> CHEMICAL_TANK = SLOT_DISPLAY_TYPES.register("chemical_tank",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.BASE).forGetter(ChemicalTankSlotDisplay::chemicalSource)
          ).apply(instance, ChemicalTankSlotDisplay::new)), SlotDisplay.STREAM_CODEC.map(
                ChemicalTankSlotDisplay::new, ChemicalTankSlotDisplay::chemicalSource
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalConversionSlotDisplay>> CHEMICAL_CONVERSION = SLOT_DISPLAY_TYPES.register("chemical_conversion",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.BASE).forGetter(ChemicalConversionSlotDisplay::chemicalSource)
          ).apply(instance, ChemicalConversionSlotDisplay::new)), SlotDisplay.STREAM_CODEC.map(
                ChemicalConversionSlotDisplay::new, ChemicalConversionSlotDisplay::chemicalSource
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalSolidTagSlotDisplay>> CHEMICAL_SOLID_TAG = SLOT_DISPLAY_TYPES.register("chemical_solid_tag",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                SlotDisplay.CODEC.fieldOf(SerializationConstants.BASE).forGetter(ChemicalSolidTagSlotDisplay::chemicalSource)
          ).apply(instance, ChemicalSolidTagSlotDisplay::new)), SlotDisplay.STREAM_CODEC.map(
                ChemicalSolidTagSlotDisplay::new, ChemicalSolidTagSlotDisplay::chemicalSource
          )));
}