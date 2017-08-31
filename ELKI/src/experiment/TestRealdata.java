package experiment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import de.lmu.ifi.dbs.elki.KDDTask;
import de.lmu.ifi.dbs.elki.algorithm.Algorithm;
import de.lmu.ifi.dbs.elki.application.AbstractApplication;
import de.lmu.ifi.dbs.elki.utilities.Alias;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.workflow.OutputStep;

@Alias({ "cli", "kddtask" })
public class TestRealdata extends AbstractApplication {
	/**
	 * The KDD Task to perform.
	 */
	KDDTask task;

	/**
	 * Constructor.
	 * 
	 * @param task
	 *            Task to run
	 */
	public TestRealdata(KDDTask task) {
		super();
		this.task = task;
	}

	@Override
	public void run() {
		task.run();
	}

	/**
	 * Parameterization class.
	 * 
	 * @author Erich Schubert
	 * 
	 * @apiviz.exclude
	 */
	public static class Parameterizer extends AbstractApplication.Parameterizer {
		/**
		 * The KDD Task to perform.
		 */
		protected KDDTask task;

		@Override
		protected void makeOptions(Parameterization config) {
			super.makeOptions(config);
			task = config.tryInstantiate(KDDTask.class);
		}

		@Override
		protected TestRealdata makeInstance() {
			return new TestRealdata(task);
		}
	}

	public static void testEM(String filepath, int mink, int maxk) {
		String[] para = new String[8];
		para[0] = "experiment.TestRealdata";
		para[1] = "-dbc.in";
		para[2] = "E:\\workspace\\data\\data.txt";
		para[3] = "-time";
		para[4] = "-algorithm";
		para[5] = "clustering.em.EM";
		para[6] = "-em.k";
		para[7] = "4";
		testClassicMethod(para);
	}

	public static void testAP(String filepath, int bandwidth) {
		String[] para = new String[8];
		para[0] = "experiment.TestRealdata";
		para[1] = "-dbc.in";
		para[2] = filepath;
		para[3] = "-time";
		para[4] = "-algorithm";
		para[5] = "clustering.affinitypropagation.AffinityPropagationClusteringAlgorithm";
		para[6] = "-meanshift.kernel-bandwidth";
		para[7] = String.valueOf(bandwidth * 0.01);
		testClassicMethod(para);
	}

	public static void testDBSCAN(String filepath, double epsilon, int minpts) {
		String[] para = new String[10];
		para[0] = "experiment.TestRealdata";
		para[1] = "-dbc.in";
		para[2] = filepath;
		para[3] = "-time";
		para[4] = "-algorithm";
		para[5] = "clustering.DBSCAN";
		para[6] = "-dbscan.epsilon";
		para[7] = String.valueOf(epsilon);
		para[8] = "-dbscan.minpts";
		para[9] = String.valueOf(minpts);
		testClassicMethod(para);
	}

	public static void testNMS(String filepath, int bandwidth) {
		String[] para = new String[8];
		para[0] = "experiment.TestRealdata";
		para[1] = "-dbc.in";
		para[2] = filepath;
		para[3] = "-time";
		para[4] = "-algorithm";
		para[5] = "clustering.NaiveMeanShiftClustering";
		para[6] = "-meanshift.kernel-bandwidth";
		para[7] = String.valueOf(bandwidth * 0.01);
		testClassicMethod(para);
	}

	public static void testxmeans(String filepath, int kmeansK, int xmeansk) {
		String[] xmeanpara = new String[12];
		xmeanpara[0] = "experiment.TestRealdata";
		xmeanpara[1] = "-dbc.in";
		xmeanpara[2] = filepath;
		xmeanpara[3] = "-time";
		xmeanpara[4] = "-algorithm";
		xmeanpara[5] = "clustering.kmeans.XMeans";
		xmeanpara[6] = "-kmeans.k";
		xmeanpara[7] = String.valueOf(kmeansK);
		xmeanpara[8] = "-xmeans.k_min";
		xmeanpara[9] = String.valueOf(xmeansk);
		xmeanpara[10] = "-xmeans.quality";
		xmeanpara[11] = "BayesianInformationCriterion";
		testClassicMethod(xmeanpara);
	}

	public static void testIpeaks(String filepath, int splittimes, int mink,
			int maxk) {
		String[] ipeakpara = new String[12];
		ipeakpara[0] = "experiment.TestRealdata";
		ipeakpara[1] = "-dbc.in";
		ipeakpara[2] = filepath;
		ipeakpara[3] = "-time";
		ipeakpara[4] = "-algorithm";
		ipeakpara[5] = "clustering.ipeaks.Ipeaks";
		ipeakpara[6] = "-splitTimes";
		ipeakpara[7] = Integer.toString(splittimes);
		ipeakpara[8] = "-mink";
		ipeakpara[9] = Integer.toString(mink);;
		ipeakpara[10] = "-maxk";
		ipeakpara[11] = Integer.toString(maxk);;
		testClassicMethod(ipeakpara);
	}
	public static void testClassicMethod(String[] arg) {
		OutputStep.setDefaultHandlerWriter();
		// OutputStep.setDefaultHandlerVisualizer();
		// String[] arg = new String[8];
		// arg[0] = "de.lmu.ifi.dbs.elki.application.KDDCLIApplication";
		// arg[1] = "-dbc.in";
		// arg[2] = "E:\\workspace\\data\\data.txt";
		// arg[3] = "-time";
		// arg[4] = "-algorithm";
		// arg[5] = "clustering.em.EM";
		// arg[6] = "-em.k";
		// arg[7] = "4";
		runCLIApplication(TestRealdata.class, arg);

	}

	/**
	 * Runs a KDD task accordingly to the specified parameters.
	 * 
	 * @param args
	 *            parameter list according to description
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		 for (int i = 1; i < 380; i++) {
		 String filepath = "E:\\workspace\\data\\datasetlabel\\data" + i
		 + ".csv";
		 //testxmeans(filepath,2,2);
		 //testIpeaks(filepath,50,2,60);
		 //testDBSCAN(filepath, 0.5, 5);
		 testAP(filepath, 20);
		 }
		//String filepath = "D:\\picture\\data.txt";
		// testIpeaks(filepath,10,0,0);
		// testXmeans
		// for (int kmeansk = 2; kmeansk <= 20; kmeansk++) {
		// for (int xmeansk = 2; xmeansk <= kmeansk; xmeansk++) {
		// testxmeans(filepath, kmeansk, xmeansk);
		// }
		// }

		// testNMS

		// for (int i = 0; i < 200; i++) {
		// testAP(filepath,i+1);
		// }

		// testDBSCAN(filepath, 0, 0);
		// testNMS(filepath, 0, 0);
		// testAP(filepath, 0, 0);
		// }
		// 测试split、overlap生成3维图数据
		// for (int i = 1; i <= 20; i++) {
		// String filepath = "E:\\workspace\\data\\overlapdatasetlabel\\data"
		// + i + ".txt";
		// for (int j = 2; j < 101; j++) {
		// testIpeaks(filepath, j, 0, 0);
		// }
		// }
		// 测试利用epsilon、minpts生成DBSCAN三维图
		// for (int i = 280; i < 380; i++) {
		// String filepath = "E:\\workspace\\data\\datasetlabel\\data" + i
		// + ".csv";
		// 测试不同参数下的DBSCAN
		// for(int l = 1 ; l < 20 ; l++ )
		// {
		// for(int j = 2 ; j <=20 ; j++)
		// {
		// testDBSCAN(filepath, (j-2)*0.1+0.01, j);
		// }
		// }
		// }
		// String filepath =
		// "C:\\Users\\Administrator\\Desktop\\博士后研究工作\\正在进行的工作\\试验原始数据\\实验数据\\realdata\\yeast.csv";
		// testIpeaks(filepath,50,0,0);
		// testxmeans(filepath,0,0);
		// testDBSCAN(filepath, 0.02, 0);
		// testNMS(filepath, 0, 0);
		// testAP(filepath, 0, 0);
		// testIpeaks(filepath,0,0);
		// testIpeaks(filepath,2,0,0);
		// testIpeaks(filepath,3,0,0);
		// testIpeaks(filepath,5,0,0);
		// testIpeaks(filepath,10,0,0);
		// testIpeaks(filepath,20,0,0);
		// testIpeaks(filepath,64,0,0);
		// testxm testIpeaks(filepath,64,0,0);eans(filepath,0,0);
		// testIpeaks(filepath,70,0,0);
		// testIpeaks(filepath,100,0,0);

//		for (int i = 2; i < 100; i++) {
//			 testIpeaks(filepath,i,0,0);
//		}
		
		//testxmeans(filepath,3,3);
	}
}
