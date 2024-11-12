package com.xbaimiao.fastfiller.listener

import com.plotsquared.core.PlotSquared
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.util.EListener
import com.xbaimiao.easylib.util.isAir
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.config.Config
import com.xbaimiao.fastfiller.core.Hook
import com.xbaimiao.fastfiller.ui.FillerUIManager
import landMain.LandMain
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import world.bentobox.bentobox.BentoBox

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
            if (!checkRes(player, location)) {
                player.sendLang("select-res-not-player")
                return false
            }
            if (!checkPlotSquared(player, location)) {
                player.sendLang("select-ps-not-player")
                return false
            }
            if (!checkBentoBox(player, location)) {
                player.sendLang("select-bentobox-not-player")
                return false
            }
            if (!checkLand(player, location)) {
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

    /**
     * 检查选点是否在自己领地 或者有权限
     * 未安装RES插件 则始终返回 TRUE
     * @return 有权限和自己领地 返回 true 否则 false 如果不在领地内也返回false
     */
    private fun checkRes(player: Player, location: Location): Boolean {
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

    private fun checkBentoBox(player: Player, location: Location): Boolean {
        if (!Hook.hasBentoBox) {
            return true
        }
        val island = BentoBox.getInstance().islandsManager.getIslandAt(location) ?: return true
        if (!island.isPresent) {
            return true
        }
        val land = island.get()
        return land.owner == player.uniqueId || player.uniqueId in land.members.keys
    }

    private fun checkPlotSquared(player: Player, location: Location): Boolean {
        if (!Hook.hasPlotSquared) {
            return true
        }
        val plot = location.adaptPlotSquared.plot ?: return true
        return plot.owner == player.uniqueId
    }

    val Location.adaptPlotSquared: com.plotsquared.core.location.Location
        get() {
            return com.plotsquared.core.location.Location.at(
                PlotSquared.platform().getPlatformWorld(world!!.name)!!,
                blockX,
                blockY,
                blockZ
            )
        }

    private fun checkLand(player: Player, location: Location): Boolean {
        if (!Hook.hasLand) {
            return true
        }
        val land = LandMain.getLandManager().getHighestPriorityLand(location) ?: return false
        return land.owner == player.name
    }

}
