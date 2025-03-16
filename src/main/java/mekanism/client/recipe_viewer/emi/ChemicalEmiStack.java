package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;

@NothingNullByDefault
public class ChemicalEmiStack extends EmiStack {

    private final Holder<Chemical> chemical;

    public ChemicalEmiStack(ChemicalStack stack) {
        this(stack.getChemicalHolder(), stack.getAmount());
    }

    @SuppressWarnings("removal")
    public ChemicalEmiStack(Chemical chemical, DataComponentPatch ignored, long amount) {
        this(chemical.getAsHolder(), amount);
        //TODO - 1.22: Re-evaluate usage and if we can just use the below, or what needs to happen
        // I am not sure if EmiRegistryAdapter even works with data pack registries in the first place,
        // so we might have to request a different entry point from Emi anyway
        //this(MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical), amount);
    }

    public ChemicalEmiStack(Holder<Chemical> chemical, long amount) {
        this.chemical = chemical;
        this.amount = amount;
    }

    @Override
    public EmiStack copy() {
        ChemicalEmiStack e = new ChemicalEmiStack(this.chemical, this.amount);
        e.setChance(this.chance);
        e.setRemainder(getRemainder().copy());
        e.comparison = this.comparison;
        return e;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float delta, int flags) {
        if ((flags & RENDER_ICON) != 0) {
            int color = MekanismRenderer.getTint(chemical);
            TextureAtlasSprite sprite = MekanismRenderer.getChemicalTexture(chemical);
            float red = MekanismRenderer.getRed(color);
            float green = MekanismRenderer.getGreen(color);
            float blue = MekanismRenderer.getBlue(color);
            graphics.blit(x, y, 0, 16, 16, sprite, red, green, blue, 1);

        }

        if ((flags & RENDER_REMAINDER) != 0) {
            EmiRender.renderRemainderIcon(this, graphics, x, y);
        }
    }

    public ChemicalStack getStack() {
        if (isEmpty()) {
            return ChemicalStack.EMPTY;
        }
        return new ChemicalStack(chemical, amount);
    }

    @Override
    public boolean isEmpty() {
        return amount <= 0 || chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY);
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Chemical getKey() {
        return chemical.value();
    }

    @Override
    public ResourceLocation getId() {
        ResourceKey<Chemical> key = chemical.getKey();
        return key == null ? MekanismAPI.CHEMICAL_REGISTRY.getDefaultKey() : key.location();
    }

    @Override
    public List<Component> getTooltipText() {
        if (isEmpty()) {
            return Collections.emptyList();
        }
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(getName());
        ChemicalStack stack = new ChemicalStack(chemical, amount > 1 ? amount : 1);
        stack.appendHoverText(RecipeViewerUtils.getRVTooltipContext(), tooltips, TooltipFlag.NORMAL);
        return tooltips;
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltips = getTooltipText().stream()
              .map(EmiTooltipComponents::of)
              .collect(Collectors.toList());
        if (amount > 1) {
            //TODO - 1.20.4: https://github.com/emilyploszaj/emi/issues/482
            tooltips.add(EmiTooltipComponents.of(MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, TextUtils.format(amount))));
        }

        EmiTooltipComponents.appendModName(tooltips, getId().getNamespace());
        tooltips.addAll(super.getTooltip());
        return tooltips;
    }

    @Override
    public Component getName() {
        return TextComponentUtil.build(chemical);
    }
}