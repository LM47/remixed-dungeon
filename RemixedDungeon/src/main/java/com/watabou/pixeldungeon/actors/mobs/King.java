/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.Packable;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.necropolis.UndeadMob;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.items.ArmorKit;
import com.watabou.pixeldungeon.items.keys.SkeletonKey;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.items.wands.WandOfDisintegration;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class King extends Boss {
	
	private static final int MAX_ARMY_SIZE	= 5;

	@Packable
	private int lastPedestal;

	@Packable
	private int targetPedestal;

	public King() {
		hp(ht(300));
		exp = 40;
		defenseSkill = 25;

		lastPedestal   = -1;
		targetPedestal = -1;

		addResistance( ToxicGas.class );
		addResistance( WandOfDisintegration.class );
		
		addImmunity( Paralysis.class );
	}


	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 20, 38 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 32;
	}
	
	@Override
	public int dr() {
		return 14;
	}
	
	
	@Override
	public boolean getCloser(int target) {

		Level level = Dungeon.level;
		int x = level.cellX(getPos());
		int y = level.cellY(getPos());

 		targetPedestal = level.getNearestTerrain(x,y, Terrain.PEDESTAL, lastPedestal);

		if(canTryToSummon()) {
			return super.getCloser( targetPedestal );
		}

		return super.getCloser(target);
	}
	
	@Override
    public boolean canAttack(Char enemy) {
		return canTryToSummon() ? 
			getPos() == targetPedestal :
			Dungeon.level.adjacent( getPos(), enemy.getPos() );
	}

	private int countServants() {
		int count = 0;

		for(Mob mob:level().getCopyOfMobsArray()){
			if (mob instanceof Undead) {
				count++;
			}
		}
		return count;
	}

	private boolean canTryToSummon() {
		if (!level().cellValid(targetPedestal)) {
			return false;
		}

		if (countServants() < maxArmySize()) {
			Char ch = Actor.findChar(targetPedestal);
			return ch == this || ch == null;
		} else {
			return false;
		}
	}

	@Override
	public void die(NamedEntityKind cause) {

		level().drop( new ArmorKit(), getPos() ).sprite.drop();
		level().drop( new SkeletonKey(), getPos() ).sprite.drop();
		
		super.die( cause );
		
		Badges.validateBossSlain(Badges.Badge.BOSS_SLAIN_4);
		
		yell(Utils.format(Game.getVar(R.string.King_Info1), Dungeon.hero.getHeroClass().title()));
	}
	
	private int maxArmySize() {
		return (int) (1 + MAX_ARMY_SIZE * (ht() - hp()) / ht() * Game.getDifficultyFactor());
	}

	@Override
	public boolean zap(@NotNull Char enemy) {
		summon();
		return true;
	}

	private void summon() {
		lastPedestal = targetPedestal;

		getSprite().centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.4f, 2 );		
		Sample.INSTANCE.play( Assets.SND_CHALLENGE );
		
		int undeadsToSummon = maxArmySize() - countServants();

		for (int i=0; i < undeadsToSummon; i++) {
			int pos = level().getEmptyCellNextTo(lastPedestal);

			if (level().cellValid(pos)) {
				Mob servant = new Undead();
				servant.setPos(pos);
				level().spawnMob(servant, 0, lastPedestal);

				WandOfBlink.appear(servant, pos);
				new Flare(3, 32).color(0x000000, false).show(servant.getSprite(), 2f);
			}
		}
		yell(Game.getVar(R.string.King_Info2));
	}
	
	@Override
	public void notice() {
		super.notice();
		yell(Game.getVar(R.string.King_Info3));
	}
	
	public static class Undead extends UndeadMob {

		public Undead() {
			hp(ht(28));
			defenseSkill = 15;
			
			exp = 0;
			
			setState(MobAi.getStateByClass(Wandering.class));
		}

		@Override
		public int damageRoll() {
			return Random.NormalIntRange( 12, 16 );
		}
		
		@Override
		public int attackSkill( Char target ) {
			return 16;
		}
		
		@Override
		public int attackProc(@NotNull Char enemy, int damage ) {
			if (Random.Int( MAX_ARMY_SIZE ) == 0) {
				Buff.prolong( enemy, Paralysis.class, 1 );
			}
			
			return damage;
		}
		
		@Override
		public void damage(int dmg, @NotNull NamedEntityKind src ) {
			super.damage( dmg, src );
			if (src instanceof ToxicGas) {		
				((ToxicGas)src).clearBlob( getPos() );
			}
		}
		
		@Override
		public void die(NamedEntityKind cause) {
			super.die( cause );
			
			if (Char.isVisible(this)) {
				Sample.INSTANCE.play( Assets.SND_BONES );
			}
		}
		
		@Override
		public int dr() {
			return 5;
		}
	}
}
