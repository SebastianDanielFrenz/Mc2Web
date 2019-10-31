package io.github.SebastianDanielFrenz.Mc2Web.lang;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.SebastianDanielFrenz.Mc2Web.Mc2Web;

public class Lang {

	public static Map<String, Object> langs = new HashMap<String, Object>();

	private HashMap<String, String> map;

	public static void initLangsMap() {
		String _langs = "af_za ar_sa ast_es az_az ba_ru be_by bg_bg br_fr brb bs_BA ca_es cs_cz cy_gb da_dk de_at de_ch de_de el_gr en_au en_ca en_gb en_nz en_pt en_ud en_us enp en_ws eo_uy es_ar es_CL es_es es_mx es_uy es_ve et_ee eu_es fa_ir fi_fi fil_ph fo_fo fr_ca fr_fr vmf_de fy_nl ga_le gd_gb gl_es got gv_im haw he_il hi_in hr_hr hu_hu hy_am id_id ig_ng io_en is_is it_it ja_jp jbo kab_dz kn_in ko_kr ksh_de kw_gb la_va lb_lu li_li lol_aa lt_lt lv_lv mi_nz mk_mk mn_mn moh_us ms_my mt_mt nds_de nl_be nl_nl nn_no no_no nb_no nuk oc_fr oj_ca ovd_se pl_pl pt_br pt_pt qya_aa ro_ro ru_ru sme sk_sk sl_si so_so sr_sp sv_se swg sxu szl ta_IN th_th tlh_aa tr_tr tzl_tzl uk_ua ca-val_es vec_it vi_vn yi_de yo_ng zh_cn zh_tw";
		for (String lang : _langs.split(" ")) {
			langs.put(lang, "en_us");
		}
	}

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

	public Lang(String lang) throws IOException {
		this(Paths.get(Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG_PATH) + lang + ".lang"));
	}

	public Lang(Path lang) throws IOException {
		map = new HashMap<String, String>();
		List<String> lines;

		while (true) {
			lines = Files.readAllLines(lang, StandardCharsets.ISO_8859_1);

			if (lines.get(0).startsWith("redirect=")) {
				lang = Paths.get(
						Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG_PATH) + lines.get(0).substring(9) + ".lang");
			} else {
				break;
			}
		}

		for (int i = 0; i < lines.size(); i++) {
			String[] split = lines.get(i).split("[=]");
			map.put(split[0], split[1]);
		}
	}

	public static void registerLangs() throws IOException {
		for (File file : new File(Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG_PATH)).listFiles()) {
			langs.put(file.getName().replace(".lang", ""), new Lang(file.toPath()));
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
			return "en_us";
		}
		Field f;
		try {
			f = ep.getClass().getDeclaredField("locale");
		} catch (NoSuchFieldException | SecurityException e1) {
			e1.printStackTrace();
			return "en_us";
		}
		f.setAccessible(true);
		String language;
		try {
			language = (String) f.get(ep);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return "en_us";
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

	public static String getLanguage(CommandSender sender) {
		return sender instanceof Player ? getLanguage((Player) sender)
				: Mc2Web.plugin.getConfig().getString(Mc2Web.cLANG);
	}

	public static String get(Player player, String name) {
		return get(getLanguage(player), name);
	}

	public static String get(CommandSender sender, String name) {
		return get(getLanguage(sender), name);
	}

	public static String get(String lang, String name, Object[] params) {
		return langs.get(lang) instanceof Lang ? ((Lang) langs.get(lang)).get(name, params)
				: get((String) langs.get(lang), name, params);
	}

	public static String get(Player player, String name, Object[] params) {
		return get(getLanguage(player), name, params);
	}

	public static String get(CommandSender sender, String name, Object[] params) {
		return get(getLanguage(sender), name, params);
	}
}
