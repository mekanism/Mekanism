package codechicken.multipart.asm

import scala.collection.mutable.{Map => MMap, ListBuffer => MList, Set => MSet}
import java.util.{Set => JSet}
import scala.collection.JavaConversions._
import org.objectweb.asm.tree._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.ClassReader
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.Type
import org.objectweb.asm.MethodVisitor
import Type._
import codechicken.lib.asm.ASMHelper._
import codechicken.lib.asm.ObfMapping
import java.io.File
import java.io.PrintWriter
import ScalaSignature._
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import net.minecraft.launchwrapper.LaunchClassLoader
import codechicken.multipart.handler.MultipartProxy
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper

object DebugPrinter
{
    val debug = MultipartProxy.config.getTag("debug_asm").getBooleanValue(!ObfMapping.obfuscated)
    
    private var permGenUsed = 0
    val dir = new File("asm/multipart")
    if(debug)
    {
        if(!dir.exists)
            dir.mkdirs()
        for(file <- dir.listFiles)
            file.delete
    }
    
    def dump(name:String, bytes:Array[Byte])
    {
        if(!debug) return
        
        val fileout = new File(dir, name.replace('/', '#')+".txt")
        val pout = new PrintWriter(fileout)
        
        new ClassReader(bytes).accept(new TraceClassVisitor(null, new Textifier(), pout), 0)
        pout.close()
    }
    
    def defined(name:String, bytes:Array[Byte])
    {
        if((permGenUsed+bytes.length)/16000 != permGenUsed/16000)
            log((permGenUsed+bytes.length)+" bytes of permGen has been used by ASMMixinCompiler")
        
        permGenUsed += bytes.length
    }
    
    def log(msg:String) = if(debug) println(msg)
}

object ASMMixinCompiler
{
    import StackAnalyser.width
    
    val cl = getClass.getClassLoader.asInstanceOf[LaunchClassLoader]
    val m_defineClass = classOf[ClassLoader].getDeclaredMethod("defineClass", classOf[Array[Byte]], Integer.TYPE, Integer.TYPE)
    val m_runTransformers = classOf[LaunchClassLoader].getDeclaredMethod("runTransformers", classOf[String], classOf[String], classOf[Array[Byte]])
    val f_transformerExceptions = classOf[LaunchClassLoader].getDeclaredField("transformerExceptions")
    m_defineClass.setAccessible(true)
    m_runTransformers.setAccessible(true)
    f_transformerExceptions.setAccessible(true)
    
    private val traitByteMap = MMap[String, Array[Byte]]()
    private val mixinMap = MMap[String, MixinInfo]()
    
    def define(name:String, bytes:Array[Byte]) = 
    {
        internalDefine(name, bytes)
        DebugPrinter.defined(name, bytes)

        try {
            m_defineClass.invoke(cl, bytes, 0:Integer, bytes.length:Integer).asInstanceOf[Class[_]]
        } catch {
            case link:LinkageError if link.getMessage.contains("duplicate") =>
                throw new IllegalStateException("class with name: "+name+" already loaded. Do not reference your mixin classes before registering them with MultipartGenerator", link)
        }
    }
    
    getBytes("cpw/mods/fml/common/asm/FMLSanityChecker")
    
    def getBytes(name:String):Array[Byte] =
    {
        val jName = name.replace('/', '.')
        if(jName.equals("java.lang.Object"))
            return null
        
        def useTransformers = f_transformerExceptions.get(cl).asInstanceOf[JSet[String]]
                .find(jName.startsWith).isEmpty
        
        val obfName = FMLDeobfuscatingRemapper.INSTANCE.unmap(name).replace('/', '.')
        val bytes = cl.getClassBytes(obfName)
        if(bytes != null && useTransformers)
            return m_runTransformers.invoke(cl, jName, obfName, bytes).asInstanceOf[Array[Byte]]
            
        return bytes
    }
    
    def internalDefine(name:String, bytes:Array[Byte])
    {
        traitByteMap.put(name.replace('.', '/'), bytes)
        BaseNodeInfo.clear(name)
        DebugPrinter.dump(name, bytes)
    }
    
    def classNode(name:String) = traitByteMap.getOrElseUpdate(name.replace('.', '/'), getBytes(name.replace('.', '/'))) match {
        case null => null
        case v => createClassNode(v, ClassReader.EXPAND_FRAMES)
    }
    
    def isScala(cnode:ClassNode) = ScalaSigReader.ann(cnode).isDefined
    
    def isTrait(cnode:ClassNode) = 
    {
        val csym:ClassSymbol = ScalaSigReader.read(ScalaSigReader.ann(cnode).get).evalT(0)
        csym.isTrait && !csym.isInterface
    }
    
    def getMixinInfo(name:String) = mixinMap.get(name)
    
    case class FieldMixin(name:String, desc:String, access:Int)
    {
        def accessName(owner:String) = if((access & ACC_PRIVATE) != 0)
                owner.replace('/', '$')+"$$"+name
            else
                name
    }
    
    case class MixinInfo(name:String, parent:String, fields:Seq[FieldMixin], 
            methods:Seq[MethodNode], supers:Map[String, String])
    {
        def tname = name+"$class"
    }
    
    object BaseNodeInfo
    {
        private val baseNodeMap = MMap[String, BaseNodeInfo]()
        
        def clear(name:String) = baseNodeMap.remove(name)
        
        def getNodeInfo(name:String) = baseNodeMap.getOrElseUpdate(name, new BaseNodeInfo(name))
        
        private def getNodeInfo(clazz:ClassInfoSource) = clazz match {
            case null => null
            case v => baseNodeMap.getOrElseUpdate(v.name, new BaseNodeInfo(v))
        }
        
        case class MethodNodeInfo(owner:String, m:MethodInfoSource)
        {
            def name = m.name
            def desc = m.desc
        }
        
        trait MethodInfoSource
        {
            def name:String
            def desc:String
            def isPrivate:Boolean
            def isAbstract:Boolean
        }
        
        trait ClassInfoSource
        {
            def name:String
            def superClass:Option[BaseNodeInfo]
            def interfaces:Seq[BaseNodeInfo]
            def methods:Seq[MethodInfoSource]
        }
        
        implicit def JClassInfoSource(clazz:Class[_]) = if(clazz == null) null else new JClassInfoSource(clazz)
        class JClassInfoSource(clazz:Class[_]) extends ClassInfoSource
        {
            case class JMethodInfoSource(method:Method) extends MethodInfoSource
            {
                def name = method.getName
                def desc = getType(method).getDescriptor
                def isPrivate = Modifier.isPrivate(method.getModifiers)
                def isAbstract = Modifier.isAbstract(method.getModifiers)
            }
            
            def name = clazz.getName.replace('.', '/')
            def superClass = Option(getNodeInfo(clazz.getSuperclass))
            def interfaces = clazz.getInterfaces.map(getNodeInfo(_))
            def methods = clazz.getMethods.map(JMethodInfoSource(_))
        }
        
        implicit def ClassNodeInfoSource(cnode:ClassNode) = if(cnode == null) null else new ClassNodeInfoSource(cnode)
        class ClassNodeInfoSource(cnode:ClassNode) extends ClassInfoSource
        {
            case class MethodNodeInfoSource(mnode:MethodNode) extends MethodInfoSource
            {
                def name = mnode.name
                def desc = mnode.desc
                def isPrivate = (mnode.access & ACC_PRIVATE) != 0
                def isAbstract = (mnode.access & ACC_ABSTRACT) != 0
            }
            
            def name = cnode.name
            def superClass = Option(getNodeInfo(cnode.superName))
            def interfaces = cnode.interfaces match {
                case null => Seq()
                case v => v.map(getNodeInfo)
            }
            def methods = cnode.methods.map(MethodNodeInfoSource(_))
        }
        
        class ScalaNodeInfoSource(cnode:ClassNode) extends ClassNodeInfoSource(cnode)
        {
            val sig = ScalaSigReader.read(ScalaSigReader.ann(cnode).get)
            val csym = sig.evalT(0):ClassSymbol
            
            override def superClass = Option(getNodeInfo(csym.jParent(sig)))
            override def interfaces = csym.jInterfaces(sig).map(getNodeInfo)
        }
        
        def classInfo(name:String):ClassInfoSource = name match {
            case null => null
            case s => classNode(s) match {
                case null => cl.findClass(s.replace('/', '.'))
                case v if isScala(v) => new ScalaNodeInfoSource(v)
                case v => v
            }
        }
    }
    
    import BaseNodeInfo._
    class BaseNodeInfo(val clazz:ClassInfoSource)
    {
        def this(name:String) = this(classInfo(name))
        
        val publicmethods = exportedMethods(clazz)//map of nameDesc to owner+method
        
        def exportedMethods(o:ClassInfoSource):Map[String, MethodNodeInfo] =
        {
            val eMethods = o.methods.filter(!_.isPrivate)//defined accessible methods
                    .map(m => (m.name+m.desc, MethodNodeInfo(o.name, m))).toMap
            
            eMethods ++ (o.superClass++o.interfaces).flatMap(_.publicmethods)
        }
    }
    
    def finishBridgeCall(mv:MethodVisitor, mvdesc:String, opcode:Int, owner:String, name:String, desc:String)
    {
        val args = getArgumentTypes(mvdesc)
        val ret = getReturnType(mvdesc)
        var localIndex = 1
        args.foreach{arg =>
            mv.visitVarInsn(arg.getOpcode(ILOAD), localIndex)
            localIndex+=width(arg)
        }
        mv.visitMethodInsn(opcode, owner, name, desc)
        mv.visitInsn(ret.getOpcode(IRETURN))
        mv.visitMaxs(Math.max(width(args)+1, width(ret)), width(args)+1)
    }
    
    def writeBridge(mv:MethodVisitor, mvdesc:String, opcode:Int, owner:String, name:String, desc:String)
    {
        mv.visitVarInsn(ALOAD, 0)
        finishBridgeCall(mv, mvdesc, opcode, owner, name, desc)
    }
    
    def writeStaticBridge(mv:MethodNode, mname:String, t:MixinInfo) = 
        writeBridge(mv, mv.desc, INVOKESTATIC, t.tname, mname, staticDesc(t.name, mv.desc))
    
    def mixinClasses(name:String, superClass:String, traits:Seq[String]) =
    {
        val traitInfos = traits.map(t => mixinMap(t.replace('.', '/')))
        
        val cnode = new ClassNode()
        cnode.visit(V1_6, ACC_PUBLIC, name, null, superClass.replace('.', '/'), traitInfos.map(_.name).toArray[String])
        
        val minit = cnode.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
        minit.visitVarInsn(ALOAD, 0)
        minit.visitMethodInsn(INVOKESPECIAL, cnode.superName, "<init>", "()V")
        
        val prevInfos = MList[MixinInfo]()
        
        traitInfos.foreach{ t =>
            minit.visitVarInsn(ALOAD, 0)
            minit.visitMethodInsn(INVOKESTATIC, t.tname, "$init$", "(L"+t.name+";)V")
            
            t.fields.foreach{ f =>
                val fv = cnode.visitField(ACC_PRIVATE, f.accessName(t.name), f.desc, null, null).asInstanceOf[FieldNode]
                
                val ftype = getType(fv.desc)
                var mv = cnode.visitMethod(ACC_PUBLIC, fv.name, "()"+f.desc, null, null)
                mv.visitVarInsn(ALOAD, 0)
                mv.visitFieldInsn(GETFIELD, name, fv.name, fv.desc)
                mv.visitInsn(ftype.getOpcode(IRETURN))
                mv.visitMaxs(1, 1)
                
                mv = cnode.visitMethod(ACC_PUBLIC, fv.name+"_$eq", "("+f.desc+")V", null, null)
                mv.visitVarInsn(ALOAD, 0)
                mv.visitVarInsn(ftype.getOpcode(ILOAD), 1)
                mv.visitFieldInsn(PUTFIELD, name, fv.name, fv.desc)
                mv.visitInsn(RETURN)
                mv.visitMaxs(width(ftype)+1, width(ftype)+1)
            }
            
            t.supers.foreach{ s => 
                val (name, desc) = seperateDesc(s._1)
                val mv = cnode.visitMethod(ACC_PUBLIC, t.name.replace('/', '$')+"$$super$"+name, desc, null, null).asInstanceOf[MethodNode]
                
                prevInfos.reverse.find(t => t.supers.contains(s._1)) match {//each super goes to the one before
                    case Some(st) => writeStaticBridge(mv, name, st)
                    case None => writeBridge(mv, desc, INVOKESPECIAL, s._2, name, desc)
                }
            }
            
            prevInfos+=t
        }
        
        val methodSigs = MSet[String]()
        traitInfos.reverse.foreach{ t => //last trait gets first pick on methods
            t.methods.foreach{ m => 
                if(!methodSigs(m.name+m.desc))
                {
                    val mv = cnode.visitMethod(ACC_PUBLIC, m.name, m.desc, null, Array(m.exceptions:_*)).asInstanceOf[MethodNode]
                    copy(m, mv)
                    mv.instructions = new InsnList()
                    mv.tryCatchBlocks.clear()
                    
                    writeStaticBridge(mv, m.name, t)
                    methodSigs+=m.name+m.desc
                }
            }
        }
        
        minit.visitInsn(RETURN)
        minit.visitMaxs(1, 1)
        
        define(cnode.name, createBytes(cnode, 0))
    }
    
    def seperateDesc(nameDesc:String) = {
        val n = nameDesc.indexOf('(')
        (nameDesc.substring(0, n), nameDesc.substring(n))
    }
    
    def staticDesc(owner:String, desc:String) = 
    {
        val descT = getMethodType(desc)
        getMethodDescriptor(descT.getReturnType, getType("L"+owner+";")+:descT.getArgumentTypes : _*)
    }
    
    def getSuper(minsn:MethodInsnNode, stack:StackAnalyser):Option[String] =
    {
        import StackAnalyser._
        
        if(minsn.getOpcode != INVOKESPECIAL) return None
        val oname = stack.owner.getInternalName
        
        if(minsn.owner.equals(oname)) return None//private this
        
        stack.peek(Type.getType(minsn.desc).getArgumentTypes.length) match {
            case Load(This(o)) =>
            case _ => return None//have to be invoked on this
        }
        
        getSuper(minsn.name+minsn.desc, oname)
    }
    
    def getSuper(nameDesc:String, cname:String) = 
        getNodeInfo(cname).clazz.superClass match {
            case None => None//no super class
            case Some(o) => o.publicmethods.get(nameDesc) match {
                case None => None//no method with sig in super
                case Some(m) => if(m.m.isAbstract) 
                        None //abstract can't be called for supers
                    else 
                        Some(m.owner)//congrats, that's a super
            }
        }
    
    def registerJavaTrait(cnode:ClassNode)
    {
        if((cnode.access & ACC_INTERFACE) != 0)
            throw new IllegalArgumentException("Cannot register java interface "+cnode.name+" as a multipart trait. Try register passThroughInterface")
        if((cnode.access & ACC_ABSTRACT) != 0)
            throw new IllegalArgumentException("Cannot register abstract class "+cnode.name+" as a multipart trait")
        if(!cnode.innerClasses.isEmpty)
            throw new IllegalArgumentException("Inner classes are not permitted for "+cnode.name+" as a multipart trait. Use scala")
        
        val inode = new ClassNode()//impl node
        inode.visit(V1_6, ACC_ABSTRACT|ACC_PUBLIC, cnode.name+"$class", null, "java/lang/Object", null)
        inode.sourceFile = cnode.sourceFile
        
        val fields = cnode.fields.map(f => (f.name, FieldMixin(f.name, f.desc, f.access))).toMap
        val supers = MMap[String, String]()//nameDesc to super owner
        val methods = MList[MethodNode]()
        val methodSigs = cnode.methods.map(m => m.name+m.desc).toSet
        
        val tnode = new ClassNode()//trait node (interface)
        tnode.visit(V1_6, ACC_INTERFACE|ACC_ABSTRACT|ACC_PUBLIC, cnode.name, null, "java/lang/Object", Array(cnode.interfaces:_*))
        
        def fname(name:String) = fields(name).accessName(cnode.name)
        
        fields.values.foreach{ fnode =>
            tnode.visitMethod(ACC_PUBLIC|ACC_ABSTRACT, fname(fnode.name), "()"+fnode.desc, null, null)
            tnode.visitMethod(ACC_PUBLIC|ACC_ABSTRACT, fname(fnode.name)+"_$eq", "("+fnode.desc+")V", null, null)
        }
        
        def superInsn(minsn:MethodInsnNode) =
        {
            val bridgeName = cnode.name.replace('/', '$')+"$$super$"+minsn.name
            if(!supers.contains(minsn.name+minsn.desc))
            {
                tnode.visitMethod(ACC_PUBLIC|ACC_ABSTRACT, bridgeName, minsn.desc, null, null)
                supers.put(minsn.name+minsn.desc, minsn.owner)
            }
            new MethodInsnNode(INVOKEINTERFACE, cnode.name, bridgeName, minsn.desc)
        }
        
        def staticClone(mnode:MethodNode, name:String, access:Int) = 
        {
            val mv = inode.visitMethod(access|ACC_STATIC, name, 
                    staticDesc(cnode.name, mnode.desc),
                    null, Array(mnode.exceptions:_*)).asInstanceOf[MethodNode]
            copy(mnode, mv)
            mv
        }
        
        def staticTransform(mnode:MethodNode, base:MethodNode)
        {
            val stack = new StackAnalyser(getType(cnode.name), base)
            val insnList = mnode.instructions
            var insn = insnList.getFirst
            
            def replace(newinsn:AbstractInsnNode)
            {
                insnList.insert(insn, newinsn)
                insnList.remove(insn)
                insn = newinsn
            }

            //transform
            while(insn != null)
            {
                insn match {
                    case finsn:FieldInsnNode => insn.getOpcode match
                    {
                        case GETFIELD => replace(new MethodInsnNode(INVOKEINTERFACE, cnode.name, 
                                fname(finsn.name), "()"+finsn.desc))
                        case PUTFIELD => replace(new MethodInsnNode(INVOKEINTERFACE, cnode.name, 
                                fname(finsn.name)+"_$eq", "("+finsn.desc+")V"))
                        case _ =>
                    }
                    case minsn:MethodInsnNode => insn.getOpcode match
                    {
                        case INVOKESPECIAL => 
                            if(getSuper(minsn, stack).isDefined)
                                replace(superInsn(minsn))
                        case INVOKEVIRTUAL =>
                            if(minsn.owner.equals(cnode.name)) {
                                if(methodSigs.contains(minsn.name+minsn.desc))//call the interface method
                                    minsn.setOpcode(INVOKEINTERFACE)
                                else {//cast to parent class and call
                                    val mType = Type.getMethodType(minsn.desc)
                                    val instanceEntry = stack.peek(width(mType.getArgumentTypes))
                                    insnList.insert(instanceEntry.insn, new TypeInsnNode(CHECKCAST, cnode.superName))
                                    minsn.owner = cnode.superName
                                }
                            }
                        case _ =>
                    }
                    case _ =>
                }
                stack.visitInsn(insn)
                insn = insn.getNext
            }
        }
        
        def convertMethod(mnode:MethodNode)
        {
            if(mnode.name.equals("<clinit>"))
                throw new IllegalArgumentException("Static initialisers are not permitted "+cnode.name+" as a multipart trait")
            
            if(mnode.name.equals("<init>"))
            {
                if(!mnode.desc.equals("()V"))
                    throw new IllegalArgumentException("Constructor arguments are not permitted "+cnode.name+" as a multipart trait")
                
                val mv = staticClone(mnode, "$init$", ACC_PUBLIC)
                def removeSuperConstructor()
                {
                    def throwI = throw new IllegalArgumentException("Invalid constructor insn sequence "+cnode.name)
                    
                    val insnList = mv.instructions
                    var insn = insnList.getFirst
                    var state = 0
                    while(insn != null && state < 2)
                    {
                        val next = insn.getNext
                        insn match {
                            case linsn:LabelNode =>
                            case linsn:LineNumberNode =>
                            case vinsn:VarInsnNode => 
                                if(state == 0)
                                {
                                    if(vinsn.getOpcode == ALOAD && vinsn.`var` == 0)
                                    {
                                        state = 1
                                        insnList.remove(insn)
                                    }
                                    else
                                        throwI
                                }
                                else
                                    throwI
                            case minsn:MethodInsnNode =>
                                if(state == 1)
                                {
                                    if(minsn.getOpcode == INVOKESPECIAL && minsn.name.equals("<init>") && 
                                            minsn.owner.equals(cnode.superName))
                                    {
                                        insnList.remove(insn)
                                        state = 2
                                    }
                                    else
                                        throwI
                                }
                                else
                                    throwI
                            case _ => throwI
                        }
                        insn = next
                    }
                    if(state != 2)
                        throwI
                }
                removeSuperConstructor()
                staticTransform(mv, mnode)
                return
            }
            
            if((mnode.access & ACC_PRIVATE) == 0)
            {
                val mv = tnode.visitMethod(ACC_PUBLIC|ACC_ABSTRACT, mnode.name, mnode.desc, null, Array(mnode.exceptions:_*))
                methods+=mv.asInstanceOf[MethodNode]
            }
            
            //convert that method!
            val access = if((mnode.access & ACC_PRIVATE) == 0) ACC_PUBLIC else ACC_PRIVATE
            val mv = staticClone(mnode, mnode.name, access)
            staticTransform(mv, mnode)
        }
        
        cnode.methods.foreach(convertMethod)

        define(inode.name, createBytes(inode, 0))
        define(tnode.name, createBytes(tnode, 0))
        
        mixinMap.put(tnode.name, MixinInfo(tnode.name, cnode.superName, 
                fields.values.toSeq,
                methods.toSeq, supers.toMap))
    }
    
    def registerScalaTrait(cnode:ClassNode)
    {
        for(i <- cnode.interfaces)
        {
            val inode = classNode(i)
            if(isScala(inode) && isTrait(inode))
                throw new IllegalArgumentException("Multipart trait "+cnode.name+" cannot extend other traits")
        }
        
        val fieldAccessors = MMap[String, MethodSymbol]()
        val fields = MList[MethodSymbol]()
        val methods = MList[MethodNode]()
        val supers = MMap[String, String]()
        
        val sig = ScalaSigReader.read(ScalaSigReader.ann(cnode).get)
        val csym:ClassSymbol = sig.evalT(0)
        for(i <- 0 until sig.table.length)
        {
            import ScalaSignature._
            
            val e = sig.table(i)
            if(e.id == 8)//method
            {
                val sym:MethodSymbol = sig.evalT(i)
                if(sym.isParam || !sym.owner.equals(csym)){}
                else if(sym.isAccessor)
                {
                    fieldAccessors.put(sym.name, sym)
                }
                else if(sym.isMethod)
                {
                    val desc = sym.jDesc(sig)
                    if(sym.name.contains("super$"))
                    {
                        val name = sym.name.substring(6)
                        supers.put(name+desc, getSuper(name+desc, cnode.name).get)
                    }
                    else if(!sym.name.equals("$init$") && !sym.isPrivate)
                    {
                        methods+=findMethod(new ObfMapping(cnode.name, sym.name, desc), cnode)
                    }
                }
                else
                {
                    fields+=sym
                }
            }
        }

        mixinMap.put(cnode.name, MixinInfo(cnode.name, csym.jParent(sig), 
                fields.map(sym => FieldMixin(sym.name.trim, getReturnType(sym.jDesc(sig)).getDescriptor, 
                        if(fieldAccessors(sym.name.trim).isPrivate) ACC_PRIVATE else ACC_PUBLIC)), 
                methods, supers.toMap))
    }
    
    
}