package org.screamingsandals.bedwars.listener;

import org.screamingsandals.bedwars.BedWarsPlugin;
import org.screamingsandals.bedwars.api.config.ConfigurationContainer;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.config.MainConfig;
import org.screamingsandals.bedwars.entities.EntitiesManagerImpl;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.GameManagerImpl;
import org.screamingsandals.bedwars.game.TeamImpl;
import org.screamingsandals.bedwars.utils.ArenaUtils;
import org.screamingsandals.lib.event.Cancellable;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.block.*;
import org.screamingsandals.lib.event.chunk.SChunkUnloadEvent;
import org.screamingsandals.lib.event.entity.SCreatureSpawnEvent;
import org.screamingsandals.lib.event.entity.SEntityChangeBlockEvent;
import org.screamingsandals.lib.event.entity.SEntityExplodeEvent;
import org.screamingsandals.lib.event.world.SPlantGrowEvent;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.block.BlockHolder;
import org.screamingsandals.lib.world.LocationHolder;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class WorldListener {

    @OnEvent
    public void onBurn(SBlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        onBlockChange(event.getBlock(), event);
    }

    @OnEvent
    public void onFade(SBlockFadeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        onBlockChange(event.getBlock(), event);
    }

    @OnEvent
    public void onLeavesDecay(SLeavesDecayEvent event) {
        if (event.isCancelled()) {
            return;
        }

        onBlockChange(event.getBlock(), event);
    }

    @OnEvent
    public void onGrow(SBlockGrowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getNewBlockState().getType().isSameType("SNOW")) {
            return;
        }

        onBlockChange(event.getBlock(), event);
    }

    public void onBlockChange(BlockHolder block, Cancellable cancellable) {
        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (ArenaUtils.isInArea(block.getLocation(), game.getPos1(), game.getPos2())) {
                    if (!BedWarsPlugin.isFarmBlock(block.getType()) && !game.isBlockAddedDuringGame(block.getLocation())) {
                        cancellable.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    @OnEvent
    public void onFertilize(SBlockFertilizeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        event.getChangedBlockStates().removeIf(blockState -> {
            for (var game : GameManagerImpl.getInstance().getGames()) {
                if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                    if (ArenaUtils.isInArea(blockState.getLocation(), game.getPos1(), game.getPos2())) {
                        return !game.isBlockAddedDuringGame(blockState.getLocation());
                    }
                }
            }
            return false;
        });
    }

    @OnEvent
    public void onExplode(SEntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        onExplode(event.getLocation(), event.getBlocks(), event);
    }

    @OnEvent
    public void onExplode(SBlockExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        onExplode(event.getBlock().getLocation(), event.getDestroyed(), event);
    }

    public void onExplode(LocationHolder location, Collection<BlockHolder> blockList, org.screamingsandals.lib.event.Cancellable cancellable) {
        final var explosionExceptionTypeName = MainConfig.getInstance().node("destroy-placed-blocks-by-explosion-except").childrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
        final var destroyPlacedBlocksByExplosion = MainConfig.getInstance().node("destroy-placed-blocks-by-explosion").getBoolean(true);

        GameManagerImpl.getInstance().getGames().forEach(game -> {
            if (ArenaUtils.isInArea(location, game.getPos1(), game.getPos2())) {
                if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                    blockList.removeIf(block -> {
                        if (!game.isBlockAddedDuringGame(block.getLocation())) {
                            if (game.getConfigurationContainer().getOrDefault(ConfigurationContainer.TARGET_BLOCK_EXPLOSIONS, Boolean.class, false)) {
                                for (var team : game.getActiveTeams()) {
                                    if (team.getTargetBlock().equals(block.getLocation())) {
                                        ((GameImpl) game).targetBlockExplode((TeamImpl) team);
                                        break;
                                    }
                                }
                            }
                            return true;
                        }
                        return explosionExceptionTypeName.contains(block.getType().platformName()) || !destroyPlacedBlocksByExplosion;
                    });
                } else {
                    cancellable.setCancelled(true);
                }
            }
        });
    }

    @OnEvent
    public void onIgnite(SBlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (ArenaUtils.isInArea(event.getBlock().getLocation(), game.getPos1(), game.getPos2())) {
                    game.getRegion().addBuiltDuringGame(event.getBlock().getLocation());
                    return;
                }
            }
        }
    }

    @OnEvent
    public void onStructureGrow(SPlantGrowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (ArenaUtils.isInArea(event.getLocation(), game.getPos1(), game.getPos2())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @OnEvent
    public void onCreatureSpawn(SCreatureSpawnEvent event) {
        if (event.isCancelled() || event.getSpawnReason() == SCreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (game.getStatus() != GameStatus.DISABLED)
                // prevent creature spawn everytime, not just in game
                if (/*(game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) &&*/ game.getConfigurationContainer().getOrDefault(ConfigurationContainer.PREVENT_SPAWNING_MOBS, Boolean.class, false)) {
                    if (ArenaUtils.isInArea(event.getEntity().getLocation(), game.getPos1(), game.getPos2())) {
                        event.setCancelled(true);
                        return;
                        //}
                    } else /*if (game.getStatus() == GameStatus.WAITING) {*/
                        if (game.getLobbyWorld().equals(event.getEntity().getLocation().getWorld())) {
                            if (event.getEntity().getLocation().getDistanceSquared(game.getLobbySpawn().as(LocationHolder.class)) <= Math
                                    .pow(MainConfig.getInstance().node("prevent-lobby-spawn-mobs-in-radius").getInt(), 2)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                }
        }
    }

    @OnEvent
    public void onLiquidFlow(SBlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (ArenaUtils.isInArea(event.getSourceBlock().getLocation(), game.getPos1(), game.getPos2())) {
                if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                    var block = event.getFacedBlock();
                    if (block.getType().isAir()
                            || game.isBlockAddedDuringGame(block.getLocation())) {
                        game.getRegion().addBuiltDuringGame(block.getLocation());
                    } else {
                        event.setCancelled(true);
                    }
                } else if (game.getStatus() != GameStatus.DISABLED) {
                    event.setCancelled(true);
                }
            }
        }

    }

    @OnEvent
    public void onEntityChangeBlock(SEntityChangeBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (ArenaUtils.isInArea(event.getBlock().getLocation(), game.getPos1(), game.getPos2())) {
                if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                    if (event.getEntity().getEntityType().is("FALLING_BLOCK")
                            && game.getConfigurationContainer().getOrDefault(ConfigurationContainer.BLOCK_FALLING, Boolean.class, false)) {
                        if (!event.getBlock().getType().equals(event.getTo())) {
                            if (!game.isBlockAddedDuringGame(event.getBlock().getLocation())) {
                                if (!event.getBlock().getType().isAir()) {
                                    game.getRegion().putOriginalBlock(event.getBlock().getLocation(),
                                            event.getBlock().getBlockState().orElseThrow());
                                }
                                game.getRegion().addBuiltDuringGame(event.getBlock().getLocation());
                            }
                        }
                        return; // allow block fall
                    }
                }

                if (game.getStatus() != GameStatus.DISABLED) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @OnEvent(priority = org.screamingsandals.lib.event.EventPriority.HIGHEST)
    public void onBedWarsSpawnIsCancelled(SCreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        // Fix for uSkyBlock plugin
        if (event.getSpawnReason() == SCreatureSpawnEvent.SpawnReason.CUSTOM && EntitiesManagerImpl.getInstance().isEntityInGame(event.getEntity())) {
            event.setCancelled(false);
        }
    }

    @OnEvent
    public void onChunkUnload(SChunkUnloadEvent unload) {
        var chunk = unload.getChunk();

        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (game.getStatus() != GameStatus.DISABLED && game.getStatus() != GameStatus.WAITING
                    && ArenaUtils.isChunkInArea(chunk, game.getPos1(), game.getPos2())) {
                unload.setCancelled(true);
                return;
            }
        }
    }
}
