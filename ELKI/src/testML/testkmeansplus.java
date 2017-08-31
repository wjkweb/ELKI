package testML;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.stat.clustering.Cluster;
import org.apache.commons.math.stat.clustering.EuclideanIntegerPoint;
import org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math.util.*;

public class testkmeansplus {

	public double[][] getdataSetformCSV() throws IOException {
//		String line;
//		String name;
//		String age;
//		String birthday;
//		BufferedReader br = new BufferedReader(new InputStreamReader(
//				new FileInputStream("test.csv")));
//
//		while ((line = br.readLine()) != null) {
//			// System.out.println(line);
//			String[] info = line.split(",");
//			name = info[0].trim();
//			age = info[1].trim();
//			birthday = info[2].trim();
//			System.out.print(name + "\t" + age + "\t" + birthday + "\n");
//		}
		return null;
	}

	private static void testKMeansPP() {

		// ori is sample as n instances with m features, here n=8,m=2

		int ori[][] = { { 2, 5 }, { 6, 4 }, { 5, 3 }, { 2, 2 }, { 1, 4 },
				{ 5, 2 }, { 3, 3 }, { 2, 3 } };

		int n = 8;

		Collection<EuclideanIntegerPoint> col = new ArrayList<EuclideanIntegerPoint>();
		for (int i = 0; i < n; i++) {

			EuclideanIntegerPoint ec = new EuclideanIntegerPoint(ori[i]);

			col.add(ec);

		}

		KMeansPlusPlusClusterer<EuclideanIntegerPoint> km = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
				new Random(n));

		List<Cluster<EuclideanIntegerPoint>> list = new ArrayList<Cluster<EuclideanIntegerPoint>>();

		list = km.cluster(col, 2, 10);

		output(list);

	}

	private static void output(List<Cluster<EuclideanIntegerPoint>> list) {

		int ind = 1;

		Iterator<Cluster<EuclideanIntegerPoint>> it = list.iterator();

		while (it.hasNext()) {

			Cluster<EuclideanIntegerPoint> cl = it.next();

			System.out.print("Cluster" + (ind++) + " :");

			List<EuclideanIntegerPoint> li = cl.getPoints();

			Iterator<EuclideanIntegerPoint> ii = li.iterator();

			while (ii.hasNext()) {

				EuclideanIntegerPoint eip = ii.next();

				System.out.print(eip + " ");

			}

			System.out.println();

		}

	}

	public static void main(String[] args) {

		// testHierachicalCluster();

		testKMeansPP();

		// testBSAS();

		// testMBSAS();
	}
}
