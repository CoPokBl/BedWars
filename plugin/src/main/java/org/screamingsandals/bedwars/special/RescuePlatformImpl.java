package org.screamingsandals.bedwars.special;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.screamingsandals.bedwars.api.special.RescuePlatform;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.TeamImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.block.BlockTypeHolder;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.item.Item;
import org.screamingsandals.lib.item.builder.ItemFactory;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskerTask;
import org.screamingsandals.lib.utils.BlockFace;
import org.screamingsandals.lib.block.BlockHolder;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
public class RescuePlatformImpl extends SpecialItem implements RescuePlatform {
    private final Item item;
    private List<BlockHolder> platformBlocks;
    private BlockTypeHolder material;
    private boolean breakable;
    private int breakingTime;
    private int livingTime;
    private TaskerTask task;

    public RescuePlatformImpl(GameImpl game, BedWarsPlayer player, TeamImpl team, Item item) {
        super(game, player, team);
        this.item = item;
    }

    @Override
    public void runTask() {
        this.task = Tasker.build(() -> {
            livingTime++;
            int time = breakingTime - livingTime;

            if (time < 6 && time > 0) {
                MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_RESCUE_PLATFORM_DESTROY).placeholder("time", time));
            }

            if (livingTime == breakingTime) {
                for (var block : List.copyOf(platformBlocks)) {
                    block.getLocation().getChunk().load(false);
                    block.setType(BlockTypeHolder.air());

                    removeBlockFromList(block);
                    game.getRegion().removeBlockBuiltDuringGame(block.getLocation());

                }
                game.unregisterSpecialItem(this);
                this.task.cancel();
            }
        })
        .delay(20, TaskerTime.TICKS)
        .repeat(20, TaskerTime.TICKS)
        .start();
    }

    private void addBlockToList(BlockHolder block) {
        platformBlocks.add(block);
        game.getRegion().addBuiltDuringGame(block.getLocation());
    }

    private void removeBlockFromList(BlockHolder block) {
        platformBlocks.remove(block);
        game.getRegion().removeBlockBuiltDuringGame(block.getLocation());
    }

    public void createPlatform(boolean bre, int time, int dist, BlockTypeHolder bMat) {
        breakable = bre;
        breakingTime = time;
        material = bMat;
        platformBlocks = new ArrayList<>();

        var center = player.getLocation().clone();
        center.setY(center.getY() - dist);

        for (var blockFace : BlockFace.values()) {
            if (blockFace.equals(BlockFace.DOWN) || blockFace.equals(BlockFace.UP)) {
                continue;
            }

            var placedBlock = center.add(blockFace.getDirection()).getBlock();
            if (!placedBlock.getType().isAir()) {
                continue;
            }

            var coloredMatrerial = material.colorize(team.getColor().material1_13);
            placedBlock.setType(coloredMatrerial);
            addBlockToList(placedBlock);
        }

        if (breakingTime > 0) {
            game.registerSpecialItem(this);
            runTask();

            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_RESCUE_PLATFORM_CREATED).placeholder("time", breakingTime));

            var stack = item.withAmount(1);
            try {
                if (player.getPlayerInventory().getItemInOffHand().equals(stack)) {
                    player.getPlayerInventory().setItemInOffHand(ItemFactory.getAir());
                } else {
                    player.getPlayerInventory().removeItem(stack);
                }
            } catch (Throwable e) {
                player.getPlayerInventory().removeItem(stack);
            }
            player.forceUpdateInventory();
        } else {
            game.registerSpecialItem(this);

            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_RESCUE_PLATFORM_CREATED_UNBREAKABLE));
            var stack = item.withAmount(1);
            try {
                if (player.getPlayerInventory().getItemInOffHand().equals(stack)) {
                    player.getPlayerInventory().setItemInOffHand(ItemFactory.getAir());
                } else {
                    player.getPlayerInventory().removeItem(stack);
                }
            } catch (Throwable e) {
                player.getPlayerInventory().removeItem(stack);
            }
            player.forceUpdateInventory();
        }
    }
}
