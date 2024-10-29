package com.github.tartaricacid.netmusic.inventory

import com.github.tartaricacid.netmusic.init.InitItems
import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.getSongInfo
import com.github.tartaricacid.netmusic.item.ItemMusicCD.Companion.setSongInfo
import com.github.tartaricacid.netmusic.item.ItemMusicCD.SongInfo
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.extensions.IForgeMenuType
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler

class CDBurnerMenu(
    id: Int,
    inventory: Inventory
) : AbstractContainerMenu(TYPE, id) {

    val input: ItemStackHandler = object : ItemStackHandler() {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return stack.item === InitItems.MUSIC_CD.get()
        }
    }

    private val output: ItemStackHandler = object : ItemStackHandler() {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
            return false
        }

        override fun getStackLimit(slot: Int, stack: ItemStack): Int {
            return 1
        }
    }

    private var songInfo: SongInfo? = null

    init {
        this.addSlot(SlotItemHandler(input, 0, 147, 14))
        this.addSlot(SlotItemHandler(output, 0, 147, 67))

        for (i in 0..8) {
            this.addSlot(Slot(inventory, i, 8 + i * 18, 152))
        }

        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(inventory, j + i * 9 + 9, 8 + j * 18, 94 + i * 18))
            }
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        var itemStack = ItemStack.EMPTY
        val slot = slots[index]

        if (slot != null && slot.hasItem()) {
            val slotItem = slot.item
            itemStack = slotItem.copy()
            if (index < 2) {
                if (!this.moveItemStackTo(slotItem, 2, slots.size, false)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.moveItemStackTo(slotItem, 0, 2, true)) {
                return ItemStack.EMPTY
            }

            if (slotItem.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
        }

        return itemStack
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun removed(player: Player) {
        super.removed(player)
        ItemHandlerHelper.giveItemToPlayer(player, input.getStackInSlot(0))
        ItemHandlerHelper.giveItemToPlayer(player, output.getStackInSlot(0))
    }

    fun setSongInfo(setSongInfo: SongInfo?) {
        this.songInfo = setSongInfo

        if (!input.getStackInSlot(0).isEmpty && output.getStackInSlot(0).isEmpty) {
            val itemStack = input.extractItem(0, 1, false)
            val rawSongInfo = getSongInfo(itemStack)
            if (rawSongInfo == null || !rawSongInfo.readOnly) {
                setSongInfo(songInfo!!, itemStack)
            }

            output.setStackInSlot(0, itemStack)
        }
    }

    companion object {
        val TYPE: MenuType<CDBurnerMenu> =
            IForgeMenuType.create { windowId: Int, inv: Inventory, data: FriendlyByteBuf? ->
                CDBurnerMenu(
                    windowId,
                    inv
                )
            }
    }
}