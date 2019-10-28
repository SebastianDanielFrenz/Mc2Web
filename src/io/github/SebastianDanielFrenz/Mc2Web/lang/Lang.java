package io.github.SebastianDanielFrenz.Mc2Web.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public abstract class Lang {

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
		List<String> lines = Files.readAllLines(Paths.get("plugins/" + plugin + "/" + lang + ".lang"));

		for (int i = 0; i < lines.size(); i++) {
			String[] split = lines.get(i).split("[=]");
			map.put(split[0], split[1]);
		}
	}

}
