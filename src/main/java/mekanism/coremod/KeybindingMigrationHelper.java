package mekanism.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

/**
 * Allows us to run a mini datafixer on the GameSettings to migrate the old keybind options (before we correctly used the translation key).
 *
 * Hooks into {@link net.minecraft.client.settings.GameSettings#dataFix(net.minecraft.nbt.CompoundNBT)} and always calls our static method datafixer {@link
 * mekanism.common.fixers.KeybindingFixer#runFix(net.minecraft.nbt.CompoundNBT)}
 */
@SuppressWarnings("unused")//coremod land
public class KeybindingMigrationHelper implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger("Mekanism KeybindingMigrationHelper");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals("net.minecraft.client.settings.GameSettings")) {
            return basicClass;
        }
        ClassWriter cw = new ClassWriter(0);
        new ClassReader(basicClass).accept(new Visitor(cw), ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

    private static class Visitor extends ClassVisitor {

        private static final String DATAFIX_SIG = "(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;";

        //net.minecraft.client.settings.GameSettings func_189988_a(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT; #dataFix
        private String datafixMethodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName("net/minecraft/client/settings/GameSettings", "func_189988_a", DATAFIX_SIG);

        Visitor(ClassVisitor parent) {
            super(Opcodes.ASM5, parent);
            LOGGER.debug("Looking for " + datafixMethodName + DATAFIX_SIG);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals(datafixMethodName) && desc.equals(DATAFIX_SIG)) {
                LOGGER.info("Patching GameSettings.datafix");
                return new GeneratorAdapter(Opcodes.ASM5, visitor, access, name, desc) {
                    @Override
                    public void visitCode() {
                        super.visitCode();
                        loadArg(0);
                        invokeStatic(Type.getObjectType("mekanism/common/fixers/KeybindingFixer"), new Method("runFix", "(Lnet/minecraft/nbt/CompoundNBT;)V"));
                    }
                };
            }
            return visitor;
        }
    }
}