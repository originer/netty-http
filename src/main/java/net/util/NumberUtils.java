package net.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class NumberUtils {

    public static Integer parse(String str, Integer defaultVal) {
        if (null != str && str.trim().length() != 0) {
            int val = 0;
            char[] chars = str.toCharArray();
            int len = chars.length;
            for (int i = 0; i < len; ++i) {
                char c = chars[i];
                if (c < '0' || c > '9') {
                    return defaultVal;
                }
                val = val * 10 + (c - 48);
            }
            return val;
        } else {
            return defaultVal;
        }
    }

    public static Integer parse(String str) {
        return parse(str, (Integer) null);
    }

    public static Double parseDouble(String str) {
        try {
            return Double.valueOf(str);
        } catch (Exception var2) {
            return null;
        }
    }

    public static <T> T nullDefault(T num, T defaultVal) {
        return null == num ? defaultVal : num;
    }

    public static int toInt(Object obj) {
        if (obj == null) {
            return 0;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        } else {
            try {
                return Integer.parseInt(obj.toString());
            } catch (Exception var2) {
                return 0;
            }
        }
    }

    public static float toFloat(Object obj) {
        String str = obj + "";

        try {
            return Float.parseFloat(str);
        } catch (Exception var3) {
            return 0.0F;
        }
    }

    public static BigDecimal toBigDecimal(String val) {
        if (StringUtils.isEmpty(val)) {
            return null;
        }
        BigDecimal convertVal = null;
        try {
            convertVal = new BigDecimal(val);
        } catch (Exception ex) {
        }
        return convertVal;
    }

    public static double retainDecimal(double num, int position) {
        if (0 == position) {
            num = (double) ((int) num);
        } else if (0 < position) {
            double dec = Math.pow(10.0D, (double) position);
            double nex = dec * 10.0D;
            BigDecimal target = new BigDecimal((double) ((int) (num * dec)) * 1.0D / dec);
            if ((int) (num * nex) % 10 > 4) {
                target.add((new BigDecimal(1)).divide(new BigDecimal(nex)));
            }

            num = target.doubleValue();
        }

        return num;
    }

    public static BigDecimal retainDecimal2(double num, int position) {
        BigDecimal target = null;
        if (0 == position) {
            new BigDecimal((long) num);
        } else if (0 < position) {
            double dec = Math.pow(10.0D, (double) position);
            double nex = dec * 10.0D;
            target = new BigDecimal((double) ((int) (num * dec)) * 1.0D / dec);
            if ((int) (num * nex) % 10 > 4) {
                target.add((new BigDecimal(1)).divide(new BigDecimal(nex)));
            }
        }

        return target;
    }

    public static long toInt64(String source, long defaultValue) {
        if (source != null && !source.isEmpty()) {
            try {
                return Long.parseLong(source);
            } catch (Exception ex) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

}
