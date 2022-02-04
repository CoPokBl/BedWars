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

package org.screamingsandals.bedwars.game;

import lombok.Data;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskerTask;

@Data
public class RespawnProtection implements Runnable {
    private final GameImpl game;
    private final BedWarsPlayer player;
    private final int seconds;
    private int length;
    private boolean running;
    private TaskerTask task;

    @Override
    public void run() {
    	if (!running) return;
        if (length > 0) {
            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.IN_GAME_RESPAWN_PROTECTION_REMAINING).placeholder("time", this.length));
        }
        if (length <= 0) {
            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.IN_GAME_RESPAWN_PROTECTION_END));
            game.removeProtectedPlayer(player);
            running = false;
        }
        length--;
    }

    public void runProtection() {
        if (!running) {
            running = true;
            this.task = Tasker.build(this)
                    .async()
                    .delay(5, TaskerTime.TICKS)
                    .repeat(20, TaskerTime.TICKS)
                    .start();
        }
    }
}

