package net.util;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtils {

    private static final int BUFFER = 1024;

    public GzipUtils() {
    }


    public static byte[] compress(byte[] data) {
        ByteArrayInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        byte[] output = null;

        try {
            inputStream = new ByteArrayInputStream(data);
            outputStream = new ByteArrayOutputStream();
            compress(inputStream, outputStream);
            output = outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    ;
                }
            }

            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    ;
                }
            }

            if (null != outputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    ;
                }
            }
        }

        return output;
    }

    /**
     * gzip compress
     *
     * @param inputStream
     * @param outputStream
     * @throws Exception
     */
    public static void compress(InputStream inputStream, OutputStream outputStream) throws Exception {

        GZIPOutputStream gzipOutputStream = null;

        try {
            gzipOutputStream = new GZIPOutputStream(outputStream);
            byte[] data = new byte[1024];

            int count;
            while ((count = inputStream.read(data, 0, 1024)) != -1) {
                gzipOutputStream.write(data, 0, count);
            }
            gzipOutputStream.finish();
        } catch (Exception e) {
            throw e;
        } finally {
            if (null != gzipOutputStream) {
                gzipOutputStream.flush();
            }

            if (null != gzipOutputStream) {
                gzipOutputStream.close();
            }
        }
    }

    /**
     * decompress
     *
     * @param data
     * @return
     */
    public static byte[] decompress(byte[] data) {
        ByteArrayInputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        try {
            inputStream = new ByteArrayInputStream(data);
            outputStream = new ByteArrayOutputStream();
            decompress(inputStream, outputStream);
            data = outputStream.toByteArray();
        } catch (Exception e) {
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.flush();
                } catch (IOException e) {
                }
            }

            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }

            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }

        return data;
    }

    /**
     * gzip decompress
     *
     * @param inputStream
     * @param outputStream
     */
    public static void decompress(InputStream inputStream, OutputStream outputStream) {
        GZIPInputStream gzipInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(inputStream);
            byte[] data = new byte[1024];

            int count;
            while ((count = gzipInputStream.read(data, 0, 1024)) != -1) {
                outputStream.write(data, 0, count);
            }
        } catch (Exception e) {
        } finally {
            if (null != gzipInputStream) {
                try {
                    gzipInputStream.close();
                } catch (Exception e) {
                    ;
                }
            }
        }
    }
}
