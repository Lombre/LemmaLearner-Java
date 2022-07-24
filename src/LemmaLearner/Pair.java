/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package LemmaLearner;

 /**
  * 

A convenience class to represent name-value pairs.

  * @since JavaFX 2.0
  */
public class Pair<K, V>{

    /**
     * Key of this Pair.
     */
    private K firstValue;

    /**
     * Gets the key for this pair.
     * @return key for this pair
     */
    public K getFirstValue() { return firstValue; }

    /**
     * Value of this this Pair.
     */
    private V secondValue;

    /**
     * Gets the value for this pair.
     * @return value for this pair
     */
    public V getSecondValue() { return secondValue; }

    /**
     * Creates a new pair
     * @param firstValue The key for this pair
     * @param secondValue The value to use for this pair
     */
    public Pair(K firstValue, V secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    /**
     *

String representation of this
     * Pair.

     *
     *

The default name/value delimiter '=' is always used.

     *
     *  @return String representation of this Pair
     */
    @Override
    public String toString() {
        return firstValue + "=" + secondValue;
    }

    /**
     *

Generate a hash code for this Pair.

     *
     *

The hash code is calculated using both the name and
     * the value of the Pair.

     *
     * @return hash code for this Pair
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (firstValue != null ? firstValue.hashCode() : 0);
        hash = 31 * hash + (secondValue != null ? secondValue.hashCode() : 0);
        return hash;
    }

     /**
      *

Test this Pair for equality with another
      * Object.

      *
      *

If the Object to be tested is not a
      * Pair or is null, then this method
      * returns false.

      *
      *

Two Pairs are considered equal if and only if
      * both the names and values are equal.

      *
      * @param o the Object to test for
      * equality with this Pair
      * @return true if the given Object is
      * equal to this Pair else false
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o instanceof Pair<?, ?>) {
             Pair<?, ?> pair = (Pair<?, ?>) o;
             if (firstValue != null ? !firstValue.equals(pair.firstValue) : pair.firstValue != null) return false;
             if (secondValue != null ? !secondValue.equals(pair.secondValue) : pair.secondValue != null) return false;
             return true;
         }
         return false;
     }
 }
