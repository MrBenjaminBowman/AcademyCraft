/**
 * 
 */
package cn.academy.ability.electro.skill;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import cn.academy.ability.electro.CatElectro;
import cn.academy.ability.electro.client.render.skill.SRSmallCharge;
import cn.academy.ability.electro.entity.fx.ChargeEffectS;
import cn.academy.api.ability.SkillBase;
import cn.academy.api.ctrl.RawEventHandler;
import cn.academy.api.ctrl.pattern.PatternHold;
import cn.academy.api.data.AbilityData;
import cn.academy.api.data.AbilityDataMain;
import cn.academy.core.client.render.SkillRenderManager;
import cn.academy.core.client.render.SkillRenderManager.RenderNode;
import cn.liutils.util.GenericUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bioelectricity Itensify skill
 * @author WeathFolD
 */
public class SkillBuff extends SkillBase {
	
	@SideOnly(Side.CLIENT)
	static SRSmallCharge render = new SRSmallCharge(5, 0.5);

	public SkillBuff() {
		this.setLogo("electro/buff.png");
	}
	
	public String getInternalName() {
		return "em_buff";
	}
	
	public void initPattern(RawEventHandler reh) {
		reh.addPattern(new PatternHold(100) {

			@Override
			public State createSkill(EntityPlayer player) {
				return new BuffState(player);
			}
			
		});
	}
	
	public static class BuffState extends PatternHold.State {
		
		boolean good = true;
		
		@SideOnly(Side.CLIENT)
		RenderNode node;
		
		static List<Integer> buffs = new ArrayList<Integer>();
		static {
			buffs.addAll(Arrays.asList(new Integer[] {
				Potion.jump.id, Potion.damageBoost.id, Potion.moveSpeed.id, Potion.regeneration.id
			}));
		}
		
		static Random rand = new Random();
		
		final AbilityData data;
		final float consume;

		public BuffState(EntityPlayer player) {
			super(player);
			data = AbilityDataMain.getData(player);
			consume = 30 - data.getSkillLevel(CatElectro.buff) * 0.5F+ data.getLevelID() *2;
		}

		@Override
		public void onStart() {
			if(isRemote()) {
				node = SkillRenderManager.addEffect(render);
			}
		}
		
		@Override
		protected boolean onTick() {
			boolean b = data.decreaseCP(consume);
			good = good && b;
			return !b;
		}

		@Override
		public void onFinish() {
			if(isRemote()) {
				node.setDead();
			} 
			
			if(good) {
				int time = 50; //TODO: Change to min(real tick, 60)
				double prob = (time - 10) / 18.0;
				int life = GenericUtils.randIntv(2, 4) * time * 
						(data.getSkillID(CatElectro.buff) + data.getLevelID() * 2);
				int level = (int) prob;
				
				Collections.shuffle(buffs);
				for(int i = 0; i < prob; ++i) {
					double tmp = Math.min(1.0, prob - i);
					if(rand.nextDouble() < tmp) {
						player.addPotionEffect(new PotionEffect(buffs.get(i), life, level));
					}
				}
				
				if(isRemote()) {
					player.worldObj.spawnEntityInWorld(new ChargeEffectS(player, life, 8));
				}
			}
		}

		@Override
		public void onHold() {}
		
	}

}