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
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class KindOfWeapon extends EquipableItem {

	protected static final float TIME_TO_EQUIP = 1f;

	public static final String BASIC_ATTACK = "none";
	public static final String SWORD_ATTACK = "sword";
	public static final String SPEAR_ATTACK = "spear";
	public static final String BOW_ATTACK   = "bow";
	public static final String STAFF_ATTACK = "staff";
	public static final String HEAVY_ATTACK = "heavy";
	public static final String WAND_ATTACK  = STAFF_ATTACK;
	public static final String KUSARIGAMA_ATTACK  = "kusarigama";
    public static final String CROSSBOW_ATTACK = "crossbow";

    protected String animation_class = BASIC_ATTACK;

	public int		MIN	= 0;
	public int		MAX = 1;
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( isEquipped( hero ) ? AC_UNEQUIP : AC_EQUIP );
		return actions;
	}
	
	@Override
	public boolean isEquipped( Char chr ) {
		if(chr instanceof Hero) {
			Hero hero = (Hero)chr;
			return hero.belongings.weapon == this;
		}
		return false;
	}
	
	@Override
	public boolean doEquip( Hero hero ) {
		
		detachAll( hero.belongings.backpack );
		
		if (hero.belongings.weapon == null || hero.belongings.weapon.doUnequip( hero, true )) {
			
			hero.belongings.weapon = this;
			activate( hero );
			
			QuickSlot.refresh();

			hero.updateSprite();

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n(Game.getVar(R.string.KindOfWeapon_EquipCursed), name() );
			}
			
			hero.spendAndNext( TIME_TO_EQUIP );
			return true;
			
		} else {
			
			collect( hero.belongings.backpack );
			return false;
		}
	}
	
	@Override
	public boolean doUnequip(Char hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {
			
			hero.getBelongings().weapon = null;
			hero.updateSprite();
			return true;
			
		} else {
			
			return false;
			
		}
	}
	
	public void activate( Char hero ) {
	}
	
	public int damageRoll( Hero owner ) {
		return Random.NormalIntRange( MIN, MAX );
	}
	
	public float accuracyFactor(Hero hero ) {
		return 1f;
	}
	
	public float speedFactor( Hero hero ) {
		return 1f;
	}
	
	public void proc( Char attacker, Char defender, int damage ) {
	}

	public String getVisualName() {
		return getClassName();
	}

	public String getAnimationClass() {
		return animation_class;
	}

	public boolean goodForMelee() {
		return true;
	}
}
