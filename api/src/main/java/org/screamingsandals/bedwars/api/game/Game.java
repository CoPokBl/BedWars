package org.screamingsandals.bedwars.api.game;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.bedwars.api.ArenaTime;
import org.screamingsandals.bedwars.api.Region;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.api.boss.StatusBar;
import org.screamingsandals.bedwars.api.config.ConfigurationContainer;
import org.screamingsandals.bedwars.api.player.BWPlayer;
import org.screamingsandals.bedwars.api.special.SpecialItem;
import org.screamingsandals.bedwars.api.utils.DelayFactory;
import org.screamingsandals.bedwars.api.variants.Variant;
import org.screamingsandals.lib.utils.Wrapper;

import java.io.File;
import java.util.List;
import java.util.UUID;


/**
 * @author ScreamingSandals
 */
@ApiStatus.NonExtendable
public interface Game {
    /**
     * @return arena's unique id
     */
    UUID getUuid();

    /**
     * @return Arena name
     */
    String getName();

    /**
     * @return display name of the arena or null if there's no display name
     */
    @Nullable String getDisplayName();

    /**
     * @return GameStatus of the arena
     */
    GameStatus getStatus();

    /**
     *
     */
    void start();

    /**
     *
     */
    void stop();

    /**
     * @return true if GameStatus is different than DISABLED
     */
    default boolean isActivated() {
        return getStatus() != GameStatus.DISABLED;
    }

    // PLAYER MANAGEMENT

    /**
     * @param player
     */
    void joinToGame(BWPlayer player);

    /**
     * @param player
     */
    void leaveFromGame(BWPlayer player);

    /**
     * @param player
     * @param team
     */
    void selectPlayerTeam(BWPlayer player, Team team);

    /**
     * @param player
     */
    void selectPlayerRandomTeam(BWPlayer player);

    /**
     * @return defined world of the game
     */
    Wrapper getGameWorld();

    /**
     * @return
     */
    Wrapper getPos1();

    /**
     * @return
     */
    Wrapper getPos2();

    /**
     * @return
     */
    Wrapper getSpectatorSpawn();

    /**
     * @return configured time of the game
     */
    int getGameTime();

    /**
     * @return configured minimal players to start the game
     */
    int getMinPlayers();

    /**
     * @return configured maximal players of the arena
     */
    int getMaxPlayers();

    /**
     * @return players in game
     */
    int countConnectedPlayers();

    /**
     * @return list of players in game
     */
    List<? extends BWPlayer> getConnectedPlayers();

    /**
     * @return list of game stores
     */
    List<GameStore> getGameStores();

    /**
     * @return
     */
    int countGameStores();

    /**
     * @return Team instance from the name
     */
    Team getTeamFromName(String name);

    /**
     * @return
     */
    List<Team> getAvailableTeams();

    /**
     * @return
     */
    int countAvailableTeams();

    /**
     * @return
     */
    List<Team> getActiveTeams();

    /**
     * @return
     */
    int countActiveTeams();

    /**
     * @param player
     * @return
     */
    Team getTeamOfPlayer(BWPlayer player);

    /**
     * @param player
     * @return
     */
    boolean isPlayerInAnyTeam(BWPlayer player);

    boolean isTeamActive(Team team);

    /**
     * @param player
     * @param team
     * @return
     */
    boolean isPlayerInTeam(BWPlayer player, Team team);

    /**
     * @param location
     * @return
     */
    boolean isLocationInArena(Object location);

    /**
     * @param location
     * @return
     */
    boolean isBlockAddedDuringGame(Object location);

    /**
     * @return
     */
    List<SpecialItem> getActiveSpecialItems();

    /**
     * @param type
     * @return
     */
    <I extends SpecialItem> List<I> getActiveSpecialItems(Class<I> type);

    /**
     * @param team
     * @return
     */
    List<SpecialItem> getActiveSpecialItemsOfTeam(Team team);

    /**
     * @param team
     * @param type
     * @return
     */
    <I extends SpecialItem> List<I> getActiveSpecialItemsOfTeam(Team team, Class<I> type);

    /**
     * @param team
     * @return
     */
    SpecialItem getFirstActiveSpecialItemOfTeam(Team team);

    /**
     * @param team
     * @param type
     * @return
     */
    <I extends SpecialItem> I getFirstActiveSpecialItemOfTeam(Team team, Class<I> type);

    /**
     * @param player
     * @return
     */
    List<SpecialItem> getActiveSpecialItemsOfPlayer(BWPlayer player);

    /**
     * @param player
     * @param type
     * @return
     */
    <I extends SpecialItem> List<I> getActiveSpecialItemsOfPlayer(BWPlayer player, Class<I> type);

    /**
     * @param player
     * @return
     */
    SpecialItem getFirstActiveSpecialItemOfPlayer(BWPlayer player);

    /**
     * @param player
     * @param type
     * @return
     */
    <I extends SpecialItem> I getFirstActiveSpecialItemOfPlayer(BWPlayer player, Class<I> type);

    /**
     * @return
     */
    List<DelayFactory> getActiveDelays();

    /**
     * @param player
     * @return
     */
    List<DelayFactory> getActiveDelaysOfPlayer(BWPlayer player);

    /**
     * @param player
     * @param specialItem
     * @return
     */
    DelayFactory getActiveDelay(BWPlayer player, Class<? extends SpecialItem> specialItem);

    /**
     * @param delayFactory
     */
    void registerDelay(DelayFactory delayFactory);

    /**
     * @param delayFactory
     */
    void unregisterDelay(DelayFactory delayFactory);

    /**
     * @param player
     * @param specialItem
     * @return
     */
    boolean isDelayActive(BWPlayer player, Class<? extends SpecialItem> specialItem);

    /**
     * @param item
     */
    void registerSpecialItem(SpecialItem item);

    /**
     * @param item
     */
    void unregisterSpecialItem(SpecialItem item);

    /**
     * @param item
     * @return
     */
    boolean isRegisteredSpecialItem(SpecialItem item);

    /**
     * @return
     */
    List<ItemSpawner> getItemSpawners();

    /**
     * @return
     */
    Region getRegion();

    /**
     * @return
     */
    StatusBar getStatusBar();

    // LOBBY

    /**
     * @return
     */
    Wrapper getLobbyWorld();

    /**
     * @return
     */
    Wrapper getLobbySpawn();

    /**
     * @return
     */
    int getLobbyCountdown();

    /**
     * @return
     */
    int countTeamChests();

    /**
     * @param team
     * @return
     */
    int countTeamChests(Team team);

    /**
     * @param location
     * @return
     */
    Team getTeamOfChest(Object location);

    /**
     * @param entity
     * @return
     */
    boolean isEntityShop(Object entity);

    /**
     * @return
     */
    boolean getBungeeEnabled();

    /**
     * @return
     */
    ArenaTime getArenaTime();

    /**
     * @return
     */
    Wrapper getArenaWeather();

    /**
     * @return
     */
    Wrapper getLobbyBossBarColor();

    /**
     * @return
     */
    Wrapper getGameBossBarColor();

    /**
     * @return
     */
    boolean isProtectionActive(BWPlayer player);

    int getPostGameWaiting();

    default boolean hasCustomPrefix() {
        return getCustomPrefix() != null;
    }

    String getCustomPrefix();

    @Nullable Variant getGameVariant();

    /**
     * Returns configuration container for this game
     *
     * @return game's configuration container
     * @since 0.3.0
     */
    ConfigurationContainer getConfigurationContainer();

    /**
     * Checks if game is in edit mode
     *
     * @return true if game is in edit mode
     * @since 0.3.0
     */
    boolean isInEditMode();

    /**
     * This methods allows you to save the arena to config (useful when using custom config options)
     *
     * @since 0.3.0
     */
    void saveToConfig();

    /**
     * Gets file with this game
     *
     * @return file where game is saved
     * @since 0.3.0
     */
    File getFile();

    /**
     * @return
     * @since 0.3.0
     */
    Wrapper getCustomPrefixComponent();

    Wrapper getDisplayNameComponent();

    /**
     * @return
     * @since 0.3.0
     */
    @Nullable Wrapper getLobbyPos1();

    /**
     * @return
     * @since 0.3.0
     */
    @Nullable Wrapper getLobbyPos2();

    /**
     * @return
     * @since 0.3.0
     */
    double getFee();

    /**
     * @return
     * since 0.3.0
     */
    int countPlayers();
}
