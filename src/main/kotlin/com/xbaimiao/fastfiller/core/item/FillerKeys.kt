package com.xbaimiao.fastfiller.core.item

import com.xbaimiao.fastfiller.FastFiller
import org.bukkit.NamespacedKey

/**
 * 创世斧使用的 PersistentDataContainer 键
 *
 * 全部走 PDC 存储, 不依赖具体服务端版本的 NBT 结构,
 * 1.18.2 - 26.x 表现一致
 */
internal object FillerKeys {

    /** 创世斧标记 **/
    val marker: NamespacedKey by lazy { key("filler") }

    /** 容器内方块材质名 **/
    val blockMaterial: NamespacedKey by lazy { key("block_material") }

    /** 容器内方块数量 **/
    val blockAmount: NamespacedKey by lazy { key("block_amount") }

    private fun key(value: String): NamespacedKey {
        return NamespacedKey(FastFiller.inst, value)
    }

}
