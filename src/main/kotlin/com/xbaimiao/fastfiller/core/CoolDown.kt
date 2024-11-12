package com.xbaimiao.fastfiller.core

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.fastfiller.FastFiller
import org.bukkit.Bukkit

object CoolDown {

    private val cooldown by lazy { FastFiller.conf.getInt("cooldown", 0) }
    private val dataMap = HashMap<String, Long>()

    /**
     * 检查是否还在冷却中
     * @return true: 冷却中
     */
    fun checkIsCoolDown(playerName: String): Boolean {
        if (Bukkit.getPlayerExact(playerName)?.hasPermission("playerfiller.vip") == true) {
            return false
        }
        if (cooldown <= 0) {
            return false
        }
        val time = dataMap[playerName] ?: return false

        val result = cooldown - ((System.currentTimeMillis() - time) / 1000)

        if (result > 0) {
            Bukkit.getPlayerExact(playerName)?.sendLang("filler-cooldown", result)
            return true
        }
        return false
    }


    fun setCoolDown(playerName: String) {
        if (cooldown <= 0) {
            return
        }
        dataMap[playerName] = System.currentTimeMillis()
    }

}