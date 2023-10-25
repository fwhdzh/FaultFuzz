package edu.iscas.tcse.favtrigger.triggering;

import edu.iscas.tcse.favtrigger.tracing.FAVPathType;

/**
 * @author Wenhan Feng
 * Temporarily store some old codes for path similarity.
 * In fact, they are not used anywhere.
 * I store them for referring them to write new codes for Path.
 */
public class OldPathSimilarUtil {

    // -1: not similar
    // 0: total same
    // 1: similar path
    // for substring, tolerate at most 1 similar difference
    private static int similarStringWithSign(String str1, String str2, String sign) {
        int diff = 0;
        if (!str1.contains(sign) || !str2.contains(sign)) {
            return -1;
        }
        String[] sec1 = str1.split(sign);
        String[] sec2 = str2.split(sign);

        if (sec1.length != sec2.length) {
            return -1;
        }
        for (int i = 0; i < sec1.length; i++) {
            if (!sec1[i].equals(sec2[i])) {
                if (isHexNumberRex(sec1[i]) && isHexNumberRex(sec2[i])) {
                    diff++;
                    if (diff > 1) {
                        return -1;
                    } else {
                        continue;
                    }
                } else {
                    return -1;
                }
            }
        }
        if (diff == 1) {
            return 1;
        } else {
            return 0;
        }
    }

    static int similarStringWithSigns(String str1, String str2) {
        String[] signs = new String[] { "-", "_", ",", "." };
        for (String sign : signs) {
            int similar = similarStringWithSign(str1, str2, sign);
            if (similar != -1) {
                return similar;
            }
        }
        return -1;
    }

    static boolean isHexNumberRex(String str) {
        String validate = "(?i)[0-9a-f]+";
        return str.matches(validate);
    }

    // can tolerate at most 2 similar differences
    public static boolean likelySamePath(String str1, String str2) {
        if (str1.startsWith(FAVPathType.FAVMSG.toString()) && str1.lastIndexOf("&") != -1) {
            str1 = str1.substring(0, str1.lastIndexOf("&"));
        }
        if (str2.startsWith(FAVPathType.FAVMSG.toString()) && str2.lastIndexOf("&") != -1) {
            str2 = str2.substring(0, str2.lastIndexOf("&"));
        }
        String[] secs1 = str1.split("/");
        // System.out.println(secs1.length+" "+secs1[0]);
        String[] secs2 = str2.split("/");
        // System.out.println(secs2.length+" "+secs2[0]);
        int diff = 0;
        if (secs1.length != secs2.length) {
            return false;
        }
        for (int i = 0; i < secs1.length; i++) {
            if (!secs1[i].equals(secs2[i])) {
                if (isHexNumberRex(secs1[i]) && isHexNumberRex(secs2[i])) {
                    diff++;
                    if (diff > 2) {
                        return false;
                    }
                } else {
                    int similar = similarStringWithSigns(secs1[i], secs2[i]);
                    if (similar == 1) {
                        diff++;
                        if (diff > 2) {
                            return false;
                        }
                    } else if (similar == -1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
