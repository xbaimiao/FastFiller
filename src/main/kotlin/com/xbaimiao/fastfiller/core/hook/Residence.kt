package com.xbaimiao.fastfiller.core.hook

import org.bukkit.Location
import org.bukkit.entity.Player


/**
 * 检查选点是否在自己领地 或者有权限
 * 未安装RES插件 则始终返回 TRUE
 * @return 有权限和自己领地 返回 true 否则 false 如果不在领地内也返回false
 */
fun checkRes(player: Player, location: Location): Boolean {
    if (!Hook.hasResidence) {
        return true
    }
    val manager = Hook.residence!!.residenceManager
    val res = manager.getByLoc(location) ?: return false
    // 没有领地
    // 玩家是领地主人
    if (res.owner == player.name) {
        return true
    }
    // 如果玩家有这个领地的admin权限
    val pPermission = res.permissions.getPlayerFlags(player.name) ?: return false
    return pPermission["admin"] == true
}