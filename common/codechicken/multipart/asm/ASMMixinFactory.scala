package codechicken.multipart.asm

import scala.collection.Seq
import codechicken.multipart.TileMultipart
import codechicken.multipart.TileMultipartClient
import scala.collection.JavaConversions._
import org.objectweb.asm.Opcodes._
import codechicken.lib.asm.CC_ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.tree._
import codechicken.lib.asm.ASMHelper._
import codechicken.lib.asm.ObfMapping
import codechicken.multipart.MultipartGenerator
import ASMMixinCompiler._

object ASMMixinFactory extends IMultipartFactory
{
    type TMClass = Class[_ <: TileMultipart]
    
    private var ugenid = 0
    private var generatorMap = Map[SuperSet, Constructor]()
    
    def uniqueName(prefix:String):String = {
        val ret = prefix+"$$"+ugenid
        ugenid += 1
        return ret
    }
    
    def simpleName(name:String) = name.substring(name.replace('/', '.').lastIndexOf('.')+1)
   
    abstract class Constructor
    {
        def generate():TileMultipart
    }
    
    object SuperSet
    {
        val TileMultipartType = classOf[TileMultipart].getName
        val TileMultipartClientType = classOf[TileMultipartClient].getName
        def apply(types:Seq[String], client:Boolean) = new SuperSet(types, client)
    }
    
    class SuperSet(types:Seq[String], client:Boolean)
    {
        import SuperSet._
        val set = baseType+:types.sorted
        
        def baseType = if(client) TileMultipartClientType else TileMultipartType
        
        override def equals(obj:Any) = obj match
        {
            case x:SuperSet => set == x.set
            case _ => false
        }
        
        override def hashCode = set.hashCode()
        
        def generate:TileMultipart = get.generate()
        
        def get = generatorMap.getOrElse(this, gen_sync())
        
        def gen_sync():Constructor = ASMMixinFactory.synchronized
        {
            return generatorMap.getOrElse(this, {
                val gen = generator()
                generatorMap = generatorMap+(this->gen)
                gen
            })
        }
        
        def generator():Constructor = 
        {
            val startTime = System.currentTimeMillis
            
            val cmpClass = if(!types.isEmpty)
                mixinClasses(uniqueName("TileMultipart_cmp"), set.head, set.drop(1))
            else
                cl.loadClass(baseType)
            
            MultipartGenerator.registerTileClass(cmpClass.asInstanceOf[TMClass], types.toSet)
            val c = constructor(cmpClass)
            DebugPrinter.log("Generation ["+types.mkString(", ")+"] took: "+(System.currentTimeMillis-startTime))
            return c
        }
    }
        
    def constructor(clazz:Class[_]):Constructor =
    {
        val name = uniqueName("TileMultipart_gen")
        val cw = new CC_ClassWriter(ASM4)
        val superName = classOf[Constructor].getName.replace('.', '/')
        val className = clazz.getName.replace('.', '/')
        var mv:MethodVisitor = null
        
        cw.visit(V1_6, ACC_PUBLIC|ACC_SUPER, name, null, superName, null)
        
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V")
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
        
        mv = cw.visitMethod(ACC_PUBLIC, "generate", "()Lcodechicken/multipart/TileMultipart;", null, null)
        mv.visitCode()
        mv.visitTypeInsn(NEW, className)
        mv.visitInsn(DUP)
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V")
        mv.visitInsn(ARETURN)
        mv.visitMaxs(2, 1)
        mv.visitEnd()
        
        cw.visitEnd()
        
        return define(name, cw.toByteArray).newInstance.asInstanceOf[Constructor]
    }
    
    private def autoCompleteJavaTrait(cnode:ClassNode)
    {
        if(!cnode.fields.isEmpty && findMethod(new ObfMapping(cnode.name, "copyFrom", "(Lcodechicken/multipart/TileMultipart;)V"), cnode) == null)
        {
            val mv = cnode.visitMethod(ACC_PUBLIC, "copyFrom", "(Lcodechicken/multipart/TileMultipart;)V", null, null)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/TileMultipart", "copyFrom", "(Lcodechicken/multipart/TileMultipart;)V")

            mv.visitVarInsn(ALOAD, 1)
            mv.visitTypeInsn(INSTANCEOF, cnode.name)
            val end = new Label()
            mv.visitJumpInsn(IFEQ, end)
            
            cnode.fields.foreach{ f =>
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ALOAD, 1)
                mv.visitFieldInsn(GETFIELD, cnode.name, f.name, f.desc)
                mv.visitFieldInsn(PUTFIELD, cnode.name, f.name, f.desc)
            }
            
            mv.visitLabel(end)
            mv.visitInsn(RETURN)
            mv.visitMaxs(2, 2)
        }
    }
    
    def registerTrait(s_interface:String, s_trait:String, client:Boolean)
    {
        val cnode = classNode(s_trait)
        if(cnode == null)
            throw new ClassNotFoundException(s_trait)
        
        def superClass = getMixinInfo(cnode.name) match {
            case Some(m) => m.parent
            case None => BaseNodeInfo.getNodeInfo(cnode.name).clazz.superClass.get.clazz.name
        }
        
        superClass match {
            case "codechicken/multipart/TileMultipartClient" => if(!client)
                throw new IllegalArgumentException("Multipart trait "+s_trait+" cannot implement TileMultipartClient on the server")
            case "codechicken/multipart/TileMultipart" => 
            case _ => throw new IllegalArgumentException("Multipart trait "+s_trait+" must implement TileMultipart or TileMultipartClient")
        }
        
        if(getMixinInfo(cnode.name).isDefined)
            return
        
        if(isScala(cnode) && isTrait(cnode))
        {
            registerScalaTrait(cnode)
        }
        else
        {
            autoCompleteJavaTrait(cnode)
            registerJavaTrait(cnode)
        }
    }
    
    def generatePassThroughTrait(s_interface:String):String =
    {
        def passThroughTraitName(iName:String) = 
        "T" + (if(iName.startsWith("I")) iName.substring(1) else iName)
        
        val tname = uniqueName(passThroughTraitName(simpleName(s_interface)))
        val vname = "impl"
        val iname = s_interface.replace('.', '/')
        val idesc = "L"+iname+";"
        
        val inode = classNode(s_interface)
        if(inode == null) {
            System.out.println("Unable to generate pass through trait for: "+s_interface+" class not found.")
            return null
        }
        if((inode.access&ACC_INTERFACE) == 0) throw new IllegalArgumentException(s_interface+" is not an interface.")
        
        val cw = new CC_ClassWriter(ASM4)
        var mv:MethodVisitor = null
        var fv:FieldVisitor = null
        
        cw.visit(V1_6, ACC_PUBLIC|ACC_SUPER, tname, null, "codechicken/multipart/TileMultipart", Array(iname))
        
        {
            fv = cw.visitField(ACC_PRIVATE, vname, idesc, null, null)
            fv.visitEnd()
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/TileMultipart", "<init>", "()V")
            mv.visitInsn(RETURN)
            mv.visitMaxs(1, 1)
            mv.visitEnd()
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "bindPart", "(Lcodechicken/multipart/TMultiPart;)V", null, null)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/TileMultipart", "bindPart", "(Lcodechicken/multipart/TMultiPart;)V")
            mv.visitVarInsn(ALOAD, 1)
            mv.visitTypeInsn(INSTANCEOF, iname)
            val l2 = new Label()
            mv.visitJumpInsn(IFEQ, l2)
            val l3 = new Label()
            mv.visitLabel(l3)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitTypeInsn(CHECKCAST, iname)
            mv.visitFieldInsn(PUTFIELD, tname, vname, idesc)
            mv.visitLabel(l2)
            mv.visitFrame(F_SAME, 0, null, 0, null)
            mv.visitInsn(RETURN)
            mv.visitMaxs(2, 2)
            mv.visitEnd()
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "partRemoved", "(Lcodechicken/multipart/TMultiPart;I)V", null, null)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitVarInsn(ILOAD, 2)
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/TileMultipart", "partRemoved", "(Lcodechicken/multipart/TMultiPart;I)V")
            mv.visitVarInsn(ALOAD, 1)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, tname, vname, idesc)
            val l2 = new Label()
            mv.visitJumpInsn(IF_ACMPNE, l2)
            val l3 = new Label()
            mv.visitLabel(l3)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitInsn(ACONST_NULL)
            mv.visitFieldInsn(PUTFIELD, tname, vname, idesc)
            mv.visitLabel(l2)
            mv.visitFrame(F_SAME, 0, null, 0, null)
            mv.visitInsn(RETURN)
            mv.visitMaxs(3, 3)
            mv.visitEnd()
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "canAddPart", "(Lcodechicken/multipart/TMultiPart;)Z", null, null)
            mv.visitCode()
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, tname, vname, idesc)
            val l1 = new Label()
            mv.visitJumpInsn(IFNULL, l1)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitTypeInsn(INSTANCEOF, iname)
            mv.visitJumpInsn(IFEQ, l1)
            val l2 = new Label()
            mv.visitLabel(l2)
            mv.visitInsn(ICONST_0)
            mv.visitInsn(IRETURN)
            mv.visitLabel(l1)
            mv.visitFrame(F_SAME, 0, null, 0, null)
            mv.visitVarInsn(ALOAD, 0)
            mv.visitVarInsn(ALOAD, 1)
            mv.visitMethodInsn(INVOKESPECIAL, "codechicken/multipart/TileMultipart", "canAddPart", "(Lcodechicken/multipart/TMultiPart;)Z")
            mv.visitInsn(IRETURN)
            mv.visitMaxs(2, 2)
            mv.visitEnd()
        }
        
        def methods(cnode:ClassNode):Map[String, MethodNode] =
        {
            val m = cnode.methods.map(m => (m.name+m.desc, m)).toMap
            if(cnode.interfaces != null)
                m++cnode.interfaces.flatMap(i => methods(classNode(i)))
            else
                m
        }
        
        def generatePassThroughMethod(m:MethodNode)
        {
            mv = cw.visitMethod(ACC_PUBLIC, m.name, m.desc, m.signature, Array(m.exceptions:_*))
            mv.visitVarInsn(ALOAD, 0)
            mv.visitFieldInsn(GETFIELD, tname, vname, idesc)
            finishBridgeCall(mv, m.desc, INVOKEINTERFACE, iname, m.name, m.desc)
        }
        
        methods(inode).values.foreach(generatePassThroughMethod)
        
        cw.visitEnd()
        internalDefine(tname, cw.toByteArray)
        registerTrait(s_interface, tname, false)
        return tname
    }
    
    def generateTile(types:Seq[String], client:Boolean) = SuperSet(types, client).generate
}