package com.xbaimiao.fastfiller.core.item

import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.util.ItemBuilder
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easylib.xseries.XMaterial
import com.xbaimiao.fastfiller.core.hook.CraftEngineHook
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

/**
 * 配置文件里的物品构建
 *
 * `material` 支持三种写法
 * - `STONE` 原版材质, 走 XMaterial 解析
 * - `head:<base64>` 自定义头颅
 * - `ce:<namespace:id>` CraftEngine 物品
 */
object ItemFactory {

    private const val HEAD_PREFIX = "head:"
    private const val CRAFT_ENGINE_PREFIX = "ce:"

    /**
     * 物品定义
     *
     * @param material 材质写法
     * @param name 展示名, null 代表不修改(保留 CE 物品自带的名字)
     * @param lore 描述, 空列表代表不修改(保留 CE 物品自带的 lore)
     * @param customModelData null 代表不设置
     */
    data class Spec(
        val material: String,
        val name: String?,
        val lore: List<String>,
        val customModelData: Int?,
    )

    /**
     * 从配置节点读取物品定义
     */
    fun readSpec(section: ConfigurationSection, defaultMaterial: String = "STONE"): Spec {
        val customModelData = when {
            section.isSet("custom-model-data") -> section.getInt("custom-model-data")
            // 兼容 1.x 的键名
            section.isSet("custom") -> section.getInt("custom")
            else -> null
        }
        return Spec(
            material = section.getString("material") ?: defaultMaterial,
            name = section.getString("name")?.colored(),
            lore = section.getStringList("lore").colored(),
            customModelData = customModelData?.takeIf { it > 0 },
        )
    }

    /**
     * 按定义构建物品
     *
     * @param where 出错时提示用的位置描述
     */
    fun build(spec: Spec, where: String): ItemStack {
        val material = spec.material.trim()
        // CraftEngine 物品
        if (material.startsWith(CRAFT_ENGINE_PREFIX, ignoreCase = true)) {
            val id = material.substring(CRAFT_ENGINE_PREFIX.length).trim()
            val item = buildCraftEngineItem(id, where)
            if (item != null) {
                return item.applySpecToMeta(spec)
            }
            // CE 物品拿不到时退回石头, 至少让菜单能开
            return buildVanillaItem(spec, XMaterial.STONE)
        }
        // 自定义头颅
        if (material.startsWith(HEAD_PREFIX, ignoreCase = true)) {
            return buildItem(XMaterial.PLAYER_HEAD) {
                applySpec(spec)
                skullTexture = ItemBuilder.SkullTexture(material.substring(HEAD_PREFIX.length))
            }
        }
        // 原版材质
        val xMaterial = XMaterial.matchXMaterial(material).orElse(null)
        if (xMaterial == null) {
            warn("$where 的材质 $material 无效, 已使用 STONE 代替")
            return buildVanillaItem(spec, XMaterial.STONE)
        }
        return buildVanillaItem(spec, xMaterial)
    }

    private fun buildVanillaItem(spec: Spec, xMaterial: XMaterial): ItemStack {
        return buildItem(xMaterial) { applySpec(spec) }
    }

    /**
     * 构建 CE 物品并把配置里的名字 / lore 覆盖上去
     *
     * CE 物品自带模型与组件数据, 因此直接改 meta 而不是重建物品,
     * 避免丢掉 CE 写在物品上的数据
     */
    private fun buildCraftEngineItem(id: String, where: String): ItemStack? {
        if (!CraftEngineHook.enabled) {
            warn("$where 配置了 CraftEngine 物品 $id, 但服务器没有安装 CraftEngine")
            return null
        }
        val item = CraftEngineHook.createItem(id)
        if (item == null) {
            warn("$where 配置的 CraftEngine 物品 $id 不存在")
            return null
        }
        return item
    }

    /**
     * 把配置里的名字 / lore / CustomModelData 覆盖到已有物品上
     *
     * 只覆盖配置里写了的字段, 没写的保留物品原样
     */
    private fun ItemStack.applySpecToMeta(spec: Spec): ItemStack {
        val meta = this.itemMeta ?: return this
        spec.name?.let { meta.setDisplayName(it) }
        if (spec.lore.isNotEmpty()) {
            meta.lore = spec.lore
        }
        spec.customModelData?.let { data ->
            // 1.21.4+ 该方法已弃用但仍然可用
            runCatching { meta.setCustomModelData(data) }
        }
        this.itemMeta = meta
        return this
    }

    private fun ItemBuilder.applySpec(spec: Spec) {
        spec.name?.let { name = it }
        if (spec.lore.isNotEmpty()) {
            lore.addAll(spec.lore)
        }
        spec.customModelData?.let { customModelData = it }
    }

}
