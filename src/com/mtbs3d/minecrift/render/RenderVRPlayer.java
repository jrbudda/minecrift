package com.mtbs3d.minecrift.render;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.Reflector;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;

public class RenderVRPlayer extends RenderPlayer
{
    private static final ResourceLocation steveTextures = new ResourceLocation("textures/entity/steve.png");
    /** MINECRIFT FORGE **/
    public ModelBiped modelBipedMain;
    public ModelBiped modelArmorChestplate;
    public ModelBiped modelArmor;
    /** END MINECRIFT FORGE **/
	private static final String __OBFID = "CL_00001020";

    /** MINECRIFT */
    public float vrOffset;
    public boolean _renderHeadwear;
    public boolean _renderHead;
    /** END MINECRIFT */

    public RenderVRPlayer()
    {
        super();
        this.modelBipedMain = (ModelBiped)this.mainModel;
        this.modelArmorChestplate = new ModelBiped(1.0F);
        this.modelArmor = new ModelBiped(0.5F);
    }

    /** MINECRIFT */
    public void setVR(boolean vrMode, boolean renderHeadWear, float offset) {

        vrOffset = vrMode? offset : 0.0F;
        if (false) {
            this.modelBipedMain.vrMode = vrMode;
        }
        else {
            this.modelBipedMain.vrMode = false;
        }
        this.modelBipedMain.vrOffset = this.modelArmorChestplate.vrOffset = this.modelArmor.vrOffset = vrOffset;
        this.modelBipedMain._renderHead = this.modelArmorChestplate._renderHead = this.modelArmor._renderHead = _renderHead = !vrMode;
        this.modelBipedMain._renderHeadWear = this.modelArmorChestplate._renderHeadWear = this.modelArmor._renderHeadWear = _renderHeadwear = renderHeadWear;
    }
    /** END MINECRIFT */

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(AbstractClientPlayer p_77032_1_, int p_77032_2_, float p_77032_3_)
    {
        ItemStack var4 = p_77032_1_.inventory.armorItemInSlot(3 - p_77032_2_);

		/** MINECRIFT FORGE **/
        if (Reflector.forgeExists()) {
            Object event = Reflector.newInstance(Reflector.ForgeRenderPlayerEvent_SetArmorModel_Constructor, new Object[]{p_77032_1_, this, 3 - p_77032_2_, p_77032_3_, var4});
            Reflector.postForgeBusEvent(event);
            int result = (Integer) Reflector.getFieldValue(event, Reflector.ForgeRenderPlayerEvent_SetArmorModel_result);
            if (result != -1) {
                return result;
            }
        }
		/** END MINECRIFT FORGE **/

        if (var4 != null)
        {
            Item var5 = var4.getItem();

            if (var5 instanceof ItemArmor)
            {
                ItemArmor var6 = (ItemArmor)var5;
				/** MINECRIFT FORGE **/
				if (Reflector.forgeExists()) {
					//this.bindTexture(RenderBiped.getArmorResource(p_77032_1_, var4, p_77032_2_, null));
                    ResourceLocation resLoc = (ResourceLocation)Reflector.call(Reflector.ForgeRenderBiped_getArmorResource, new Object[] { p_77032_1_, var4, p_77032_2_, null });
                    this.bindTexture(resLoc);
				}
				else {
                	this.bindTexture(RenderBiped.func_110857_a(var6, p_77032_2_));
				}
				/** END MINECRIFT FORGE **/
                ModelBiped var7 = p_77032_2_ == 2 ? this.modelArmor : this.modelArmorChestplate;
                var7.bipedHead.showModel = p_77032_2_ == 0;
                var7.bipedHeadwear.showModel = p_77032_2_ == 0;
                var7.bipedBody.showModel = p_77032_2_ == 1 || p_77032_2_ == 2;
                var7.bipedRightArm.showModel = p_77032_2_ == 1;
                var7.bipedLeftArm.showModel = p_77032_2_ == 1;
                var7.bipedRightLeg.showModel = p_77032_2_ == 2 || p_77032_2_ == 3;
                var7.bipedLeftLeg.showModel = p_77032_2_ == 2 || p_77032_2_ == 3;
				/** MINECRIFT FORGE **/
				if (Reflector.forgeExists()) {
					//var7 = net.minecraftforge.client.ForgeHooksClient.getArmorModel(p_77032_1_, var4, p_77032_2_, var7);
                    var7 = (ModelBiped)Reflector.call(Reflector.ForgeHooksClient_getArmorModel, new Object[] { p_77032_1_, var4, p_77032_2_, var7 });
				}
				/** END MINECRIFT FORGE **/
                this.setRenderPassModel(var7);
                var7.swingProgress = this.mainModel.swingProgress;
                var7.isRiding = this.mainModel.isRiding;
                var7.isChild = this.mainModel.isChild;

				/** MINECRIFT FORGE **/
				boolean result = (var6.getArmorMaterial() == ItemArmor.ArmorMaterial.CLOTH);
				int var8 = var6.getColor(var4);
				if (Reflector.forgeExists()) {
					result = (var8 != -1);
				}
                if (result)
                {
				/** END MINECRIFT FORGE **/
                    float var9 = (float)(var8 >> 16 & 255) / 255.0F;
                    float var10 = (float)(var8 >> 8 & 255) / 255.0F;
                    float var11 = (float)(var8 & 255) / 255.0F;
                    GL11.glColor3f(var9, var10, var11);

                    if (var4.isItemEnchanted())
                    {
                        return 31;
                    }

                    return 16;
                }

                GL11.glColor3f(1.0F, 1.0F, 1.0F);

                if (var4.isItemEnchanted())
                {
                    return 15;
                }

                return 1;
            }
        }

        return -1;
    }

    protected void func_82408_c(AbstractClientPlayer p_82408_1_, int p_82408_2_, float p_82408_3_)
    {
        ItemStack var4 = p_82408_1_.inventory.armorItemInSlot(3 - p_82408_2_);

        if (var4 != null)
        {
            Item var5 = var4.getItem();

            if (var5 instanceof ItemArmor)
            {
			    /** MINECRIFT FORGE **/
				if (Reflector.forgeExists()) {
                    //this.bindTexture(RenderBiped.getArmorResource(p_82408_1_, var4, p_82408_2_, "overlay"));
                    ResourceLocation resLoc = (ResourceLocation)Reflector.call(Reflector.ForgeRenderBiped_getArmorResource, new Object[] { p_82408_1_, var4, p_82408_2_, "overlay" });
                    this.bindTexture(resLoc);
				}
				else {
                	this.bindTexture(RenderBiped.func_110858_a((ItemArmor)var5, p_82408_2_, "overlay"));
				}
				/** END MINECRIFT FORGE **/
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
            }
        }
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(AbstractClientPlayer p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
		/** MINECRIFT FORGE **/
        if (Reflector.forgeExists()) {
            if (Reflector.postForgeBusEvent(Reflector.ForgeRenderPlayerEvent_Pre_Constructor, new Object[]{p_76986_1_, this, p_76986_9_})) return;
        }
		/** END MINECRIFT FORGE **/
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        ItemStack var10 = p_76986_1_.inventory.getCurrentItem();
        this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = var10 != null ? 1 : 0;

        if (var10 != null && p_76986_1_.getItemInUseCount() > 0)
        {
            EnumAction var11 = var10.getItemUseAction();

            if (var11 == EnumAction.block)
            {
                this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 3;
            }
            else if (var11 == EnumAction.bow)
            {
                this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = true;
            }
        }

        this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = p_76986_1_.isSneaking();
        double var13 = p_76986_4_ - (double)p_76986_1_.yOffset;

        if (p_76986_1_.isSneaking() && !(p_76986_1_ instanceof EntityPlayerSP))
        {
            var13 -= 0.125D;
        }

        super.doRender((EntityLivingBase)p_76986_1_, p_76986_2_, var13, p_76986_6_, p_76986_8_, p_76986_9_);
        this.modelArmorChestplate.aimedBow = this.modelArmor.aimedBow = this.modelBipedMain.aimedBow = false;
        this.modelArmorChestplate.isSneak = this.modelArmor.isSneak = this.modelBipedMain.isSneak = false;
        this.modelArmorChestplate.heldItemRight = this.modelArmor.heldItemRight = this.modelBipedMain.heldItemRight = 0;
        /** MINECRIFT FORGE **/
		if (Reflector.forgeExists()) {
            if (Reflector.postForgeBusEvent(Reflector.ForgeRenderPlayerEvent_Post_Constructor, new Object[]{p_76986_1_, this, p_76986_9_})) return;
        }
		/** END MINECRIFT FORGE **/
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(AbstractClientPlayer p_110775_1_)
    {
        return p_110775_1_.getLocationSkin();
    }

    protected void renderEquippedItems(AbstractClientPlayer p_77029_1_, float p_77029_2_)
    {
		/** MINECRIFT FORGE **/
        Object event = null;
		boolean renderHelmet = _renderHead;
        boolean Forge = Reflector.forgeExists();
        if (Forge) {
            event = Reflector.newInstance(Reflector.ForgeRenderPlayerEvent_Specials_Pre_Constructor, new Object[]{p_77029_1_, this, p_77029_2_});
            if (Reflector.postForgeBusEvent(event)) return;
            boolean eventRenderHelmet = (Boolean) Reflector.getFieldValue(event, Reflector.ForgeRenderPlayerEvent_Specials_Pre_renderHelmet);
			if (renderHelmet == true && !eventRenderHelmet) {
				renderHelmet = false;
			}
        }
		/** END MINECRIFT FORGE **/
        GL11.glColor3f(1.0F, 1.0F, 1.0F);
        super.renderEquippedItems(p_77029_1_, p_77029_2_);
        super.renderArrowsStuckInEntity(p_77029_1_, p_77029_2_);
        ItemStack var3 = p_77029_1_.inventory.armorItemInSlot(3);

		/** MINECRIFT */
        if (var3 != null && _renderHead && renderHelmet) 
		/** END MINECRIFT */
        {
            GL11.glPushMatrix();
            /** MINECRIFT */
            GL11.glTranslatef(0, 0, vrOffset);
            /** END MINECRIFT */
            this.modelBipedMain.bipedHead.postRender(0.0625F);
            float var4;

            if (var3.getItem() instanceof ItemBlock)
            {
				/** MINECRIFT FORGE **/
				boolean result = RenderBlocks.renderItemIn3d(Block.getBlockFromItem(var3.getItem()).getRenderType());
				if (Forge) {
                    Object itemRenderTypeEquipped = Reflector.getFieldValue(Reflector.ItemRenderType_EQUIPPED);
                    Object itemRenderHelperType3D = Reflector.getFieldValue(Reflector.ForgeItemRendererHelper_BLOCK_3D);
                    Object customRenderer = Reflector.call(Reflector.MinecraftForgeClient_getItemRenderer, new Object[] {var3, itemRenderTypeEquipped});
                    boolean is3D = (customRenderer != null && Reflector.callBoolean(customRenderer, Reflector.ForgeIItemRenderer_shouldUseRenderHelper, new Object[] {itemRenderTypeEquipped, var3, itemRenderHelperType3D}));
					result = (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(var3.getItem()).getRenderType()));
				}
                if (result)
				/** END MINECRIFT FORGE **/
                {
                    var4 = 0.625F;
                    GL11.glTranslatef(0.0F, -0.25F, 0.0F);
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(var4, -var4, -var4);
                }

                this.renderManager.itemRenderer.renderItem(p_77029_1_, var3, 0);
            }
            else if (var3.getItem() == Items.skull)
            {
                var4 = 1.0625F;
                GL11.glScalef(var4, -var4, -var4);
                GameProfile var5 = null;

                if (var3.hasTagCompound())
                {
                    NBTTagCompound var6 = var3.getTagCompound();

                    if (var6.hasKey("SkullOwner", 10))
                    {
                        var5 = NBTUtil.readGameProfileFromNBT(var6.getCompoundTag("SkullOwner"));
                    }
                    else if (var6.hasKey("SkullOwner", 8) && !StringUtils.isNullOrEmpty(var6.getString("SkullOwner")))
                    {
                        var5 = new GameProfile((UUID)null, var6.getString("SkullOwner"));
                    }
                }

                TileEntitySkullRenderer.field_147536_b.func_152674_a(-0.5F, 0.0F, -0.5F, 1, 180.0F, var3.getMetadata(), var5);
            }

            GL11.glPopMatrix();
        }

        float var7;

        if (p_77029_1_.getCommandSenderName().equals("deadmau5") && p_77029_1_.hasSkin())
        {
            this.bindTexture(p_77029_1_.getLocationSkin());

            for (int var20 = 0; var20 < 2; ++var20)
            {
                float var22 = p_77029_1_.prevRotationYaw + (p_77029_1_.rotationYaw - p_77029_1_.prevRotationYaw) * p_77029_2_ - (p_77029_1_.prevRenderYawOffset + (p_77029_1_.renderYawOffset - p_77029_1_.prevRenderYawOffset) * p_77029_2_);
                float var25 = p_77029_1_.prevRotationPitch + (p_77029_1_.rotationPitch - p_77029_1_.prevRotationPitch) * p_77029_2_;
                GL11.glPushMatrix();
                GL11.glRotatef(var22, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(var25, 1.0F, 0.0F, 0.0F);
                GL11.glTranslatef(0.375F * (float)(var20 * 2 - 1), 0.0F, 0.0F);
                GL11.glTranslatef(0.0F, -0.375F, 0.0F);
                GL11.glRotatef(-var25, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-var22, 0.0F, 1.0F, 0.0F);
                var7 = 1.3333334F;
                GL11.glScalef(var7, var7, var7);
                this.modelBipedMain.renderEars(0.0625F);
                GL11.glPopMatrix();
            }
        }

        boolean var21 = p_77029_1_.hasCape();
		/** MINECRIFT FORGE **/
		if (Forge) {
            boolean renderCape = (Boolean) Reflector.getFieldValue(event, Reflector.ForgeRenderPlayerEvent_Specials_Pre_renderCape);
        	var21 = renderCape && var21;
		}
		/** END MINECRIFT FORGE **/
        float var11;

        if (var21 && !p_77029_1_.isInvisible() && !p_77029_1_.getHideCape())
        {
            this.bindTexture(p_77029_1_.getLocationCape());
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.0F, 0.125F);
            double var23 = p_77029_1_.field_71091_bM + (p_77029_1_.field_71094_bP - p_77029_1_.field_71091_bM) * (double)p_77029_2_ - (p_77029_1_.prevPosX + (p_77029_1_.posX - p_77029_1_.prevPosX) * (double)p_77029_2_);
            double var27 = p_77029_1_.field_71096_bN + (p_77029_1_.field_71095_bQ - p_77029_1_.field_71096_bN) * (double)p_77029_2_ - (p_77029_1_.prevPosY + (p_77029_1_.posY - p_77029_1_.prevPosY) * (double)p_77029_2_);
            double var9 = p_77029_1_.field_71097_bO + (p_77029_1_.field_71085_bR - p_77029_1_.field_71097_bO) * (double)p_77029_2_ - (p_77029_1_.prevPosZ + (p_77029_1_.posZ - p_77029_1_.prevPosZ) * (double)p_77029_2_);
            var11 = p_77029_1_.prevRenderYawOffset + (p_77029_1_.renderYawOffset - p_77029_1_.prevRenderYawOffset) * p_77029_2_;
            double var12 = (double)MathHelper.sin(var11 * (float)Math.PI / 180.0F);
            double var14 = (double)(-MathHelper.cos(var11 * (float)Math.PI / 180.0F));
            float var16 = (float)var27 * 10.0F;

            if (var16 < -6.0F)
            {
                var16 = -6.0F;
            }

            if (var16 > 32.0F)
            {
                var16 = 32.0F;
            }

            float var17 = (float)(var23 * var12 + var9 * var14) * 100.0F;
            float var18 = (float)(var23 * var14 - var9 * var12) * 100.0F;

            if (var17 < 0.0F)
            {
                var17 = 0.0F;
            }

            float var19 = p_77029_1_.prevCameraYaw + (p_77029_1_.cameraYaw - p_77029_1_.prevCameraYaw) * p_77029_2_;
            var16 += MathHelper.sin((p_77029_1_.prevDistanceWalkedModified + (p_77029_1_.distanceWalkedModified - p_77029_1_.prevDistanceWalkedModified) * p_77029_2_) * 6.0F) * 32.0F * var19;

            if (p_77029_1_.isSneaking())
            {
                var16 += 25.0F;
            }

            GL11.glRotatef(6.0F + var17 / 2.0F + var16, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(var18 / 2.0F, 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(-var18 / 2.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            this.modelBipedMain.renderCloak(0.0625F);
            GL11.glPopMatrix();
        }

        ItemStack var24 = p_77029_1_.inventory.getCurrentItem();
		/** MINECRIFT FORGE **/
		boolean eventRenderItem = true;
		if (Forge)
			eventRenderItem = (Boolean) Reflector.getFieldValue(event, Reflector.ForgeRenderPlayerEvent_Specials_Pre_renderItem);
			
        if (var24 != null && eventRenderItem)
		/** END MINECRIFT FORGE **/
        {
            GL11.glPushMatrix();
            /** MINECRIFT */
            GL11.glTranslatef(0, 0, vrOffset);
            /** END MINECRIFT */
            this.modelBipedMain.bipedRightArm.postRender(0.0625F);
            GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

            if (p_77029_1_.fishEntity != null)
            {
                var24 = new ItemStack(Items.stick);
            }

            EnumAction var26 = null;

            if (p_77029_1_.getItemInUseCount() > 0)
            {
                var26 = var24.getItemUseAction();
            }

			/** MINECRIFT FORGE **/
			boolean result = var24.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(var24.getItem()).getRenderType());
            if (Forge) {
                Object itemRenderTypeEquipped = Reflector.getFieldValue(Reflector.ItemRenderType_EQUIPPED);
                Object itemRenderHelperType3D = Reflector.getFieldValue(Reflector.ForgeItemRendererHelper_BLOCK_3D);
                Object customRenderer = Reflector.call(Reflector.MinecraftForgeClient_getItemRenderer, new Object[] {var24, itemRenderTypeEquipped});
                boolean is3D = (customRenderer != null && Reflector.callBoolean(customRenderer, Reflector.ForgeIItemRenderer_shouldUseRenderHelper, new Object[] {itemRenderTypeEquipped, var24, itemRenderHelperType3D}));
				result = (is3D || var24.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(var24.getItem()).getRenderType()));
            }
            if (result)
            /** END MINECRIFT FORGE **/
			{
                var7 = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                var7 *= 0.75F;
                GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(-var7, -var7, var7);
            }
            else if (var24.getItem() == Items.bow)
            {
                var7 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(var7, -var7, var7);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else if (var24.getItem().isFull3D())
            {
                var7 = 0.625F;

                if (var24.getItem().shouldRotateAroundWhenRendering())
                {
                    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                }

                if (p_77029_1_.getItemInUseCount() > 0 && var26 == EnumAction.block)
                {
                    GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                    GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
                }

                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                GL11.glScalef(var7, -var7, var7);
                GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                var7 = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(var7, var7, var7);
                GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
            }

            float var10;
            int var28;
            float var30;

            if (var24.getItem().requiresMultipleRenderPasses())
            {
                /** MINECRIFT FORGE **/
				int numOfRenderPasses = 2;
				if (Forge) {
				    numOfRenderPasses = Reflector.callInt(var24.getItem(), Reflector.MinecraftItem_Forge_getRenderPasses, new Object[] {var24.getMetadata()});
				}
                /** END MINECRIFT FORGE **/
                for (var28 = 0; var28 < numOfRenderPasses; ++var28)
                {
                    int var8 = var24.getItem().getColorFromItemStack(var24, var28);
                    var30 = (float)(var8 >> 16 & 255) / 255.0F;
                    var10 = (float)(var8 >> 8 & 255) / 255.0F;
                    var11 = (float)(var8 & 255) / 255.0F;
                    GL11.glColor4f(var30, var10, var11, 1.0F);
                    this.renderManager.itemRenderer.renderItem(p_77029_1_, var24, var28);
                }
            }
            else
            {
                var28 = var24.getItem().getColorFromItemStack(var24, 0);
                float var29 = (float)(var28 >> 16 & 255) / 255.0F;
                var30 = (float)(var28 >> 8 & 255) / 255.0F;
                var10 = (float)(var28 & 255) / 255.0F;
                GL11.glColor4f(var29, var30, var10, 1.0F);
                this.renderManager.itemRenderer.renderItem(p_77029_1_, var24, 0);
            }

            GL11.glPopMatrix();
        }
		/** MINECRIFT FORGE **/
        if (Forge) {
            Reflector.postForgeBusEvent(Reflector.ForgeRenderPlayerEvent_Specials_Post_Constructor, new Object[]{p_77029_1_, this, p_77029_2_});
        }
		/** END MINECRIFT FORGE **/
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(AbstractClientPlayer p_77041_1_, float p_77041_2_)
    {
        float var3 = 0.9375F;
        GL11.glScalef(var3, var3, var3);
    }

    protected void renderOffsetLivingLabel(AbstractClientPlayer p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_)
    {
        if (p_96449_10_ < 100.0D)
        {
            Scoreboard var12 = p_96449_1_.getWorldScoreboard();
            ScoreObjective var13 = var12.getObjectiveInDisplaySlot(2);

            if (var13 != null)
            {
                Score var14 = var12.getValueFromObjective(p_96449_1_.getCommandSenderName(), var13);

                if (p_96449_1_.isPlayerSleeping())
                {
                    this.renderLivingLabel(p_96449_1_, var14.getScorePoints() + " " + var13.getDisplayName(), p_96449_2_, p_96449_4_ - 1.5D, p_96449_6_, 64);
                }
                else
                {
                    this.renderLivingLabel(p_96449_1_, var14.getScorePoints() + " " + var13.getDisplayName(), p_96449_2_, p_96449_4_, p_96449_6_, 64);
                }

                p_96449_4_ += (double)((float)this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * p_96449_9_);
            }
        }

        super.renderOffsetLivingLabel(p_96449_1_, p_96449_2_, p_96449_4_, p_96449_6_, p_96449_8_, p_96449_9_, p_96449_10_);
    }

    public void renderFirstPersonArm(EntityPlayer p_82441_1_)
    {
        float var2 = 1.0F;
        GL11.glColor3f(var2, var2, var2);
        this.modelBipedMain.swingProgress = 0.0F;
        this.modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, p_82441_1_);
        this.modelBipedMain.bipedRightArm.render(0.0625F);
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(AbstractClientPlayer p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_)
    {
        if (p_77039_1_.isEntityAlive() && p_77039_1_.isPlayerSleeping())
        {
            super.renderLivingAt(p_77039_1_, p_77039_2_ + (double)p_77039_1_.field_71079_bU, p_77039_4_ + (double)p_77039_1_.field_71082_cx, p_77039_6_ + (double)p_77039_1_.field_71089_bV);
        }
        else
        {
            super.renderLivingAt(p_77039_1_, p_77039_2_, p_77039_4_, p_77039_6_);
        }
    }

    protected void rotateCorpse(AbstractClientPlayer p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_)
    {
        if (p_77043_1_.isEntityAlive() && p_77043_1_.isPlayerSleeping())
        {
            GL11.glRotatef(p_77043_1_.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(this.getDeathMaxRotation(p_77043_1_), 0.0F, 0.0F, 1.0F);
            GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.rotateCorpse(p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
        }
    }

    protected void renderOffsetLivingLabel(EntityLivingBase p_96449_1_, double p_96449_2_, double p_96449_4_, double p_96449_6_, String p_96449_8_, float p_96449_9_, double p_96449_10_)
    {
        this.renderOffsetLivingLabel((AbstractClientPlayer)p_96449_1_, p_96449_2_, p_96449_4_, p_96449_6_, p_96449_8_, p_96449_9_, p_96449_10_);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_)
    {
        this.preRenderCallback((AbstractClientPlayer)p_77041_1_, p_77041_2_);
    }

    protected void func_82408_c(EntityLivingBase p_82408_1_, int p_82408_2_, float p_82408_3_)
    {
        this.func_82408_c((AbstractClientPlayer)p_82408_1_, p_82408_2_, p_82408_3_);
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_)
    {
        return this.shouldRenderPass((AbstractClientPlayer)p_77032_1_, p_77032_2_, p_77032_3_);
    }

    protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_)
    {
        this.renderEquippedItems((AbstractClientPlayer)p_77029_1_, p_77029_2_);
    }

    protected void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_)
    {
        this.rotateCorpse((AbstractClientPlayer)p_77043_1_, p_77043_2_, p_77043_3_, p_77043_4_);
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(EntityLivingBase p_77039_1_, double p_77039_2_, double p_77039_4_, double p_77039_6_)
    {
        this.renderLivingAt((AbstractClientPlayer)p_77039_1_, p_77039_2_, p_77039_4_, p_77039_6_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((AbstractClientPlayer)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity p_110775_1_)
    {
        return this.getEntityTexture((AbstractClientPlayer)p_110775_1_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((AbstractClientPlayer)p_76986_1_, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
    }
}
