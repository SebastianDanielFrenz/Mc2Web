package io.github.SebastianDanielFrenz.Mc2Web.cookie;

import java.util.Random;

import io.github.SebastianDanielFrenz.SimpleDBMT.query.DataBaseQuery;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.DefaultDataBaseQuery;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.QueryResult;
import io.github.SebastianDanielFrenz.SimpleDBMT.query.SearchedValue;
import io.github.SebastianDanielFrenz.SimpleDBMT.varTypes.DBString;

import io.github.SebastianDanielFrenz.Mc2Web.Mc2Web;

public class CookieStorage {

	public static final int SUCCESS = 1;
	public static final int PASSWORD = 2;
	public static final int USER = 3;

	private static DataBaseQuery query = new DefaultDataBaseQuery(Mc2Web.dbh);

	public static void addCookie(String cookieID, Cookie cookie) {
		query.Insert("Mc2Web", "cookies", new SearchedValue[] { new SearchedValue("ID", new DBString(cookieID)),
				new SearchedValue("user", new DBString(cookie.user)) });
	}

	public static int loginUser(String user, String password) {
		QueryResult result = query.Run("Mc2Web", "users", new String[] { "password" },
				new SearchedValue[] { new SearchedValue("user", new DBString(user)) });
		if (result.rows.size() == 0) {
			return USER;
		}
		if (result.rows.get(0).get(0).Equals(new DBString(password))) {
			return SUCCESS;
		}
		return PASSWORD;
	}

	public static String loginUser(String cookieID) {
		QueryResult result = query.Run("Mc2Web", "cookies", new String[] { "user" },
				new SearchedValue[] { new SearchedValue("ID", new DBString(cookieID)) });
		return (result.rows.size() == 0) ? null : ((DBString) result.rows.get(0).get(0)).getValue();
	}

	public static void updateCookieUser(String cookieID, String user) {
		query.Update("Mc2Web", "cookies", new SearchedValue[] { new SearchedValue("ID", new DBString(cookieID)) },
				new SearchedValue[] { new SearchedValue("user", new DBString(user)) });
	}

	public static Cookie getCookie(String cookieID) {
		QueryResult result = query.Run("Mc2Web", "cookies", new String[] { "user" },
				new SearchedValue[] { new SearchedValue("ID", new DBString(cookieID)) });

		return (result.rows.size() == 0) ? null : new Cookie(((DBString) result.rows.get(0).get(0)).getValue());
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
			result = Mc2Web.query.Run("Mc2Web", "cookies", new String[] {},
					new SearchedValue[] { new SearchedValue("ID", new DBString(ID)) });
			if (result.rows.size() == 0) {
				break;
			}
		}
		return ID;
	}

}
