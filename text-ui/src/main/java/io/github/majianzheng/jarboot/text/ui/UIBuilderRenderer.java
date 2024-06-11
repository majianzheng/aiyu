/*
 * Copyright (C) 2012 eXo Platform SAS.
 *  
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.github.majianzheng.jarboot.text.ui;

import io.github.majianzheng.jarboot.text.LineRenderer;
import io.github.majianzheng.jarboot.text.Renderer;

import java.util.Iterator;
import java.util.LinkedList;

public class UIBuilderRenderer extends Renderer<UIBuilder> {

  @Override
  public Class<UIBuilder> getType() {
    return UIBuilder.class;
  }

  @Override
  public LineRenderer renderer(Iterator<UIBuilder> stream) {
    LinkedList<LineRenderer> renderers = new LinkedList<LineRenderer>();
    while (stream.hasNext()) {
      for (Element element : stream.next().getElements()) {
        renderers.add(element.renderer());
      }
    }
    return LineRenderer.vertical(renderers);
  }
}
