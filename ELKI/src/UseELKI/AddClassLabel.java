package UseELKI;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;

public class AddClassLabel {

	public static void readTxtFile(String filePath, String desfilepath) {
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					String[] splitS = lineTxt.split(",");
					String label = "c" + splitS[splitS.length - 1];
					splitS[splitS.length - 1] = label;
					for (int i = 0; i < splitS.length; i++) {
						content += splitS[i] + ",";
					}
					content += "\r\n";
					//System.out.println(content);
				}
				read.close();
				writeTxtFile(content, new File(desfilepath));
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}

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

	public static void main(String argv[]) {
//		for (int i = 1; i < 380; i++) {
//			String files = "E:\\workspace\\data\\dataset\\data" + i + ".csv";
//			String filed = "E:\\workspace\\data\\datasetlabel\\data" + i
//					+ ".csv";
//			readTxtFile(files, filed);
//		}
		String filePath = "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验数据\\realdata\\glass.csv";
		// "res/";
		readTxtFile(filePath, "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验数据\\realdata\\glasslabel.csv");
	}
}