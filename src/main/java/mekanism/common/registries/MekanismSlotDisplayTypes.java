package mekanism.common.registries;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalSlotDisplay;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackSlotDisplay;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalTagSlotDisplay;
import mekanism.common.Mekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismSlotDisplayTypes {

    public static final DeferredRegister<SlotDisplay.Type<?>> SLOT_DISPLAY_TYPES = DeferredRegister.create(Registries.SLOT_DISPLAY, Mekanism.MODID);

    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalSlotDisplay>> CHEMICAL_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register("chemical",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryFixedCodec.create(MekanismAPI.CHEMICAL_REGISTRY_NAME).fieldOf(SerializationConstants.CHEMICAL).forGetter(ChemicalSlotDisplay::chemical)
          ).apply(instance, ChemicalSlotDisplay::new)), StreamCodec.composite(
                ByteBufCodecs.holderRegistry(MekanismAPI.CHEMICAL_REGISTRY_NAME), ChemicalSlotDisplay::chemical, ChemicalSlotDisplay::new
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalStackSlotDisplay>> CHEMICAL_STACK_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register("chemical_stack",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                ChemicalStackTemplate.CODEC.fieldOf(SerializationConstants.CHEMICAL).forGetter(ChemicalStackSlotDisplay::stack)
          ).apply(instance, ChemicalStackSlotDisplay::new)), StreamCodec.composite(
                ChemicalStackTemplate.STREAM_CODEC, ChemicalStackSlotDisplay::stack, ChemicalStackSlotDisplay::new
          )));
    public static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalTagSlotDisplay>> CHEMICAL_TAG_SLOT_DISPLAY = SLOT_DISPLAY_TYPES.register("chemical_tag",
          () -> new SlotDisplay.Type<>(RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.codec(MekanismAPI.CHEMICAL_REGISTRY_NAME).fieldOf(SerializationConstants.TAG).forGetter(ChemicalTagSlotDisplay::tag)
          ).apply(instance, ChemicalTagSlotDisplay::new)), StreamCodec.composite(
                TagKey.streamCodec(MekanismAPI.CHEMICAL_REGISTRY_NAME), ChemicalTagSlotDisplay::tag, ChemicalTagSlotDisplay::new
          )));
}