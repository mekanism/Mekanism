package codechicken.multipart.asm

import codechicken.multipart.TileMultipart

trait IMultipartFactory
{
    def registerTrait(s_interface:String, s_trait:String, client:Boolean)
    
    def generatePassThroughTrait(s_interface:String):String
    
    def generateTile(types:Seq[String], client:Boolean):TileMultipart
}