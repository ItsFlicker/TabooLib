@file:Isolated
package taboolib.platform.util

import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import taboolib.common.Isolated
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.TellrawJson

fun buildBook(builder: BookBuilder.() -> Unit = {}): ItemStack {
    return BookBuilder().also(builder).build()
}

fun Player.sendBook(builder: BookBuilder.() -> Unit = {}) {
    sendBook(buildBook(builder))
}

fun Player.sendBook(itemStack: ItemStack) {
    try {
        invokeMethod<Void>("openBook", itemStack)
    } catch (ex: NoSuchMethodException) {
        val itemInHand = itemInHand
        setItemInHand(itemStack)
        try {
            val nmsItemStack = classCraftItemStack.invokeMethod<Any>("asNMSCopy", itemStack, fixed = true)
            val handle = getProperty<Any>("entity")!!
            try {
                handle.invokeMethod<Void>("a", nmsItemStack, enumHandMainHand)
            } catch (ex: NoSuchMethodException) {
                handle.invokeMethod<Void>("openBook", nmsItemStack, enumHandMainHand)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        setItemInHand(itemInHand)
    }
}

private val classCraftItemStack by lazy {
    obcClassLegacy("inventory.CraftItemStack")
}

private val classChatSerializer by lazy {
    nmsClassLegacy("IChatBaseComponent\$ChatSerializer")
}

private val enumHandMainHand by lazy {
    nmsClassLegacy("EnumHand").enumConstants[0]
}

private fun obcClassLegacy(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}

private fun nmsClassLegacy(name: String): Class<*> {
    return Class.forName("net.minecraft.server.${Bukkit.getServer().javaClass.name.split('.')[3]}.$name")
}

@Isolated
open class BookBuilder : ItemBuilder(XMaterial.WRITTEN_BOOK) {

    class Text(val text: String, val raw: Boolean = false)

    var title = "untitled"
    var author = "untitled"

    val bookPages = ArrayList<Text>()

    fun write(text: String) {
        bookPages += Text(text)
    }

    fun write(text: TellrawJson) {
        writeRaw(text.toRawMessage())
    }

    fun writeRaw(text: String) {
        bookPages += Text(text, raw = true)
    }

    override fun build(): ItemStack {
        return super.build().modifyMeta<BookMeta> {
            title = "untitled"
            author = "untitled"
            bookPages.forEach {
                println("text ${it.text} row ${it.raw}")
                if (it.raw) {
                    try {
                        spigot().addPage(ComponentSerializer.parse(it.text))
                    } catch (ex: NoSuchMethodError) {
                        getProperty<MutableList<Any>>("pages")!! += classChatSerializer.invokeMethod<Any>("a", it.text, fixed = true)!!
                    }
                } else {
                    addPage(it.text)
                }
            }
        }
    }
}