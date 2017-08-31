package pictureClustering;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

public class HandleImage {
	public static int[][] getImageGRB(String path) {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		int[][] result = new int[height][width];
		for (int h = 0; h < height; h++) {
			for (int w = 0; w < width; w++) {
				// 使用getRGB(w,
				// h)获取该点的颜色值是ARGB，而在实际应用中使用的是RGB，所以需要将ARGB转化成RGB，即bufImg.getRGB(w,
				// h) & 0xFFFFFF。
				result[h][w] = bi.getRGB(w, h) & 0xFFFFFF;
			}
		}
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++)
				System.out.print(result[i][j] + " ");
			System.out.println();
		}
		return result;
	}

	public static boolean writeTxtFile(String content, File fileName) throws Exception {
		RandomAccessFile mm = null;
		boolean flag = false;
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(fileName);
			o.write(content.getBytes("GBK"));
			o.close();
			// mm=new RandomAccessFile(fileName,"rw");
			// mm.writeBytes(content);
			flag = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (mm != null) {
				mm.close();
			}
		}
		return flag;
	}

	public static void getImagePixel(String image) throws Exception {
		String content = "";
		int[] rgb = new int[3];
		File file = new File(image);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		System.out.println("width=" + width + ",height=" + height + ".");
		System.out.println("minx=" + minx + ",miniy=" + miny + ".");
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j); // 下面三行代码将一个数字转换为RGB数字
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				content += rgb[0] + " " + rgb[1] + " " + rgb[2] + "\r\n";
			}
			// content+="\r\n";
		}
		writeTxtFile(content, new File("d:\\picture\\data.txt"));
		System.out.println(content);
	}

	public static void main(String[] args) throws Exception {
		String path = "d:\\picture\\181018.jpg";
		getImagePixel(path);
	}
}
