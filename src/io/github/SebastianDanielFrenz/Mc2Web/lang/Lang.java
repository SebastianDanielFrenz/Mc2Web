package io.github.SebastianDanielFrenz.Mc2Web.lang;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import io.github.SebastianDanielFrenz.Mc2Web.Mc2Web;

public class Lang {

	public static Map<String, Object> langs = new HashMap<String, Object>();

	private HashMap<String, String> map;

	public HashMap<String, String> getMap() {
		return map;
	}

	public void setMap(HashMap<String, String> map) {
		this.map = map;
	}

	public String get(String name) {
		return map.getOrDefault(name, "INVALID LANG");
	}

	public String get(String name, Object[] params) {
		String out = map.get(name);
		if (out == null) {
			out = "INVALID LANG (Args: ";
			for (int i = 0; i < params.length; i++) {
				out += params[i].toString() + ", ";
			}
			out += ")";
			return out;
		} else {
			for (int i = 0; i < params.length; i++) {
				out = out.replace("%param" + i + "%", params[i].toString());
			}
			return out;
		}
	}

	public Lang(String lang, String plugin) throws IOException {
		map = new HashMap<String, String>();
		List<String> lines = Files.readAllLines(
				Paths.get(Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG_PATH) + lang + ".lang"),
				StandardCharsets.ISO_8859_1);

		for (int i = 0; i < lines.size(); i++) {
			String[] split = lines.get(i).split("[=]");
			map.put(split[0], split[1]);
		}
	}

	/**
	 * This is not original code.<br>
	 * <a href=
	 * "https://bukkit.org/threads/get-a-players-minecraft-language.172468/">Credit
	 * to this post on the bukkit forum.</a>
	 * 
	 * @param p
	 * @return
	 */
	public static String getLanguage(Player p) {
		Object ep;
		try {
			ep = getMethod("getHandle", p.getClass()).invoke(p, (Object[]) null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return "en_US";
		}
		Field f;
		try {
			f = ep.getClass().getDeclaredField("locale");
		} catch (NoSuchFieldException | SecurityException e1) {
			e1.printStackTrace();
			return "en_US";
		}
		f.setAccessible(true);
		String language;
		try {
			language = (String) f.get(ep);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return "en_US";
		}
		return language;
	}

	private static Method getMethod(String name, Class<?> clazz) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.getName().equals(name))
				return m;
		}
		return null;
	}

	public static String get(String lang, String name) {
		Object obj = langs.get(lang);
		if (obj instanceof Lang) {
			return ((Lang) obj).get(name);
		} else if (obj instanceof String) {
			return get((String) obj, name);
		} else {
			return "INVALID LANG";
		}
	}
}
