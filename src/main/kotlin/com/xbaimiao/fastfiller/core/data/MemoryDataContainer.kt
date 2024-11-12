package com.xbaimiao.fastfiller.core.data

import com.xbaimiao.easylib.util.submit
import org.bukkit.entity.Player

/**
 * @author: xbaimiao
 * @date: 2022年03月19日 14:11
 * @description:
 */
class MemoryDataContainer : DataContainer {

    private val map = HashMap<String, Boolean>()

    override fun inFill(player: Player): Boolean {
        return map[player.name] ?: false
    }

    override fun canFill(player: Player): Boolean = !inFill(player)

    override fun setInFill(player: Player, boolean: Boolean) {
        map[player.name] = boolean
        submit(delay = 20 * 120) {
            map[player.name] = false
        }
    }

}