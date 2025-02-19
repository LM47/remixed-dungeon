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
package com.watabou.pixeldungeon.actors.buffs;

import com.nyrds.LuaInterface;
import com.nyrds.Packable;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.mechanics.buffs.BuffFactory;
import com.nyrds.pixeldungeon.ml.EventCollector;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Thief;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.ui.BuffIndicator;
import com.watabou.pixeldungeon.utils.GLog;

import java.util.HashSet;
import java.util.Set;

public class Buff extends Actor implements NamedEntityKind, CharModifier {

	protected final Set<String> EMPTY_STRING_SET = new HashSet<>();
	public Char target;

	@Packable(defaultValue = "1")
	protected int level=1;

	@Override
	public String getEntityKind() {
		return getClass().getSimpleName();
	}

	@Override
	public String name() {
		return getEntityKind();
	}

	public void attachVisual() {
		target.getSprite().add(charSpriteStatus());
	}

	interface itemAction{
		Item   act(Item srcItem);
		String actionText(Item srcItem);
		void   carrierFx();
	}
	
	public boolean attachTo( Char target ) {

		if (target.immunities().contains( getEntityKind() )) {
			return false;
		}
		
		this.target = target;
		target.add(this);
		
		return true;
	}
	
	public void detach() {
		target.remove(this);
	}

	@Override
	public boolean act() {
		deactivate();
		return true;
	}

	public int icon() {
		return BuffIndicator.NONE;
	}
	
	public static<T extends Buff> T affect( Char target, Class<T> buffClass ) {
		T buff = target.buff( buffClass );
		if (buff != null) {
			return buff;
		} else {
			try {
				buff = buffClass.newInstance();
				buff.attachTo(target);
				return buff;
			} catch (Exception e) {
				throw new TrackedRuntimeException(e);
			}
		}
	}

	@LuaInterface
	public static Buff permanent( Char target, String buffClass ) {
		Buff buff = BuffFactory.getBuffByName(buffClass);
		buff.attachTo(target);
		buff.deactivate();
		return buff;
	}

	@LuaInterface
	public static<T extends Buff> T permanent( Char target, Class<T> buffClass ) {
		T buff = affect( target, buffClass );
		buff.deactivate();
		return buff;
	}

	@LuaInterface
	public static Buff affect( Char target, String buffClass, float duration ) {
		Buff buff = BuffFactory.getBuffByName(buffClass);
		buff.attachTo(target);
		buff.spend( duration );
		return buff;
	}

	@LuaInterface
	public static<T extends Buff> T affect( Char target, Class<T> buffClass, float duration ) {
		T buff = affect( target, buffClass );
		buff.spend( duration );
		return buff;
	}

	@LuaInterface
	public static Buff prolong( Char target, String  buffClass, float duration ) {
		Buff buff = BuffFactory.getBuffByName(buffClass);
		buff.attachTo(target);
		buff.postpone( duration );
		return buff;
	}


	@LuaInterface
	public static<T extends Buff> T prolong( Char target, Class<T> buffClass, float duration ) {
		T buff = affect( target, buffClass );
		buff.postpone( duration );
		return buff;
	}
	
	public static void detach( Buff buff ) {
		if (buff != null) {
			buff.detach();
		}
	}
	
	public static void detach( Char target, Class<? extends Buff> cl ) {
		detach( target.buff( cl ) );
	}

	public void level(int level ) {
		this.level = level;
	}

	public int level() {
		return level;
	}

	public int drBonus() {
		return 0;
	}

	public int stealthBonus() { return 0; }

	public float speedMultiplier() { return 1;}

	public int defenceProc(Char defender, Char enemy, int damage)
	{
		return damage;
	}

	@Override
	public int regenerationBonus() {
		return 0;
	}

	@Override
	public void charAct() {
	}

	@Override
	public int dewBonus() {
		return 0;
	}

	@Override
	public Set<String> resistances() {
		return EMPTY_STRING_SET;
	}

	@Override
	public Set<String> immunities() {
		return EMPTY_STRING_SET;
	}

	public CharSprite.State charSpriteStatus() {
		return CharSprite.State.NONE;
	}

	private void collectOrDropItem(Item item){
		if(!item.collect( ((Hero)target).belongings.backpack )){
			Dungeon.level.drop(item, target.getPos()).sprite.drop();
		}	
	}

	protected void applyToCarriedItems(itemAction action ){
		if (target instanceof Hero) {
			Hero hero = (Hero) target;
			
			int n = 1;
			
			if(hero.getDifficulty()>=3) {
				n = 5;
			}
			
			for (int i = 0; i < n; i++) {
				Item item = hero.belongings.randomUnequipped();

				if (item == null || item instanceof Bag || item instanceof Gold) {
					continue;
				}

				Item srcItem = item.detach(hero.belongings.backpack);

				if(srcItem == null) {
					EventCollector.logException(item.getClassName());
					continue;
				}

				item = action.act(srcItem);

				if (item == srcItem) { // item unaffected by buff
					collectOrDropItem(item);
					continue;
				}

				String actionText = null;

				if (item == null) {
					actionText = action.actionText(srcItem);
					action.carrierFx();
				} else {
					if (!item.equals(srcItem)) {
						actionText = action.actionText(srcItem);
						collectOrDropItem(item);

						action.carrierFx();
					}
				}

				if (actionText != null) {
					GLog.w(actionText);
				}
			}
		} else if (target instanceof Thief){
			if (((Thief)target).item == null)
			{
				return;
			}
			((Thief)target).item = action.act(((Thief)target).item);
			action.carrierFx();
		}
	}
}
