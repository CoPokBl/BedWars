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

package org.screamingsandals.bedwars.special;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.screamingsandals.bedwars.api.special.Golem;
import org.screamingsandals.bedwars.entities.EntitiesManagerImpl;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.TeamImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.lib.nms.entity.EntityUtils;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.entity.EntityLiving;
import org.screamingsandals.lib.entity.EntityMapper;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.nms.accessors.ServerPlayerAccessor;
import org.screamingsandals.lib.utils.AdventureHelper;
import org.screamingsandals.lib.world.LocationHolder;

@Getter
@EqualsAndHashCode(callSuper = true)
public class GolemImpl extends SpecialItem implements Golem {
    private EntityLiving entity;
    private final LocationHolder location;
    private final Item item;
    private final double speed;
    private final double followRange;
    private final double health;
    private final String name;
    private final boolean showName;

    public GolemImpl(GameImpl game, BedWarsPlayer player, TeamImpl team,
                     Item item, LocationHolder location, double speed, double followRange, double health,
                     String name, boolean showName) {
        super(game, player, team);
        this.location = location;
        this.item = item;
        this.speed = speed;
        this.followRange = followRange;
        this.health = health;
        this.name = name;
        this.showName = showName;
    }

    public void spawn() {
        final var golem = EntityMapper.<EntityLiving>spawn("iron_golem", location).orElseThrow();
        golem.setHealth(health);
        golem.setCustomName(name
                .replace("%teamcolor%", AdventureHelper.toLegacyColorCode(team.getColor().getTextColor()))
                .replace("%team%", team.getName()));
        golem.setCustomNameVisible(showName);
        try {
            golem.setInvulnerable(false);
        } catch (Throwable ignored) {
            // Still can throw an exception on some old versions
        }
        
        entity = golem;

        //noinspection ConstantConditions - suppressing nullability check, if this throws a NPE, something went wrong badly
        EntityUtils.makeMobAttackTarget(golem, speed, followRange, -1)
                .getTargetSelector()
                .attackNearestTarget(0, ServerPlayerAccessor.getType());

        game.registerSpecialItem(this);
        EntitiesManagerImpl.getInstance().addEntityToGame(golem, game);
        MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_GOLEM_CREATED));

        //TODO - make this better by checking full inventory
        var stack = item.withAmount(1);
        try {
            if (player.getPlayerInventory().getItemInOffHand().equals(stack)) {
                player.getPlayerInventory().setItemInOffHand(ItemFactory.getAir());
            } else {
                player.getPlayerInventory().removeItem(stack);
            }
        } catch (Throwable ignored) {
            player.getPlayerInventory().removeItem(stack);
        }

        player.forceUpdateInventory();
    }
}
