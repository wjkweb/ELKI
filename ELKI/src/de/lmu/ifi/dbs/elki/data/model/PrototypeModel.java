package de.lmu.ifi.dbs.elki.data.model;

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

import de.lmu.ifi.dbs.elki.result.textwriter.TextWriteable;
import de.lmu.ifi.dbs.elki.result.textwriter.TextWriterStream;

/**
 * Cluster model that stores a prototype for each cluster.
 * 
 * @author Erich Schubert
 * @since 0.3
 */
public class PrototypeModel<V> extends AbstractModel implements TextWriteable {
  /**
   * Cluster prototype
   */
  protected V prototype;

  /**
   * Constructor with prototype
   * 
   * @param prototype Cluster prototype
   */
  public PrototypeModel(V prototype) {
    super();
    this.prototype = prototype;
  }

  /**
   * @return mean
   */
  public V getPrototype() {
    return prototype;
  }

  /**
   * Implementation of {@link TextWriteable} interface.
   */
  @Override
  public void writeToText(TextWriterStream out, String label) {
    super.writeToText(out, label);
    out.commentPrintLn("Cluster " + getPrototypeType() + ": " + prototype.toString());
  }

  /**
   * Type of prototype (Median, Mean, ...) for printing.
   * 
   * @return String name
   */
  protected String getPrototypeType() {
    return "Prototype";
  }
}