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
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.text.EnumColor;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalEmiStack<CHEMICAL extends Chemical<CHEMICAL>> extends EmiStack {

    private final CHEMICAL chemical;

    protected ChemicalEmiStack(ChemicalStack<CHEMICAL> stack) {
        this(stack.getType(), stack.getAmount());
    }

    protected ChemicalEmiStack(CHEMICAL chemical, long amount) {
        this.chemical = chemical;
        this.amount = amount;
    }

    public boolean isHidden() {
        return chemical.isHidden();
    }

    protected abstract ChemicalEmiStack<CHEMICAL> construct(CHEMICAL chemical, long amount);

    @Override
    public EmiStack copy() {
        ChemicalEmiStack<CHEMICAL> e = construct(this.chemical, this.amount);
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

    @Nullable
    @Override
    public CompoundTag getNbt() {
        return null;
    }

    @Override
    public CHEMICAL getKey() {
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

    public static ChemicalEmiStack<?> create(ChemicalStack<?> stack) {
        return create(stack.getType(), stack.getAmount());
    }

    public static ChemicalEmiStack<?> create(Chemical<?> chemical, long amount) {
        ChemicalType type = ChemicalType.getTypeFor(chemical);
        return switch (type) {
            case GAS -> new GasEmiStack((Gas) chemical, amount);
            case INFUSION -> new InfusionEmiStack((InfuseType) chemical, amount);
            case PIGMENT -> new PigmentEmiStack((Pigment) chemical, amount);
            case SLURRY -> new SlurryEmiStack((Slurry) chemical, amount);
        };
    }

    public static class GasEmiStack extends ChemicalEmiStack<Gas> {

        public GasEmiStack(Gas gas, long amount) {
            super(gas, amount);
        }

        @Override
        protected GasEmiStack construct(Gas gas, long amount) {
            return new GasEmiStack(gas, amount);
        }
    }

    public static class InfusionEmiStack extends ChemicalEmiStack<InfuseType> {

        public InfusionEmiStack(InfuseType infuseType, long amount) {
            super(infuseType, amount);
        }

        @Override
        protected InfusionEmiStack construct(InfuseType infuseType, long amount) {
            return new InfusionEmiStack(infuseType, amount);
        }
    }

    public static class PigmentEmiStack extends ChemicalEmiStack<Pigment> {

        public PigmentEmiStack(Pigment pigment, long amount) {
            super(pigment, amount);
        }

        @Override
        protected PigmentEmiStack construct(Pigment pigment, long amount) {
            return new PigmentEmiStack(pigment, amount);
        }
    }

    public static class SlurryEmiStack extends ChemicalEmiStack<Slurry> {

        public SlurryEmiStack(Slurry slurry, long amount) {
            super(slurry, amount);
        }

        @Override
        protected SlurryEmiStack construct(Slurry slurry, long amount) {
            return new SlurryEmiStack(slurry, amount);
        }
    }
}