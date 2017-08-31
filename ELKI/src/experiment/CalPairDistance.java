package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

public class CalPairDistance {
	public static void readTxtFile(String filePath, String desfilepath) {
		StringBuffer content = new StringBuffer();
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				int countline = 0;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					String[] splitS = lineTxt.split(" ");
					//System.out.println(splitS.length);
					for (int i = countline + 1; i < splitS.length; i++) {
						double d = 1-Double.parseDouble(splitS[i]);
						if(d==0)
						{
							d=0.01;
						}
						content.append((countline + 1) + "," + (i + 1) + ","
								+ d + "\r\n");
					}
					countline++;
				}
				read.close();
				writeTxtFile(new String(content), new File(desfilepath));
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		System.out.println(content);
	}

	public static boolean writeTxtFile(String content, File fileName)
			throws Exception {
		RandomAccessFile mm = null;
		boolean flag = false;
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(fileName);
			o.write(content.getBytes("GBK"));
			o.close();
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mm != null) {
				mm.close();
			}
		}
		return flag;
	}

	public static void main(String[] args) {
		String filepath = "E:/workspace/data/datadistance.csv";
		String destpath = "E:/workspace/data/pairdistance.csv";
		readTxtFile(filepath, destpath);
	}

}
