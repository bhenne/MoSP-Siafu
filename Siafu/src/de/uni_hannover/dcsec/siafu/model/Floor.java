/*
 * (c) 2012, Distributed Computing & Security Group, Leibniz Universitaet Hannover
 * 
 * This file is part of an extension of the Siafu simulator connect to our
 * work in the field of Mobile Security & Prvacy (MoSP) simulation. 
 * 
 * Siafu as well as its extension is free software; you can redistribute it 
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 * 
 * Siafu as well as its extension is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.uni_hannover.dcsec.siafu.model;

import de.nec.nle.siafu.types.FlatData;
import de.nec.nle.siafu.types.Publishable;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * 
 * @author P. Salomon
 */
public class Floor implements Serializable, Publishable {

	private Rectangle2D rect;
	private int level;

	public Floor(Rectangle2D r, int l) {
		this.rect = r;
		this.level = l;

	}

	public boolean contains(Position pos) {
		return rect.contains(pos.getCol(), pos.getRow());
	}

	public Rectangle2D getRectangle() {
		return rect;
	}

	@Override
	public String toString() {
		return "Floor { level:" + this.level + " minx:" + rect.getMinX()
				+ " miny:" + rect.getMinY() + " w:" + rect.getWidth() + " h:"
				+ rect.getHeight() + " }";
	}

	public int getLevel() {
		return level;
	}

	public float[] positionInOtherFloor(float[] pos, Floor floor) {
		// System.out.println(this);
		// System.out.println(floor);
		float shiftY = pos[0] - (float) rect.getMinY();
		float shiftX = pos[1] - (float) rect.getMinX();
		return new float[] { (float) floor.getRectangle().getMinY() + shiftY,
				(float) floor.getRectangle().getMinX() + shiftX };
	}

	public FlatData flatten() {
		return new FlatData("Level:" + level);
	}

}
