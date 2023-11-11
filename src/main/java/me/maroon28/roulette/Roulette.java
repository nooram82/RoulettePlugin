package me.maroon28.roulette;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.Lightable;
import redempt.redlib.multiblock.MultiBlockStructure;
import redempt.redlib.multiblock.Structure;

import java.util.List;
import java.util.Random;

public class Roulette {
    private final Location center;
    // Hardcoded (unfortunately) values for the order of the circle.
    private final Structure roulette;
    private final List<RelativeLocation> relativeLocations = List.of(
            new RelativeLocation(6, 0, 4), new RelativeLocation(6, 0, 3),
            new RelativeLocation(5, 0, 2), new RelativeLocation(4, 0, 1),
            new RelativeLocation(3, 0, 1), new RelativeLocation(2, 0, 1),
            new RelativeLocation(1, 0, 2), new RelativeLocation(0, 0, 3),
            new RelativeLocation(0, 0, 4), new RelativeLocation(0, 0, 5),
            new RelativeLocation(1, 0, 6), new RelativeLocation(2, 0, 7),
            new RelativeLocation(3, 0, 7), new RelativeLocation(4, 0, 7),
            new RelativeLocation(5, 0, 6), new RelativeLocation(6, 0, 5)
    );
    private static final MultiBlockStructure ROULETTE_STRUCTURE = MultiBlockStructure.create(RoulettePlugin.getInstance().getResource("roulette-structure.dat"), "roulette");
    public Roulette(Location center) {
        this.center = center;
        this.roulette = buildRoulette();
    }
    public Roulette(Structure roulette) {
        this.roulette = roulette;
        center = roulette.getRelative(4, 0 ,4).getBlock().getLocation();
    }

    public static Structure getRouletteStructureAt(Location location) {
        return ROULETTE_STRUCTURE.getAt(location);
    }

    private Structure buildRoulette() {
        var structure = ROULETTE_STRUCTURE.build(center, 4, 0, 4, getRandomRotation());
        addButton(structure);
        return structure;
    }

    public void startRoulette() {
        int repetitions = 2;
        int delayTicks = 8;
        int animationLength = relativeLocations.size();

        // Use runTaskTimer to repeat the animation
        int taskId = Bukkit.getScheduler().runTaskTimer(RoulettePlugin.getInstance(), this::playAnimation, 0L, (long) animationLength * delayTicks).getTaskId();
        // Schedule a task to cancel the timer after all repetitions
        Bukkit.getScheduler().runTaskLater(RoulettePlugin.getInstance(), () -> {
            Bukkit.getScheduler().cancelTask(taskId);
            endRoulette();
        }, (long) repetitions * animationLength * delayTicks);


    }

    private void endRoulette() {
        ParticleBuilder builder = new ParticleBuilder(Particle.FIREWORKS_SPARK);
        builder.count(100).allPlayers().location(center).spawn();
        playSound(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1);
    }

    private void playAnimation() {
        int delayTicks = 5;
        int animationLength = relativeLocations.size();

        for (int i = 0; i < animationLength; i++) {
            RelativeLocation loc = relativeLocations.get(i);
            Block currentLamp = getLamp(roulette, loc);
            // Start at 0.5 pitch and gradually increase on every light up.
            float pitch = 0.5F + (i * 0.1F);
            Bukkit.getScheduler().runTaskLater(RoulettePlugin.getInstance(), () -> {
                lightTemporarily(currentLamp);
                playSound(Sound.BLOCK_NOTE_BLOCK_BANJO, pitch);
            }, (long) i * delayTicks);
        }

    }

    private void addButton(Structure roulette) {
        // Get the block where the button should be placed
        var buttonBlock = roulette.getRelative(4, 0, 4).getBlock().getRelative(BlockFace.UP, 1);

        // Set the button type
        buttonBlock.setType(Material.STONE_BUTTON);

        // Set the orientation of the button to face upwards
        FaceAttachable buttonData = (FaceAttachable) buttonBlock.getBlockData();
        buttonData.setAttachedFace(FaceAttachable.AttachedFace.FLOOR);
        buttonBlock.setBlockData(buttonData);

    }

    private Block getLamp(Structure roulette, RelativeLocation loc) {
        return roulette.getRelative(loc.x + 1, loc.y, loc.z).getBlock();
    }


    private void lightTemporarily(Block block) {
        if (block == null)
            return;
        Lightable lightable = (Lightable) block.getBlockData();
        lightable.setLit(true);
        block.setBlockData(lightable);
        int duration = 8;
        Bukkit.getScheduler().runTaskLater(RoulettePlugin.getInstance(), () -> {
            lightable.setLit(false);
            block.setBlockData(lightable);
        }, duration);

    }

    // Gets a random rotation for the roulette structure.
    // Since this isn't actually noticeable, all it does is it randomizes the location where it starts and ends to an extent
    // Generates a number between 0 - 3
    public int getRandomRotation() {
        Random random = new Random();
        return random.nextInt(4);
    }

    private void playSound(Sound sound, float pitch) {
        center.getWorld().playSound(center, sound, 10, pitch);
    }

    private record RelativeLocation(int x, int y, int z) {
        @Override
        public String toString() {
            return "{x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

}
