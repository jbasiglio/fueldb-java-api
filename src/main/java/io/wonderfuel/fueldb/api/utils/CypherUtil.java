package io.wonderfuel.fueldb.api.utils;

import java.security.Key;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class CypherUtil {
	
	private static final char[] DIGITS_LOWER =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	private static Mac mac;
	
	static {
		try {
			mac = Mac.getInstance("hmacSHA256");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String hash(String data, String keyString) {
		try {
			byte[] keyByte = keyString.getBytes("UTF-8");
			Key key = new SecretKeySpec(keyByte, "HmacSHA1");
			mac.init(key);
			return encodeHex(mac.doFinal(data.getBytes("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String encodeHex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return new String(out);
    }

}
