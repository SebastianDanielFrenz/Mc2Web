package io.github.SebastianDanielFrenz.Mc2Web.cookie;

import java.util.Random;

import com.youtube.crash_games_cr_mc.simpleDB.query.QueryResult;
import com.youtube.crash_games_cr_mc.simpleDB.query.SearchedValue;
import com.youtube.crash_games_cr_mc.simpleDB.varTypes.DBString;

import io.github.SebastianDanielFrenz.Mc2Web.Mc2Web;

public class CookieStorage {

	public static final String[] M_GETCOOKIE_COLUMNS = new String[] { "user" };
	public static final String[] M_GENERATECOOKIEID_COLUMNS = new String[] {};

	public static Cookie getCookie(String ID) {
		QueryResult result = Mc2Web.query.Run("accounts", "users", M_GETCOOKIE_COLUMNS,
				new SearchedValue[] { new SearchedValue("ID", new DBString(ID)) });
		return result.rows.size() >= 1 ? new Cookie(((DBString) result.rows.get(0).get(0)).getValue()) : null;
	}

	public static void addCookie(String ID, Cookie cookie) {
		Mc2Web.query.Insert("accounts", "table", new SearchedValue[] { new SearchedValue("ID", new DBString(ID)),
				new SearchedValue("user", new DBString(cookie.user)) });
	}

	public static void updateCookieID(String oldID, String ID) {
		Mc2Web.query.Update("accounts", "users", new SearchedValue[] { new SearchedValue("ID", new DBString(oldID)) },
				new SearchedValue[] { new SearchedValue("ID", new DBString(ID)) });
	}

	public static void updateCookie(String ID, Cookie cookie) {
		Mc2Web.query.Update("accounts", "users", new SearchedValue[] { new SearchedValue("ID", new DBString(ID)) },
				new SearchedValue[] { new SearchedValue("user", new DBString(cookie.user)),
						new SearchedValue("last_url", new DBString(cookie.last_url)) });
	}

	public static void deleteCookie(String ID) {
		Mc2Web.query.Delete("accounts", "users", new SearchedValue[] { new SearchedValue("ID", new DBString(ID)) });
	}

	public static String generateCookieID() {
		QueryResult result;
		Random random = new Random();
		String ID;
		byte[] bytes;
		while (true) {
			bytes = new byte[128];
			random.nextBytes(bytes);
			ID = new String(bytes);
			result = Mc2Web.query.Run("accounts", "users", M_GENERATECOOKIEID_COLUMNS,
					new SearchedValue[] { new SearchedValue("ID", new DBString(ID)) });
			if (result.rows.size() == 0) {
				break;
			}
		}
		return ID;
	}

}
