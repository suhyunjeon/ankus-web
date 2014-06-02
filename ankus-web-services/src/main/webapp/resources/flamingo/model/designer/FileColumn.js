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

Ext.define('Flamingo.model.designer.FileColumn', {
    extend: 'Ext.data.Model',
    fields: ['name', 'length', 'modificationTime', 'permission', 'group', 'owner', 'replication', 'blocksize'],
    proxy: {
        type: 'ajax',
        url: CONSTANTS.REST_HDFS_GET_FILE,
        headers: {
            'Accept': 'application/json'
        },
        reader: {
            type: 'json',
            root: 'file'
        }
    }
});