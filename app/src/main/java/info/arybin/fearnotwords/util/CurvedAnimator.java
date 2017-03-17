/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.arybin.fearnotwords.util;

import java.util.ArrayList;
import java.util.List;

public class CurvedAnimator {

    private List<SimplePoint> points = new ArrayList<>();

    public CurvedAnimator(float fromX, float fromY, float toX, float toY) {
        points.add(new SimplePoint(fromX, fromY));
        points.add(new SimplePoint(toX, toY));
    }

    public Object[] getPoints() {
        return points.toArray();
    }
}