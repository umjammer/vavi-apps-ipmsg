/*
 * 1998/02/04 (C)Copyright T.Kazawa(Digitune)
 */

package vavi.apps.ipm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Message Digester Class.
 *
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 050802 nsano initial version <br>
 */
final class MessageDigester {

    /** */
    private static MessageDigest md;

    /** */
    private MessageDigester() {
    }

    /* */
    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** @throws IllegalStateException */
    public static String getMD5(String src) {
        byte[] buf = md.digest(src.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            sb.append(Integer.toString(new Byte(b).intValue(), Character.MAX_RADIX));
        }

        return sb.toString();
    }
}

/* */
