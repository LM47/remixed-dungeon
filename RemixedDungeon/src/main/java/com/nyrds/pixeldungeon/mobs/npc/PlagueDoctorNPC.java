package com.nyrds.pixeldungeon.mobs.npc;

import com.nyrds.pixeldungeon.items.common.RatArmor;
import com.nyrds.pixeldungeon.items.common.RatHide;
import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Journal;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class PlagueDoctorNPC extends ImmortalNPC {

	public PlagueDoctorNPC() {
	}

	@Override
	public boolean interact(final Char hero) {
		getSprite().turnTo(getPos(), hero.getPos());
		if (Quest.completed) {
			GameScene.show(new WndQuest(this, Game.getVar(R.string.PlagueDoctorNPC_After_Quest)));
			return true;
		}

		if (Quest.given) {

			Item item = hero.getBelongings().getItem(RatHide.class);
			if (item != null && item.quantity() >= 5) {
				item.removeItemFrom(hero);

				Item reward = new RatArmor();
				reward.identify();

				if (reward.doPickUp(hero)) {
					GLog.i(Hero.getHeroYouNowHave(), reward.name());
				} else {
					level().drop(reward, hero.getPos()).sprite.drop();
				}
				Quest.complete();
				GameScene.show(new WndQuest(this, Game.getVar(R.string.PlagueDoctorNPC_Quest_End)));
			} else {
				GameScene.show(new WndQuest(this, (Utils.format(Game.getVar(R.string.PlagueDoctorNPC_Quest_Reminder), 5)) ) );
			}

		} else {
			String txtQuestStart = Utils.format(Game.getVar(R.string.PlagueDoctorNPC_Quest_Start_Male), 5);
			if (hero.getGender() == Utils.FEMININE) {
				txtQuestStart = Utils.format(Game.getVar(R.string.PlagueDoctorNPC_Quest_Start_Female), 5);
			}
			GameScene.show(new WndQuest(this, txtQuestStart));
			Quest.process(hero.getPos());
			Quest.given = true;
			Journal.add(Journal.Feature.PLAGUEDOCTOR.desc());
		}
		return true;
	}

	public static class Quest {

		private static boolean completed;
		private static boolean given;
		private static boolean processed;

		private static int   depth;

		public static void reset() {
			completed = false;
			processed = false;
			given = false;
		}

		private static final String COMPLETED = "completed";
		private static final String NODE      = "plaguedoctornpc";
		private static final String GIVEN     = "given";
		private static final String PROCESSED = "processed";
		private static final String DEPTH     = "depth";

		public static void storeInBundle(Bundle bundle) {
			Bundle node = new Bundle();

			node.put(GIVEN, given);
			node.put(DEPTH, depth);
			node.put(PROCESSED, processed);
			node.put(COMPLETED, completed);

			bundle.put(NODE, node);
		}

		public static void restoreFromBundle(Bundle bundle) {

			Bundle node = bundle.getBundle(NODE);

			if (!node.isNull()) {
				given = node.getBoolean(GIVEN);
				depth = node.getInt(DEPTH);
				processed = node.getBoolean(PROCESSED);
				completed = node.getBoolean(COMPLETED);
			}
		}

		public static void process(int pos) {
			Item item = Dungeon.hero.belongings.getItem(RatHide.class);
			if (completed){
				return;
			}

			if (item != null && item.quantity() == 5) {
				processed = true;
			}

			if (given && !processed) {
				if (Random.Int(2) == 1) {
					Dungeon.level.drop(new RatHide(), pos).sprite.drop();
				}
			}
		}

		static void complete() {
			completed = true;
			Journal.remove(Journal.Feature.PLAGUEDOCTOR.desc());
		}
	}
}