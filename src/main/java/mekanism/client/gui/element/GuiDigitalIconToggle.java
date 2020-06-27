package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.inventory.GuiComponents.IToggleEnum;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.audio.SimpleSound;

public class GuiDigitalIconToggle<TYPE extends Enum<TYPE> & IToggleEnum<TYPE>> extends GuiInnerScreen {

    private final Supplier<TYPE> typeSupplier;
    private final Consumer<TYPE> typeSetter;
    private final TYPE[] options;

    public GuiDigitalIconToggle(IGuiWrapper gui, int x, int y, int width, int height, Class<TYPE> enumClass, Supplier<TYPE> typeSupplier, Consumer<TYPE> typeSetter) {
        super(gui, x, y, width, height);
        this.typeSupplier = typeSupplier;
        this.typeSetter = typeSetter;
        this.options = enumClass.getEnumConstants();
        field_230693_o_ = true;
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.func_230431_b_(matrix, mouseX, mouseY, partialTicks);
        TYPE type = typeSupplier.get();
        minecraft.textureManager.bindTexture(type.getIcon());
        func_238463_a_(matrix, field_230690_l_ + 3, field_230691_m_ + 3, 0, 0, field_230688_j_ - 6, field_230689_k_ - 6, 6, 6);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        minecraft.getSoundHandler().play(SimpleSound.master(MekanismSounds.BEEP.get(), 1.0F));
        TYPE nextType = options[(typeSupplier.get().ordinal() + 1) % options.length];
        typeSetter.accept(nextType);
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        displayTooltip(matrix, typeSupplier.get().getTooltip(), mouseX, mouseY);
    }
}
