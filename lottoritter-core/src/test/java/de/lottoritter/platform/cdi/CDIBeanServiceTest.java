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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Test;

import de.lottoritter.business.validation.control.WeldManager;

/**
 * @author Ulrich Cech
 */
public class CDIBeanServiceTest {

    @BeforeClass
    public static void setup() {
        WeldManager.getWeld();
    }

    @Test
    public void testGetInstance() throws Exception {
        final CDIBeanService cut = CDIBeanService.getInstance();
        assertThat(cut, CoreMatchers.notNullValue());
    }

    @Test
    public void getCDIBean() throws Exception {
        final CDIBeanService cut = CDIBeanService.getInstance();
        final CDITestBean cdiTestBean = cut.getCDIBean(CDITestBean.class);
        final CDITestBean cdiTestBean2 = cut.getCDIBean(CDITestBean.class);
        assertThat(cdiTestBean, notNullValue());
        assertThat(cdiTestBean == cdiTestBean2, is(true));
        assertThat(cdiTestBean.getName(), is("I am a singleton bean"));
        assertThat(cdiTestBean.getCdiTestInjected(), notNullValue());
        assertThat(cdiTestBean.getCdiTestInjected().getLabel(), is("I am a manuel injected bean"));

    }

    @Test
    public void getCDIBeanByName() throws Exception {
        final CDIBeanService cut = CDIBeanService.getInstance();
        final CDITestBean cdiTestBean = cut.getCDIBeanByName("cDITestBean", CDITestBean.class);
        assertThat(cdiTestBean, notNullValue());
        assertThat(cdiTestBean.getName(), is("I am a singleton bean"));
    }

    @Test
    public void injectManual() throws Exception {
        final CDIBeanService cut = CDIBeanService.getInstance();
        NoCdiBean noCdiBean = new NoCdiBean();
        assertThat(noCdiBean.getCdiTestInjected(), nullValue());
        cut.injectManual(noCdiBean);
        assertThat(noCdiBean.getCdiTestInjected(), notNullValue());
        assertThat(noCdiBean.getCdiTestInjected().getLabel(), is("I am a manuel injected bean"));
    }

    @Named
    @Singleton
    static class CDITestBean {

        @Inject
        CDITestInjected cdiTestInjected;

        String getName() {
            return "I am a singleton bean";
        }

        CDITestInjected getCdiTestInjected() {
            return cdiTestInjected;
        }
    }

    @Named
    @Singleton
    static class CDITestInjected {

        String getLabel() {
            return "I am a manuel injected bean";
        }
    }

    @Stateless
    static class NoCdiBean {

        @Inject
        CDITestInjected testInjected;

        CDITestInjected getCdiTestInjected() {
            return testInjected;
        }
    }

}