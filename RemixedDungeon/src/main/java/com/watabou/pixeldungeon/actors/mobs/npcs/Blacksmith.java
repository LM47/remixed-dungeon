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
package com.watabou.pixeldungeon.actors.mobs.npcs;

import com.nyrds.pixeldungeon.mechanics.NamedEntityKind;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.EquipableItem;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.quest.DarkGold;
import com.watabou.pixeldungeon.items.quest.Pickaxe;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.BlacksmithSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBlacksmith;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Blacksmith extends NPC {

	{
		spriteClass = BlacksmithSprite.class;
	}
	
	@Override
    public boolean act() {
		throwItem();		
		return super.act();
	}
	
	@Override
	public boolean interact(final Char hero) {
		
		getSprite().turnTo( getPos(), hero.getPos() );
		
		if (!Quest.given) {
			
			GameScene.show( new WndQuest( this, 
				Quest.alternative ?
						Game.getVar(R.string.Blacksmith_Blood1) :
						Game.getVar(R.string.Blacksmith_Gold1) )
			{
				
				@Override
				public void onBackPressed() {
					super.onBackPressed();
					
					Quest.given = true;
					Quest.completed = false;
					
					Pickaxe pick = new Pickaxe();
					if (pick.doPickUp( hero )) {
						GLog.i( Hero.getHeroYouNowHave(), pick.name() );
					} else {
						Dungeon.level.drop( pick, hero.getPos() ).sprite.drop();
					}
				}
			} );
			
			Journal.add( Journal.Feature.TROLL.desc() );
			
		} else if (!Quest.completed) {
			if (Quest.alternative) {
				
				Pickaxe pick = hero.getBelongings().getItem( Pickaxe.class );
				if (pick == null) {
					tell( Game.getVar(R.string.Blacksmith_Txt2) );
				} else if (!pick.bloodStained) {
					tell( Game.getVar(R.string.Blacksmith_Txt4) );
				} else {
					if (pick.isEquipped( hero )) {
						pick.doUnequip( hero, false );
					}
					pick.detach( hero.getBelongings().backpack );
					tell( Game.getVar(R.string.Blacksmith_Completed) );
					
					Quest.completed = true;
					Quest.reforged = false;
				}
				
			} else {
				
				Pickaxe pick = hero.getBelongings().getItem( Pickaxe.class );
				DarkGold gold = hero.getBelongings().getItem( DarkGold.class );
				if (pick == null) {
					tell( Game.getVar(R.string.Blacksmith_Txt2) );
				} else if (gold == null || gold.quantity() < 15) {
					tell( Game.getVar(R.string.Blacksmith_Txt3) );
				} else {
					if (pick.isEquipped( hero )) {
						pick.doUnequip( hero, false );
					}
					pick.detach( hero.getBelongings().backpack );
					gold.detachAll( hero.getBelongings().backpack );
					tell( Game.getVar(R.string.Blacksmith_Completed) );
					
					Quest.completed = true;
					Quest.reforged = false;
				}
				
			}
		} else if (!Quest.reforged) {
			
			GameScene.show( new WndBlacksmith( this) );
			
		} else {
			
			tell( Game.getVar(R.string.Blacksmith_GetLost) );
			
		}
		return true;
	}
	
	private void tell( String text ) {
		GameScene.show( new WndQuest( this, text ) );
	}
	
	public static String verify( Item item1, Item item2 ) {
		
		if (item1 == item2) {
			return Game.getVar(R.string.Blacksmith_Verify1);
		}
		
		if (item1.getClass() != item2.getClass()) {
			return Game.getVar(R.string.Blacksmith_Verify2);
		}
		
		if (!item1.isIdentified() || !item2.isIdentified()) {
			return Game.getVar(R.string.Blacksmith_Verify3);
		}
		
		if (item1.cursed || item2.cursed) {
			return Game.getVar(R.string.Blacksmith_Verify4);
		}
		
		if (item1.level() < 0 || item2.level() < 0) {
			return Game.getVar(R.string.Blacksmith_Verify5);
		}
		
		if (!item1.isUpgradable() || !item2.isUpgradable()) {
			return Game.getVar(R.string.Blacksmith_Verify6);
		}
		
		return null;
	}
	
	public static void upgrade( Item item1, Item item2 ) {
		
		Item first, second;
		if (item2.level() > item1.level()) {
			first = item2;
			second = item1;
		} else {
			first = item1;
			second = item2;
		}

		Sample.INSTANCE.play( Assets.SND_EVOKE );
		ScrollOfUpgrade.upgrade( Dungeon.hero );
		Item.evoke( Dungeon.hero );
		
		if (first.isEquipped( Dungeon.hero )) {
			((EquipableItem)first).doUnequip( Dungeon.hero, true );
		}
		first.upgrade();
		GLog.p( Game.getVar(R.string.Blacksmith_LooksBetter), first.name() );
		Dungeon.hero.spendAndNext( 2f );
		Badges.validateItemLevelAcquired( first );
		
		if (second.isEquipped( Dungeon.hero )) {
			((EquipableItem)second).doUnequip( Dungeon.hero, false );
		}
		second.detach( Dungeon.hero.belongings.backpack );
		
		Quest.reforged = true;
		
		Journal.remove( Journal.Feature.TROLL.desc() );
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		return 1000;
	}
	
	@Override
	public void damage(int dmg, @NotNull NamedEntityKind src ) {
	}
	
	@Override
	public void add( Buff buff ) {
	}
	
	@Override
	public boolean reset() {
		return true;
	}
	
	public static class Quest {
		
		private static boolean spawned;
		
		private static boolean alternative;
		private static boolean given;
		private static boolean completed;
		private static boolean reforged;
		
		public static void reset() {
			spawned		= false;
			given		= false;
			completed	= false;
			reforged	= false;
		}
		
		private static final String NODE	= "blacksmith";
		
		private static final String SPAWNED		= "spawned";
		private static final String ALTERNATIVE	= "alternative";
		private static final String GIVEN		= "given";
		private static final String COMPLETED	= "completed";
		private static final String REFORGED	= "reforged";
		
		public static void storeInBundle( Bundle bundle ) {
			
			Bundle node = new Bundle();
			
			node.put( SPAWNED, spawned );
			
			if (spawned) {
				node.put( ALTERNATIVE, alternative );
				node.put( GIVEN, given );
				node.put( COMPLETED, completed );
				node.put( REFORGED, reforged );
			}
			
			bundle.put( NODE, node );
		}
		
		public static void restoreFromBundle( Bundle bundle ) {

			Bundle node = bundle.getBundle( NODE );
			
			if (!node.isNull() && (spawned = node.getBoolean( SPAWNED ))) {
				alternative	=  node.getBoolean( ALTERNATIVE );
				given = node.getBoolean( GIVEN );
				completed = node.getBoolean( COMPLETED );
				reforged = node.getBoolean( REFORGED );
			} else {
				reset();
			}
		}
		
		public static void spawn( Collection<Room> rooms ) {
			if (!spawned && Dungeon.depth > 11 && Random.Int( 15 - Dungeon.depth ) == 0) {
				
				Room blacksmith;
				for (Room r : rooms) {
					if (r.type == Type.STANDARD && r.width() > 4 && r.height() > 4) {
						blacksmith = r;
						blacksmith.type = Type.BLACKSMITH;
						
						spawned = true;
						alternative = Random.Int( 2 ) == 0;
						
						given = false;
						
						break;
					}
				}
			}
		}
	}
}
