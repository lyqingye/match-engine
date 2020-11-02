package com.trader.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * GZIP 工具类
 *
 * @author ex
 */
public class GZIPUtils {

    /**
     * 解压数据
     *
     * @param data
     *         需要解压的数据
     *
     * @return 解压后的数据
     *
     * @throws IOException
     *         如果解压失败
     */
    public static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream byteIps = new ByteArrayInputStream(data);
        ByteArrayOutputStream byteOps = new ByteArrayOutputStream();
        decompress(byteIps, byteOps);
        byteOps.flush();
        byteOps.close();
        byteIps.close();
        return byteOps.toByteArray();
    }

    /**
     * 解压数据
     *
     * @param is
     *         输入流
     * @param os
     *         输出流
     *
     * @throws IOException
     *         如果解压失败
     */
    public static void decompress(InputStream is, OutputStream os) throws IOException {
        GZIPInputStream gis = new GZIPInputStream(is);
        int count;
        byte[] data = new byte[gis.available()];
        while ((count = gis.read(data, 0, gis.available())) != -1) {
            os.write(data, 0, count);
        }
        gis.close();
    }

    /**
     * 压缩数据
     *
     * @param data
     *         需要压缩的数据
     *
     * @return 压缩后的数据
     *
     * @throws IOException
     *         如果压缩失败
     */
    public static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gos = new GZIPOutputStream(bos);
        gos.write(data);
        gos.finish();
        return bos.toByteArray();
    }
}
