package de.lmu.ifi.dbs.elki.algorithm.clustering.ipeaks;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2015
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.ClusteringAlgorithm;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.model.ClusterModel;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.QueryUtil;
import de.lmu.ifi.dbs.elki.database.ids.ArrayModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDVar;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.integer.IntegerDBIDs;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.range.RangeQuery;
import de.lmu.ifi.dbs.elki.database.relation.MaterializedRelation;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.EvaluateSilhouette;
import de.lmu.ifi.dbs.elki.evaluation.clustering.internal.EvaluateSquaredErrors;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.logging.progress.IndefiniteProgress;
import de.lmu.ifi.dbs.elki.logging.statistics.DoubleStatistic;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

public class Ipeaks<O> extends
		AbstractDistanceBasedAlgorithm<O, Clustering<Model>> implements
		ClusteringAlgorithm<Clustering<Model>> {
	/**
	 * The logger for this class.
	 */
	private static final Logging LOG = Logging.getLogger(Ipeaks.class);

	protected int splitTimes;
	protected int maxk;
	protected int mink;
	protected Vector dataset;

	/**
	 * Holds a list of clusters found.
	 */
	protected List<ModifiableDBIDs> resultList;

	/**
	 * Holds a set of noise.
	 */
	protected ModifiableDBIDs noise;

	protected ModifiableDBIDs processedIDs;

	/**
	 * Constructor with parameters.
	 *
	 * @param distanceFunction
	 *            Distance function
	 * @param epsilon
	 *            Epsilon value
	 * @param minpts
	 *            Minpts parameter
	 */
	public Ipeaks(DistanceFunction<? super O> distanceFunction, int splitTimes,
			int maxk, int mink) {
		super(distanceFunction);
		this.splitTimes = splitTimes;
		this.maxk = maxk;
		this.mink = mink;
	}

	/**
	 * Performs the Ipeaks algorithm on the given database.
	 */
	/**
	 * @param db
	 * @param relation
	 * @return
	 */
	public Clustering<Model> run(Database db, Relation<O> relation) {
		Clustering<Model> finalresult = new Clustering<>("Ipeaks Clustering",
				"Ipeaks-clustering");
		double silValue = 0.0;
		int finalKvalue = 0;
		EvaluateSilhouette ev = new EvaluateSilhouette(getDistanceFunction(),
				false);
		DistanceQuery<O> dq = db.getDistanceQuery(relation,
				getDistanceFunction());
		MaterializedRelation<O> re = (MaterializedRelation) relation;
		int[] densitys = getDensity(getSimMatrix(getOriginalData(relation),
				this.splitTimes));
		int[] peakIds = getPeaksIds(relation, densitys);
		noise = DBIDUtil.newHashSet();
		final int size = relation.size();
		processedIDs = DBIDUtil.newHashSet(size);
		int[] idList = getIdlist(relation);
		for (int m = this.mink; m <= this.maxk; m++) {
			resultList = new ArrayList<>();
			Vector<ModifiableDBIDs> clusterResult = new Vector<ModifiableDBIDs>();
			{
				for (int i = 0; i < m; i++) {
					clusterResult.add(DBIDUtil.newArray());
				}
			}
			for (DBIDIter iditer = relation.iterDBIDs(); iditer.valid(); iditer
					.advance()) {
				int id = DBIDUtil.asInteger(DBIDUtil.deref(iditer));
				double minDistinace = Math.sqrt(idList.length);
				ArrayList ar1 = (ArrayList) this.dataset.get(id - idList[0]);
				int cLabel = 0;
				for (int n = 0; n < m; n++) {
					ArrayList ar2 = (ArrayList) this.dataset
							.get(peakIds[n] - 1);
					double currentDistiance = getVectorDistiance(ar1, ar2);
					{
						if (currentDistiance < minDistinace) {
							minDistinace = currentDistiance;
							cLabel = n;
						}
					}

				}
				clusterResult.get(cLabel).add(DBIDUtil.deref(iditer));
			}

			for (int j = 0; j < m; j++) {
				resultList.add(clusterResult.get(j));
			}
			Clustering<Model> result = new Clustering<>("Ipeaks Clustering",
					"Ipeaks-clustering");
			for (ModifiableDBIDs res : resultList) {
				result.addToplevelCluster(new Cluster<Model>(res,
						ClusterModel.CLUSTER));
			}
//			result.addToplevelCluster(new Cluster<Model>(noise, true,
//					ClusterModel.CLUSTER));
			double currentsil = ev.evaluateClustering(db, relation, dq, result);
			// System.out.println(currentsil + ":" + m);
			if (silValue < currentsil) {
				silValue = currentsil;
				finalresult = result;
				finalKvalue = m;
			}
		}
		//System.out.println(finalKvalue);
	//	finalresult.getClusterHierarchy().
		return finalresult;
	}

	public int[] getIdlist(Relation<O> relation) {
		int[] ret = new int[relation.size()];
		int k = 0;
		for (DBIDIter iditer = relation.iterDBIDs(); iditer.valid(); iditer
				.advance()) {
			int id = DBIDUtil.asInteger(DBIDUtil.deref(iditer));
			ret[k] = id;
			k++;
		}
		return ret;
	}

	public Vector getOriginalData(Relation<O> relation) {
		Vector v = new Vector();
		MaterializedRelation<O> re = (MaterializedRelation) relation;
		for (DBIDIter iditer = relation.iterDBIDs(); iditer.valid(); iditer
				.advance()) {
			String[] s = re.get(iditer).toString().split(" ");
			ArrayList ar = new ArrayList();
			for (int i = 0; i < s.length; i++) {
				ar.add(new Double(s[i]));
			}
			v.add(ar);
		}
		this.dataset = v;
		return v;
	}

	public int[] getPeaksIds(Relation<O> relation, int[] density) {
		double[] ret = new double[density.length];
		Vector<ArrayList<Double>> dataset = getOriginalData(relation);
		for (int i = 0; i < density.length; i++) {
			if (i == 0) {
				ret[i] = Math.sqrt(density.length);
			} else {
				double peakValues = Math.sqrt(density.length);
				double currentValue = 0;
				for (int j = 0; j < i; j++) {
					currentValue = getVectorDistiance(
							dataset.get(density[i] - 1), dataset.get(density[j]-1));
					if (currentValue < peakValues) {
						peakValues = currentValue;
						ret[i] = peakValues;
					}
				}
			}
		}
		for (int i = 0; i < ret.length - 1; i++) {
			for (int j = i + 1; j < ret.length; j++) {
				if (ret[i] < ret[j]) {
					int temp = 0;
					double tempvalue = 0;
					tempvalue = ret[i];
					ret[i] = ret[j];
					ret[j] = tempvalue;
					temp = density[i];
					density[i] = density[j];
					density[j] = temp;
				}
			}
		}
		System.out.println();
		for (int m = 0; m < ret.length; m++) {
			//System.out.print(density[m] + " ");
		}
		System.out.println();
		return density;
	}

	public double getVectorDistiance(ArrayList<Double> v1, ArrayList<Double> v2) {
		double distiance = 0;
		for (int i = 0; i < v1.size(); i++) {
			double value1 = v1.get(i).doubleValue();
			double value2 = v2.get(i).doubleValue();
			distiance += (value1 - value2) * (value1 - value2);
		}
		distiance = Math.sqrt(distiance);
		return distiance;
	}

	public int[][] getSplitsResult(Vector originalData,
			ArrayList<Double> randObserveroint) {
		String[] v = new String[originalData.size()];
		int size = originalData.size();
		ArrayList<Double> ob = randObserveroint;
		for (int k = 0; k < originalData.size(); k++) {
			double distiance = 0.0;
			ArrayList<Double> ar = (ArrayList) originalData.get(k);
			for (int i = 0; i < ar.size(); i++) {
				double value1 = ob.get(i).doubleValue();
				double value2 = ar.get(i).doubleValue();
				distiance += (value1 - value2) * (value1 - value2);
			}
			distiance = Math.sqrt(distiance);
			String sDistiance = String.valueOf(distiance) + ":" + k;
			v[k] = sDistiance;
		}
		Arrays.sort(v);

		int[][] ret = new int[size][size];
		String[] lable = new String[size];
		for (int i = 0; i < v.length; i++) {
			String[] s = v[i].split(":");
			if (i < v.length / 2) {
				lable[i] = s[1] + ":" + 1;
			} else {
				lable[i] = s[1] + ":" + 2;
			}
		}
		for (int i = 0; i < lable.length; i++) {
			String[] idAndk = lable[i].split(":");
			int id = Integer.parseInt(idAndk[0]);
			int lab = Integer.parseInt(idAndk[1]);
			for (int j = 0; j < lable.length; j++) {
				String[] idAndk1 = lable[j].split(":");
				int id1 = Integer.parseInt(idAndk1[0]);
				int lab1 = Integer.parseInt(idAndk1[1]);
				if (id1 != id && lab == lab1) {
					ret[id][id1] = 1;
				}
			}

		}
		return ret;
	}

	public int[][] getSimMatrix(Vector originalData, int splitTimes) {
		int size = originalData.size();
		int nColumn = ((ArrayList) originalData.get(0)).size();
		int[][] retMatrix = new int[size][size];
		for (int i = 0; i < splitTimes; i++) {
			ArrayList<Double> Observeroint = new ArrayList<Double>();
			long seed = i;
			Random random = new Random(i * 1000);
			for (int l = 0; l < nColumn; l++) {
				Observeroint.add(random.nextDouble() * 3 - 1);
			}
			int[][] currentret = getSplitsResult(originalData, Observeroint);
			for (int k = 0; k < size; k++) {
				for (int l = 0; l < size; l++) {
					retMatrix[k][l] = retMatrix[k][l] + currentret[k][l];
				}
			}
		}
/*		for (int k = 0; k < size; k++) {
			for (int l = 0; l < size; l++) {
				System.out.print(retMatrix[k][l] + " ");
			}
			System.out.println();
		}*/
		return retMatrix;
	}

	public int[] getDensity(int[][] simMatrix) {
		int[] ret = new int[simMatrix.length];
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret.length; j++) {
				if (simMatrix[i][j] == this.splitTimes)
					ret[i] += simMatrix[i][j];
			}
		}

		ret = getDesityIds(ret);

		for (int k = 0; k < ret.length; k++) {
			System.out.print(ret[k] + " ");
		}

		return ret;
	}

	public int[] getDesityIds(int[] sortValue) {
		int[] ret = new int[sortValue.length];
		for (int k = 0; k < sortValue.length; k++) {
			ret[k] = k + 1;
		}
		for (int i = 0; i < sortValue.length - 1; i++) {
			for (int j = i + 1; j < sortValue.length; j++) {
				if (sortValue[i] < sortValue[j]) {
					int temp = 0;
					temp = ret[i];
					ret[i] = ret[j];
					ret[j] = temp;
					int tempvalue = 0;
					tempvalue = sortValue[i];
					sortValue[i] = sortValue[j];
					sortValue[j] = tempvalue;
				}
			}
		}
		return ret;
	}

	@Override
	public TypeInformation[] getInputTypeRestriction() {
		return TypeUtil.array(getDistanceFunction().getInputTypeRestriction());
	}

	@Override
	protected Logging getLogger() {
		return LOG;
	}

	/**
	 * Parameterization class.
	 *
	 * @author Erich Schubert
	 *
	 * @apiviz.exclude
	 */
	public static class Parameterizer<O> extends
			AbstractDistanceBasedAlgorithm.Parameterizer<O> {

		/**
		 * Parameter to specify the threshold for minimum number of points in
		 * the epsilon-neighborhood of a point, must be an integer greater than
		 * 0.
		 */
		public static final OptionID MINPTS_ID = new OptionID(
				"splitTimes", "");
		public static final OptionID PEAKSK = new OptionID("maxk", "");
		public static final OptionID PEAKSminK = new OptionID("mink", "");

		/**
		 * Holds the minimum cluster size.
		 */
		protected int splitTimes;
		protected int maxk;
		protected int mink;

		@Override
		protected void makeOptions(Parameterization config) {
			super.makeOptions(config);

			IntParameter splitTimesP = new IntParameter(MINPTS_ID) //
					.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
			if (config.grab(splitTimesP)) {
				splitTimes = splitTimesP.getValue();
				if (splitTimes <= 10) {
					LOG.warning("Ipeaks.");
				}
			}
			IntParameter kmincluster = new IntParameter(PEAKSminK) //
					.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
			if (config.grab(kmincluster)) {
				mink = kmincluster.getValue();
			}
			IntParameter kcluster = new IntParameter(PEAKSK) //
					.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_INT);
			if (config.grab(kcluster)) {
				maxk = kcluster.getValue();
			}

		}

		@Override
		protected Ipeaks<O> makeInstance() {
			return new Ipeaks<>(distanceFunction, splitTimes, maxk, mink);
		}
	}

	public static void main(String[] args) {
		int[] a = { 20, 10, 30, 50, 12, 1 };
		int[] b = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			b[i] = i + 1;
		}
		for (int i = 0; i < a.length - 1; i++) {
			for (int j = i + 1; j < a.length; j++) {
				if (a[i] < a[j]) {
					int temp = 0;
					int tempvalue = 0;
					tempvalue = a[i];
					a[i] = a[j];
					a[j] = tempvalue;
					temp = b[i];
					b[i] = b[j];
					b[j] = temp;
				}
			}
		}
		for (int k = 0; k < b.length; k++) {
			// System.out.print(a[k]+" ");
			System.out.print(b[k] + " ");
			System.out.print(":");
		}
	}
}
