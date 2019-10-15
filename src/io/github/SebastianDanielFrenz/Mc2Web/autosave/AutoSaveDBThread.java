package io.github.SebastianDanielFrenz.Mc2Web.autosave;

import io.github.SebastianDanielFrenz.Mc2Web.Mc2Web;

public class AutoSaveDBThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			Mc2Web.saveDBs();
			if (Mc2Web.plugin.getConfig().getBoolean(Mc2Web.cDEBUG)) {
				Mc2Web.plugin.getLogger().info("autosaved databases!");
			}
			try {
				Thread.sleep((long) (1000 * 60 * Mc2Web.plugin.getConfig().getDouble(Mc2Web.cAUTOSAVE_FREQUENCY)));
			} catch (InterruptedException e) {
				Mc2Web.plugin.getLogger().info("Mc2Web autosave disabled!");
				break;
			}
		}
	}

}
