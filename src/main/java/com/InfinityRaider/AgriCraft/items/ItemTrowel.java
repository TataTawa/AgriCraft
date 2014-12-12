package com.InfinityRaider.AgriCraft.items;

import com.InfinityRaider.AgriCraft.blocks.BlockCrop;
import com.InfinityRaider.AgriCraft.creativetab.AgriCraftTab;
import com.InfinityRaider.AgriCraft.reference.Names;
import com.InfinityRaider.AgriCraft.tileentity.TileEntityCrop;
import com.InfinityRaider.AgriCraft.utility.LogHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemTrowel extends ModItem {
    private IIcon[] icons = new IIcon[2];

    public ItemTrowel() {
        super();
        this.setCreativeTab(AgriCraftTab.agriCraftTab);
        this.maxStackSize=1;
    }

    //I'm overriding this just to be sure
    @Override
    public boolean canItemEditBlocks() {return true;}

    //this is called when you right click with this item in hand
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
           if(world.getBlock(x, y, z)!=null && world.getBlock(x, y, z) instanceof BlockCrop) {
               TileEntity te = world.getTileEntity(x, y, z);
               if(te!=null && te instanceof TileEntityCrop) {
                   TileEntityCrop crop = (TileEntityCrop) te;
                   //put plant on trowel
                   if(crop.hasPlant() && stack.getItemDamage()==0) {
                       //put plant on trowel
                       NBTTagCompound tag = new NBTTagCompound();
                       tag.setShort(Names.growth, (short) crop.growth);
                       tag.setShort(Names.gain, (short) crop.gain);
                       tag.setShort(Names.strength, (short) crop.strength);
                       tag.setString(Names.seed, crop.getSeedString());
                       tag.setShort(Names.meta, (short) crop.seedMeta);
                       tag.setShort(Names.materialMeta, (short) world.getBlockMetadata(x, y, z));
                       stack.setTagCompound(tag);
                       stack.setItemDamage(1);
                       //clear crop
                       world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                       crop.growth=0;
                       crop.gain=0;
                       crop.strength=0;
                       crop.seed=null;
                       crop.seedMeta=0;
                       crop.markDirty();
                       //return true to avoid further processing
                       return true;
                   }
                   //plant crop from trowel
                   else if(!crop.hasPlant() && !crop.crossCrop && stack.getItemDamage()==1) {
                       //set crop
                       NBTTagCompound tag = stack.getTagCompound();
                       crop.growth = tag.getShort(Names.growth);
                       crop.gain = tag.getShort(Names.gain);
                       crop.strength = tag.getShort(Names.strength);
                       crop.setSeed(tag.getString(Names.seed));
                       crop.seedMeta = tag.getShort(Names.meta);
                       world.setBlockMetadataWithNotify(x, y, z, tag.getShort(Names.materialMeta), 3);
                       crop.markDirty();
                       //clear trowel
                       stack.setTagCompound(null);
                       stack.setItemDamage(0);
                       //return true to avoid further processing
                       return true;
                   }
               }
           }
        }
        return false;   //return false or else no other use methods will be called (for instance "onBlockActivated" on the crops block)
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flag) {
       if(stack.hasTagCompound() && stack.stackTagCompound.hasKey(Names.seed) && stack.stackTagCompound.hasKey(Names.meta)) {
           NBTTagCompound tag = stack.getTagCompound();
           ItemStack seed = new ItemStack((Item) Item.itemRegistry.getObject(tag.getString(Names.seed)), 1, tag.getShort(Names.meta));
            list.add(StatCollector.translateToLocal("agricraft_tooltip.seed")+": "+ seed.getItem().getItemStackDisplayName(seed));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        LogHelper.debug("registering icon for: " + this.getUnlocalizedName());
        icons[0] = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.')+1)+"_empty");
        icons[1] = reg.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf('.')+1)+"_full");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        if(meta<=1) {
            return this.icons[meta];
        }
        return null;
    }
}