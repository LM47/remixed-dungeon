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
package com.watabou.pixeldungeon.items.potions;

import com.nyrds.pixeldungeon.items.common.UnknownItem;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.effects.Splash;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.ItemStatusHandler;
import com.watabou.pixeldungeon.items.food.RottenFood;
import com.watabou.pixeldungeon.items.scrolls.Scroll;
import com.watabou.pixeldungeon.items.weapon.missiles.Arrow;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashSet;

public class Potion extends Item implements UnknownItem {

	private static final String AC_DRINK   = "Potion_ACDrink";
	private static final String AC_MOISTEN = "Potion_ACMoisten";
	
	private static final float TIME_TO_DRINK = 1f;
	private static final float TIME_TO_MOISTEN = 1f;

	protected int labelIndex = -1;
	
	private static final Class<?>[] potions = {
		PotionOfHealing.class, 
		PotionOfExperience.class, 
		PotionOfToxicGas.class, 
		PotionOfLiquidFlame.class,
		PotionOfStrength.class,
		PotionOfParalyticGas.class,
		PotionOfLevitation.class,
		PotionOfMindVision.class, 
		PotionOfPurity.class,
		PotionOfInvisibility.class,
		PotionOfMight.class,
		PotionOfFrost.class
	};

	private static final Integer[] images = {
		ItemSpriteSheet.POTION_TURQUOISE, 
		ItemSpriteSheet.POTION_CRIMSON, 
		ItemSpriteSheet.POTION_AZURE, 
		ItemSpriteSheet.POTION_JADE, 
		ItemSpriteSheet.POTION_GOLDEN, 
		ItemSpriteSheet.POTION_MAGENTA, 
		ItemSpriteSheet.POTION_CHARCOAL, 
		ItemSpriteSheet.POTION_IVORY, 
		ItemSpriteSheet.POTION_AMBER, 
		ItemSpriteSheet.POTION_BISTRE, 
		ItemSpriteSheet.POTION_INDIGO, 
		ItemSpriteSheet.POTION_SILVER};
	
	private static ItemStatusHandler<Potion> handler;
	
	private String color;
	
	{	
		stackable = true;
		setDefaultAction(AC_DRINK);
	}
	
	
	private boolean shatterd = false;

	@SuppressWarnings("unchecked")
	public static void initColors() {
		handler = new ItemStatusHandler<>((Class<? extends Potion>[]) potions, images);
	}
	
	public static void save( Bundle bundle ) {
		handler.save( bundle );
	}
	
	@SuppressWarnings("unchecked")
	public static void restore( Bundle bundle ) {
		handler = new ItemStatusHandler<>((Class<? extends Potion>[]) potions, images, bundle);
	}
	
	public Potion() {
		image = handler.index( this );
		color = Game.getVars(R.array.Potion_Colors)[ItemStatusHandler.indexByImage(image,images)];
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_DRINK );
		actions.add( AC_MOISTEN );
		return actions;
	}
	
	@Override
	public void execute( final Hero hero, String action ) {
		setUser(hero);

		switch (action) {
			case AC_DRINK:
				if (isKnown() && (
						this instanceof PotionOfLiquidFlame ||
								this instanceof PotionOfToxicGas ||
								this instanceof PotionOfParalyticGas)) {

					GameScene.show(
							new WndOptions(Game.getVar(R.string.Potion_Harmfull),
									Game.getVar(R.string.Potion_SureDrink),
									Game.getVar(R.string.Potion_Yes),
									Game.getVar(R.string.Potion_No)) {
								@Override
								protected void onSelect(int index) {
									if (index == 0) {
										drink(hero);
									}
								}
							}
					);

				} else {
					drink(hero);
				}

				break;
			case AC_MOISTEN:
				moisten(hero);
				break;
			default:

				super.execute(hero, action);

				break;
		}
	}
	
	@Override
	public void doThrow( final Hero hero ) {

		if (isKnown() && (
			this instanceof PotionOfExperience || 
			this instanceof PotionOfHealing || 
			this instanceof PotionOfLevitation ||
			this instanceof PotionOfMindVision ||
			this instanceof PotionOfStrength ||
			this instanceof PotionOfInvisibility || 
			this instanceof PotionOfMight)) {
		
			GameScene.show( 
				new WndOptions( Game.getVar(R.string.Potion_Beneficial),
						Game.getVar(R.string.Potion_SureThrow),
						Game.getVar(R.string.Potion_Yes),
						Game.getVar(R.string.Potion_No) ) {
					@Override
					protected void onSelect(int index) {
						if (index == 0) {
							Potion.super.doThrow( hero );
						}
					}
				}
			);
			
		} else {
			super.doThrow( hero );
		}
	}
	
	protected void drink( Hero hero ) {
		
		detach( hero.belongings.backpack );
		
		hero.spend( TIME_TO_DRINK );
		hero.busy();
		onThrow( hero.getPos() );
		
		Sample.INSTANCE.play( Assets.SND_DRINK );
		
		hero.getSprite().operate( hero.getPos() );
		shatterd = false;
	}
	
	private void moisten(Hero hero) {
		
		hero.spend( TIME_TO_MOISTEN);
		hero.busy();
		
		GameScene.selectItem( itemSelector, WndBag.Mode.MOISTABLE, Game.getVar(R.string.Potion_SelectForMoisten) );
		
		hero.getSprite().operate( hero.getPos() );
	}
	
	@Override
	protected void onThrow( int cell ) {
		if (Dungeon.hero.getPos() == cell) {
			apply( Dungeon.hero );
		} else if (Dungeon.level.map[cell] == Terrain.WELL || Dungeon.level.pit[cell]) {
			super.onThrow( cell );
		} else  {
			shatter( cell );
		}
	}
	
	protected void apply( Hero hero ) {
		shatter( hero.getPos() );
	}

	protected boolean canShatter() {
		if(!shatterd) {
			shatterd = true;
			return true;
		}
		return false;
	}

	public void shatter( int cell ) {
		GLog.i(Utils.format(Game.getVar(R.string.Potion_Shatter), color()));
		Sample.INSTANCE.play( Assets.SND_SHATTER );
		splash( cell );
	}
	
	public boolean isKnown() {
		return handler.isKnown( this );
	}
	
	public void setKnown() {
		if (!isKnown()) {
			handler.know( this );
		}
		
		Badges.validateAllPotionsIdentified();
	}
	
	@Override
	public Item identify() {
		setKnown();
		return this;
	}
	
	protected String color() {
		return color;
	}
	
	@Override
	public String name() {
		return isKnown() ? name : Utils.format(Game.getVar(R.string.Potion_Name), color);
	}
	
	@Override
	public String info() {
		return isKnown() ? desc() : Utils.format(Game.getVar(R.string.Potion_Info), color);
	}
	
	@Override
	public boolean isIdentified() {
		return isKnown();
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	public static HashSet<Class<? extends Potion>> getKnown() {
		return handler.known();
	}
	
	public static HashSet<Class<? extends Potion>> getUnknown() {
		return handler.unknown();
	}
	
	public static boolean allKnown() {
		return handler.known().size() == potions.length;
	}
	
	protected void splash( int cell ) {
		final int color = ItemSprite.pick( image, 8, 10 );
		Splash.at( cell, color, 5 );
	}

	public int basePrice() {
		return 20;
	}

	@Override
	public int price() {
		return (int) ((isKnown() ? basePrice() : 20) * quantity() * qualityFactor());
	}
	
	@Override
	public Item freeze(int cell){
		shatter(cell);
		return null;
	}
	
	private final WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				
				if(item instanceof Arrow) {
					moistenArrow ((Arrow) item );
				}
				
				if(item instanceof Scroll) {
					moistenScroll ((Scroll) item );
				}
				
				if(item instanceof RottenFood) {
					moistenRottenFood((RottenFood) item);
				}
			}
		}
	};

	protected int detachMoistenItems(Item item, int maxQuantity) {
		setKnown();
		int quantity = item.quantity();
		
		if(quantity <= maxQuantity){
			
			if(item.equals(getUser().belongings.weapon)) {
				getUser().belongings.weapon = null;
			} else {
				item.detachAll( getUser().belongings.backpack );
			}
		} else {
			item.quantity(item.quantity() - maxQuantity);
			quantity = maxQuantity;
		}
		return quantity;
	}

	double qualityFactor() {
		return 1;
	}

	protected int reallyMoistArrows(Arrow arrow) {
		int quantity = detachMoistenItems(arrow, (int) (10*qualityFactor()));
		moistenEffective();
		GLog.i(Game.getVar(R.string.Potion_ArrowMoisten));
		return quantity;
	}

	protected void moistenRottenFood(RottenFood scroll) {
		moistenUseless();
	}
	
	protected void moistenScroll(Scroll scroll) {
		moistenUseless();
	}
	
	protected void moistenArrow(Arrow arrow) {
		moistenUseless();
	}
	
	private void moistenUseless() {
		detach(getUser().belongings.backpack );
		GLog.i(Game.getVar(R.string.Potion_MoistenUseless));
		getUser().getSprite().operate( getUser().getPos() );
		getUser().spend( TIME_TO_MOISTEN );
		getUser().busy();
	}
	
	protected void moistenEffective() {
		detach(getUser().belongings.backpack );
		identify();
		getUser().getSprite().operate( getUser().getPos() );
		getUser().spend( TIME_TO_MOISTEN );
		getUser().busy();
	}

	@Override
	public int overlayIndex() {
		if(!isIdentified()) {
			return super.overlayIndex();
		}
		return labelIndex;
	}
}
