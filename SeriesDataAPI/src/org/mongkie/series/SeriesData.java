/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.series;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SeriesData {

    public static final double[][] EMPTY = new double[1][1];
    private String title;

    static {
        EMPTY[0] = new double[]{0.0D};
    }
    private boolean empty;
    private double[][] matrix;

    public SeriesData(String title, double[][] matrix) {
        this.title = title;
        set(matrix);
    }

    public SeriesData(double[][] matrix) {
        this("UNTITLED", matrix);

    }

    public SeriesData() {
        this.title = "UNTITLED";
        clear();
    }

    public boolean isEmpty() {
        return empty;
    }

    public int getColumnCount() {
        if (empty) {
            return 0;
        }
        return matrix.length;
    }

    public int getRowCount() {
        if (empty) {
            return 0;
        }
        return matrix[0].length;
    }

    public double[][] get() {
        return matrix;
    }

    public final void set(double[][] matrix) {
        empty = false;
        this.matrix = matrix;
    }

    public final void clear() {
        empty = true;
        matrix = EMPTY;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
