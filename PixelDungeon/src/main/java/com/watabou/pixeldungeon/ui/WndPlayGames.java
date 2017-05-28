package com.watabou.pixeldungeon.ui;

import com.nyrds.android.util.GuiProperties;
import com.nyrds.pixeldungeon.items.common.Library;
import com.nyrds.pixeldungeon.support.PlayGames;
import com.watabou.noosa.Game;
import com.watabou.noosa.Text;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.windows.WndMessage;

/**
 * Created by mike on 14.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

class WndPlayGames extends Window {

	private int y = GAP;

	public WndPlayGames() {

		boolean playGamesConnected = PlayGames.isConnected();
		resizeLimited(120);

		Text listTitle = PixelScene.createMultiline("Google Play Games", GuiProperties.mediumTitleFontSize());
		listTitle.hardlight(TITLE_COLOR);
		listTitle.maxWidth(width - GAP * 2);
		listTitle.measure();
		listTitle.x = (width - listTitle.width()) / 2;
		listTitle.y = y;

		add(listTitle);

		y += listTitle.height() + GAP;

		CheckBox usePlayGames = new CheckBox("use Google Play Games", playGamesConnected) {
			@Override
			public void checked(boolean value) {
				super.checked(value);

				if (value) {
					PlayGames.connect();
					Game.scene().add(new WndMessage("Connecting to Google Play Games, please wait a bit"));
				} else {
					PlayGames.disconnect();
				}

				hide();
			}
		};

		addButton(usePlayGames);

		if (!playGamesConnected) {
			resize(width, y);
			return;
		}

		addButton(new RedButton("Show badges") {
			@Override
			protected void onClick() {
				super.onClick();
				PlayGames.showBadges();
			}
		});

		addButton(new RedButton("Local -> Cloud") {
			@Override
			protected void onClick() {
				super.onClick();
				Game.instance().executor.execute(new Runnable() {
					@Override
					public void run() {

						boolean res = PlayGames.copyFileToCloud(Badges.BADGES_FILE)
								&& PlayGames.copyFileToCloud(Library.LIBRARY_FILE);

						showActionResult(res);
					}
				});
			}
		});


		addButton(new RedButton("Cloud -> Local") {
			@Override
			protected void onClick() {
				super.onClick();
				Game.instance().executor.execute(new Runnable() {
					@Override
					public void run() {
						boolean res = PlayGames.copyFileFromCloud(Badges.BADGES_FILE)
								&& PlayGames.copyFileFromCloud(Library.LIBRARY_FILE);

						showActionResult(res);
					}
				});
			}
		});
		resize(width, y);
	}

	private void showActionResult(final boolean res) {
		Game.executeInGlThread(new Runnable() {
			@Override
			public void run() {
				if (res) {
					Game.scene().add(new WndMessage("ok!"));
				} else {
					Game.scene().add(new WndMessage("something went wrong..."));
				}
				hide();
				;
			}
		});
	}

	private void addButton(TextButton btn) {
		btn.setRect(0, y, width, BUTTON_HEIGHT);
		add(btn);
		y += btn.height() + GAP;
	}
}
