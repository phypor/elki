/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2018
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.math.statistics.dependence;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.DoubleArrayAdapter;

/**
 * Validate correlation by comparing to the results of R.
 * 
 * @author Erich Schubert
 * @since 0.4.0
 */
public class CorrelationDependenceMeasureTest {
  double[][] data = { //
      { 1, 2, 3, 4 }, //
      { 1, 3, 5, 7 }, //
      { 4, 3, 2, 1 }, //
      { 1, 4, 2, 3 }, //
      { 1, 0, 0, 1 }, //
  };

  double[][] R = { //
      { 1. }, //
      { 1., 1. }, //
      { -1., -1., 1. }, //
      { 0.4, 0.4, -0.4, 1. }, //
      { 0., 0., 0., -0.4472136, 1 }, //
  };

  @Test
  public void testBasic() {
    DependenceMeasure cor = CorrelationDependenceMeasure.STATIC;
    // This will become better with data size.
    AbstractDependenceMeasureTest.checkPerfectLinear(cor, 1000, 1, -1, 1e-14);
    AbstractDependenceMeasureTest.checkUniform(cor, 1000, 1.0, 1e-15, 0, 0.03);
  }

  @Test
  public void testR() {
    DependenceMeasure cor = CorrelationDependenceMeasure.STATIC;
    // Single computations
    for(int i = 0; i < data.length; i++) {
      for(int j = 0; j <= i; j++) {
        double co = cor.dependence(data[i], data[j]);
        assertEquals("Cor does not match for " + i + "," + j, R[i][j], co, 1e-7);
      }
    }
    // Bulk computation
    double[] mat = cor.dependence(DoubleArrayAdapter.STATIC, Arrays.asList(data));
    for(int i = 0, c = 0; i < data.length; i++) {
      for(int j = 0; j < i; j++) {
        double co = mat[c++];
        assertEquals("Cor does not match for " + i + "," + j, R[i][j], co, 1e-7);
      }
    }
  }
}
