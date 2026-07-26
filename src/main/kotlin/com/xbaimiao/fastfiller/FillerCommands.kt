package com.xbaimiao.fastfiller

import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.chat.Lang.sendLang
import com.xbaimiao.easylib.command.command
import com.xbaimiao.easylib.util.giveItem
import com.xbaimiao.fastfiller.core.Permissions
import com.xbaimiao.fastfiller.core.config.BlockNames
import com.xbaimiao.fastfiller.core.config.FillerConfig
import com.xbaimiao.fastfiller.core.hook.Hooks
import com.xbaimiao.fastfiller.ui.FillerMenus
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * /playerfiller 命令
 */
internal object FillerCommands {

    fun register() {
        command<CommandSender>("playerfiller") {
            permission = Permissions.ADMIN
            description = "PlayerFiller 管理命令"

            subCommand<CommandSender>("give") {
                description = "给玩家一把创世斧"
                val playerArg = players()
                exec {
                    val target = playerArg.value() ?: return@exec sender.sendMessage("§c玩家不在线")
                    target.giveItem(FastFiller.api.createFillerItem())
                    sender.sendLang("giveItem", target.name)
                }
            }

            subCommand<Player>("add") {
                description = "为主手创世斧增加方块储量"
                val amountArg = number()
                exec {
                    val fillerItem = FastFiller.api.fillerItemInMainHand(sender)
                    if (fillerItem == null) {
                        sender.sendLang("filler-noItem")
                        return@exec
                    }
                    val stored = fillerItem.storage.get()
                    if (stored.isEmpty) {
                        sender.sendLang("addItem-isAIR")
                        return@exec
                    }
                    val amount = amountArg.value()?.toInt()
                    if (amount == null || amount <= 0) {
                        sender.sendMessage("§c数量必须是大于 0 的整数")
                        return@exec
                    }
                    val added = fillerItem.storage.add(stored.material, amount)
                    if (added <= 0) {
                        sender.sendLang("addItem-full")
                        return@exec
                    }
                    sender.sendMessage("§a已添加 §e$added §a个 §e${BlockNames.of(stored.material)}")
                }
            }

            subCommand<CommandSender>("reload") {
                description = "重载配置文件"
                exec {
                    FastFiller.inst.reloadConfig()
                    FillerConfig.load()
                    BlockNames.load()
                    Lang.reload()
                    Hooks.init()
                    FillerMenus.load()
                    sender.sendMessage("§a配置文件已重载")
                }
            }
        }.register()
    }

}
