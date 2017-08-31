package de.lmu.ifi.dbs.elki.index.preprocessed.localpca;

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

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.index.preprocessed.AbstractPreprocessorIndex;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.PCAFilteredResult;
import de.lmu.ifi.dbs.elki.math.linearalgebra.pca.PCAFilteredRunner;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.exceptions.ExceptionMessages;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;

/**
 * Abstract base class for a local PCA based index.
 * 
 * @author Elke Achtert
 * @author Erich Schubert
 * @since 0.4.0
 * 
 * @apiviz.has PCAFilteredRunner
 * 
 * @param <NV> Vector type
 */
// TODO: loosen DoubleDistance restriction.
@Title("Local PCA Preprocessor")
@Description("Materializes the local PCA and the locally weighted matrix of objects of a database.")
public abstract class AbstractFilteredPCAIndex<NV extends NumberVector> extends AbstractPreprocessorIndex<NV, PCAFilteredResult> implements FilteredLocalPCAIndex<NV> {
  /**
   * PCA utility object.
   */
  protected final PCAFilteredRunner pca;

  /**
   * Constructor.
   * 
   * @param relation Relation to use
   * @param pca PCA runner to use
   */
  public AbstractFilteredPCAIndex(Relation<NV> relation, PCAFilteredRunner pca) {
    super(relation);
    this.pca = pca;
  }

  @Override
  public void initialize() {
    if(relation == null || relation.size() <= 0) {
      throw new IllegalArgumentException(ExceptionMessages.DATABASE_EMPTY);
    }

    // Note: this is required for ERiC to work properly, otherwise the data is
    // recomputed for the partitions!
    if(storage != null) {
      return;
    }

    storage = DataStoreUtil.makeStorage(relation.getDBIDs(), DataStoreFactory.HINT_HOT | DataStoreFactory.HINT_TEMP, PCAFilteredResult.class);

    long start = System.currentTimeMillis();
    FiniteProgress progress = getLogger().isVerbose() ? new FiniteProgress("Performing local PCA", relation.size(), getLogger()) : null;

    // TODO: use a bulk operation?
    for(DBIDIter iditer = relation.iterDBIDs(); iditer.valid(); iditer.advance()) {
      DoubleDBIDList objects = objectsForPCA(iditer);

      PCAFilteredResult pcares = pca.processQueryResult(objects, relation);

      storage.put(iditer, pcares);

      getLogger().incrementProcessed(progress);
    }
    getLogger().ensureCompleted(progress);

    long end = System.currentTimeMillis();
    if(getLogger().isVerbose()) {
      long elapsedTime = end - start;
      getLogger().verbose(this.getClass().getName() + " runtime: " + elapsedTime + " milliseconds.");
    }
  }

  @Override
  public PCAFilteredResult getLocalProjection(DBIDRef objid) {
    if(storage == null) {
      initialize();
    }
    return storage.get(objid);
  }

  /**
   * Returns the objects to be considered within the PCA for the specified query
   * object.
   * 
   * @param id the id of the query object for which a PCA should be performed
   * @return the list of the objects (i.e. the ids and the distances to the
   *         query object) to be considered within the PCA
   */
  protected abstract DoubleDBIDList objectsForPCA(DBIDRef id);

  /**
   * Factory class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.stereotype factory
   * @apiviz.uses AbstractFilteredPCAIndex oneway - - «create»
   */
  public abstract static class Factory<NV extends NumberVector, I extends AbstractFilteredPCAIndex<NV>> implements FilteredLocalPCAIndex.Factory<NV, I> {
    /**
     * Parameter to specify the distance function used for running PCA.
     * 
     * Key: {@code -localpca.distancefunction}
     */
    public static final OptionID PCA_DISTANCE_ID = new OptionID("localpca.distancefunction", "The distance function used to select objects for running PCA.");

    /**
     * Holds the instance of the distance function specified by
     * {@link #PCA_DISTANCE_ID}.
     */
    protected DistanceFunction<NV> pcaDistanceFunction;

    /**
     * PCA utility object.
     */
    protected PCAFilteredRunner pca;

    /**
     * Constructor.
     * 
     * @param pcaDistanceFunction distance Function
     * @param pca PCA runner
     */
    public Factory(DistanceFunction<NV> pcaDistanceFunction, PCAFilteredRunner pca) {
      super();
      this.pcaDistanceFunction = pcaDistanceFunction;
      this.pca = pca;
    }

    @Override
    public abstract I instantiate(Relation<NV> relation);

    @Override
    public TypeInformation getInputTypeRestriction() {
      return pcaDistanceFunction.getInputTypeRestriction();
    }

    /**
     * Parameterization class.
     * 
     * @author Erich Schubert
     * 
     * @apiviz.exclude
     */
    public abstract static class Parameterizer<NV extends NumberVector, I extends AbstractFilteredPCAIndex<NV>> extends AbstractParameterizer {
      /**
       * Holds the instance of the distance function specified by
       * {@link #PCA_DISTANCE_ID}.
       */
      protected DistanceFunction<NV> pcaDistanceFunction;

      /**
       * PCA utility object.
       */
      protected PCAFilteredRunner pca;

      @Override
      protected void makeOptions(Parameterization config) {
        super.makeOptions(config);
        final ObjectParameter<DistanceFunction<NV>> pcaDistanceFunctionP = new ObjectParameter<>(PCA_DISTANCE_ID, DistanceFunction.class, EuclideanDistanceFunction.class);

        if(config.grab(pcaDistanceFunctionP)) {
          pcaDistanceFunction = pcaDistanceFunctionP.instantiateClass(config);
        }

        Class<PCAFilteredRunner> cls = ClassGenericsUtil.uglyCastIntoSubclass(PCAFilteredRunner.class);
        pca = config.tryInstantiate(cls);
      }
    }
  }
}