package de.lmu.ifi.dbs.elki.algorithm.clustering.ipeaks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.ClusterModel;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.integer.ArrayModifiableIntegerDBIDs;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;

public class DPCsplit<O> extends AbstractDistanceBasedAlgorithm<O, Clustering<Model>>
		implements ClusteringAlgorithm<Clustering<Model>> {
	private static final Logging LOG = Logging.getLogger(DPCsplit.class);

	protected DPCsplit(DistanceFunction<? super O> distanceFunction, double deltaThreshold,
			double rhoThreshold, double multiValue) {
		super(distanceFunction);
		this.deltaThreshold = deltaThreshold;
		this.rhoThreshold = rhoThreshold;
		this.multiValue = multiValue;
	}

	protected List<ModifiableDBIDs> resultList;
	protected double deltaThreshold;
	protected double rhoThreshold;
	protected double multiValue;
	private ArrayList<Sample> samples;
	/** <index, sample >Map */
	// private HashMap<Integer, Sample> sampleIndexMap;
	// 保存每个样本的所有最近邻
	private HashMap<Integer, Vector> allNeighbours;
	/** 局部密度Map ：<index,densitycount> */
	private HashMap<Integer, Integer> densityCountMap;
	private HashMap<Integer, Vector> knnIds;
	/** 由大到小排序的Density list */
	private ArrayList<Map.Entry<Integer, Integer>> sortedDensityList;
	/** deltaMap:<index, delta> */
	private HashMap<Integer, Double> deltaMap;
	/** 每个样本的最近邻：<sampleIndex, nearestNeighborIndex> */
	private HashMap<Integer, Integer> nearestNeighborMap;
	/** 样本对距离：<"index1 index2", distance> */
	private HashMap<String, Double> pairDistanceMap;
	/** 最大样本距离 */
	private double maxDistance;
	/** 最小样本距离 */
	private double minDistance;
	/** 选取的簇中心 */
	private ArrayList<Integer> centerList;
	/** 划分的聚类结果<sampleIndex, clusterIndex> */
	private HashMap<Integer, Integer> clusterMap;

	public ArrayList<Sample> getOriginalData(Relation<O> relation) {
		this.samples = new ArrayList<>();
		MaterializedRelation<O> re = (MaterializedRelation) relation;
		for (DBIDIter iditer = relation.iterDBIDs(); iditer.valid(); iditer.advance()) {
			int id = DBIDUtil.asInteger(DBIDUtil.deref(iditer));
			String[] s = re.get(iditer).toString().split(" ");
			double[] value = new double[s.length];
			for (int i = 0; i < s.length; i++) {
				value[i] = new Double(s[i]);
			}
			Sample aSample = new Sample(value, "");
			aSample.setId(id);
			samples.add(aSample);
		}
		return samples;
	}

	public int[] getIdlist(Relation<O> relation) {
		int[] ret = new int[relation.size()];
		int k = 0;
		for (DBIDIter iditer = relation.iterDBIDs(); iditer.valid(); iditer.advance()) {
			int id = DBIDUtil.asInteger(DBIDUtil.deref(iditer));
			ret[k] = id;
			k++;
		}
		return ret;
	}

	public Clustering<Model> run(Database db, Relation<O> relation) {
		int[] idList = getIdlist(relation);
		resultList = new ArrayList<>();
		getOriginalData(relation);
		calPairDistance();
		double dc = findDC();
		calRho(dc);
		calDelta();
		Clustering<Model> finalResult = new Clustering<>("DPCsplitMerge", "DPCsplitMerge");
		centerList = new ArrayList<Integer>();
		clusterMap = new HashMap<Integer, Integer>();

		// get centers
		List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(deltaMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
			// 降序排序
			public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {

				return o2.getValue().compareTo(o1.getValue());
			}

		});
		// for (Map.Entry<Integer, Double> deltaEntry : deltaMap.entrySet()) {
		int countcentrals = 0;
			for (Map.Entry<Integer, Double> deltaEntry : list) {
			//if (deltaEntry.getValue() >= deltaThreshold && densityCountMap.get(deltaEntry.getKey()) >= rhoThreshold) {
				centerList.add(deltaEntry.getKey());
				clusterMap.put(deltaEntry.getKey(), deltaEntry.getKey());
				countcentrals++;
				if(countcentrals>=2)
					break;
			//}
		}
		// System.out.println(centerList.size());
		// calculate clusters，注意：一定要按照密度由大到小逐个划分簇（从高局部密度到低局部密度）
		Vector<ModifiableDBIDs> clusterResult = new Vector<ModifiableDBIDs>();

		for (int i = 0; i < clusterMap.size(); i++) {
			ModifiableDBIDs temp = DBIDUtil.newArray();
			temp.add(DBIDUtil.importInteger(centerList.get(i) + idList[0]));
			//clusterResult.add(temp);
			 clusterResult.add(DBIDUtil.newArray());
		}

		allNeighbours = new HashMap<Integer, Vector>();
		for (int j = 0; j < clusterMap.size(); j++) {
			Integer cl = new Integer(j);
			Vector neighbors = new Vector();
			allNeighbours.put(cl, neighbors);
		}
		int countAllneighbors = 0;
		HashMap resultLabel = new HashMap<Integer, Vector>();
		for (Map.Entry<Integer, Integer> candidate : sortedDensityList) {
			if (!centerList.contains(candidate.getKey())) {
				// 将最近邻居的类别索引作为该样本的类别索引
				if (clusterMap.containsKey(nearestNeighborMap.get(candidate.getKey()))) {
					Integer neid = clusterMap.get(nearestNeighborMap.get(candidate.getKey()));
					clusterMap.put(candidate.getKey(), clusterMap.get(nearestNeighborMap.get(candidate.getKey())));
					for (int k = 0; k < centerList.size(); k++) {
						if (neid == centerList.get(k)) {
							clusterResult.get(k).add(DBIDUtil.importInteger(candidate.getKey() + idList[0]));
							Vector ids = knnIds.get(candidate.getKey());
							// System.out.println(ids.size());
							if (ids != null) {
								for (int m = 0; m < ids.size(); m++) {
									if (!allNeighbours.get(k).contains(candidate.getKey()))
										allNeighbours.get(k).add(ids.get(m));
								}
							}
							continue;
						}
					}

				} else {
					clusterMap.put(candidate.getKey(), -1);
				}
			}
		}
		
		//clusterResult.get(0).addDBIDs(clusterResult.get(1));
		for (int j = 0; j < clusterResult.size(); j++) {
			resultList.add(clusterResult.get(j));
			if(true)
			{
			ArrayModifiableIntegerDBIDs ad = (ArrayModifiableIntegerDBIDs)clusterResult.get(j);
			for (int k = 0; k < ad.size(); k++) {
				
				 System.out.println((j+1)+ " " +
				 this.samples.get(DBIDUtil.asInteger(ad.get(k))- idList[0]).getAttributes()[0] + " "
				 + this.samples.get(DBIDUtil.asInteger(ad.get(k))- idList[0]).getAttributes()[1]);
			}
			}
		}
		for (ModifiableDBIDs res : resultList) {
			finalResult.addToplevelCluster(new Cluster<Model>(res, ClusterModel.CLUSTER));
		}
		return finalResult;
	}

	public void calDelta() {
		// 局部密度由大到小排序
		sortedDensityList = new ArrayList<Map.Entry<Integer, Integer>>(densityCountMap.entrySet());
		Collections.sort(sortedDensityList, new Comparator<Map.Entry<Integer, Integer>>() {

			@Override
			public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
				if (o1.getValue() > o2.getValue())
					return -1;
				else if (o1.getValue() < o2.getValue()) {
					return 1;
				}
				return 0;
			}
		});
		nearestNeighborMap = new HashMap<Integer, Integer>(samples.size());
		deltaMap = new HashMap<Integer, Double>(samples.size());
		for (int i = 0; i < sortedDensityList.size(); i++) {
			if (i == 0) {
				nearestNeighborMap.put(sortedDensityList.get(i).getKey(), -1);
				deltaMap.put(sortedDensityList.get(i).getKey(), maxDistance);
			} else {
				double minDij = Double.MAX_VALUE;
				int index = 0;
				for (int j = 0; j < i; j++) {
					if (i != j) {
						double dis = getDistanceFromIndex(sortedDensityList.get(i).getKey(),
								sortedDensityList.get(j).getKey());
						if (dis < minDij) {
							index = j;
							minDij = dis;
						}
					}
				}
				nearestNeighborMap.put(sortedDensityList.get(i).getKey(), sortedDensityList.get(index).getKey());
				deltaMap.put(sortedDensityList.get(i).getKey(), minDij);
			}
		}

		// 输出样本索引+样本局部密度+最近邻索引+delta值
		System.out.println("输出样本索引  样本局部密度  最近邻索引  delta值");
		for (Map.Entry<Integer, Integer> entry : sortedDensityList) {
			System.out.print(entry.getKey() + " " + entry.getValue() + " " + nearestNeighborMap.get(entry.getKey())
					+ " " + deltaMap.get(entry.getKey()));
			System.out.println(" " + this.samples.get(entry.getKey()).getAttributes()[0] + " "
					+ this.samples.get(entry.getKey()).getAttributes()[1]);
		}
	}

	/**
	 * 根据索引获得两个样本间距离
	 * 
	 * @param index1
	 * @param index2
	 * @return
	 */
	private double getDistanceFromIndex(int index1, int index2) {
		if (pairDistanceMap.containsKey(index1 + " " + index2)) {
			return pairDistanceMap.get(index1 + " " + index2);
		} else {
			return pairDistanceMap.get(index2 + " " + index1);
		}
	}

	/**
	 * 计算局部密度
	 */
	public void calRho(double dcThreshold) {
		densityCountMap = new HashMap<Integer, Integer>(samples.size());
		// 初始化为0
		for (int i = 0; i < samples.size(); i++) {
			densityCountMap.put(i, 0);
		}

		knnIds = new HashMap<Integer, Vector>(samples.size());
		// 初始化为0
		for (int i = 0; i < samples.size(); i++) {
			knnIds.put(i, new Vector());
		}
		for (Map.Entry<String, Double> diss : pairDistanceMap.entrySet()) {
			String[] segs = diss.getKey().split(" ");
			int[] indexs = new int[2];
			indexs[0] = Integer.parseInt(segs[0]);
			indexs[1] = Integer.parseInt(segs[1]);
			if (diss.getValue() < dcThreshold) {
				knnIds.get(indexs[0]).add(indexs[1]);
				knnIds.get(indexs[1]).add(indexs[0]);
			}
			if (diss.getValue() < dcThreshold) {
				for (int i = 0; i < indexs.length; i++) {
					densityCountMap.put(indexs[i], densityCountMap.get(indexs[i]) + 1);
				}
			}
		}
	}

	/**
	 * 在dcThreshold范围内得到各样本点的近邻
	 */
	public void calKNN(double dcThreshold) {
		// knnIds = new HashMap<Integer, Vector>(samples.size());
		// // 初始化为0
		// for (int i = 0; i < samples.size(); i++) {
		// knnIds.put(i, new Vector());
		// }
		// for (Map.Entry<String, Double> diss : pairDistanceMap.entrySet()) {
		// if (diss.getValue() < dcThreshold) {
		// String[] segs = diss.getKey().split(" ");
		// int[] indexs = new int[2];
		// indexs[0] = Integer.parseInt(segs[0]);
		// indexs[1] = Integer.parseInt(segs[1]);
		// knnIds.get(indexs[0]).add(indexs[1]);
		// knnIds.get(indexs[1]).add(indexs[0]);
		// }
		// }
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
				pairDistanceMap.put(i + " " + j, dis);
				if (dis > maxDistance)
					maxDistance = dis;
				if (dis < minDistance)
					minDistance = dis;
			}
		}
	}

	/**
	 * 计算截断距离
	 * 
	 * @return
	 */
	public double findDC() {
		double tmpMax = maxDistance;
		double tmpMin = minDistance;
		double dc = 0.5 * (tmpMax + tmpMin);
		for (int iteration = 0; iteration < 100; iteration++) {
			int neighbourNum = 0;
			for (Map.Entry<String, Double> dis : pairDistanceMap.entrySet()) {
				if (dis.getValue() < dc)
					neighbourNum += 2;
			}
			double neighborPercentage = neighbourNum / Math.pow(samples.size(), 2);
			if (neighborPercentage >= 0.01 && neighborPercentage <= 0.02)
				break;
			if (neighborPercentage > 0.02) {
				tmpMax = dc;
				dc = 0.5 * (tmpMax + tmpMin);
			}
			if (neighborPercentage < 0.01) {
				tmpMin = dc;
				dc = 0.5 * (tmpMax + tmpMin);
			}
		}
		System.out.println("截断距离为：" + dc);
		return dc * this.multiValue;
	}

	/**
	 * 计算两个样本的高斯距离
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private double twoSampleDistance(Sample a, Sample b) {
		double[] aData = a.getAttributes();
		double[] bData = b.getAttributes();
		double distance = 0.0;
		for (int i = 0; i < aData.length; i++) {
			distance += Math.pow(aData[i] - bData[i], 2);
		}
		return 1 - Math.exp(distance * (-0.5));
	}

	public ArrayList<Integer> getCenterList() {
		return centerList;
	}

	public void predictLabel() {
		for (int i = 0; i < samples.size(); i++) {
			// System.out.println(clusterMap.get(i));
			if (clusterMap.get(i) != -1)
				samples.get(i).setPredictLabel(samples.get(clusterMap.get(i)).getLabel());
		}
	}

	public static void main(String[] args) {
		// DataReader reader = new DataReader();
		// reader.readData();
		// ArrayList<Sample> samples = reader.getSamples();
		// DPCsplit cluster = new DPCsplit(samples);
		// cluster.calPairDistance();
		// double dc = cluster.findDC();
		// System.out.println(dc);
		// cluster.calRho(dc);
		// cluster.calDelta();
		// cluster.clustering(0.3, 1);
		// System.out.println(cluster.getCenterList());
	}

	@Override
	public TypeInformation[] getInputTypeRestriction() {
		// TODO Auto-generated method stub
		return TypeUtil.array(getDistanceFunction().getInputTypeRestriction());
	}

	@Override
	protected Logging getLogger() {
		// TODO Auto-generated method stub
		return LOG;
	}

	public static class Parameterizer<O> extends AbstractDistanceBasedAlgorithm.Parameterizer<O> {

		/**
		 * Parameter to specify the threshold for minimum number of points in
		 * the epsilon-neighborhood of a point, must be an integer greater than
		 * 0.
		 */
		public static final OptionID DELTHRESHOLD = new OptionID("deltaThreshold", "");
		public static final OptionID RHOTHRESHOLD = new OptionID("rhoThreshold", "");
		public static final OptionID MultiValue = new OptionID("multiValue", "");

		/**
		 * Holds the minimum cluster size.
		 */
		protected double deltaThreshold;
		protected double rhoThreshold;
		protected double multiValue;

		@Override
		protected void makeOptions(Parameterization config) {
			super.makeOptions(config);

			DoubleParameter deltaThresholdp = new DoubleParameter(DELTHRESHOLD)
					.addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
			if (config.grab(deltaThresholdp)) {
				deltaThreshold = deltaThresholdp.getValue();
			}
			DoubleParameter rhoThresholdp = new DoubleParameter(RHOTHRESHOLD) //
					.addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
			if (config.grab(rhoThresholdp)) {
				rhoThreshold = rhoThresholdp.getValue();
			}

			DoubleParameter multiValuep = new DoubleParameter(MultiValue) //
					.addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
			if (config.grab(multiValuep)) {
				multiValue = multiValuep.getValue();
			}
		}

		@Override
		protected DPCsplit<O> makeInstance() {
			return new DPCsplit<>(distanceFunction, deltaThreshold, rhoThreshold, multiValue);
		}
	}
}
