package UseELKI;

import java.util.List;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansLloyd;
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyGeneratedInitialMeans;
import de.lmu.ifi.dbs.elki.algorithm.clustering.trivial.ByLabelClustering;
import de.lmu.ifi.dbs.elki.application.KDDCLIApplication;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.Clustering;
import de.lmu.ifi.dbs.elki.data.LabelList;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.datasource.FileBasedDatabaseConnection;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.SquaredEuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.AutomaticEvaluation;
import de.lmu.ifi.dbs.elki.evaluation.clustering.EvaluateClustering;
import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.result.ResultHierarchy;
import de.lmu.ifi.dbs.elki.result.SettingsResult;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;
import de.lmu.ifi.dbs.elki.workflow.EvaluationStep;
import de.lmu.ifi.dbs.elki.workflow.OutputStep;

public class UseKmeans {

	public static Clustering<KMeansModel> runKmeans(String filename) {
		EvaluationStep evaluationStep;

		/**
		 * The output/visualization step
		 */
		OutputStep outputStep;

		/**
		 * The result hierarchy.
		 */
		ResultHierarchy hier;

		ListParameterization params = new ListParameterization();
		params.addParameter(FileBasedDatabaseConnection.Parameterizer.INPUT_ID, filename);
		// Add other parameters for the database here!

		// Instantiate the database:
		Database db = ClassGenericsUtil.parameterizeOrAbort(StaticArrayDatabase.class, params);
		// Don't forget this, it will load the actual data (otherwise you get
		// null values below)
		db.initialize();

		Relation<NumberVector> vectors = db.getRelation(TypeUtil.NUMBER_VECTOR_FIELD);
		Relation<LabelList> labels = db.getRelation(TypeUtil.LABELLIST);

		// K-means should be used with squared Euclidean (least squares):
		SquaredEuclideanDistanceFunction dist = SquaredEuclideanDistanceFunction.STATIC;
		// Default initialization, using global random:
		// To fix the random seed, use: new RandomFactory(seed);
		RandomlyGeneratedInitialMeans init = new RandomlyGeneratedInitialMeans(RandomFactory.DEFAULT);

		// Setup textbook k-means clustering:
		KMeansLloyd<NumberVector> km = new KMeansLloyd<>(dist, 3, 0, init);
		// Run the algorithm:
		Clustering<KMeansModel> c = km.run(db);
		new EvaluateClustering(new ByLabelClustering(), false, true).processNewResult(db.getHierarchy(), c);
		return c;

	}

	public static void main(String[] args) {

		// Clustering<KMeansModel> c =
		// runKmeans("C:\\Users\\Administrator\\workspace\\ELKI\\src\\data1.csv");
		Clustering<KMeansModel> c = runKmeans("E:\\workspace\\data\\data.txt");
		c.getAllClusters();

	}

}
