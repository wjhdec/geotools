/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2018, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.mbstyle.function;

import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.capability.FunctionNameImpl;
import org.json.simple.JSONArray;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Takes an object as an argument and returns the color value if possible.
 * Evaluates string in the formats of:
 * "rgb(int, int, int)"
 * "rgba(int, int, int, double)"
 *
 * And in arrays of 3 and 4 numbers for rgb and rgba colors:
 * [int, int, int]
 * [int, int, int, double]
 *
 * The integer values should be between 0-255, for red, green, and blue color values.
 * The double value should be between 0-1, and is converted to a 0-255 alpha value.
 */
class ToColorFunction extends FunctionExpressionImpl {

    public static FunctionName NAME = new FunctionNameImpl("toColor");

    ToColorFunction() {
        super(NAME);
    }

    /**
     * @see org.geotools.filter.FunctionExpressionImpl#setParameters(java.util.List)
     */
    @Override
    public void setParameters(List<Expression> params) {
        // set the parameters
        this.params = new ArrayList<>(params);
    }

    @Override
    public Object evaluate(Object feature) {
        Color c;
        for (Integer i = 1; i <= this.params.size() - 1; i++) {
            Object evaluation = this.params.get(i).evaluate(feature);
            if (evaluation instanceof Color) {
                return evaluation;
            }
            if (evaluation instanceof JSONArray) {
                JSONArray je = (JSONArray) evaluation;
                if (je.size() == 3 || je.size() == 4) {
                    Long r = (Long) je.get(0);
                    Long g = (Long) je.get(1);
                    Long b = (Long) je.get(2);
                    if (je.size() == 3) {
                        try {
                            c = new Color(r.intValue(), g.intValue(), b.intValue());
                            return c;
                        } catch (Exception e) {
                        }
                    }
                    if (je.size() == 4) {
                        Double a = (Double) je.get(3);
                        Integer alpha = ((Long) Math.round(a * 255)).intValue();
                        try {
                            c = new Color(r.intValue(), g.intValue(), b.intValue(), alpha);
                            return c;
                        } catch (Exception e) {
                        }
                    }
                }
            }
            if (evaluation instanceof String) {
                if (((String) evaluation).startsWith("#")) {
                    if (((String) evaluation).length() == 7) {
                        try {
                            c = Color.decode((String) evaluation);
                            return c;
                        } catch (Exception e) {
                        }

                    }
                    if (((String) evaluation).length() == 4) {
                        String[] split = ((String) evaluation).split("");
                        StringBuilder builder = new StringBuilder();
                        builder.append(split[0]);
                        for (Integer j = 1; j < split.length; j++) {
                            builder.append(split[j]).append(split[j]);
                        }
                        String cstring = builder.toString();
                        try {
                            c = Color.decode(cstring);
                            return c;
                        } catch (Exception e) {
                        }
                    }
                }
                if (((String) evaluation).startsWith("rgb(") ||
                        ((String) evaluation).startsWith("rgba(") &&
                                ((String) evaluation).endsWith(")")){
                    String[] split = ((String) evaluation).split("\\(");
                    String[] split2 = split[1].split("\\)");
                    String[] splitfinal = split2[0].split(",");
                    if (splitfinal.length == 3) {
                        try {
                            c = new Color(Integer.valueOf(splitfinal[0]), Integer.valueOf(splitfinal[1]),
                                    Integer.valueOf(splitfinal[2]));
                            return c;
                        } catch (Exception e) {
                        }
                    }
                    if (splitfinal.length == 4){
                        try{
                            Double alpha = Double.valueOf(splitfinal[3]);
                            Long converted = (Math.round(alpha * 255));
                            c = new Color(Integer.valueOf(splitfinal[0]), Integer.valueOf(splitfinal[1]),
                                    Integer.valueOf(splitfinal[2]), Integer.valueOf(converted.intValue()));
                            return c;
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
        throw new IllegalArgumentException("No arguments provided to \"mbToColor\" can be converted to a Color value");
    }
}