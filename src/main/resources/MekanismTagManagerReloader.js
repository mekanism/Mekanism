function initializeCoreMod() {
  return {
    'Mekanism Tag Manager Reload Handler': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.resources.DataPackRegistries',
        'methodName': '<init>',
        'methodDesc': '(Lnet/minecraft/command/Commands$EnvironmentType;I)V'
      },
      'transformer': function (method) {
        var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
        var target = ASM.findFirstMethodCall(method,
            ASM.MethodType.INTERFACE,
            "net/minecraft/resources/IReloadableResourceManager",
            ASM.mapMethod("func_219534_a"),// addReloadListener
            "(Lnet/minecraft/resources/IFutureReloadListener;)V"
        );
        var newInstructions = new InsnList();
        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        newInstructions.add(new FieldInsnNode(
            Opcodes.GETFIELD,
            "net/minecraft/resources/DataPackRegistries",
            ASM.mapField("field_240952_b_"), //resource manager
            "Lnet/minecraft/resources/IReloadableResourceManager;"
        ));
        newInstructions.add(ASM.buildMethodCall(
            "mekanism/common/tags/TagManagerReloadHelper",
            "addListener",
            "(Lnet/minecraft/resources/IReloadableResourceManager;)V",
            ASM.MethodType.STATIC)
        );
        method.instructions.insert(target, newInstructions);
        return method;
      }
    }
  }
}