package ru.flametaichou.ordinarycoins.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import ru.flametaichou.ordinarycoins.OrdinaryCoinsBase;

public class ItemCoinSilver extends Item {

    public ItemCoinSilver() {
        setRegistryName("coinSilver");
        setUnlocalizedName(OrdinaryCoinsBase.MODID + ".coinSilver");
        this.setCreativeTab(CreativeTabs.MISC);
    }
}
