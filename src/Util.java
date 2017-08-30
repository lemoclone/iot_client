public class Util {
    /**
     * byte数组转换转16进制字符串
     *
     * @param array
     * @return hex string. if array is null, return null.
     */
    public static String byteArrayToString(byte[] array) {
        if (array == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            int val = array[i] & 0xff;
            sb.append(hexArray[val >>> 4]);
            sb.append(hexArray[val & 0x0f]);
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * 打印日志到控制台
     *
     * @param info log information
     */
    public static void log(Object info) {
        System.out.println(info);
    }
}
