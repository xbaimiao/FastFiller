package com.xbaimiao.fastfiller

import com.xbaimiao.easylib.util.isAir
import org.bukkit.entity.Player

object InventoryUtil {

    /**
     * 获取玩家背包容量
     */
    fun Player.getInventoryVolume(): Int {
        var amount = 0
        val inventory = this.inventory
        for (itemStack in inventory) {
            if (itemStack == null || itemStack.isAir()) {
                amount += 64
            }
        }
        if (inventory.boots == null || inventory.boots.isAir()) {
            amount -= 64
        }
        if (inventory.itemInOffHand.isAir()) {
            amount -= 64
        }
        if (inventory.chestplate == null || inventory.chestplate.isAir()) {
            amount -= 64
        }
        if (inventory.helmet == null || inventory.helmet.isAir()) {
            amount -= 64
        }
        if (inventory.leggings == null || inventory.leggings.isAir()) {
            amount -= 64
        }
        return amount
    }

}