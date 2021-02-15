/*
 * Copyright (c) 2005, 2020, Oracle and/or its affiliates. All rights reserved.
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

package jdk.internal.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility methods for class loader.
 */
public class ClassLoaderUtil {
    private ClassLoaderUtil() {}

    private static Enumeration<URL> getResources(ClassLoader classLoader, String name) throws IOException {
        if (classLoader == null) {
            return BootLoader.findResources(name);
        }
        return classLoader.getResources(name);
    }

    private static Set<URL> getResourcesAsSet(ClassLoader classLoader, String name) throws IOException {
        Enumeration<URL> urls = getResources(classLoader, name);
        Set<URL> set = new LinkedHashSet<>();
        urls.asIterator().forEachRemaining(set::add);
        return set;
    }

    /**
     * Returns an Iterator to iterate over the resources of the given name
     * in any of the modules defined to given class loader.
     */
    public static Enumeration<URL> findResources(ClassLoader classLoader, String name) throws IOException {
        if (classLoader == null) {
            return BootLoader.findResources(name);
        } else if (classLoader instanceof BuiltinClassLoader) {
            return ((BuiltinClassLoader) classLoader).findResources(name);
        } else if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).findResources(name);
        }
        Set<URL> resources = getResourcesAsSet(classLoader, name);
        Set<URL> parentResources = getResourcesAsSet(classLoader.getParent(), name);
        if (resources.size() == parentResources.size()) {
            return Collections.emptyEnumeration();
        }
        resources.removeAll(parentResources);
        Iterator<URL> iterator = resources.iterator();
        return new Enumeration<URL>() {
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }
}
