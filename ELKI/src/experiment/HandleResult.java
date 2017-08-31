package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import experiment.AddClassLabel;

public class HandleResult {
	public static void calScore(String filePath) {
		double[] scores = new double[20];
		int count = 0;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						String[] splitS = lineTxt.split(":");
						scores[count % 20] += Double.parseDouble(splitS[1]);
						// System.out.println(scores[count]);
						count++;
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		for (int i = 0; i < scores.length; i++) {
			System.out.println(scores[i] / 380);
		}

	}

	public static void getDetailByIndex(String filePath, String desfilepath) throws Exception {
		String[] scores = new String[20];
		String content = "";
		for (int k = 0; k < scores.length; k++) {
			scores[k] = "";
		}
		int count = 0;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			// String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						String[] splitS = lineTxt.split(":");
						// for(int k =0 ; k< splitS.length ; k++)
						// {
						// if(splitS[1].trim().equals(index))
						// System.out.println(Double.parseDouble(splitS[1]));
						// }
						scores[count % 20] += "," + Double.parseDouble(splitS[1]);
						// System.out.println(scores[count]);
						count++;
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		for (int l = 0; l < 20; l++) {
			content += scores[l];
			content += "\r\n";
			// System.out.println(scores[0]);
		}
		writeTxtFile(content, new File(desfilepath));
	}

	public static void getAIndexScore(String filePath) {
		
		
		HashMap indexScore = new HashMap<Integer, Vector>();
		for (int i = 0; i < 20; i++) {
			indexScore.put(i, new Vector());
		}
		int count = 0;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						System.out.println(lineTxt);
						String[] splitS = lineTxt.split(":");
						Integer indexC = new Integer(count % 20);
						((Vector) indexScore.get(indexC)).add(splitS[1]);
						count++;
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		Iterator<Integer> it = (Iterator<Integer>) indexScore.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			Vector values = (Vector) indexScore.get(key);
			for (int j = 0; j < values.size(); j++) {
				System.out.print(Double.valueOf(values.get(j).toString()) + ",");
			}
			System.out.println();
		}

	}

	public static void getAVGScore(String filePath) {
		HashMap indexScore = new HashMap<Integer, Vector>();
		for (int i = 0; i < 19; i++) {
			indexScore.put(i, new Vector());
		}
		int count = 0;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						System.out.println(lineTxt);
						String[] splitS = lineTxt.split(":");
						Integer indexC = new Integer(count % 19);
						((Vector) indexScore.get(indexC)).add(splitS[1]);
						count++;
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		Iterator<Integer> it = (Iterator<Integer>) indexScore.keySet().iterator();
		while (it.hasNext()) {
			Integer key = it.next();
			Vector values = (Vector) indexScore.get(key);
			for (int j = 0; j < values.size(); j++) {
				System.out.print(values.get(j) + ",");
			}
			System.out.println();
		}

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

	public static void get3DValue(String filePath, String desfilepath) {
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			int countLine = 1;
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						String content = "";
						int count = 0;
						String[] splitS = lineTxt.split(",");
						for (int k = 0; k < splitS.length; k++) {
							content += (count / 99 * 0.015 + 0.015) + " " + (count % 99 + 2) + " " + splitS[k] + "\r\n";
							count++;
						}
						writeTxtFile(content, new File(desfilepath + "3d" + countLine + ".txt"));
						countLine++;
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		// for (int i = 0; i < scores.length; i++) {
		//
		// }

	}

	public static double[] getMaxMinAVG(String filePath) {
		double[] scores = new double[19];
		int count = 0;
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		// int countAll = 0;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						String[] splitS = lineTxt.split(":");
						// System.out.println(splitS[1]);
						scores[count % 19] += Double.parseDouble(splitS[1]);
						// System.out.println(scores[count % 20]);
						count++;
					}
				}
				int times = count / 19;
				for (int i = 0; i < scores.length; i++) {
					scores[i] = scores[i] / times;
					System.out.println(nf.format(scores[i]));

				}
				System.out.println("############################################");
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return scores;
	}

	public static void getVariance(String filePath, double[] meanValue) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);
		double[] scores = new double[19];
		int count = 0;
		double countVariance = 0.0;
		// int countAll = 0;
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			String content = "";
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						String[] splitS = lineTxt.split(":");
						// System.out.println(splitS[1]);
						scores[count % 19] += (Double.parseDouble(splitS[1]) - meanValue[count % 19])
								* (Double.parseDouble(splitS[1]) - meanValue[count % 19]);
						// System.out.println(scores[count % 20]);
						count++;
					}
				}

				int times = count / 19;
				for (int i = 0; i < scores.length; i++) {
					System.out.println(nf.format(Math.sqrt(scores[i] / times)));
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}

	}

	public static void getReal3DValue(String filePath, String desfilepath) {
		try {
			String encoding = "GBK";
			File file = new File(filePath);
			int countLine = 1;
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					if (!lineTxt.equals("")) {
						String content = "";
						int count = 0;
						String[] splitS = lineTxt.split(",");
						for (int k = 0; k < splitS.length; k++) {
							content += ((count / 19) * 0.1 + 0.01) + " " + (count % 19 + 2) + " " + splitS[k] + "\r\n";
							count++;
						}
						System.out.println(content);
						writeTxtFile(content, new File(desfilepath + "ecoli3d" + countLine + ".txt"));
						countLine++;
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		// for (int i = 0; i < scores.length; i++) {
		//
		// }

	}

	public static void main(String[] args) throws Exception {
		String filepath = "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验结果收集\\模拟数据实验结果\\DBSCAN.txt";
		 getVariance(filepath, getMaxMinAVG(filepath));
		// getDetailByIndex(
		// filepath,
		// "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验结果收集\\实验采用数据\\真实数据集上验证\\XMeans\\testResult.txt");
		//getAVGScore(filepath);
		// //
		// getReal3DValue(
		// "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验结果收集\\实验采用数据\\真实数据集上验证\\XMeans\\testResult.txt",
		// "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验结果收集\\实验采用数据\\真实数据集上验证\\");
	}
}
