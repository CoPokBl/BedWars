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

package org.screamingsandals.bedwars.utils;

import lombok.experimental.UtilityClass;
import org.screamingsandals.lib.utils.Wrapper;
import org.screamingsandals.lib.world.LocationHolder;
import org.screamingsandals.lib.world.chunk.ChunkHolder;

@UtilityClass
public class ArenaUtils {

    public boolean isInArena(LocationHolder l, LocationHolder p1, LocationHolder p2) {
        if (!p1.getWorld().equals(l.getWorld())) {
            return false;
        }

        var min = new LocationHolder(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ()), 0, 0, p1.getWorld());
        var max = new LocationHolder(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ()), 0, 0, p1.getWorld());
        return (min.getX() <= l.getX() && min.getY() <= l.getY() && min.getZ() <= l.getZ() && max.getX() >= l.getX()
                && max.getY() >= l.getY() && max.getZ() >= l.getZ());
    }

    public boolean isInArea(Wrapper w, Wrapper w1, Wrapper w2) {
        return isInArena(w.as(LocationHolder.class), w1.as(LocationHolder.class), w2.as(LocationHolder.class));
    }

    public boolean isChunkInArena(LocationHolder l, LocationHolder p1, LocationHolder p2) {
        if (!p1.getWorld().equals(l.getWorld())) {
            return false;
        }

        var min = new LocationHolder(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ()), 0, 0, p1.getWorld()).getChunk();
        var max = new LocationHolder(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ()), 0, 0, p1.getWorld()).getChunk();
        return (min.getX() <= l.getX() && min.getZ() <= l.getZ() && max.getX() >= l.getX() && max.getZ() >= l.getZ());
    }

    public boolean isChunkInArea(Wrapper w, Wrapper w1, Wrapper w2) {
        return isChunkInArena(w.as(LocationHolder.class), w1.as(LocationHolder.class), w2.as(LocationHolder.class));
    }
}
