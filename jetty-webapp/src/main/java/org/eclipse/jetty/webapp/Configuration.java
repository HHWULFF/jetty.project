//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.webapp;

import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;

import org.eclipse.jetty.util.TopologicalSort;
import org.eclipse.jetty.util.annotation.Name;

/* ------------------------------------------------------------------------------- */
/** Base Class for WebApplicationContext Configuration.
 * This class can be extended to customize or extend the configuration
 * of the WebApplicationContext. 
 */
public interface Configuration 
{
    public final static String ATTR="org.eclipse.jetty.webapp.configuration";
    
    /* ------------------------------------------------------------------------------- */
    /** Get a class that this class replaces/extends.
     * If this is added to {@link Configurations} collection that already contains a 
     * configuration of the replaced class or that reports to replace the same class, then
     * it is replaced with this instance. 
     * @return The class this Configuration replaces/extends or null if it replaces no other configuration
     */
    public default Class<? extends Configuration> replaces() { return null; } 

    /* ------------------------------------------------------------------------------- */
    /** Get known Configuration Dependencies.
     * @return The names of Configurations that {@link TopologicalSort} must order 
     * before this configuration.
     */
    public default Collection<String> getConfigurationsBeforeThis() { return Collections.emptyList(); }

    /* ------------------------------------------------------------------------------- */
    /** Get known Configuration Dependents.
     * @return The names of Configurations that {@link TopologicalSort} must order 
     * after this configuration.
     */
    public default Collection<String> getConfigurationsAfterThis(){ return Collections.emptyList(); }

    /* ------------------------------------------------------------------------------- */
    /** Get the system classes associated with this Configuration.
     * @return ClasspathPattern of system classes.
     */
    public default ClasspathPattern getSystemClasses() { return new ClasspathPattern();  }

    /* ------------------------------------------------------------------------------- */
    /** Get the system classes associated with this Configuration.
     * @return ClasspathPattern of server classes.
     */
    public default ClasspathPattern getServerClasses() { return new ClasspathPattern();  }
    
    /**
     * @return true if the Configuration should be added to a Context by default 
     */
    public default boolean isEnabledByDefault() { return false; }
    
    /* ------------------------------------------------------------------------------- */
    /** Set up for configuration.
     * <p>
     * Typically this step discovers configuration resources.
     * Calls to preConfigure may alter the Configurations configured on the
     * WebAppContext, so long as configurations prior to this configuration
     * are not altered.
     * @param context The context to configure
     * @throws Exception if unable to pre configure
     */
    public void preConfigure (WebAppContext context) throws Exception;
    
    
    /* ------------------------------------------------------------------------------- */
    /** Configure WebApp.
     * <p>
     * Typically this step applies the discovered configuration resources to
     * either the {@link WebAppContext} or the associated {@link MetaData}.
     * @param context The context to configure
     * @return True if configuration is successful or false if context should not be started.
     * @throws Exception if unable to configure
     */
    public boolean configure (WebAppContext context) throws Exception;
    
    
    /* ------------------------------------------------------------------------------- */
    /** Clear down after configuration.
     * @param context The context to configure
     * @throws Exception if unable to post configure
     */
    public void postConfigure (WebAppContext context) throws Exception;
    
    /* ------------------------------------------------------------------------------- */
    /** DeConfigure WebApp.
     * This method is called to undo all configuration done. This is
     * called to allow the context to work correctly over a stop/start cycle
     * @param context The context to configure
     * @throws Exception if unable to deconfigure
     */
    public void deconfigure (WebAppContext context) throws Exception;

    /* ------------------------------------------------------------------------------- */
    /** Destroy WebApp.
     * This method is called to destroy a webappcontext. It is typically called when a context 
     * is removed from a server handler hierarchy by the deployer.
     * @param context The context to configure
     * @throws Exception if unable to destroy
     */
    public void destroy (WebAppContext context) throws Exception;
    
    
    /**
     * @deprecated Use {@link Configurations}
     */
    public class ClassList extends Configurations
    {
        @Deprecated
        public void addAfter(@Name("afterClass") String afterClass,@Name("configClass")String... configClass)
        {
            if (configClass!=null && afterClass!=null)
            {
                ListIterator<Configuration> iter = _configurations.listIterator();
                while (iter.hasNext())
                {
                    Configuration c=iter.next();
                    
                    if (afterClass.equals(c.getClass().getName()) || afterClass.equals(c.replaces().getName()))
                    {
                        for (String cc: configClass)
                            iter.add(newConfiguration(cc));
                        return;
                    }
                }
            }
            throw new IllegalArgumentException("afterClass '"+afterClass+"' not found in "+this);
        }

        @Deprecated
        public void addBefore(@Name("beforeClass") String beforeClass,@Name("configClass")String... configClass)
        {
            if (configClass!=null && beforeClass!=null)
            {
                ListIterator<Configuration> iter = _configurations.listIterator();
                while (iter.hasNext())
                {
                    Configuration c=iter.next();
                    
                    if (beforeClass.equals(c.getClass().getName()) || beforeClass.equals(c.replaces().getName()))
                    {
                        iter.previous();
                        for (String cc: configClass)
                            iter.add(newConfiguration(cc));
                        return;
                    }
                }
            }
            
            throw new IllegalArgumentException("beforeClass '"+beforeClass+"' not found in "+this);
        }
    }
}
