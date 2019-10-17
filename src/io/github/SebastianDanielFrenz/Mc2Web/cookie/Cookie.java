package io.github.SebastianDanielFrenz.Mc2Web.cookie;

public class Cookie {

	public Cookie(String user) {
		this.user = user;
	}

	public Cookie(String user, String lastURL) {
		this.user = user;
		this.last_url = lastURL;
	}

	public String user;
	public String last_url;

}
