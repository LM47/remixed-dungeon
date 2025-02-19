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

import com.nyrds.pixeldungeon.items.common.MasteryItem;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndChooseWay;

import java.util.ArrayList;

public class TomeOfMastery extends MasteryItem {

	public static final String AC_READ                 = "TomeOfMastery_ACRead";

	{
		stackable = false;
		image = ItemSpriteSheet.MASTERY;
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );		
		actions.add( AC_READ );
		
		return actions;
	}

	@Override
	protected boolean givesMasteryTo(Char hero) {
		switch (hero.getHeroClass()) {
			case NECROMANCER:
			case GNOLL:
				return false;
			default:
				return true;
		}
	}

	@Override
	public boolean doPickUp(Char hero ) {
		if (hero.getHeroClass() != HeroClass.NECROMANCER)
		{
			Badges.validateMastery();
		}
		return super.doPickUp( hero );
	}

	@Override
	public void execute( Hero hero, String action ) {
		if (action.equals( AC_READ )) {
			
			if (hero.hasBuff( Blindness.class )) {
				GLog.w( Game.getVar(R.string.TomeOfMastery_Blinded) );
				return;
			}

			if(hero.getSubClass() != HeroSubClass.NONE) {
				GLog.w( Game.getVar(R.string.TomeOfMastery_WayAlreadyChosen) );
				return;
			}

			setUser(hero);
			
			HeroSubClass way1 = null;
			HeroSubClass way2 = null;
			switch (hero.getHeroClass()) {
			case WARRIOR:
				way1 = HeroSubClass.GLADIATOR;
				way2 = HeroSubClass.BERSERKER;
				break;
			case MAGE:
				way1 = HeroSubClass.BATTLEMAGE;
				way2 = HeroSubClass.WARLOCK;
				break;
			case ROGUE:
				way1 = HeroSubClass.FREERUNNER;
				way2 = HeroSubClass.ASSASSIN;
				break;
			case HUNTRESS:
				way1 = HeroSubClass.SNIPER;
				way2 = HeroSubClass.WARDEN;
				break;
			case ELF:
				way1 = HeroSubClass.SCOUT;
				way2 = HeroSubClass.SHAMAN;
				break;
			default:
				GLog.w( Game.getVar(R.string.TomeOfMastery_WayAlreadyChosen) );
				return;
			}
			GameScene.show( new WndChooseWay( this, way1, way2 ) );
			
		} else {
			
			super.execute( hero, action );
			
		}
	}
}
