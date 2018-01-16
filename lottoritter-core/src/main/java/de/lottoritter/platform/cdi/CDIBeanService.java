/*
 * Copyright 2017 Ulrich Cech & Christopher Schmidt
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
package de.lottoritter.platform.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.logging.Logger;

/**
 * Service for getting CDI-Beans programmatically.
 *
 * @author Ulrich Cech
 */
@Named
public class CDIBeanService {

    private static final Logger LOGGER = Logger.getLogger(CDIBeanService.class.getName());

    private static CDIBeanService INSTANCE = new CDIBeanService();

    private CDIBeanService() {
        super();
    }

    public static CDIBeanService getInstance() {
        return INSTANCE;
    }

    public <T> T getCDIBean(final Class<T> clazz) {
        BeanManager manager = getBeanManager();
        if (manager != null) {
            Bean<T> bean = (Bean<T>) manager.getBeans(clazz).iterator().next();
            CreationalContext<T> ctx = manager.createCreationalContext(bean);
            return (T) manager.getReference(bean, clazz, ctx);
        } else {
            try {
                return clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.warning("Could not create instance of class <" + clazz + ">.");
            }
            return null;
        }
    }

    public <T> T getCDIBeanByName(final String name, Class<T> clazz) {
        BeanManager bm = getBeanManager();
        Bean<T> bean = (Bean<T>) bm.getBeans(name).iterator().next();
        CreationalContext<T> ctx = bm.createCreationalContext(bean);
        return (T) bm.getReference(bean, clazz, ctx);
    }

    private BeanManager getBeanManager() {
        CDI<Object> containerAccessor;
        try {
            containerAccessor = CDI.current();
            if (containerAccessor != null) {
                return containerAccessor.getBeanManager();
            } else {
                try {
                    InitialContext initialContext = new InitialContext();
                    return (BeanManager) initialContext.lookup("java:comp/BeanManager");
                } catch (NamingException e) {
                    return null;
                }
            }
        } catch (IllegalStateException ignore) {}
        return null;
    }

    /**
     * Used for unmanaged objects
     */
    public void injectManual(Object obj) {
        BeanManager beanManager = getBeanManager();
        AnnotatedType type = (AnnotatedType) beanManager.createAnnotatedType(obj.getClass());
        InjectionTarget it = beanManager.createInjectionTarget(type);
        CreationalContext instanceContext = beanManager.createCreationalContext(null);
        it.inject(obj, instanceContext); // calls the initializer methods and performs field injection
        it.postConstruct(obj); // finally call the @PostConstruct-annotated method
    }

}
