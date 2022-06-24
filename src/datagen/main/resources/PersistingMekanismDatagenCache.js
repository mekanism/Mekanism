var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')

function initializeCoreMod() {
  return {
    'Persisting Mekanism Datagen Cache': {
      'target': {
        'type': 'METHOD',
        'class': 'net.minecraft.data.DataGenerator',
        'methodName': ASMAPI.mapMethod('m_123917_'),
        'methodDesc': '()V'
      },
      'transformer': function (method) {
        var Opcodes = Java.type('org.objectweb.asm.Opcodes');
        var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
        var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
        var target = ASMAPI.findFirstInstruction(
            method,
            Opcodes.ASTORE
        );
        var newInstructions = new InsnList();
        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        newInstructions.add(ASMAPI.buildMethodCall(
            "mekanism/common/PersistingDisabledProvidersProvider",
            "captureGlobalCache",
            "(Lnet/minecraft/data/HashCache;)V",
            ASMAPI.MethodType.STATIC)
        );
        method.instructions.insert(target, newInstructions);
        return method;
      }
    }
  }
}