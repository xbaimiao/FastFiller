package com.xbaimiao.fastfiller.ui

import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.ui.Basic
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.fastfiller.FastFiller
import com.xbaimiao.fastfiller.api.impl.DefaultContainer
import com.xbaimiao.fastfiller.config.Config
import com.xbaimiao.fastfiller.core.BlockCompare
import com.xbaimiao.fastfiller.core.CoolDown
import com.xbaimiao.fastfiller.core.fill.ClearBlocks
import com.xbaimiao.fastfiller.core.fill.FillBlocks
import com.xbaimiao.fastfiller.core.fill.MagicFillBlocks
import com.xbaimiao.fastfiller.core.hook.Hook
import com.xbaimiao.fastfiller.core.hook.adaptPlotSquared
import com.xbaimiao.fastfiller.listener.InteractEvent
import landMain.LandMain
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import world.bentobox.bentobox.BentoBox

class MainFillerUI(
    override val section: ConfigurationSection
) : FillerUI() {

    private val containerKey = section.getString("identifier.container")!![0]
    private val clearKey = section.getString("identifier.clear")!![0]
    private val waterKey = section.getString("identifier.water")!![0]
    private val fillKey = section.getString("identifier.fill")!![0]

    override fun onOpen(player: Player, basic: Basic) {
        basic.onClick { it.isCancelled = true }
        basic.onClick(containerKey) {
            FillerUIManager.container.open(player)
        }

        basic.onClick(clearKey) {
            player.confirmMenu {
                if (!FastFiller.dataContainer.canFill(player)) {
                    player.sendLang("filler-infill")
                    return@confirmMenu
                }
                clear(player, Config.whiteListBlock)
            }
        }

        basic.onClick(waterKey) {
            player.confirmMenu {
                if (!FastFiller.dataContainer.canFill(player)) {
                    player.sendLang("filler-infill")
                    return@confirmMenu
                }
                clear(
                    player,
                    listOfNotNull(Material.WATER, runCatching { Material.valueOf("STATIONARY_WATER") }.getOrNull())
                )
            }
        }

        basic.onClick(fillKey) {
            player.confirmMenu {
                if (!FastFiller.dataContainer.canFill(player)) {
                    player.sendLang("filler-infill")
                    return@confirmMenu
                }
                if (Hook.hasMagicBlock) {
                    val offItem = player.inventory.itemInOffHand
                    if (Hook.isMagic(offItem)) {
                        if (!player.hasPermission("playerfiller.magic")) {
                            player.sendLang("filler-magic-no-permission")
                            return@confirmMenu
                        }
                        if (Hook.isHasAmountMagic(offItem)) {
                            player.sendLang("filler-magic-no-amount")
                            return@confirmMenu
                        }
                        player.sendLang("filler-magicblock")
                        submit(async = true) {
                            if (CoolDown.checkIsCoolDown(player.name)) {
                                return@submit
                            }
                            CoolDown.setCoolDown(player.name)
                            val location = checkLocation(player) ?: return@submit
                            MagicFillBlocks(player).fill(location.first, location.second, offItem.type)
                        }
                        return@confirmMenu
                    }
                }
                val fillerItem = FastFiller.api.toFillerItem(player.inventory.itemInMainHand)

                if (fillerItem == null) {
                    player.sendLang("filler-noItem")
                    return@confirmMenu
                }

                val container = fillerItem.getContainer()
                val items = container.getItem()

                if (items == null || items.second <= 0) {
                    player.sendLang("fill-noItem")
                    player.closeInventory()
                    return@confirmMenu
                }
                submit(async = true) {
                    val location1 = InteractEvent.cache["${player.name}loc1"]
                        ?: return@submit let { player.sendLang("select-noSelect") }
                    val location2 = InteractEvent.cache["${player.name}loc2"]
                        ?: return@submit let { player.sendLang("select-noSelect") }

                    val blockCompare = BlockCompare(location1, location2)
                    if (!player.hasPermission("playerfiller.vip")) {
                        if (blockCompare.maxZ - blockCompare.minZ > Config.maxRange.second || blockCompare.maxX - blockCompare.minX > Config.maxRange.first) {
                            player.sendLang("filler-tooLarge")
                            return@submit
                        }
                    }

                    player.sendLang("filler", DefaultContainer.getChineseName(items.first))
                    FastFiller.dataContainer.setInFill(player, true)

                    if (CoolDown.checkIsCoolDown(player.name)) {
                        return@submit
                    }
                    CoolDown.setCoolDown(player.name)
                    FillBlocks(items, container, player).fill(location1, location2, Material.STONE)
                }
            }
        }
    }

    private fun Player.confirmMenu(confirm: () -> Unit) {
        FillerUIManager.confirm.open(this, confirm)
    }

    /**
     * 检查选点是否正确
     */
    private fun checkLocation(player: Player): Pair<Location, Location>? {
        val location1 = InteractEvent.cache["${player.name}loc1"]
            ?: return let {
                player.sendLang("select-noSelect")
                null
            }
        val location2 = InteractEvent.cache["${player.name}loc2"]
            ?: return let {
                player.sendLang("select-noSelect")
                null
            }
        if (Hook.hasResidence) {
            val res1 = Hook.residence!!.residenceManager.getByLoc(location1)
            val res2 = Hook.residence!!.residenceManager.getByLoc(location2)
            if (res1 != null && res2 != null && res1.name != res2.name) {
                player.sendLang("select-res-dissimilarity")
                return null
            }
        }
        if (Hook.hasPlotSquared) {
            val plot1 = location1.adaptPlotSquared.plot
            val plot2 = location2.adaptPlotSquared.plot
            if (plot1 != null || plot2 != null) {
                if (plot1 != plot2) {
                    player.sendLang("select-res-dissimilarity")
                    return null
                }
            }
        }
        if (Hook.hasBentoBox) {
            val island1 = BentoBox.getInstance().islandsManager.getIslandAt(location1)
            val island2 = BentoBox.getInstance().islandsManager.getIslandAt(location2)
            if (island1.isPresent || island2.isPresent) {
                if (island1.get() != island2.get()) {
                    player.sendLang("select-res-dissimilarity")
                    return null
                }
            }
        }
        if (Hook.hasLand) {
            val land1 = LandMain.getLandManager().getHighestPriorityLand(location1)
            val land2 = LandMain.getLandManager().getHighestPriorityLand(location2)
            if (land1 != null || land2 != null) {
                if (land2 != land1) {
                    player.sendLang("select-res-dissimilarity")
                    return null
                }
            }
        }
        return Pair(location1, location2)
    }

    private fun clear(player: Player, whitelist: List<Material>) {
        if (CoolDown.checkIsCoolDown(player.name)) {
            return
        }
        CoolDown.setCoolDown(player.name)
        val location = checkLocation(player) ?: return
        player.sendLang("fill-clear")
        player.closeInventory()
        submit(async = true) {
            FastFiller.dataContainer.setInFill(player, true)
            ClearBlocks(whitelist, player).fill(location.first, location.second, Material.AIR)
        }
    }

}
