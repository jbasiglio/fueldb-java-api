package io.wonderfuel.fueldb.api.utils;

import java.util.Date;

/**
 * @author Joris Basiglio
 *
 */
public class WebUtils {

	public static String computeURL(String point, String user, String password) {
		String toSign = "/" + point + "?timestamp=" + new Date().getTime()
				+ "&user=" + user;
		String key = CypherUtil.hash(password, user);
		String sign = CypherUtil.hash(toSign, key);
		return toSign + "&signature=" + sign;
	}
}
