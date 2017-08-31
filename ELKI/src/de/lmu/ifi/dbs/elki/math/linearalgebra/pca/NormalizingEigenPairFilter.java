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

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.math.linearalgebra.EigenPair;
import de.lmu.ifi.dbs.elki.math.linearalgebra.SortedEigenPairs;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;

/**
 * The NormalizingEigenPairFilter normalizes all eigenvectors s.t. <eigenvector,
 * eigenvector> * eigenvalue = 1, where <,> is the standard dot product
 * 
 * @author Simon Paradies
 * @since 0.2
 */
@Title("Perecentage based Eigenpair filter")
@Description("Normalizes all eigenpairs, consisting of eigenvalue e and eigenvector v such that <v,v> * e = 1, where <,> is the standard dot product.")
public class NormalizingEigenPairFilter implements EigenPairFilter {
  /**
   * The logger for this class.
   */
  private static final Logging LOG = Logging.getLogger(NormalizingEigenPairFilter.class);

  /**
   * Constructor.
   */
  public NormalizingEigenPairFilter() {
    super();
  }

  @Override
  public FilteredEigenPairs filter(final SortedEigenPairs eigenPairs) {
    // initialize strong and weak eigenpairs
    // all normalized eigenpairs are regarded as strong
    final List<EigenPair> strongEigenPairs = new ArrayList<>();
    final List<EigenPair> weakEigenPairs = new ArrayList<>();
    for(int i = 0; i < eigenPairs.size(); i++) {
      final EigenPair eigenPair = eigenPairs.getEigenPair(i);
      normalizeEigenPair(eigenPair);
      strongEigenPairs.add(eigenPair);
    }
    if(LOG.isDebugging()) {
      final StringBuilder msg = new StringBuilder();
      msg.append("strong EigenPairs = ").append(strongEigenPairs);
      msg.append("\nweak EigenPairs = ").append(weakEigenPairs);
      LOG.debugFine(msg.toString());
    }

    return new FilteredEigenPairs(weakEigenPairs, strongEigenPairs);
  }

  /**
   * Normalizes an eigenpair consisting of eigenvector v and eigenvalue e s.t.
   * <v,v> * e = 1
   * 
   * @param eigenPair the eigenpair to be normalized
   * 
   */
  private void normalizeEigenPair(final EigenPair eigenPair) {
    final Vector eigenvector = eigenPair.getEigenvector();
    final double scaling = 1.0 / Math.sqrt(eigenPair.getEigenvalue()) * eigenvector.euclideanLength();
    eigenvector.timesEquals(scaling);
  }
}