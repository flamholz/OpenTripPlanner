/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opentripplanner.common.geometry.GeometryUtils;
import org.opentripplanner.routing.core.IntersectionTraversalCostModel;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.PlainStreetEdge;
import org.opentripplanner.routing.edgetype.StreetTraversalPermission;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.location.StreetLocation;
import org.opentripplanner.routing.vertextype.IntersectionVertex;
import org.opentripplanner.routing.vertextype.StreetVertex;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

public class StateTest {
    
    private Graph _graph;
    private IntersectionVertex v1, v2, v3, v4;
    private PlainStreetEdge e1, e1Reverse, e2, e3;
    
    @Before
    public void setUp() throws Exception {
        _graph = new Graph();

        // Graph for a fictional grid city with turn restrictions
        v1 = vertex("maple_1st", 2.0, 2.0);
        v2 = vertex("maple_2nd", 1.0, 2.0);
        v3 = vertex("maple_3rd", 0.0, 2.0);
        v4 = vertex("maple_4th", -1.0, 2.0);
        
        e1 = edge(v1, v2, 1.0, StreetTraversalPermission.ALL);
        e1Reverse = edge(v2, v1, 1.0, StreetTraversalPermission.ALL);
        e2 = edge(v2, v3, 1.0, StreetTraversalPermission.ALL);
        e3 = edge(v3, v4, 1.0, StreetTraversalPermission.ALL);
    }

    public void checkTimes(TraverseMode mode) {
        RoutingRequest options = new RoutingRequest();
        options.setMode(mode);
        options.setRoutingContext(_graph, v1, v2);
        
        // Traverse both the partial and parent edges.
        State s0 = new State(options);
        State s1 = e1.traverse(s0);
        
        assertEquals(options.dateTime, s0.getTimeSeconds());
        assertEquals(s0.getTimeSeconds() + s1.getElapsedTimeSeconds(),
                s1.getTimeSeconds());
        assertEquals(s0.getTimeInMillis() / 1000, s0.getTimeSeconds());
        assertEquals(s1.getTimeInMillis() / 1000, s1.getTimeSeconds());
        assertEquals(s1.getElapsedTimeSeconds(), s1.getTimeDeltaSeconds());
        assertEquals(s1.getElapsedTimeSeconds(), s1.getAbsTimeDeltaSeconds());        
    }
    
    @Test
    public void testTimesWalk() {
        checkTimes(TraverseMode.WALK);
    }
    
    @Test
    public void testTimesCar() {
        checkTimes(TraverseMode.CAR);
    }

    @Test
    public void testTimesCustomMotorVehicle() {
        checkTimes(TraverseMode.CUSTOM_MOTOR_VEHICLE);
    }
    
    /****
     * Private Methods
     ****/

    private IntersectionVertex vertex(String label, double lat, double lon) {
        IntersectionVertex v = new IntersectionVertex(_graph, label, lat, lon);
        return v;
    }

    /**
     * Create an edge. If twoWay, create two edges (back and forth).
     * 
     * @param vA
     * @param vB
     * @param length
     * @param back true if this is a reverse edge
     */
    private PlainStreetEdge edge(StreetVertex vA, StreetVertex vB, double length,
            StreetTraversalPermission perm) {
        String labelA = vA.getLabel();
        String labelB = vB.getLabel();
        String name = String.format("%s_%s", labelA, labelB);
        Coordinate[] coords = new Coordinate[2];
        coords[0] = vA.getCoordinate();
        coords[1] = vB.getCoordinate();
        LineString geom = GeometryUtils.getGeometryFactory().createLineString(coords);

        return new PlainStreetEdge(vA, vB, geom, name, length, perm, false, 5.0f);
    }

}
