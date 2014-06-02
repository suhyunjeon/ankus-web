/*
 * Copyright (C) 2011  Flamingo Project (http://www.opencloudengine.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Ankus : Collaborative Filtering based Item Recommendation
 *
 * @author <a href="mailto:fharenheit@gmail.com">Byoung Gon, Kim</a>
 * @author <a href="mailto:myeongha.kim@cloudine.co.kr">Myeong Ha, Kim</a>
 * @see <a href="http://www.openankus.org/display/DOC/Flamingo+Hadoop+Manager+Integration">Ankus Algorithm</a>
 */
Ext.ns('Flamingo.view.designer.shape.ankus');
Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND = function (image, label) {
    Flamingo.ALG_ANKUS_CF_ITEM_RECOMMEND.superclass.call(this, image, label);
    this.SHAPE_ID = 'Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND';
};
Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND.prototype = new OG.shape.ImageShape();
Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND.superclass = OG.shape.ImageShape;
Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND.prototype.constructor = Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND;
Flamingo.ALG_ANKUS_CF_ITEM_RECOMMEND = Flamingo.view.designer.shape.ankus.ALG_ANKUS_CF_ITEM_RECOMMEND;