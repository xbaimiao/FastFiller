package com.xbaimiao.fastfiller.core.hook

import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

/**
 * CraftEngine 物品兼容
 *
 * CE 的类只在 [bridge] 里出现, 而 [bridge] 只在服务器装了 CraftEngine 时才创建,
 * 因此没装 CE 的服务器不会触发 CE 类的加载
 */
internal object CraftEngineHook {

    private var bridge: CraftEngineBridge? = null

    val enabled: Boolean get() = bridge != null

    fun init() {
        bridge = null
        if (Bukkit.getPluginManager().getPlugin("CraftEngine") == null) {
            return
        }
        val created = runCatching { CraftEngineBridge() }
        val failure = created.exceptionOrNull()
        if (failure != null) {
            warn("挂钩 CraftEngine 失败, 已禁用 CraftEngine 物品支持: ${failure.message}")
            return
        }
        bridge = created.getOrNull()
        info("已挂钩 CraftEngine")
    }

    /**
     * 按 CE 物品 id 创建物品
     *
     * @param id `namespace:value` 或只写 value
     * @return CE 里没有这个物品时返回 null
     */
    fun createItem(id: String): ItemStack? {
        val bridge = this.bridge ?: return null
        return runCatching { bridge.createItem(id) }.onFailure {
            warn("创建 CraftEngine 物品 $id 失败: ${it.message}")
        }.getOrNull()
    }

    /**
     * 判断物品是不是 CE 自定义物品
     */
    fun isCustomItem(itemStack: ItemStack?): Boolean {
        val bridge = this.bridge ?: return false
        if (itemStack == null) {
            return false
        }
        return runCatching { bridge.isCustomItem(itemStack) }.getOrDefault(false)
    }

    /**
     * 获取物品的 CE 物品 id, 不是 CE 物品时返回 null
     */
    fun customItemId(itemStack: ItemStack?): String? {
        val bridge = this.bridge ?: return null
        if (itemStack == null) {
            return null
        }
        return runCatching { bridge.customItemId(itemStack) }.getOrNull()
    }

}
