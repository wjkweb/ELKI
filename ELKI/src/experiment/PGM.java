/**
 * @PGM.java
 * @Version 1.0, 2010.02.26
 * @Author Xie-Hua Sun
 */

package experiment;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.util.Scanner;

public class PGM {

	public static void readPGMHeader(String picturepath) throws IOException {
		try {
			FileInputStream f = new FileInputStream(picturepath);
			// InputStream f =
			// ClassLoader.getSystemClassLoader().getResourceAsStream(picturepath);
			BufferedReader d = new BufferedReader(new InputStreamReader(f));
			String magic = d.readLine(); // first line contains P2 or P5
			String line = d.readLine(); // second line contains height and width
			while (line.startsWith("#")) {
				line = d.readLine();
			}
			Scanner s = new Scanner(line);
			int width = s.nextInt();
			int height = s.nextInt();
			line = d.readLine();// third line contains maxVal
			s = new Scanner(line);
			int maxVal = s.nextInt();
			byte[][] im = new byte[height][width];
			int count = 0;
			int b = 0;
			try {
				while (count < height * width) {
					b = d.read();
					if (b < 0)
						break;
					if (b == '\n') { // do nothing if new line encountered
					} else {
						if ("P5".equals(magic)) { // Binary format
							im[count / width][count % width] = (byte) ((b >> 8) & 0xFF);
							count++;
							im[count / width][count % width] = (byte) (b & 0xFF);
							count++;
						} else { // ASCII format
							im[count / width][count % width] = (byte) b;
							count++;
						}
					}
				}
			} catch (EOFException eof) {
				eof.printStackTrace(System.out);
			}
			System.out.println("Height=" + height);
			System.out.println("Width=" + width);
			System.out.println("Required elements=" + (height * width));
			System.out.println("Obtained elements=" + count);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					System.out.print(im[i][j] + " ");
				}
				System.out.println();
			}
		} catch (Throwable t) {
			t.printStackTrace(System.err);
			return;
		}
	}

	public static void main(String[] args) throws IOException {
		String picturepath = "C:\\Users\\Administrator\\Desktop\\clusterdataset\\faces\\s1\\1.pgm";
		readPGMHeader(picturepath);
	}
}
