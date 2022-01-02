package org.screamingsandals.bedwars.game;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameManager;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.variants.VariantManagerImpl;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.annotations.parameters.DataFolder;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service(dependsOn = {
        VariantManagerImpl.class // it's important to have variant manager loaded before games manager
})
@RequiredArgsConstructor
public class GameManagerImpl implements GameManager {
    @DataFolder("arenas")
    private final Path arenasFolder;
    private final LoggerWrapper logger;
    private final List<GameImpl> games = new LinkedList<>();

    public static GameManagerImpl getInstance() {
        return ServiceManager.get(GameManagerImpl.class);
    }

    @Override
    public Optional<Game> getGame(String name) {
        try {
            var uuid = UUID.fromString(name);
            return getGame(uuid);
        } catch (Throwable ignored) {
            return games.stream().filter(game -> game.getName().equals(name)).findFirst().map(game -> game);
        }
    }

    @Override
    public Optional<Game> getGame(UUID uuid) {
        return games.stream().filter(game -> game.getUuid().equals(uuid)).findFirst().map(game -> game);
    }

    @Override
    public List<Game> getGames() {
        return List.copyOf(games);
    }

    @Override
    public List<String> getGameNames() {
        return games.stream().map(GameImpl::getName).collect(Collectors.toList());
    }

    @Override
    public boolean hasGame(String name) {
        return getGame(name).isPresent();
    }

    @Override
    public boolean hasGame(UUID uuid) {
        return getGame(uuid).isPresent();
    }

    @Override
    public Optional<Game> getGameWithHighestPlayers(boolean fee) {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .filter(game -> game.countConnectedPlayers() < game.getMaxPlayers())
                .filter(game -> {
                    if (fee) {
                        return game.getFee() > 0;
                    }
                    return true;
                })
                .max(Comparator.comparingInt(GameImpl::countConnectedPlayers))
                .map(game -> game);
    }

    @Override
    public Optional<Game> getGameWithLowestPlayers(boolean fee) {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .filter(game -> game.countConnectedPlayers() < game.getMaxPlayers())
                .filter(game -> {
                    if (fee) {
                        return game.getFee() > 0;
                    }
                    return true;
                })
                .min(Comparator.comparingInt(GameImpl::countConnectedPlayers))
                .map(game -> game);
    }

    @Override
    public Optional<Game> getFirstWaitingGame(boolean fee) {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .filter(game -> {
                    if (fee) {
                        return game.getFee() > 0;
                    }
                    return true;
                })
                .max(Comparator.comparingInt(GameImpl::countConnectedPlayers))
                .map(game -> game);
    }

    @Override
    public Optional<Game> getFirstRunningGame(boolean fee) {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING)
                .filter(game -> {
                    if (fee) {
                        return game.getFee() > 0;
                    }
                    return true;
                })
                .max(Comparator.comparingInt(GameImpl::countConnectedPlayers))
                .map(game -> game);
    }

    public void addGame(@NotNull GameImpl game) {
        if (!games.contains(game)) {
            games.add(game);
        }
    }

    public void removeGame(@NotNull GameImpl game) {
        games.remove(game);
    }

    @OnPostEnable
    public void onPostEnable() {
        if (Files.exists(arenasFolder)) {
            try (var stream = Files.walk(arenasFolder.toAbsolutePath())) {
                final var results = stream.filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
                if (results.isEmpty()) {
                    logger.debug("No arenas have been found!");
                } else {
                    results.forEach(file -> {
                        if (file.exists() && file.isFile() && !file.getName().toLowerCase().endsWith(".disabled")) {
                            var game = GameImpl.loadGame(file);
                            if (game != null) {
                                games.add(game);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnPreDisable
    public void onPreDisable() {
        games.forEach(GameImpl::stop);
        games.clear();
    }
}
