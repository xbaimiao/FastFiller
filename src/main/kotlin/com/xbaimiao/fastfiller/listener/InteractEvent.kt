package com.xbaimiao.fastfiller.listener

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.easylib.util.isAir
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.Config
import com.xbaimiao.fastfiller.core.hook.*
import com.xbaimiao.fastfiller.ui.FillerUIManager
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

@EListener
object InteractEvent : Listener {

    val cache = HashMap<String, Location>()
    private val limit = ArrayList<String>()

    @EventHandler
    fun i(event: PlayerInteractEvent) {
        val item = event.player.inventory.itemInMainHand
        if (item.isAir()) {
            return
        }
        if (!FastFiller.api.isFillerItem(item)) {
            return
        }
        event.isCancelled = true
        val player = event.player
        if (!player.hasPermission("playerfiller.use")) {
            player.sendLang("no-permission")
            return
        }
        // 限制时间
        if (limit.contains(player.name)) {
            return
        }
        limit.add(player.name)
        submit(delay = 3) { limit.remove(player.name) }
        // 如果下蹲右击
        if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
            if (player.isSneaking) {
                FillerUIManager.main.open(player)
                return
            }
        }

        fun check(player: Player, location: Location): Boolean {
            if (location.world?.name !in Config.enableWorlds) {
                player.sendLang("select-not-enable-world")
                return false
            }
            if (Hook.hasResidence && !checkRes(player, location)) {
                player.sendLang("select-res-not-player")
                return false
            }
            if (Hook.hasPlotSquared && !checkPlotSquared(player, location)) {
                player.sendLang("select-ps-not-player")
                return false
            }
            if (Hook.hasBentoBox && !checkBentoBox(player, location)) {
                player.sendLang("select-bentobox-not-player")
                return false
            }
            if (Hook.hasLand && !checkLand(player, location)) {
                player.sendLang("select-res-not-player")
                return false
            }
            return true
        }

        //如果选点
        if (event.action == Action.LEFT_CLICK_BLOCK) {
            if (event.clickedBlock != null) {
                val location = event.clickedBlock!!.location
                if (!check(player, location)) {
                    return
                }

                cache["${player.name}loc1"] = location
                player.sendLang("select-loc1", location.blockX, location.blockY, location.blockZ)
            }
        }
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            if (event.clickedBlock != null) {
                val location = event.clickedBlock!!.location
                if (!check(player, location)) {
                    return
                }
                cache["${player.name}loc2"] = location
                player.sendLang("select-loc2", location.blockX, location.blockY, location.blockZ)
            }
        }
    }


}
