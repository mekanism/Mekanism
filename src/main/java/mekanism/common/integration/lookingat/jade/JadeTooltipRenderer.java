package mekanism.common.integration.lookingat.jade;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.SerializationConstants;
import mekanism.client.render.IFancyFontRenderer.TextAlignment;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.ILookingAtElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import mekanism.common.integration.lookingat.TextElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;

public class JadeTooltipRenderer<ACCESSOR extends Accessor<?>> implements IComponentProvider<ACCESSOR> {

    static final JadeTooltipRenderer<?> INSTANCE = new JadeTooltipRenderer<>();

    private static <B, L extends B, R extends B> MapCodec<B> alternativeElement(MapCodec<L> leftBase, MapCodec<R> rightBase,
          final Function<? super B, ? extends DataResult<? extends Either<L, R>>> from) {
        MapCodec<Either<L, R>> base = Codec.mapEither(leftBase, rightBase);
        return Codec.of(base.flatComap(from), base.map(Either::unwrap), () -> base + "[flatComapMapped]");
    }

    private static final MapCodec<ILookingAtElement> FLUID_OR_CHEMICAL_CODEC = alternativeElement(
          FluidElement.CODEC,
          ChemicalElement.CODEC,
          (ILookingAtElement element) -> switch (element) {
              case FluidElement fluidElement -> DataResult.success(Either.left(fluidElement));
              case ChemicalElement chemicalElement -> DataResult.success(Either.right(chemicalElement));
              default -> DataResult.error(() -> "Unknown Element Type, expected either fluid or chemical");
          }
    );
    private static final MapCodec<ILookingAtElement> ENERGY_OR_TEXT_CODEC = alternativeElement(
          EnergyElement.CODEC,
          TextElement.CODEC,
          (ILookingAtElement element) -> switch (element) {
              case EnergyElement energyElement -> DataResult.success(Either.left(energyElement));
              case TextElement textElement -> DataResult.success(Either.right(textElement));
              default -> DataResult.error(() -> "Unknown Element Type, expected either energy or text");
          }
    );
    static final Codec<ILookingAtElement> ELEMENT_CODEC = NeoForgeExtraCodecs.withAlternative(FLUID_OR_CHEMICAL_CODEC, ENERGY_OR_TEXT_CODEC).codec();

    @Override
    public Identifier getUid() {
        return JadeConstants.TOOLTIP_RENDERER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, ACCESSOR accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        Optional<ListTag> optionalData = data.getList(SerializationConstants.MEK_DATA);
        if (optionalData.isPresent()) {
            Component lastText = null;
            RegistryOps<Tag> registryOps = accessor.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
            //Copy the data we need and have from the server and pass it on to the tooltip rendering
            ListTag list = optionalData.get();
            for (int i = 0; i < list.size(); i++) {
                //TODO - 26.1: Make this non capturing if jade doesn't end up switching to value inputs/outputs and we have to stay with using compound tags
                Optional<ILookingAtElement> lookingAtElement = list.getCompound(i).flatMap(compound -> ELEMENT_CODEC.parse(registryOps, compound).result());
                if (lookingAtElement.isEmpty()) {
                    //Error deserializing, skip it
                    continue;
                }
                ILookingAtElement element = lookingAtElement.get();
                if (element instanceof TextElement(Component text)) {
                    if (lastText != null) {//Fallback to printing the last text
                        tooltip.add(lastText);
                    }
                    lastText = text;
                } else {
                    Identifier name = element.getID();
                    if (config.get(name)) {
                        tooltip.add(new MekElement(lastText, (LookingAtElement) element).tag(name));
                    }
                    lastText = null;
                }
            }
            if (lastText != null) {
                tooltip.add(lastText);
            }
        }
    }

    private static class MekElement extends Element {

        public Element create(@Nullable Component text, LookingAtElement element) {
            MekElement mekElement = new MekElement(text, element);
            int width = element.getWidth();
            int height = element.getHeight() + 2;
            if (text != null) {
                width = Math.max(width, 96);
                height += 14;
            }
            return mekElement.size(width, height);
        }

        @Nullable
        private final Component text;
        private final LookingAtElement element;

        private MekElement(@Nullable Component text, LookingAtElement element) {
            this.element = element;
            this.text = text;
        }

        @Override
        @Nullable
        public Component getNarration() {
            return text;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
            int x = getX();
            int y = getY();
            if (text != null) {
                element.drawScrollingString(guiGraphics, text, x,y + 3, TextAlignment.LEFT, 0xFFFFFF, 4, false);
                y += 13;
            }
            element.render(guiGraphics, x, y + 1);
        }
    }
}