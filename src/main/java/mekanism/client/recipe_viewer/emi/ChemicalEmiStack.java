package mekanism.client.recipe_viewer.emi;

import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.providers.IChemicalProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

@NothingNullByDefault
public class ChemicalEmiStack extends EmiStack {

    private final Chemical chemical;

    public ChemicalEmiStack(ChemicalStack stack) {
        this(stack.getChemical(), stack.getAmount());
    }

    public ChemicalEmiStack(Chemical chemical, DataComponentPatch ignored, long amount) {
        this(chemical, amount);
    }

    public ChemicalEmiStack(Chemical chemical, long amount) {
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
            ResourceLocation texture = chemical.getIcon();
            int color = chemical.getTint();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
            float red = MekanismRenderer.getRed(color);
            float green = MekanismRenderer.getGreen(color);
            float blue = MekanismRenderer.getBlue(color);
            graphics.blit(x, y, 0, 16, 16, sprite, red, green, blue, 1);

        }

        if ((flags & RENDER_REMAINDER) != 0) {
            EmiRender.renderRemainderIcon(this, graphics, x, y);
        }
    }

    @Override
    public boolean isEmpty() {
        return chemical.isEmptyType() || amount == 0;
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public Chemical getKey() {
        return chemical;
    }

    @Override
    public ResourceLocation getId() {
        return chemical.getRegistryName();
    }

    @Override
    public List<Component> getTooltipText() {
        if (chemical.isEmptyType()) {
            return Collections.emptyList();
        }
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(getName());
        ChemicalUtil.addChemicalDataToTooltip(tooltips, chemical, false);
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
        return chemical.getTextComponent();
    }

    public static ChemicalEmiStack create(ChemicalStack stack) {
        return create(stack.getChemical(), stack.getAmount());
    }

    public static ChemicalEmiStack create(IChemicalProvider chemicalProvider, long amount) {
        return new ChemicalEmiStack(chemicalProvider.getChemical(), amount);
    }

    public static class GasEmiStack extends ChemicalEmiStack {

        public GasEmiStack(Chemical gas, DataComponentPatch ignored, long amount) {
            this(gas, amount);
        }

        public GasEmiStack(Chemical gas, long amount) {
            super(gas, amount);
        }

    }

    public static class InfusionEmiStack extends ChemicalEmiStack {

        public InfusionEmiStack(Chemical infuseType, DataComponentPatch ignored, long amount) {
            this(infuseType, amount);
        }

        public InfusionEmiStack(Chemical infuseType, long amount) {
            super(infuseType, amount);
        }

    }

    public static class PigmentEmiStack extends ChemicalEmiStack {

        public PigmentEmiStack(Chemical pigment, DataComponentPatch ignored, long amount) {
            this(pigment, amount);
        }

        public PigmentEmiStack(Chemical pigment, long amount) {
            super(pigment, amount);
        }

    }

    public static class SlurryEmiStack extends ChemicalEmiStack {

        public SlurryEmiStack(Chemical slurry, DataComponentPatch ignored, long amount) {
            this(slurry, amount);
        }

        public SlurryEmiStack(Chemical slurry, long amount) {
            super(slurry, amount);
        }

    }
}