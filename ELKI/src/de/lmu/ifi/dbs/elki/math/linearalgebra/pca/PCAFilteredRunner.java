package de.lmu.ifi.dbs.elki.math.linearalgebra.pca;

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
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.math.linearalgebra.EigenvalueDecomposition;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.SortedEigenPairs;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.LessGlobalConstraint;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.ObjectParameter;

/**
 * PCA runner that will do dimensionality reduction. PCA is computed as with the
 * regular runner, but afterwards, an {@link EigenPairFilter} is applied.
 * 
 * @author Erich Schubert
 * @since 0.2
 * 
 * @apiviz.landmark
 * @apiviz.has PCAFilteredResult oneway - - «create»
 * @apiviz.composedOf EigenPairFilter
 */
public class PCAFilteredRunner extends PCARunner {
  /**
   * Holds the instance of the EigenPairFilter specified by
   * {@link Parameterizer#PCA_EIGENPAIR_FILTER}.
   */
  private EigenPairFilter eigenPairFilter;

  /**
   * Holds the value of {@link Parameterizer#BIG_ID}.
   */
  private double big;

  /**
   * Holds the value of {@link Parameterizer#SMALL_ID}.
   */
  private double small;

  /**
   * Constructor.
   * 
   * @param covarianceMatrixBuilder Covariance matrix builder
   * @param eigenPairFilter Eigen pair filter
   * @param big Replacement for large eigenvalues
   * @param small Replacement for small eigenvalues
   */
  public PCAFilteredRunner(CovarianceMatrixBuilder covarianceMatrixBuilder, EigenPairFilter eigenPairFilter, double big, double small) {
    super(covarianceMatrixBuilder);
    this.eigenPairFilter = eigenPairFilter;
    this.big = big;
    this.small = small;
  }

  /**
   * Run PCA on a collection of database IDs.
   * 
   * @param ids a collection of ids
   * @param database the database used
   * @return PCA result
   */
  @Override
  public PCAFilteredResult processIds(DBIDs ids, Relation<? extends NumberVector> database) {
    return processCovarMatrix(covarianceMatrixBuilder.processIds(ids, database));
  }

  /**
   * Run PCA on a QueryResult Collection.
   * 
   * @param results a collection of QueryResults
   * @param database the database used
   * @return PCA result
   */
  @Override
  public PCAFilteredResult processQueryResult(DoubleDBIDList results, Relation<? extends NumberVector> database) {
    return processCovarMatrix(covarianceMatrixBuilder.processQueryResults(results, database));
  }

  /**
   * Process an existing Covariance Matrix.
   * 
   * @param covarMatrix the matrix used for performing PCA
   * @return Filtered result
   */
  @Override
  public PCAFilteredResult processCovarMatrix(Matrix covarMatrix) {
    // TODO: add support for a different implementation to do EVD?
    EigenvalueDecomposition evd = new EigenvalueDecomposition(covarMatrix);
    return processEVD(evd);
  }

  /**
   * Process an existing eigenvalue decomposition.
   * 
   * @param evd eigenvalue decomposition to use
   * @return filtered result
   */
  @Override
  public PCAFilteredResult processEVD(EigenvalueDecomposition evd) {
    SortedEigenPairs eigenPairs = new SortedEigenPairs(evd, false);
    FilteredEigenPairs filteredEigenPairs = eigenPairFilter.filter(eigenPairs);
    return new PCAFilteredResult(eigenPairs, filteredEigenPairs, big, small);
  }

  /**
   * Retrieve the {@link EigenPairFilter} to be used. For derived PCA Runners
   * 
   * @return eigenpair filter configured.
   */
  protected EigenPairFilter getEigenPairFilter() {
    return eigenPairFilter;
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer extends PCARunner.Parameterizer {
    /**
     * Parameter to specify the filter for determination of the strong and weak
     * eigenvectors, must be a subclass of {@link EigenPairFilter}.
     * <p>
     * Default value: {@link PercentageEigenPairFilter}
     * </p>
     * <p>
     * Key: {@code -pca.filter}
     * </p>
     */
    public static final OptionID PCA_EIGENPAIR_FILTER = new OptionID("pca.filter", "Filter class to determine the strong and weak eigenvectors.");

    /**
     * Parameter to specify a constant big value to reset high eigenvalues, must
     * be a double greater than 0.
     * <p>
     * Default value: {@code 1.0}
     * </p>
     * <p>
     * Key: {@code -pca.big}
     * </p>
     */
    public static final OptionID BIG_ID = new OptionID("pca.big", "A constant big value to reset high eigenvalues.");

    /**
     * Parameter to specify a constant small value to reset low eigenvalues, must
     * be a double greater than 0.
     * <p>
     * Default value: {@code 0.0}
     * </p>
     * <p>
     * Key: {@code -pca.small}
     * </p>
     */
    public static final OptionID SMALL_ID = new OptionID("pca.small", "A constant small value to reset low eigenvalues.");

    /**
     * Holds the instance of the EigenPairFilter specified by
     * {@link #PCA_EIGENPAIR_FILTER}.
     */
    protected EigenPairFilter eigenPairFilter;

    /**
     * Holds the value of {@link #BIG_ID}.
     */
    protected double big;

    /**
     * Holds the value of {@link #SMALL_ID}.
     */
    protected double small;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      ObjectParameter<EigenPairFilter> filterP = new ObjectParameter<>(PCA_EIGENPAIR_FILTER, EigenPairFilter.class, PercentageEigenPairFilter.class);
      if(config.grab(filterP)) {
        eigenPairFilter = filterP.instantiateClass(config);
      }

      DoubleParameter bigP = new DoubleParameter(BIG_ID, 1.);
      bigP.addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
      if(config.grab(bigP)) {
        big = bigP.doubleValue();

      }

      DoubleParameter smallP = new DoubleParameter(SMALL_ID, 0.);
      smallP.addConstraint(CommonConstraints.GREATER_EQUAL_ZERO_DOUBLE);
      if(config.grab(smallP)) {
        small = smallP.doubleValue();
      }

      // global constraint small <--> big
      config.checkConstraint(new LessGlobalConstraint<>(smallP, bigP));
    }

    @Override
    protected PCAFilteredRunner makeInstance() {
      return new PCAFilteredRunner(covarianceMatrixBuilder, eigenPairFilter, big, small);
    }
  }
}
