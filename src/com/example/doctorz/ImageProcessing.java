package com.example.doctorz;

/**
 * This abstract class is used to process images.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public abstract class ImageProcessing {

    private static int decodeYCbCr420SPtoRedSum(byte[] ycbcr420sp, int width, int height) {
        if (ycbcr420sp == null) return 0;

        final int frameSize = width * height;

        int sum = 0;
        for (int j = 0, yp = 0; j < height; j++) {
        	//Binary Right Shift Operator. 
        	//The left operands value is moved right by the number of bits specified by the right operand.
            int uvp = frameSize + (j >> 1) * width, cb = 0, cr = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ycbcr420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    cr = (0xff & ycbcr420sp[uvp++]) - 128;
                    cb = (0xff & ycbcr420sp[uvp++]) - 128;
                }
               
                int r = (y * 1164 + 1569 * cr);
                int g = (y * 1164 - 813 * cr - 392 * cb);
                int b = (y * 1164 + 2017 * cb);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                int red = (pixel >> 16) & 0xff;
                sum += red;
            }
        }
        return sum;
    }

    /**
     * Given a byte array representing a yuv420sp image, determine the average
     * amount of red in the image. Note: returns 0 if the byte array is NULL.
     * 
     * @param yuv420sp
     *            Byte array representing a yuv420sp image
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     * @return int representing the average amount of red in the image.
     */
    public static int decodeYCbCr420SPtoRedAvg(byte[] ycbcr420sp, int width, int height) {
        if (ycbcr420sp == null) return 0;

        final int frameSize = width * height;

        int sum = decodeYCbCr420SPtoRedSum(ycbcr420sp, width, height);
        return (sum / frameSize);
    }
}
