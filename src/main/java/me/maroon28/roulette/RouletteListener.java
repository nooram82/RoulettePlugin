package me.maroon28.roulette;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.multiblock.Structure;

public class RouletteListener implements Listener {

    @EventHandler
    public void onRoulettePlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.CHEST)
            return;
        if (!isRouletteChest(event.getItemInHand()))
            return;
        ConfirmationGUI confirmationGUI = new ConfirmationGUI(event.getBlockPlaced().getLocation());
        confirmationGUI.getGui().show(event.getPlayer());
        event.setCancelled(true);
    }

    @EventHandler
    public void onButtonClick(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick())
            return;
        if (event.getClickedBlock() == null)
            return;
        if (event.getClickedBlock().getType() != Material.STONE_BUTTON)
            return;
        // Find where the button is, go 1 block down to check for the structure of the roulette
        Location center = getRouletteCenter(event.getClickedBlock());
        // Button is likely not on a roulette anyway, save expensive checks.
        if (center.getBlock().getType() != Material.SMOOTH_QUARTZ)
            return;
        Structure rouletteStructure = Roulette.getRouletteStructureAt(center);
        if (rouletteStructure == null)
            return;
        Roulette roulette = new Roulette(rouletteStructure);
        roulette.startRoulette();
    }

    @NotNull
    private Location getRouletteCenter(Block block) {
        return block.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getLocation();
    }

    private boolean isRouletteChest(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(RoulettePlugin.getInstance(), "roulette-chest"));
    }

}
