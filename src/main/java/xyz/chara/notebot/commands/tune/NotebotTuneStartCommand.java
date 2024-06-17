/*
This file is part of Notebot.
Notebot is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
Notebot is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License along with Notebot. If not, see <https://www.gnu.org/licenses/>.
*/

package xyz.chara.notebot.commands.tune;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registries;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import xyz.chara.notebot.NotebotPlayer;
import xyz.chara.notebot.suggestions.SongSuggestionProvider;
import xyz.chara.notebot.types.Note;
import xyz.chara.notebot.types.Song;
import xyz.chara.notebot.utils.NotebotFileManager;
import xyz.chara.notebot.utils.NotebotUtils;

import static xyz.chara.notebot.Notebot.mc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NotebotTuneStartCommand {
    public static boolean IsTuning = false;
    /* Status */
    public static boolean playing = false;

    /* Some settings */
    public static boolean loop = false;

    /* The loaded song */
    public static Song song;
    public static List<String> trail = new ArrayList<>();
    public static List<String> queue = new ArrayList<>();

    /* Map of noteblocks and their pitch around the player [blockpos:pitch] */
    private static Map<BlockPos, Integer> blockPitches = new HashMap<>();
    private static int tuneDelay = 0;

    private static Map<BlockPos, Integer> noteblockPitches = new HashMap<>();
    private static Map<BlockPos, Integer> noteblockTries = new HashMap<>();
    private static HashSet<BlockPos> noteblockIgnore = new HashSet<>();

    public static void register(CommandDispatcher<FabricClientCommandSource> clientCommandSourceCommandDispatcher,
            CommandRegistryAccess commandRegistryAccess) {
        clientCommandSourceCommandDispatcher.register(
                ClientCommandManager.literal("notebot")
                        .then(ClientCommandManager.literal("tune")
                                .then(ClientCommandManager.literal("start")
                                        .then(ClientCommandManager.argument("song", StringArgumentType.greedyString())
                                                .suggests(new SongSuggestionProvider())
                                                .executes(NotebotTuneStartCommand::run)))));
    }

    private static int run(CommandContext<FabricClientCommandSource> context) {
        NotebotPlayer.stop();
        song = NotebotUtils.parse(
                NotebotFileManager.getDir().resolve(
                        "songs/" + context.getArgument("song", String.class)));
        IsTuning = true;
        loadSong();
        mc.player.sendMessage(
                Text.literal("§6Started tuning the song §a" + song.filename + "§6."));
        return 1;
    }

    public static int getNote(BlockPos pos) {
        if (!isNoteblock(pos))
            return -1;

        return mc.world.getBlockState(pos).get(NoteBlock.NOTE);
    }

    public static NoteBlockInstrument getNoteBlockInstrumentUnderneath(BlockPos pos) {
        if (!isNoteblock(pos))
            return NoteBlockInstrument.HARP;

        // Retrieve the block underneath
        BlockPos posUnderneath = pos.down();
        Block blockUnderneath = mc.world.getBlockState(posUnderneath).getBlock();

        // Return the NoteBlockInstrument associated with the block underneath
        return blockToNoteBlockInstrument(blockUnderneath);
    }

    public static NoteBlockInstrument blockToNoteBlockInstrument(Block block) {

        // Specific block checks
        Identifier blockId = Registries.BLOCK.getId(block);
        String blockIdString = blockId.toString();
        NoteBlockInstrument instrument = NoteBlockInstrument.HARP; // Default to Harp for any other block

        if (blockIdString.equals("minecraft:dirt") || blockIdString.equals("minecraft:air")) {
            return NoteBlockInstrument.HARP;
        } else if (blockIdString.equals("minecraft:clay")) {
            return NoteBlockInstrument.FLUTE;
        } else if (blockIdString.equals("minecraft:gold_block")) {
            return NoteBlockInstrument.BELL;
        } else if (blockIdString.equals("minecraft:packed_ice")) {
            return NoteBlockInstrument.CHIME;
        } else if (blockIdString.equals("minecraft:bone_block")) {
            return NoteBlockInstrument.XYLOPHONE;
        } else if (blockIdString.equals("minecraft:iron_block")) {
            return NoteBlockInstrument.IRON_XYLOPHONE;
        } else if (blockIdString.equals("minecraft:soul_sand")) {
            return NoteBlockInstrument.COW_BELL;
        } else if (blockIdString.equals("minecraft:pumpkin")) {
            return NoteBlockInstrument.DIDGERIDOO;
        } else if (blockIdString.equals("minecraft:emerald_block")) {
            return NoteBlockInstrument.BIT;
        } else if (blockIdString.equals("minecraft:hay_block")) {
            return NoteBlockInstrument.BANJO;
        } else if (blockIdString.equals("minecraft:glowstone")) {
            return NoteBlockInstrument.PLING;
        } else if (blockIdString.equals("minecraft:sand") || blockIdString.equals("minecraft:gravel")
                || blockIdString.equals("minecraft:concrete_powder")) {
            return NoteBlockInstrument.SNARE;
        } else if (Arrays.asList("minecraft:stone", "minecraft:cobblestone", "minecraft:blackstone",
                "minecraft:netherrack", "minecraft:nylium", "minecraft:obsidian",
                "minecraft:quartz", "minecraft:sandstone", "minecraft:ores", "minecraft:bricks", "minecraft:corals",
                "minecraft:respawn_anchor", "minecraft:bedrock", "minecraft:concrete").contains(blockIdString)) {
            return NoteBlockInstrument.BASEDRUM;
        } else if (blockIdString.equals("minecraft:glass")) {
            return NoteBlockInstrument.HAT;
        }

        BlockSoundGroup material = block.getDefaultState().getSoundGroup();

        // Check for blocks with specific materials
        if (material.equals(BlockSoundGroup.WOOD)) {
            return NoteBlockInstrument.BASS;
        }
        if (material.equals(BlockSoundGroup.WOOL)) {
            return NoteBlockInstrument.GUITAR;
        }
        if (material.equals(BlockSoundGroup.GLASS)) {
            return NoteBlockInstrument.HAT;
        }
        if (material.equals(BlockSoundGroup.STONE)) {
            return NoteBlockInstrument.BASEDRUM;
        }

        return instrument;
    }

    public static boolean isNoteblock(BlockPos pos) {
        // Checks if this block is a noteblock and the noteblock can be played
        return mc.world.getBlockState(pos).getBlock() instanceof NoteBlock && mc.world.getBlockState(pos.up()).isAir();
    }

    public static void stop() {
        playing = false;
        song = null;
        blockPitches.clear();
        noteblockPitches.clear();
        noteblockTries.clear();
        noteblockIgnore.clear();
        tuneDelay = 0;
        IsTuning = false;
    }

    public static boolean loadSong() {
        blockPitches.clear();
        noteblockPitches.clear();
        noteblockTries.clear();
        noteblockIgnore.clear();

        try {
            if (!mc.interactionManager.getCurrentGameMode().isSurvivalLike()) {
                stop();
                mc.player.sendMessage(Text.literal("§cNot in Survival mode!"));

                return false;
            } else if (song == null) {
                stop();
                mc.player.sendMessage(Text.literal("§6No song in queue!, Use §c/notebot queue add §6to add a song."));

                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }

        BlockPos playerEyePos = new BlockPos((int) mc.player.getEyePos().x, (int) mc.player.getEyePos().y,
                (int) mc.player.getEyePos().z);

        var noteblocks = BlockPos.streamOutwards(playerEyePos, 5, 5, 5)
                .filter(NotebotPlayer::isNoteblock)
                .map(BlockPos::toImmutable)
                .toArray(BlockPos[]::new);

        Arrays.sort(noteblocks,
                Comparator.comparing(BlockPos::getX).thenComparing(BlockPos::getZ).thenComparing(BlockPos::getY));

        HashMap<NoteBlockInstrument, Integer> requiredNoteBlockInstruments = new HashMap<>();
        HashMap<NoteBlockInstrument, Integer> foundNoteBlockInstruments = new HashMap<>();

        var reqr = song.requirements.toArray(Note[]::new);
        Arrays.sort(reqr, Comparator.comparing(Note::getNoteBlockInstrument).thenComparing(Note::getPitch));

        for (Note note : reqr) {
            NoteBlockInstrument instrument = NoteBlockInstrument.values()[note.NoteBlockInstrument];
            requiredNoteBlockInstruments.put(instrument, requiredNoteBlockInstruments.getOrDefault(instrument, 0) + 1);
            for (BlockPos pos : noteblocks) {
                if (blockPitches.containsKey(pos))
                    continue;

                NoteBlockInstrument blockNoteBlockInstrument = getNoteBlockInstrumentUnderneath(pos);
                if (note.NoteBlockInstrument == blockNoteBlockInstrument.ordinal()
                        && blockPitches.entrySet().stream().filter(e -> e.getValue() == note.pitch).noneMatch(
                                e -> getNoteBlockInstrumentUnderneath(e.getKey()).ordinal() == blockNoteBlockInstrument
                                        .ordinal())) {
                    blockPitches.put(pos, note.pitch);
                    foundNoteBlockInstruments.put(blockNoteBlockInstrument,
                            foundNoteBlockInstruments.getOrDefault(blockNoteBlockInstrument, 0) + 1);
                    break;
                }
            }
        }

        for (NoteBlockInstrument NoteBlockInstrument : requiredNoteBlockInstruments.keySet()) {
            int requiredCount = requiredNoteBlockInstruments.get(NoteBlockInstrument);
            int foundCount = foundNoteBlockInstruments.getOrDefault(NoteBlockInstrument, 0);
            int missingCount = requiredCount - foundCount;

            if (missingCount > 0) {
                mc.player.sendMessage(
                        Text.literal(
                                "§6Warning: Missing §c" + missingCount + " §6" + NoteBlockInstrument + " Noteblocks"));
            }
        }

        return true;
    }

    public static void onTick(MinecraftClient client) {
        if (mc.world == null || mc.interactionManager == null || !IsTuning)
            return;
        if (!mc.interactionManager.getCurrentGameMode().isSurvivalLike()) {
            mc.player.sendMessage(Text.literal("§cNot in Survival mode!"));
            return;
        }

        if (song == null) {
            if (queue.isEmpty()) {
                mc.player.sendMessage(Text.literal("§cThere's no songs to tune!"));
                stop();
                return;
            }
        }

        // Tune Noteblocks
        var isTuned = true;
        var tunningAmount = 0;
        var changedTuneDelay = false;
        for (Entry<BlockPos, Integer> e : blockPitches.entrySet()) {
            int note = getNote(e.getKey());
            if (note == -1 || noteblockIgnore.contains(e.getKey())) {
                continue;
            }

            if (note != e.getValue()) {
                if (!changedTuneDelay) {
                    if (tuneDelay < 6) {
                        tuneDelay++;
                        return;
                    } else {
                        tuneDelay = 0;
                    }
                    changedTuneDelay = true;
                }
                if (noteblockPitches.containsKey(e.getKey())) {
                    if (e.getValue() == noteblockPitches.get(e.getKey())) {
                        noteblockTries.put(e.getKey(), noteblockTries.getOrDefault(e.getKey(), 0) + 1);
                        if (noteblockTries.getOrDefault(e.getKey(), 0) >= 5) {
                            var pos = e.getKey();
                            mc.player.sendMessage(Text.literal("§cUnable to tune a noteblock at X: "
                                    + String.valueOf(pos.getX()) + ", Y: " + String.valueOf(pos.getY()) + ", Z: "
                                    + String.valueOf(pos.getZ()) + ". Please try moving the noteblock."));
                            noteblockIgnore.add(e.getKey());
                        }
                    } else {
                        noteblockPitches.put(e.getKey(), e.getValue());
                        noteblockTries.put(e.getKey(), 0);
                    }
                } else {
                    noteblockPitches.put(e.getKey(), e.getValue());
                    noteblockTries.put(e.getKey(), 0);
                }
                int neededNote = e.getValue() < note ? e.getValue() + 25 : e.getValue();
                int reqTunes = Math.max(Math.min(neededNote - note, 25), 0);
                if (reqTunes > 0)
                    mc.interactionManager.interactBlock(mc.player,
                            Hand.MAIN_HAND,
                            new BlockHitResult(Vec3d.ofCenter(e.getKey(), 1), Direction.UP, e.getKey(), true));
                if (reqTunes > 1) {
                    isTuned = false;
                    tunningAmount++;
                }
                if (tunningAmount >= 6) {
                    break;
                }
            }
        }

        if (isTuned) {
            mc.player.sendMessage(Text.literal("§6Finished tuning the note blocks of §a" + song.filename + "§6."));
            stop();
        }
    }
}
