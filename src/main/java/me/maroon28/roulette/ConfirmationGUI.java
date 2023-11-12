package me.maroon28.roulette;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import redempt.redlib.itemutils.ItemUtils;

public class ConfirmationGUI {

    private final Location rouletteCenter;
    private final ChestGui gui;
    public ConfirmationGUI(Location rouletteCenter) {
        this.rouletteCenter = rouletteCenter;
        gui = new ChestGui(3, "Confirm");
        populateGui();
    }

    public ChestGui getGui() {
        return gui;
    }

    private void populateGui() {
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        StaticPane confirmationPane = new StaticPane(Slot.fromIndex(11), 5, 1);
        ItemStack confirmItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemUtils.setName(confirmItem, "§a§lConfirm");

        GuiItem confirmButton = new GuiItem(confirmItem, e -> {
            new Roulette(rouletteCenter);
            PlayerInventory inventory = e.getWhoClicked().getInventory();
            ItemUtils.remove(inventory, inventory.getItemInMainHand(), 1);
            e.getWhoClicked().closeInventory();
        });

        confirmationPane.addItem(confirmButton, 0 ,0);

        ItemStack cancelItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemUtils.setName(cancelItem, "§c§lCancel");
        GuiItem cancelButton = new GuiItem(cancelItem, e -> {
            e.getWhoClicked().closeInventory();
        });
        confirmationPane.addItem(cancelButton, 4, 0);

        gui.addPane(confirmationPane);
    }
}
