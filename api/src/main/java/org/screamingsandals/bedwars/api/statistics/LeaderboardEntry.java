package org.screamingsandals.bedwars.api.statistics;

import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.lib.utils.Wrapper;

@ApiStatus.NonExtendable
public interface LeaderboardEntry {
    Wrapper getPlayer();

    int getTotalScore();

    PlayerStatistic fetchStatistics();
}
