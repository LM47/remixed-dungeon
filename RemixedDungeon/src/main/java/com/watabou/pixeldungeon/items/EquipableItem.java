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
package com.watabou.pixeldungeon.items;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.utils.GLog;

public abstract class EquipableItem extends Item {

	protected static final String AC_EQUIP   = "EquipableItem_ACEquip";
	protected static final String AC_UNEQUIP = "EquipableItem_ACUnequip";

	@Override
	public void execute( Hero hero, String action ) {
		switch (action) {
			case AC_EQUIP:
				doEquip(hero);
				break;
			case AC_UNEQUIP:
				doUnequip(hero, true);
				break;
			default:
				super.execute(hero, action);
				break;
		}
	}
	
	@Override
	public void doDrop( Hero hero ) {
		if (!isEquipped( hero ) || doUnequip( hero, false, false )) {
			super.doDrop( hero );
		}
	}
	
	@Override
	public void cast( final Hero user, int dst ) {

		if (isEquipped( user )) {
			if (quantity() == 1 && !this.doUnequip( user, false, false )) {
				return;
			}
		}
		
		super.cast( user, dst );
	}
	
	protected static void equipCursed( Hero hero ) {
		hero.getSprite().emitter().burst( ShadowParticle.CURSE, 6 );
		Sample.INSTANCE.play( Assets.SND_CURSED );
	}
	
	protected float time2equip(Char hero ) {
		return 1;
	}
	
	public abstract boolean doEquip( Hero hero );
	
	public boolean doUnequip(Char hero, boolean collect, boolean single ) {
		
		if (cursed) {
			GLog.w( Game.getVar(R.string.EquipableItem_Unequip), name() );
			return false;
		}
		
		if (single) {
			hero.spendAndNext( time2equip( hero ) );
		} else {
			hero.spend( time2equip( hero ) );
		}
		
		if (collect && !collect( hero.getBelongings().backpack )) {
			Dungeon.level.drop( this, hero.getPos() );
		}
				
		return true;
	}

	public boolean doUnequip( Char hero, boolean collect ) {
		return doUnequip( hero, collect, true );
	}
}
