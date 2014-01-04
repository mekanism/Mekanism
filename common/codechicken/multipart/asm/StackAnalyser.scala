package codechicken.multipart.asm

import scala.collection.mutable.{Map => MMap, ListBuffer => MList}
import org.objectweb.asm.tree._
import org.objectweb.asm.Opcodes._
import org.objectweb.asm.Type
import org.objectweb.asm.Type._
import scala.collection.JavaConversions._

object StackAnalyser
{
    def width(t:Type):Int = t.getSize
    def width(s:String):Int = width(Type.getType(s))
    def width(it:Iterable[Type]):Int = it.foldLeft(0)(_+width(_))
    
    abstract class StackEntry(implicit val insn:AbstractInsnNode)
    {
        def getType:Type
    }
    abstract class LocalEntry
    {
        def getType:Type
    }

    case class This(owner:Type) extends LocalEntry
    {
        def getType = owner
    }
    case class Param(i:Int, t:Type) extends LocalEntry
    {
        def getType = t
    }
    case class Store(e:StackEntry)(implicit val insn:AbstractInsnNode) extends LocalEntry
    {
        def getType = e.getType
    }

    case class Const(c:Any)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = c match {
            case o:Byte => BYTE_TYPE
            case o:Short => SHORT_TYPE
            case o:Int => INT_TYPE
            case o:Long => LONG_TYPE
            case o:Float => FLOAT_TYPE
            case o:Double => DOUBLE_TYPE
            case o:Char => CHAR_TYPE
            case o:Boolean => BOOLEAN_TYPE
            case o:String => getObjectType("java/lang/String")
            case null => getObjectType("java/lang/Object")
            case _ => throw new IllegalArgumentException("Unknown const "+c)
        }
    }
    case class Load(e:LocalEntry)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = e.getType
    }
    case class UnaryOp(op:Int, e:StackEntry)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = e.getType
    }
    case class BinaryOp(op:Int, e2:StackEntry, e1:StackEntry)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = e1.getType
    }
    case class PrimitiveCast(e:StackEntry, t:Type)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = t
    }
    case class ReturnAddress()(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = INT_TYPE
    }
    case class GetField(obj:StackEntry, field:FieldInsnNode)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = Type.getType(field.desc)
    }
    case class Invoke(op:Int, params:Array[StackEntry], obj:StackEntry, method:MethodInsnNode)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = Type.getMethodType(method.desc).getReturnType
    }
    case class New(t:Type)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = t
    }
    case class NewArray(len:StackEntry, t:Type)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = t
    }
    case class ArrayLength(array:StackEntry)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = INT_TYPE
    }
    case class ArrayLoad(index:StackEntry, e:StackEntry)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = e.getType.getElementType
    }
    case class Cast(obj:StackEntry, t:Type)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = t
    }
    case class NewMultiArray(sizes:Array[StackEntry], t:Type)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = t
    }
    case class CaughtException(t:Type)(implicit insn:AbstractInsnNode) extends StackEntry
    {
        def getType = t
    }
}

class StackAnalyser(val owner:Type, val m:MethodNode)
{
    import StackAnalyser._
    
    val stack = MList[StackEntry]()
    val locals = MList[LocalEntry]()
    private val catchHandlers = MMap[LabelNode, TryCatchBlockNode]()
    
    {
        if((m.access & ACC_STATIC) == 0)
            pushL(This(owner))
    
        val ptypes = getArgumentTypes(m.desc)
        for(i <- 0 until ptypes.length)
            pushL(Param(i, ptypes(i)))
            
        m.tryCatchBlocks.foreach(b => catchHandlers.put(b.handler, b))
    }
    
    def pushL(entry:LocalEntry) = setL(locals.size, entry)

    def setL(i:Int, entry:LocalEntry) =
    {
        while(i+entry.getType.getSize > locals.size) locals += null
        locals(i) = entry
        if(entry.getType.getSize == 2)
            locals(i+1) = entry
    }
    
    def push(entry:StackEntry) = insert(0, entry)

    def _pop(i:Int = 0) = stack.remove(stack.size-i-1)

    def pop(i:Int = 0) = 
    {
        val e = _pop(i)
        if(e.getType.getSize == 2)
        {
            if(peek(i) != e) throw new IllegalStateException("Wide stack entry elems don't match ("+e+","+peek(i))
            _pop(i)
        }
        e
    }
    
    def peek(i:Int = 0) = stack(stack.size-i-1)
    
    def insert(i:Int, entry:StackEntry) 
    {
        if(entry.getType.getSize == 0)
            return
        stack.insert(stack.size-i, entry)
        if(entry.getType.getSize == 2)
            stack.insert(stack.size-i, entry)
    }
    
    def popArgs(desc:String) =
    {
        val t = getType(desc)
        val args = new Array[StackEntry](t.getArgumentTypes.length)
        for(i <- 0 until args.length)
            args(args.length-i-1) = pop()
        args
    }
    
    def visitInsn(ainsn:AbstractInsnNode)
    {
        implicit val thisInsn = ainsn//passes to any StackEntry we create
        ainsn match {
            case insn:InsnNode => ainsn.getOpcode match
            {
                case ACONST_NULL => push(Const(null))
                case ICONST_M1 => push(Const(-1))
                case ICONST_0 => push(Const(0))
                case ICONST_1 => push(Const(1))
                case ICONST_2 => push(Const(2))
                case ICONST_3 => push(Const(3))
                case ICONST_4 => push(Const(4))
                case ICONST_5 => push(Const(5))
                case LCONST_0 => push(Const(0L))
                case LCONST_1 => push(Const(1L))
                case FCONST_0 => push(Const(0F))
                case FCONST_1 => push(Const(1F))
                case FCONST_2 => push(Const(2F))
                case DCONST_0 => push(Const(0D))
                case DCONST_1 => push(Const(1D))
                
                case i if i >= IALOAD && i <= SALOAD =>
                    push(ArrayLoad(pop(), pop()))
                case i if i >= IASTORE && i <= SASTORE =>
                    pop(); pop(); pop()
                
                case POP => _pop()
                case POP2 => _pop(); _pop()
                case DUP => push(peek())
                case DUP_X1 => insert(2, peek())
                case DUP_X2 => insert(3, peek())
                case DUP2 => push(peek(1)); push(peek(1))
                case DUP2_X1 => insert(3, peek(1)); insert(3, peek())
                case DUP2_X2 => insert(4, peek(1)); insert(4, peek())
                case SWAP => push(pop(1))
                
                case i if i >= IADD && i <= DREM =>
                    push(BinaryOp(i, pop(), pop()))
                case i if i >= INEG && i <= DNEG =>
                    push(UnaryOp(i, pop()))
                case i if i >= ISHL && i <= LXOR =>
                    push(BinaryOp(i, pop(), pop()))
                    
                case L2I|F2I|D2I => push(PrimitiveCast(pop(), DOUBLE_TYPE))
                case I2L|F2L|D2L => push(PrimitiveCast(pop(), LONG_TYPE))
                case I2F|L2F|D2F => push(PrimitiveCast(pop(), FLOAT_TYPE))
                case I2D|L2D|F2D => push(PrimitiveCast(pop(), DOUBLE_TYPE))
                case I2B => push(PrimitiveCast(pop(), BYTE_TYPE))
                case I2C => push(PrimitiveCast(pop(), CHAR_TYPE))
                case I2S => push(PrimitiveCast(pop(), SHORT_TYPE))
                
                case i if i >= LCMP && i <= DCMPG =>
                    push(BinaryOp(i, pop(), pop()))
                
                case i if i >= IRETURN && i <= ARETURN => pop()
                
                case ARRAYLENGTH => push(ArrayLength(pop()))
                case ATHROW => pop()
                case MONITORENTER|MONITOREXIT => pop()
                
                case _ =>

            }
            case iinsn:IntInsnNode => ainsn.getOpcode match
            {
                case BIPUSH => push(Const(iinsn.operand.toByte))
                case SIPUSH => push(Const(iinsn.operand.toShort))
            }
            case ldcinsn:LdcInsnNode => ainsn.getOpcode match
            {
                case LDC => push(Const(ldcinsn.cst))
            }
            case vinsn:VarInsnNode => ainsn.getOpcode match
            {
                case i if i >= ILOAD && i <= ALOAD =>
                    push(Load(locals(vinsn.`var`)))
                case i if i >= ISTORE && i <= ASTORE =>
                    setL(vinsn.`var`, Store(pop()))
            }
            case incinsn:IincInsnNode => ainsn.getOpcode match
            {
                case IINC => setL(incinsn.`var`, Store(BinaryOp(IINC, Const(incinsn.incr), Load(locals(incinsn.`var`)))))
            }
            case jinsn:JumpInsnNode => ainsn.getOpcode match
            {
                case i if i >= IFEQ && i <= IFLE => pop()
                case i if i >= IF_ICMPEQ && i <= IF_ACMPNE => pop(); pop()
                case JSR => push(ReturnAddress())
                case IFNULL|IFNONNULL => pop()
                case GOTO =>
            }
            case sinsn:TableSwitchInsnNode => pop()
            case linsn:LookupSwitchInsnNode => pop()
            case finsn:FieldInsnNode => ainsn.getOpcode match
            {
                case GETSTATIC => push(GetField(null, finsn))
                case PUTSTATIC => pop()
                case GETFIELD => push(GetField(pop(), finsn))
                case PUTFIELD => pop(); pop()
            }
            case minsn:MethodInsnNode => ainsn.getOpcode match
            {
                case INVOKEVIRTUAL|INVOKESPECIAL|INVOKEINTERFACE =>
                    push(Invoke(ainsn.getOpcode, popArgs(minsn.desc), pop(), minsn))
                case INVOKESTATIC =>
                    push(Invoke(ainsn.getOpcode, popArgs(minsn.desc), null, minsn))
            }
            case tinsn:TypeInsnNode => ainsn.getOpcode match
            {
                case NEW => push(New(getType(tinsn.desc)))
                case NEWARRAY => push(NewArray(pop(), getType(tinsn.desc)))
                case ANEWARRAY => push(NewArray(pop(), getType("["+tinsn.desc)))
                case CHECKCAST => push(Cast(pop(), getType(tinsn.desc)))
                case INSTANCEOF => push(UnaryOp(INSTANCEOF, pop()))
            }
            case mainsn:MultiANewArrayInsnNode =>
                val sizes = new Array[StackEntry](mainsn.dims)
                for(i <- 0 until sizes.length)
                    sizes(i) = pop()
                push(NewMultiArray(sizes, getType(mainsn.desc)))
            /*case fnode:FrameNode => fnode.`type` match
            {
                case F_NEW|F_FULL => println("reset stacks/locals")
                case F_APPEND => println("add local")
                case F_CHOP => println("rem locals")
                case F_SAME => println("reset")
                case F_SAME1 => println("reset locals and all but bottom stack")
            }*/
            case lnode:LabelNode =>
                catchHandlers.get(lnode) match
                {
                    case Some(tcblock) => push(CaughtException(Type.getType(tcblock.`type`)))
                    case None =>
                }
            case _ =>
        }
    }
}