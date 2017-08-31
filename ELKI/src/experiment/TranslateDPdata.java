package experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import de.lmu.ifi.dbs.elki.algorithm.clustering.ipeaks.Sample;

public class TranslateDPdata {
	private ArrayList<Sample> samples;
	/** 最大样本距离 */
	private double maxDistance;
	/** 最小样本距离 */
	private double minDistance;
	/** 样本对距离：<"index1 index2", distance> */
	private HashMap<String, Double> pairDistanceMap;

	StringBuffer sb = new StringBuffer();

	public boolean writeTxtFile(String content, String desFile) throws Exception {
		RandomAccessFile mm = null;
		boolean flag = false;
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(desFile);
			o.write(content.getBytes("GBK"));
			o.close();
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

	public void getNormalDPdata(String filepath, int length, String desFile) throws Exception {
		StringBuffer content = new StringBuffer();
		double[] maxvalues = new double[length];
		try {
			String encoding = "GBK";
			File file = new File(filepath);

			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				InputStreamReader read1 = new InputStreamReader(new FileInputStream(file), encoding);//
				BufferedReader bufferedReader = new BufferedReader(read1);
				BufferedReader bufferedReader1 = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					String[] aline = lineTxt.split(",");
					for (int i = 0; i < aline.length; i++) {
						double value = Double.parseDouble(aline[i]);
						if (value > maxvalues[i]) {
							maxvalues[i] = value;
						}
					}
				}
				while ((lineTxt = bufferedReader1.readLine()) != null) {
					String[] aline = lineTxt.split(",");
					for (int i = 0; i < aline.length; i++) {
						double value = Double.parseDouble(aline[i]);
						content.append(value / maxvalues[i] + ",");
					}
					content.append("\r\n");
				}
				System.out.println(content);
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (

		Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		writeTxtFile(new String(content), desFile);
	}

	public void getpairWiseDistance(String filepath, String desFile) throws Exception {
		samples = new ArrayList<>();
		String content = "";
		String encoding = "GBK";
		File file = new File(filepath);
		double[][] values = new double[788][2];
		if (file.isFile() && file.exists()) { // 判断文件是否存在
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
			InputStreamReader read1 = new InputStreamReader(new FileInputStream(file), encoding);//
			BufferedReader bufferedReader = new BufferedReader(read1);
			BufferedReader bufferedReader1 = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				String[] valuess = lineTxt.split(",");
				double[] attributes = new double[valuess.length];
				for (int j = 0; j < valuess.length; j++) {
					attributes[j] = Double.parseDouble(valuess[j]);
				}
				Sample sample = new Sample(attributes, "");
				samples.add(sample);
			}
		}
		calPairDistance();
		Iterator<String> its = pairDistanceMap.keySet().iterator();
		while (its.hasNext()) {
			String key = its.next();
			sb.append(key+"," + pairDistanceMap.get(key) + "\r\n");
		}
		content= new String(sb);
		writeTxtFile(content, desFile);
	}

	/**
	 * 计算所有样本每两个样本点的距离
	 */
	public void calPairDistance() {
		pairDistanceMap = new HashMap<String, Double>();
		maxDistance = Double.MIN_VALUE;
		minDistance = Double.MAX_VALUE;
		for (int i = 0; i < samples.size() - 1; i++) {
			for (int j = i + 1; j < samples.size(); j++) {
				double dis = twoSampleDistance(samples.get(i), samples.get(j));
				pairDistanceMap.put((i+1) + "," + (j+1), dis);
				if (dis > maxDistance)
					maxDistance = dis;
				if (dis < minDistance)
					minDistance = dis;
			}
		}
	}

	private double twoSampleDistance(Sample a, Sample b) {
		double[] aData = a.getAttributes();
		double[] bData = b.getAttributes();
		double distance = 0.0;
		for (int i = 0; i < aData.length; i++) {
			distance += Math.pow(aData[i] - bData[i], 2);
		}
		return 1 - Math.exp(distance * (-0.5));
	}

	public static void main(String[] args) throws Exception {
		TranslateDPdata tsd = new TranslateDPdata();
		String filepath = "C:\\Users\\Administrator\\Desktop\\clusterdataset\\simulate\\data.csv";
		String despath = "C:\\Users\\Administrator\\Desktop\\clusterdataset\\simulate\\datas.csv";
		String distanceFile = "C:\\Users\\Administrator\\Desktop\\clusterdataset\\simulate\\distancedata.csv";
		tsd.getNormalDPdata(filepath, 2, despath);
		tsd.getpairWiseDistance(despath, distanceFile);
	}

}
