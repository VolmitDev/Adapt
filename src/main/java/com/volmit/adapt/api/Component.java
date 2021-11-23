package com.volmit.adapt.api;

import com.volmit.adapt.api.data.WorldData;
import com.volmit.adapt.api.value.MaterialValue;
import com.volmit.adapt.api.xp.XP;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public interface Component
{
    default void wisdom(Player p, long w) {
        XP.wisdom(p, w);
    }


    default void removePotion(Player p, PotionEffectType type)
    {
        p.removePotionEffect(type);
    }

    default void potion(Player p, PotionEffectType type, int power, int duration)
    {
        p.addPotionEffect(new PotionEffect(type, power, duration, true, false, false));
    }

    default double blockXP(Block block, double xp)
    {
        return Math.round(xp * getBlockMultiplier(block));
    }

    default double getBlockMultiplier(Block block)
    {
        return WorldData.of(block.getWorld()).reportEarnings(block);
    }

    default double getValue(Material material) {
        return MaterialValue.getValue(material);
    }

    default double getValue(BlockData block) {
        return MaterialValue.getValue(block.getMaterial());
    }

    default double getValue(ItemStack f) {
        return MaterialValue.getValue(f.getType());
    }

    default double getValue(Block block) {
        return MaterialValue.getValue(block.getType());
    }

    default void damageHand(Player p, int damage) {
        ItemStack is = p.getInventory().getItemInMainHand();
        ItemMeta im = is.getItemMeta();

        if(im == null) {
            return;
        }

        if(im.isUnbreakable()) {
            return;
        }

        Damageable dm = (Damageable) im;
        dm.setDamage(dm.getDamage() + damage);

        if(dm.getDamage() > is.getType().getMaxDurability()) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            return;
        }

        is.setItemMeta(im);
        p.getInventory().setItemInMainHand(is);
    }

    default void damageOffHand(Player p, int damage) {
        ItemStack is = p.getInventory().getItemInOffHand();
        ItemMeta im = is.getItemMeta();

        if(im == null) {
            return;
        }

        if(im.isUnbreakable()) {
            return;
        }

        Damageable dm = (Damageable) im;
        dm.setDamage(dm.getDamage() + damage);

        if(dm.getDamage() > is.getType().getMaxDurability()) {
            p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            return;
        }

        is.setItemMeta(im);
        p.getInventory().setItemInOffHand(is);
    }

    /**
     * Takes a custom amount of the item stack exact type (Ignores the item amount)
     * @param inv the inv
     * @param is the item ignore the amount
     * @param amount the amount to use
     * @return true if taken, false if not (missing)
     */
    default boolean takeAll(Inventory inv, ItemStack is, int amount)
    {
        ItemStack isf = is.clone();
        isf.setAmount(amount);
        return takeAll(inv, is);
    }

    /**
     * Take one of an exact type ignoring the item stack amount
     * @param inv the inv
     * @param is the item ignoring the amount
     * @return true if taken, false if diddnt
     */
    default boolean takeOne(Inventory inv, ItemStack is, int amount)
    {
        return takeAll(inv, is, 1);
    }

    /**
     * Take a specific amount of an EXACT META TYPE from an inventory
     * @param inv the inv
     * @param is uses the amount
     * @return returns false if it couldnt get enough (and none was taken)
     */
    default boolean takeAll(Inventory inv, ItemStack is)
    {
        ItemStack[] items = inv.getStorageContents();

        int take = is.getAmount();

        for(int ii = 0; ii < items.length; ii++)
        {
            ItemStack i = items[ii];

            if(i == null)
            {
                continue;
            }

            if(i.isSimilar(is))
            {
                if(take > i.getAmount())
                {
                    i.setAmount(i.getAmount() - take);
                    items[ii] = i;
                    take = 0;
                    break;
                }

                else
                {
                    items[ii] = null;
                    take -= i.getAmount();
                }
            }
        }

        if(take > 0)
        {
            return false;
        }

        inv.setStorageContents(items);
        return true;
    }
}