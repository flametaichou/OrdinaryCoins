package ru.flametaichou.ordinarycoins;

        import net.minecraft.client.audio.Sound;
        import net.minecraft.client.audio.SoundHandler;
        import net.minecraft.command.CommandBase;
        import net.minecraft.command.CommandException;
        import net.minecraft.command.ICommandSender;
        import net.minecraft.entity.player.EntityPlayer;
        import net.minecraft.entity.player.InventoryPlayer;
        import net.minecraft.init.SoundEvents;
        import net.minecraft.item.Item;
        import net.minecraft.item.ItemStack;
        import net.minecraft.server.MinecraftServer;
        import net.minecraft.util.ResourceLocation;
        import net.minecraft.util.SoundCategory;
        import net.minecraft.util.SoundEvent;
        import net.minecraft.util.math.BlockPos;
        import net.minecraft.util.text.TextComponentString;
        import net.minecraft.util.text.TextComponentTranslation;
        import net.minecraft.world.World;
        import ru.flametaichou.ordinarycoins.items.ItemCoinBronze;
        import ru.flametaichou.ordinarycoins.items.ItemCoinGold;
        import ru.flametaichou.ordinarycoins.items.ItemCoinSilver;

        import javax.annotation.Nonnull;
        import javax.annotation.Nullable;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.List;

public class OrdinaryCoinsCommands extends CommandBase {

    public OrdinaryCoinsCommands(){
        aliases = new ArrayList<String>();
        aliases.add("coins");
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getName() {
        return "coins";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "coins <stack/unstack/repair>";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        World world = sender.getEntityWorld();

        if (!world.isRemote)
        {
            if(args.length == 0)
            {
                sender.sendMessage(new TextComponentString("/coins <stack/unstack>"));
                return;
            }
            if (args[0].equals("stack"))
            {
                if(sender instanceof EntityPlayer)
                {
                    EntityPlayer player = (EntityPlayer)sender;
                    InventoryPlayer inventory = player.inventory;
                    int countBronze = 0;
                    int countSilver = 0;
                    for (ItemStack s : inventory.mainInventory) {
                        if (s != null && s.getItem() instanceof ItemCoinBronze)
                        {
                            countBronze = countBronze + s.getCount();
                        }
                        if (s != null && s.getItem() instanceof ItemCoinSilver)
                        {
                            countSilver = countSilver + s.getCount();
                        }
                    }
                    inventory.clearMatchingItems(OrdinaryCoinsBase.coinBronze, 0,countBronze,null);
                    inventory.clearMatchingItems(OrdinaryCoinsBase.coinSilver, 0,countSilver,null);
                    inventory.addItemStackToInventory(new ItemStack(OrdinaryCoinsBase.coinSilver, countBronze / ConfigHelper.coinsStackSize));
                    inventory.addItemStackToInventory(new ItemStack(OrdinaryCoinsBase.coinBronze, countBronze % ConfigHelper.coinsStackSize));
                    inventory.addItemStackToInventory(new ItemStack(OrdinaryCoinsBase.coinGold, countSilver / ConfigHelper.coinsStackSize));
                    inventory.addItemStackToInventory(new ItemStack(OrdinaryCoinsBase.coinSilver, countSilver % ConfigHelper.coinsStackSize));
                    sender.sendMessage(new TextComponentTranslation("coins.stacked"));
                }
                return;
            }

            if (args[0].equals("unstack"))
            {
                if(sender instanceof EntityPlayer)
                {
                    EntityPlayer player = (EntityPlayer)sender;
                    InventoryPlayer inventory = player.inventory;
                    int countGold = 0;
                    int countSilver = 0;
                    for (ItemStack s : inventory.mainInventory)
                    {
                        if (s != null && s.getItem() instanceof ItemCoinGold)
                        {
                            countGold = countGold + s.getCount();
                        }
                        if (s != null && s.getItem() instanceof ItemCoinSilver)
                        {
                            countSilver = countSilver + s.getCount();
                        }
                    }
                    inventory.clearMatchingItems(OrdinaryCoinsBase.coinGold, 0,countGold,null);
                    inventory.clearMatchingItems(OrdinaryCoinsBase.coinSilver, 0,countSilver,null);
                    inventory.addItemStackToInventory(new ItemStack(OrdinaryCoinsBase.coinSilver, countGold * ConfigHelper.coinsStackSize));
                    inventory.addItemStackToInventory(new ItemStack(OrdinaryCoinsBase.coinBronze, countSilver * ConfigHelper.coinsStackSize));
                    sender.sendMessage(new TextComponentTranslation("coins.unstacked"));
                }
                return;
            }

            if (args[0].equals("repair") && ConfigHelper.repair)
            {
                if(sender instanceof EntityPlayer)
                {
                    // From chat
                    EntityPlayer player = (EntityPlayer)sender;
                    InventoryPlayer inventory = player.inventory;

                    Item coin = pickCoinFromConfig();

                    int countCoins = 0;
                    for (ItemStack s : inventory.mainInventory)
                    {
                        if (s != null && s.getItem() == coin) {
                            countCoins = countCoins + s.getCount();
                        }
                    }
                    ItemStack item = player.getHeldItemMainhand();
                    if (countCoins >= ConfigHelper.repairCost)
                    {
                        if (item != null && item.isItemDamaged())
                        {
                            inventory.clearMatchingItems(coin,0, ConfigHelper.repairCost,null);
                            item.setItemDamage(0);
                            sender.sendMessage(new TextComponentTranslation("coins.repaired"));
                            //TODO: send packet to client to play sound
                            //player.world.playSound(player, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.1F, 1.1F);
                        }
                        else
                        {
                            sender.sendMessage(new TextComponentTranslation("coins.cantrepair"));
                        }
                    }
                    else
                    {
                        switch (ConfigHelper.repairCoinType)
                        {
                            case 0:
                                sender.sendMessage(new TextComponentTranslation("coins.notenough.bronze", ConfigHelper.repairCost));
                                break;
                            case 1:
                                sender.sendMessage(new TextComponentTranslation("coins.notenough.silver", ConfigHelper.repairCost));
                                break;
                            case 2:
                                sender.sendMessage(new TextComponentTranslation("coins.notenough.gold", ConfigHelper.repairCost));
                                break;
                        }
                    }
                } else if (!(sender instanceof EntityPlayer))
                {
                    // From console
                    if (args.length < 2) {
                        System.out.println("Repair error: player name is not defined!");
                        return;
                    }
                    EntityPlayer player = world.getPlayerEntityByName(args[1]);
                    InventoryPlayer inventory = player.inventory;

                    Item coin = pickCoinFromConfig();

                    int countCoins = 0;
                    for (ItemStack s : inventory.mainInventory)
                    {
                        if (s != null && s.getItem() == coin) {
                            countCoins = countCoins + s.getCount();
                        }
                    }
                    ItemStack item = player.getHeldItemMainhand();
                    if (countCoins >= ConfigHelper.repairCost)
                    {
                        if (item != null && item.isItemDamaged())
                        {
                            inventory.clearMatchingItems(coin,0, ConfigHelper.repairCost,null);
                            item.setItemDamage(0);
                            player.sendMessage(new TextComponentTranslation("coins.repaired"));
                            //TODO: send packet to client to play sound
                            //player.world.playSound(player, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_ANVIL_USE, SoundCategory.PLAYERS, 1.1F, 1.1F);
                        }
                        else
                        {
                            player.sendMessage(new TextComponentTranslation("coins.cantrepair"));
                        }
                    }
                    else
                    {
                        switch (ConfigHelper.repairCoinType)
                        {
                            case 0:
                                player.sendMessage(new TextComponentTranslation("coins.notenough.bronze", ConfigHelper.repairCost));
                                break;
                            case 1:
                                player.sendMessage(new TextComponentTranslation("coins.notenough.silver", ConfigHelper.repairCost));
                                break;
                            case 2:
                                player.sendMessage(new TextComponentTranslation("coins.notenough.gold", ConfigHelper.repairCost));
                                break;
                        }
                    }
                }
                return;
            }
        }
    }

    private Item pickCoinFromConfig() {
        Item coin = OrdinaryCoinsBase.coinSilver;
        switch (ConfigHelper.repairCoinType)
        {
            case 0:
                coin = OrdinaryCoinsBase.coinBronze;
                break;
            case 1:
                coin = OrdinaryCoinsBase.coinSilver;
                break;
            case 2:
                coin = OrdinaryCoinsBase.coinGold;
                break;
        }
        return coin;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }
}