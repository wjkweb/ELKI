package pictureClustering;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

public class Test {
	public static void main(String[] args) {
		ImageCluster ic = new ImageCluster();
		ic.kmeans("d:\\picture\\12003.jpg",256,20);
	}
}