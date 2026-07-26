package com.xbaimiao.fastfiller.core.hook

import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method

/**
 * CraftEngine 物品兼容
 *
 * CraftEngine 自身要求 Java 21, 而本插件为了兼容 1.18.2 使用 Java 17 构建,
 * 直接 compileOnly 会让 javac 读不了它的 class 文件, 因此这里用反射调用.
 * 反射目标是 CE 的公开 API `CraftEngineItems`, 签名长期稳定
 */
internal object CraftEngineHook {

    private const val API_CLASS = "net.momirealms.craftengine.bukkit.api.CraftEngineItems"

    var enabled = false
        private set

    /** CraftEngineItems#byId(String): BukkitItemDefinition **/
    private var byIdMethod: Method? = null

    /** CraftEngineItems#isCustomItem(ItemStack): boolean **/
    private var isCustomItemMethod: Method? = null

    /** CraftEngineItems#getCustomItemId(ItemStack): Key **/
    private var getCustomItemIdMethod: Method? = null

    /** BukkitItemDefinition#buildBukkitItem(): ItemStack **/
    private var buildBukkitItemMethod: Method? = null

    /** Key#asString(): String **/
    private var keyAsStringMethod: Method? = null

    fun init() {
        enabled = false
        if (Bukkit.getPluginManager().getPlugin("CraftEngine") == null) {
            return
        }
        val result = runCatching {
            val apiClass = Class.forName(API_CLASS)
            byIdMethod = apiClass.getMethod("byId", String::class.java)
            isCustomItemMethod = apiClass.getMethod("isCustomItem", ItemStack::class.java)
            getCustomItemIdMethod = apiClass.getMethod("getCustomItemId", ItemStack::class.java)
            buildBukkitItemMethod = byIdMethod!!.returnType.getMethod("buildBukkitItem")
            keyAsStringMethod = getCustomItemIdMethod!!.returnType.getMethod("asString")
        }
        if (result.isFailure) {
            warn("挂钩 CraftEngine 失败, 已禁用 CraftEngine 物品支持: ${result.exceptionOrNull()?.message}")
            return
        }
        enabled = true
        info("已挂钩 CraftEngine")
    }

    /**
     * 按 CE 物品 id 创建物品
     *
     * @param id `namespace:value` 或只写 value
     * @return CE 里没有这个物品时返回 null
     */
    fun createItem(id: String): ItemStack? {
        if (!enabled) {
            return null
        }
        return runCatching {
            val definition = byIdMethod!!.invoke(null, id) ?: return null
            buildBukkitItemMethod!!.invoke(definition) as? ItemStack
        }.onFailure {
            warn("创建 CraftEngine 物品 $id 失败: ${it.message}")
        }.getOrNull()
    }

    /**
     * 判断物品是不是 CE 自定义物品
     */
    fun isCustomItem(itemStack: ItemStack?): Boolean {
        if (!enabled || itemStack == null) {
            return false
        }
        return runCatching {
            isCustomItemMethod!!.invoke(null, itemStack) as? Boolean ?: false
        }.getOrDefault(false)
    }

    /**
     * 获取物品的 CE 物品 id, 不是 CE 物品时返回 null
     */
    fun customItemId(itemStack: ItemStack?): String? {
        if (!enabled || itemStack == null) {
            return null
        }
        return runCatching {
            val key = getCustomItemIdMethod!!.invoke(null, itemStack) ?: return null
            keyAsStringMethod!!.invoke(key) as? String
        }.getOrNull()
    }

}
