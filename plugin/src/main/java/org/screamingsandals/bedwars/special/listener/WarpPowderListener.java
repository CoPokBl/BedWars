/*
 * Copyright (C) 2022 ScreamingSandals
 *
 * This file is part of Screaming BedWars.
 *
 * Screaming BedWars is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Screaming BedWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Screaming BedWars. If not, see <https://www.gnu.org/licenses/>.
 */

package org.screamingsandals.bedwars.special.listener;

import org.screamingsandals.bedwars.utils.ItemUtils;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.player.PlayerManagerImpl;
import org.screamingsandals.bedwars.special.WarpPowderImpl;
import org.screamingsandals.bedwars.utils.DelayFactoryImpl;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.entity.EntityItem;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.entity.SEntityDamageEvent;
import org.screamingsandals.lib.event.player.SPlayerInteractEvent;
import org.screamingsandals.lib.event.player.SPlayerMoveEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.BlockFace;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public class WarpPowderListener {
    private static final String WARP_POWDER_PREFIX = "Module:WarpPowder:";

    @OnEvent
    public void onPowderItemRegister(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("warppowder")) {
            event.setStack(ItemUtils.saveData(event.getStack(), applyProperty(event)));
        }
    }

    @OnEvent
    public void onPlayerUseItem(SPlayerInteractEvent event) {
        var player = event.player();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();
        if (event.action() == SPlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action() == SPlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (game != null && game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator()) {
                if (event.item() != null) {
                    var stack = event.item();
                    var unhidden = ItemUtils.getIfStartsWith(stack, WARP_POWDER_PREFIX);

                    if (unhidden != null) {
                        event.cancelled(true);
                        if (!game.isDelayActive(gPlayer, WarpPowderImpl.class)) {
                            var propertiesSplit = unhidden.split(":");
                            int teleportTime = Integer.parseInt(propertiesSplit[2]);
                            int delay = Integer.parseInt(propertiesSplit[3]);
                            var warpPowder = new WarpPowderImpl(game, gPlayer, game.getPlayerTeam(gPlayer), stack, teleportTime);

                            if (event.player().getLocation().add(BlockFace.DOWN).getBlock().getType().isAir()) {
                                return;
                            }

                            if (delay > 0) {
                                var delayFactory = new DelayFactoryImpl(delay, warpPowder, gPlayer, game);
                                game.registerDelay(delayFactory);
                            }

                            warpPowder.runTask();
                        } else {
                            int delay = game.getActiveDelay(gPlayer, WarpPowderImpl.class).getRemainDelay();
                            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_ITEM_DELAY).placeholder("time", delay));
                        }
                    }
                }
            }
        }
    }


    @OnEvent
    public void onDamage(SEntityDamageEvent event) {
        if (event.cancelled() || !(event.entity() instanceof PlayerWrapper)) {
            return;
        }

        var player = (PlayerWrapper) event.entity();

        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();

        if (gPlayer.isSpectator() || game == null) {
            return;
        }

        var warpPowder = game.getFirstActiveSpecialItemOfPlayer(gPlayer, WarpPowderImpl.class);
        if (warpPowder != null) {
            warpPowder.cancelTeleport(false, true);
        }
    }

    @OnEvent
    public void onMove(SPlayerMoveEvent event) {
        var player = event.player();
        if (event.cancelled() || !PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        if (event.currentLocation().getX() == event.newLocation().getX()
                && event.currentLocation().getY() == event.newLocation().getY()
                && event.currentLocation().getZ() == event.newLocation().getZ()) {
            return;
        }

        var gPlayer = player.as(BedWarsPlayer.class);
        var game = gPlayer.getGame();
        if (gPlayer.isSpectator() || game == null) {
            return;
        }

        var warpPowder = game.getFirstActiveSpecialItemOfPlayer(gPlayer, WarpPowderImpl.class);
        if (warpPowder != null) {
            warpPowder.cancelTeleport(true, true);

            if (player.getPlayerInventory().firstEmptySlot() == -1 && !player.getPlayerInventory().contains(warpPowder.getItem())) {
                EntityItem.dropItem(warpPowder.getItem(), player.getLocation());
            } else {
                player.getPlayerInventory().addItem(warpPowder.getItem());
            }
            player.forceUpdateInventory();
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return WARP_POWDER_PREFIX
                + MiscUtils.getIntFromProperty(
                "teleport-time", "specials.warp-powder.teleport-time", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.warp-powder.delay", event);
    }
}
