package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Passive;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.items.armor.Armor;
import com.watabou.pixeldungeon.items.armor.ClothArmor;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.watabou.pixeldungeon.items.weapon.enchantments.Death;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.HeroSpriteDef;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class ArmoredStatue extends Mob {

	@NotNull
	private Armor armor;

	public ArmoredStatue() {
		exp = 0;
		setState(MobAi.getStateByClass(Passive.class));

		do {
			armor = (Armor) Generator.random( Generator.Category.ARMOR );
		} while (armor == null || armor.level() < 0);

		armor.identify();
		armor.inscribe( Armor.Glyph.random() );

		hp(ht(15 + Dungeon.depth * 5));
		defenseSkill = 4 + Dungeon.depth + armor.DR;
		
		addImmunity( ToxicGas.class );
		addImmunity( Poison.class );
		addResistance( Death.class );
		addResistance( ScrollOfPsionicBlast.class );
		addImmunity( Leech.class );
		addImmunity( Bleeding.class );
	}

	private static final String ARMOR	= "armor";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARMOR, armor );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		armor = (Armor) bundle.get( ARMOR );
	}
	
	@Override
    public boolean act() {
		if (!isPet() && Char.isVisible(this)) {
			Journal.add( Journal.Feature.STATUE.desc() );
		}
		return super.act();
	}

	@Override
	public int dr() {
		return Dungeon.depth + armor.DR;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 4, 8 );
	}

	@Override
	public int attackSkill( Char target ) {
		return (9 + Dungeon.depth) * 2;
	}

	@Override
	public int defenseProc(Char enemy, int damage) {
		damage = super.defenseProc(enemy, damage);
		return armor.proc(enemy, this, damage);
	}

	@Override
	public void beckon( int cell ) {
	}
	
	@Override
	public void die(NamedEntityKind cause) {
		if (armor != null) {
			Dungeon.level.drop( armor, getPos() ).sprite.drop();
		}
		super.die( cause );
	}
	
	@Override
	public void destroy() {
		Journal.remove( Journal.Feature.STATUE.desc() );
		super.destroy();
	}
	
	@Override
	public boolean reset() {
		setState(MobAi.getStateByClass(Passive.class));
		return true;
	}

	@Override
	public String description() {
		return Utils.format(Game.getVar(R.string.ArmoredStatue_Desc), armor.name());
	}

	@Override
	public CharSprite sprite() {
		if(armor == null )
		{
			armor = new ClothArmor();
			EventCollector.logException("no armor");
		}

		return HeroSpriteDef.createHeroSpriteDef(armor);
	}
}
