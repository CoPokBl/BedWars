package org.screamingsandals.bedwars.special.listener;

import org.screamingsandals.bedwars.api.config.ConfigurationContainer;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.GameManagerImpl;
import org.screamingsandals.bedwars.game.TeamImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.player.PlayerManagerImpl;
import org.screamingsandals.bedwars.special.GolemImpl;
import org.screamingsandals.bedwars.utils.DelayFactoryImpl;
import org.screamingsandals.bedwars.utils.ItemUtils;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.entity.EntityPathfindingMob;
import org.screamingsandals.lib.entity.EntityProjectile;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.entity.SEntityDamageByEntityEvent;
import org.screamingsandals.lib.event.entity.SEntityDeathEvent;
import org.screamingsandals.lib.event.entity.SEntityTargetEvent;
import org.screamingsandals.lib.event.player.SPlayerDeathEvent;
import org.screamingsandals.lib.event.player.SPlayerInteractEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.player.PlayerWrapper;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public class GolemListener {
    private static final String GOLEM_PREFIX = "Module:Golem:";

    @OnEvent
    public void onGolemRegister(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("golem")) {
            event.setStack(ItemUtils.saveData(event.getStack(), applyProperty(event)));
        }
    }

    @OnEvent
    public void onGolemUse(SPlayerInteractEvent event) {
        var player = event.getPlayer();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gamePlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gamePlayer.getGame();

        if (event.getAction() == SPlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.getAction() == SPlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (game != null && game.getStatus() == GameStatus.RUNNING && !gamePlayer.isSpectator() && event.getItem() != null) {
                var stack = event.getItem();
                var unhidden = ItemUtils.getIfStartsWith(stack, GOLEM_PREFIX);

                if (unhidden != null) {
                    if (!game.isDelayActive(gamePlayer, GolemImpl.class)) {
                        event.setCancelled(true);

                        final var propertiesSplit = unhidden.split(":");
                        var speed = Double.parseDouble(propertiesSplit[2]);
                        var follow = Double.parseDouble(propertiesSplit[3]);
                        var health = Double.parseDouble(propertiesSplit[4]);
                        var showName = Boolean.parseBoolean(propertiesSplit[5]);
                        var delay = Integer.parseInt(propertiesSplit[6]);
                        //boolean collidable = Boolean.parseBoolean(propertiesSplit[7]); //keeping this to keep configs compatible
                        var name = propertiesSplit[8];

                        var location = (event.getBlockClicked() == null) ? player.getLocation() : event.getBlockClicked().getLocation().add(event.getBlockFace().getDirection()).add(0.5, 0.5, 0.5);

                        var golem = new GolemImpl(game, gamePlayer, game.getPlayerTeam(gamePlayer),
                                stack, location, speed, follow, health, name, showName);

                        if (delay > 0) {
                            var delayFactory = new DelayFactoryImpl(delay, golem, gamePlayer, game);
                            game.registerDelay(delayFactory);
                        }

                        golem.spawn();
                    } else {
                        event.setCancelled(true);

                        var delay = game.getActiveDelay(gamePlayer, GolemImpl.class).getRemainDelay();
                        MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_ITEM_DELAY).placeholder("time", delay));
                    }
                }
            }
        }
    }

    @OnEvent
    public void onGolemDamage(SEntityDamageByEntityEvent event) {
        if (!event.getEntity().getEntityType().is("IRON_GOLEM")) {
            return;
        }

        var ironGolem = event.getEntity();
        for (var game : GameManagerImpl.getInstance().getGames()) {
            if (game.getStatus() == GameStatus.RUNNING && ironGolem.getLocation().getWorld().equals(game.getGameWorld())) {
                for (var golem : game.getActiveSpecialItems(GolemImpl.class)) {
                    if (golem.getEntity().equals(ironGolem)) {
                        if (event.getDamager() instanceof PlayerWrapper) {
                            var player = (PlayerWrapper) event.getDamager();
                            if (PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
                                if (golem.getTeam() != game.getTeamOfPlayer(player.as(BedWarsPlayer.class))) {
                                    return;
                                }
                            }
                        } else if (event.getDamager() instanceof EntityProjectile) {
                            var shooter = event.getDamager().as(EntityProjectile.class).getShooter();
                            if (shooter instanceof PlayerWrapper) {
                                var player = (PlayerWrapper) shooter;
                                if (PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
                                    if (golem.getTeam() != game.getTeamOfPlayer(player.as(BedWarsPlayer.class))) {
                                        return;
                                    }
                                }
                            }
                        }

                        event.setCancelled(game.getConfigurationContainer().getOrDefault(ConfigurationContainer.FRIENDLY_FIRE, Boolean.class, false));
                        return;
                    }
                }
            }
        }
    }

    @OnEvent
    public void onGolemTarget(SEntityTargetEvent event) {
        if (!event.getEntity().getEntityType().is("IRON_GOLEM")) {
            return;
        }

        var ironGolem = event.getEntity();
        for (var game : GameManagerImpl.getInstance().getGames()) {
            if ((game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) && ironGolem.getLocation().getWorld().equals(game.getGameWorld())) {
                for (var item : game.getActiveSpecialItems(GolemImpl.class)) {
                    if (item.getEntity().equals(ironGolem)) {
                        if (event.getTarget() instanceof PlayerWrapper) {
                            final var player = (PlayerWrapper) event.getTarget();

                            if (PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
                                var gPlayer = player.as(BedWarsPlayer.class);
                                if (game.isProtectionActive(gPlayer)) {
                                    event.setCancelled(true);
                                    return;
                                }

                                if (item.getTeam() == game.getTeamOfPlayer(gPlayer)) {
                                    event.setCancelled(true);
                                    // Try to find enemy
                                    var playerTarget = MiscUtils.findTarget((GameImpl) game, player, item.getFollowRange());
                                    if (playerTarget != null) {
                                        // Oh. We found enemy!
                                        ironGolem.as(EntityPathfindingMob.class).setCurrentTarget(playerTarget);
                                        return;
                                    }
                                }
                            } else {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @OnEvent
    public void onGolemTargetDie(SPlayerDeathEvent event) {
        if (PlayerManagerImpl.getInstance().isPlayerInGame(event.getPlayer())) {
            var game = PlayerManagerImpl.getInstance().getGameOfPlayer(event.getPlayer()).orElseThrow();

            for (var item : game.getActiveSpecialItems(GolemImpl.class)) {
                var iron = item.getEntity().as(EntityPathfindingMob.class);
                if (iron.getCurrentTarget().map(entityLiving -> entityLiving.equals(event.getPlayer())).orElse(false)) {
                    iron.setCurrentTarget(null);
                }
            }
        }
    }

    @OnEvent
    public void onGolemDeath(SEntityDeathEvent event) {
        if (!event.getEntity().getEntityType().is("IRON_GOLEM")) {
            return;
        }

        var ironGolem = event.getEntity();
        for (var game : GameManagerImpl.getInstance().getGames()) {
            if ((game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) && ironGolem.getLocation().getWorld().equals(game.getGameWorld())) {
                for (var item : game.getActiveSpecialItems(GolemImpl.class)) {
                    if (item.getEntity().equals(ironGolem)) {
                        event.getDrops().clear();
                    }
                }
            }
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return GOLEM_PREFIX
                + MiscUtils.getDoubleFromProperty(
                "speed", "specials.golem.speed", event) + ":"
                + MiscUtils.getDoubleFromProperty(
                "follow-range", "specials.golem.follow-range", event) + ":"
                + MiscUtils.getDoubleFromProperty(
                "health", "specials.golem.health", event) + ":"
                + MiscUtils.getBooleanFromProperty(
                "show-name", "specials.golem.show-name", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.golem.delay", event) + ":"
                + MiscUtils.getBooleanFromProperty("collidable",
                "specials.golem.collidable", event) + ":"
                + MiscUtils.getStringFromProperty(
                "name-format", "specials.golem.name-format", event);
    }
}
