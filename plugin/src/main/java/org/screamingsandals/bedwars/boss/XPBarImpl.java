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

package org.screamingsandals.bedwars.boss;

import lombok.Getter;
import org.screamingsandals.bedwars.api.boss.XPBar;
import org.screamingsandals.bedwars.lib.nms.entity.PlayerUtils;
import org.screamingsandals.lib.player.PlayerMapper;
import org.screamingsandals.lib.player.PlayerWrapper;

import java.util.ArrayList;
import java.util.List;

@Getter
public class XPBarImpl implements XPBar {
    private final List<PlayerWrapper> viewers = new ArrayList<>();
    private boolean visible = false;
    private float progress = 0F;
    private int seconds = 0;

    @Override
    public void addPlayer(Object player) {
        final var playerWrapper = PlayerMapper.wrapPlayer(player);
        if (!viewers.contains(playerWrapper)) {
            viewers.add(playerWrapper);
            if (visible) {
                PlayerUtils.fakeExp(playerWrapper, progress, seconds);
            }
        }
    }

    @Override
    public void removePlayer(Object player) {
        final var playerWrapper = PlayerMapper.wrapPlayer(player);
        if (viewers.contains(playerWrapper)) {
            viewers.remove(playerWrapper);
            PlayerUtils.fakeExp(playerWrapper, playerWrapper.getExp(), playerWrapper.getLevel());
        }
    }

    @Override
    public void setProgress(float progress) {
        if (Double.isNaN(progress) || progress < 0) {
            progress = 0;
        } else if (progress > 1) {
            progress = 1;
        }
        this.progress = progress;
        if (visible) {
            for (var player : viewers) {
            	PlayerUtils.fakeExp(player, this.progress, seconds);
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            if (visible) {
                for (var player : viewers) {
                	PlayerUtils.fakeExp(player, progress, seconds);
                }
            } else {
                for (var player : viewers) {
                	PlayerUtils.fakeExp(player, player.getExp(), player.getLevel());
                }
            }
        }
        this.visible = visible;
    }

    @Override
    public void setSeconds(int seconds) {
        this.seconds = seconds;
        if (visible) {
            for (var player : viewers) {
            	PlayerUtils.fakeExp(player, this.progress, seconds);
            }
        }
    }
}
