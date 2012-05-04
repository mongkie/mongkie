/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.validation.api;

import org.junit.Test;
import org.netbeans.validation.api.ui.GroupValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationUI;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class ValidationGroupTest {

    @Test public void testGroupValidatorFunctionality() {
        /*
          
           Create a ValidationGroup with a GroupValidator and add a couple of
           ValidationListeners.
          
	   Configuration variants: GroupValidator is to the ValidationGroup added with 

	   * shallShowProblemInChildrenUIs set to true

	   * shallShowProblemInChildrenUIs set to false


	   Revalidation variants:

            * group.performValidation() is called

            * one of the added ValidationListeners triggers validation
          

           Expected behaviour:

            * If any of the added children ValidationListeners has a
              FATAL problem, then the GroupValidator should not be
              invoked at all (that's how it's currently
              implemented). End of test.

	    * Else, the GroupValidator *should* be invoked. 
	    
	       * If the GroupValidator does *not* have a problem /strictly/ 
	         worse than the the worst of the added ValidationListeners, then
		 the GroupValidator will *not* have the leading problem (the worst 
		 of the ValidatationListeners will). End of test.
          
   	       * Else (If the GroupValidator *has* a problem /strictly/ worse than
	         the worst of the added ValidationListeners), then the GroupValidator
	         has the lead problem.
		 
		 * If shallShowProblemInChildrenUIs is true, then this lead problem 
		   should show in the ValidationUI:s owned by all children

		 * Else, it shouldn't. 

          
         */

      for( int index = 0; index < 2; ++index ) {
        final boolean showGroupProblemInChildrenUIs = index==0 ? false : true ;
        MyGroupValidator gval = new MyGroupValidator(showGroupProblemInChildrenUIs);

        gval.simulatedProblem = Severity.WARNING;
        UI groupUI = new UI(0);
        ValidationGroup g = new ValidationGroup(gval, groupUI) {} ;
        groupUI.assertShowProblemHasNotBeenCalled();
        groupUI.assertNotProblem();
        // Just adding a UI should not trigger validation. Updating add UI with whatever problem there aleady is is enough.
        gval.assertNotValidated();

        g.performValidation();
        /////////////////////////////////////////
        gval.assertValidated();
        groupUI.assertShowProblemHasBeenCalled();
        groupUI.assertProblem(groupValidationProblemString);

        UI vl1UI = new UI(1);
        VL vl1 = VL.create(vl1UI, 1);
        /////////////////////////////////////////
        vl1.assertNotValidated();
        vl1UI.assertShowProblemHasNotBeenCalled();
        vl1UI.assertNotProblem();

        vl1.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertPassed();
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertNotProblem();
        gval.assertNotValidated();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem(groupValidationProblemString);

        
        g.addItem(vl1, false);
        /////////////////////////////////////////
        vl1.assertNotValidated();
        gval.assertValidated(); // The add should have triggered group validation
        if(showGroupProblemInChildrenUIs){
            // The problem in MyGroupValidator should also show up in group vl1UI
            vl1UI.assertShowProblemHasBeenCalled();
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertShowProblemHasNotBeenCalled();
            vl1UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled();
        groupUI.assertProblem(groupValidationProblemString);

        g.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertPassed();
        gval.assertValidated();
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        if(showGroupProblemInChildrenUIs){
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem(groupValidationProblemString);


        vl1.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertPassed();
        gval.assertValidated(); // The add should have triggered group validation
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        if(showGroupProblemInChildrenUIs){
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem(groupValidationProblemString);
        

        UI vl2UI = new UI(2);
        VL vl2 = VL.create(vl2UI, 2);
        /////////////////////////////////////////
        vl2.assertNotValidated();
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        
        
        g.addItem(vl2, false);
        /////////////////////////////////////////
        vl1.assertNotValidated();
        vl2.assertNotValidated();
        vl1UI.assertShowProblemHasNotBeenCalled();
        gval.assertValidated(); // The add should have triggered group validation
        if(showGroupProblemInChildrenUIs){
            vl1UI.assertProblem(groupValidationProblemString);
            vl2UI.assertShowProblemHasBeenCalled();
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertNotProblem();
            vl2UI.assertShowProblemHasNotBeenCalled();
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new though
        groupUI.assertProblem(groupValidationProblemString);
        /////////////////////////////////////////


        vl1.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertPassed();
        vl2.assertNotValidated();
        gval.assertValidated(); // Group validation should have been triggered
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl2UI.assertShowProblemHasNotBeenCalled();
        if(showGroupProblemInChildrenUIs){
            vl1UI.assertProblem(groupValidationProblemString);
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertNotProblem();
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem(groupValidationProblemString);


        g.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertPassed();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertValidated(); // Group validation should have been triggered
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl2UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        if(showGroupProblemInChildrenUIs){
            vl1UI.assertProblem(groupValidationProblemString);
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertNotProblem();
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem(groupValidationProblemString);


        vl2.performValidation();
        /////////////////////////////////////////
        vl1.assertNotValidated();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertValidated(); // Group validation should have been triggered
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl2UI.assertShowProblemHasNotBeenCalled();
        if(showGroupProblemInChildrenUIs){
            vl1UI.assertProblem(groupValidationProblemString);
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertNotProblem();
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem(groupValidationProblemString);



        vl1.simulateProblem ("fail");
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertFatal();
        vl2.assertNotValidated();
        gval.assertNotValidated(); // The Problem in vl1 is fatal, so there was no need to perform group validation
        vl1UI.assertShowProblemHasBeenCalled();
        vl1UI.assertProblem("fail");
        if(showGroupProblemInChildrenUIs){
            // The group-problem was previously leading and therefore showing in this component.
            // But now that our sibling has the lead problem, we should no longer show any problem at all here.
            vl2UI.assertShowProblemHasBeenCalled();
        } else {
            vl2UI.assertShowProblemHasNotBeenCalled();
        }
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasBeenCalled();
        groupUI.assertProblem("fail");


        vl1.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertFatal();
        vl2.assertNotValidated();
        gval.assertNotValidated(); // The Problem in vl1 is fatal, so there was no need to perform group validation
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertProblem("fail");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem("fail");


        g.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertFatal();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertNotValidated(); // The Problem in vl1 is fatal, so there was no need to perform group validation
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertProblem("fail");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem("fail");


        vl2.performValidation();
        /////////////////////////////////////////
        vl1.assertNotValidated();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertNotValidated(); // The Problem in vl1 is fatal, so there was no need to perform group validation
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertProblem("fail");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem("fail");

        

        vl1.simulateProblem ("warning");
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertWarning();
        gval.assertValidated(); // The Problem in vl1 was not fatal, so group validation should have been performed
        vl2.assertNotValidated();
        groupUI.assertShowProblemHasBeenCalled();
        groupUI.assertProblem("warning");
        vl1UI.assertShowProblemHasBeenCalled();
        vl1UI.assertProblem("warning");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        /////////////////////////////////////////

        vl1.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertWarning();
        vl2.assertNotValidated();
        gval.assertValidated(); // The Problem in vl1 is not fatal, so group validation should have been perform as well
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertProblem("warning");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem("warning");


        g.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertWarning();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertValidated(); // The Problem in vl1 is fatal, so group validation should have been perform as well
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertProblem("warning");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem("warning");


        vl2.performValidation();
        /////////////////////////////////////////
        vl1.assertNotValidated();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertValidated(); // The Problem in vl1 is not fatal, so group validation should have been perform as well
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        vl1UI.assertProblem("warning");
        vl2UI.assertShowProblemHasNotBeenCalled();
        vl2UI.assertNotProblem();
        groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
        groupUI.assertProblem("warning");


        vl1.simulateProblem ("info"); 
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertInfo();
        vl2.assertNotValidated();
        gval.assertValidated(); // The Problem in vl1 was not fatal, so group validation should have been performed
        vl1UI.assertShowProblemHasBeenCalled();
        // The group problem will now be *strictly* worst, so if showGroupProblemInChildrenUIs is true then
        // the vl1UI should contain the group problem. (Otherwise it should still just contain its own problem)
        if( showGroupProblemInChildrenUIs ){
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertProblem("info");
        }
        if( showGroupProblemInChildrenUIs ){
            vl2UI.assertShowProblemHasBeenCalled();
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl2UI.assertShowProblemHasNotBeenCalled();
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasBeenCalled();
        groupUI.assertProblem(groupValidationProblemString);


        vl1.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertInfo();
        vl2.assertNotValidated();
        gval.assertValidated(); // The Problem in vl1 was not fatal, so group validation should have been performed
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        // The group problem will now be *strictly* worst, so if showGroupProblemInChildrenUIs is true then
        // the vl1UI should contain the group problem. (Otherwise it should still just contain its own problem)
        if( showGroupProblemInChildrenUIs ){
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertProblem("info");
        }
        vl2UI.assertShowProblemHasNotBeenCalled();
        if( showGroupProblemInChildrenUIs ){
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled();
        groupUI.assertProblem(groupValidationProblemString);


        g.performValidation();
        /////////////////////////////////////////
        vl1.assertValidated();
        vl1.assertInfo();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertValidated(); // The Problem in vl1 was not fatal, so group validation should have been performed
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        // The group problem will now be *strictly* worst, so if showGroupProblemInChildrenUIs is true then
        // the vl1UI should contain the group problem. (Otherwise it should still just contain its own problem)
        if( showGroupProblemInChildrenUIs ){
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertProblem("info");
        }
        vl2UI.assertShowProblemHasNotBeenCalled();
        if( showGroupProblemInChildrenUIs ){
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled();
        groupUI.assertProblem(groupValidationProblemString);

        vl2.performValidation();
        /////////////////////////////////////////
        vl1.assertNotValidated();
        vl2.assertValidated();
        vl2.assertPassed();
        gval.assertValidated(); // The Problem in vl1 was not fatal, so group validation should have been performed
        vl1UI.assertShowProblemHasNotBeenCalled(); // Nothing new
        // The group problem will now be *strictly* worst, so if showGroupProblemInChildrenUIs is true then
        // the vl1UI should contain the group problem. (Otherwise it should still just contain its own problem)
        if( showGroupProblemInChildrenUIs ){
            vl1UI.assertProblem(groupValidationProblemString);
        } else {
            vl1UI.assertProblem("info");
        }
        vl2UI.assertShowProblemHasNotBeenCalled();
        if( showGroupProblemInChildrenUIs ){
            vl2UI.assertProblem(groupValidationProblemString);
        } else {
            vl2UI.assertNotProblem();
        }
        groupUI.assertShowProblemHasNotBeenCalled();
        groupUI.assertProblem(groupValidationProblemString);
        /////////////////////////////////////////
      } // for 

    }


    @Test public void testSuspendAndPerformValidation(){
      for( int index = 0; index < 2; ++index ) {
        final boolean showGroupProblemInChildrenUIs = index==0 ? false : true ;
        {
            MyGroupValidator gval = new MyGroupValidator(showGroupProblemInChildrenUIs);
            gval.simulatedProblem = Severity.WARNING;
            UI groupUI = new UI(0);
            ValidationGroup g = new ValidationGroup(gval, groupUI) {} ;
            UI vl1UI = new UI(1);
            VL vl1 = VL.create(vl1UI, 1);
            g.addItem(vl1, false);
            gval.assertValidated();
            groupUI.assertShowProblemHasBeenCalled();
            UI vl2UI = new UI(2);
            VL vl2 = VL.create(vl2UI, 2);
            g.addItem(vl2, false);
            gval.assertValidated();
            groupUI.assertShowProblemHasNotBeenCalled();
        }
        {
            final MyGroupValidator gval = new MyGroupValidator(showGroupProblemInChildrenUIs);
            gval.simulatedProblem = Severity.WARNING;
            final UI groupUI = new UI(0);
            final ValidationGroup g = new ValidationGroup(gval, groupUI) {} ;
            final UI vl1UI = new UI(1);
            final VL vl1 = VL.create(vl1UI, 1);
            final UI vl2UI = new UI(2);
            final VL vl2 = VL.create(vl2UI, 2);

            ///////////////////////////////////////////////////
            g.runWithValidationSuspended(new Runnable() {
                public void run() {
                    g.performValidation();
                    gval.assertNotValidated();
                    groupUI.assertShowProblemHasNotBeenCalled();
                    g.addItem(vl1, false);
                    gval.assertNotValidated();
                    groupUI.assertShowProblemHasNotBeenCalled();
                    g.addItem(vl2, false);
                    gval.assertNotValidated();
                    groupUI.assertShowProblemHasNotBeenCalled();
                    g.performValidation();
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    groupUI.assertShowProblemHasNotBeenCalled();
                }});
            vl1.assertValidated();
            vl2.assertValidated();
            gval.assertValidated();
            if(showGroupProblemInChildrenUIs){
                vl1UI.assertShowProblemHasBeenCalled();
                vl1UI.assertProblem(groupValidationProblemString);
                vl2UI.assertShowProblemHasBeenCalled();
                vl2UI.assertProblem(groupValidationProblemString);
            } else {
                vl1UI.assertShowProblemHasNotBeenCalled();
                vl2UI.assertShowProblemHasNotBeenCalled();
            }
            groupUI.assertShowProblemHasBeenCalled();
            groupUI.assertProblem(groupValidationProblemString);


            ///////////////////////////////////////////////////
            g.runWithValidationSuspended(new Runnable() {
                public void run() {
                    vl1.simulateProblem("info");
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    groupUI.assertShowProblemHasNotBeenCalled();
                }});
            vl1.assertValidated();
            vl2.assertValidated();
            gval.assertValidated();
            if(showGroupProblemInChildrenUIs){
                vl1UI.assertShowProblemHasNotBeenCalled();
                vl1UI.assertProblem(groupValidationProblemString);
                vl2UI.assertShowProblemHasNotBeenCalled();
                vl2UI.assertProblem(groupValidationProblemString);
            } else {
                vl1UI.assertShowProblemHasBeenCalled();
                vl1UI.assertProblem("info");
                vl2UI.assertShowProblemHasNotBeenCalled();
            }
            groupUI.assertShowProblemHasNotBeenCalled(); // Nothing new
            groupUI.assertProblem(groupValidationProblemString);
            
            ///////////////////////////////////////////////////
            g.runWithValidationSuspended(new Runnable() {
                public void run() {
                    vl2.simulateProblem("fatal");
                    g.performValidation();
                    vl1.performValidation();
                    vl2.performValidation();
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    groupUI.assertShowProblemHasNotBeenCalled();
                }});
            vl1.assertValidated();
            vl2.assertValidated();
            gval.assertNotValidated(); // vl2 has a fatal problem so it's no point revalidating the group validator
            if(showGroupProblemInChildrenUIs){
                vl1UI.assertShowProblemHasBeenCalled(); // The group problem is no longer leading (because vl2 has fatal), so vl1UI should not show it any more but instead th info problem that vl1 has.
            } else {
                vl1UI.assertShowProblemHasNotBeenCalled();
            }
            vl1UI.assertProblem("info");
            vl2UI.assertShowProblemHasBeenCalled();
            vl2UI.assertProblem("fatal");
            groupUI.assertShowProblemHasBeenCalled();
            groupUI.assertProblem("fatal");
            

            ///////////////////////////////////////////////////
            g.runWithValidationSuspended(new Runnable() {
                public void run() {
                    vl2.simulateProblem("fatal");
                    vl1.simulateProblem("warning"); // Nothing should happen even though many problems appear and we try to provoke validation etc
                    vl1.simulateProblem("info");
                    vl1.simulateProblem("fatal 1");
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    groupUI.assertShowProblemHasNotBeenCalled();
                }});
            vl1.assertValidated();
            vl2.assertValidated();
            gval.assertNotValidated(); // vl1 and vl2 has fatal problems so it's no point revalidating the group validator
            vl1UI.assertShowProblemHasBeenCalled();
            vl1UI.assertProblem("fatal 1");
            vl2UI.assertShowProblemHasNotBeenCalled(); // Nothing new in this UI
            vl2UI.assertProblem("fatal");
            // groupUI.assertShowProblemHasBeenCalled();
            // ... hmmm, no, vl2 will still have the lead problem even though vl1 was the last one interacted with. Reason: the vl1 interaction occured under suspension, and that doesn't count (i.e that does not make vl1 the most recent interaction)
            // groupUI.assertProblem("fatal 1");
            groupUI.assertShowProblemHasNotBeenCalled();
            groupUI.assertProblem("fatal");


            ///////////////////////////////////////////////////
            // The following suspends one of the individual ValidationListeners, but not the ValidationGroup, and then
            vl1.runWithValidationSuspended(new Runnable() {
                public void run() {
                    ////////////////////////////////////////////
                    vl2.simulateProblem("fatal 2");
                    vl1.assertNotValidated();
                    vl2.assertValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasBeenCalled();
                    vl2UI.assertProblem("fatal 2");
                    groupUI.assertShowProblemHasBeenCalled();
                    groupUI.assertProblem("fatal 2");
                    vl1.simulateProblem("warning");
                    vl1.simulateProblem("info");
                    vl1.simulateProblem("fatal 1");
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    groupUI.assertShowProblemHasNotBeenCalled();
                    
                    ////////////////////////////////////////////
                    vl2.simulateProblem("info");
                    vl1.assertNotValidated();
                    vl2.assertValidated();
                    gval.assertNotValidated(); // vl1 has a fatal problem, so group validation would be superfluous
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasBeenCalled();
                    vl2UI.assertProblem("info");
                    groupUI.assertShowProblemHasBeenCalled();
                    groupUI.assertProblem("fatal 1");

                    
                    ////////////////////////////////////////////
                    vl1.simulateProblem("info");
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated(); // vl1 has a fatal problem, so group validation would be superfluous
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertProblem("info");
                    groupUI.assertShowProblemHasNotBeenCalled();
                    // the group UI will not be updated automatically because vl1
                    // is suspended. (So it'll be in an inconsistent state, but
                    // that should be ok since validation is suspended in vl1.)
                    groupUI.assertProblem("fatal 1");
                    ////////////////////////////////////////////
                }}); // vl1.performValidation is called now, under the hood.
                vl1.assertValidated();
                vl2.assertNotValidated();
                gval.assertValidated();
                vl1UI.assertShowProblemHasBeenCalled();
                if(showGroupProblemInChildrenUIs){
                    vl1UI.assertProblem(groupValidationProblemString);
                    vl2UI.assertShowProblemHasBeenCalled();
                    vl2UI.assertProblem(groupValidationProblemString);
                } else {
                    vl1UI.assertProblem("info");
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertProblem("info");
                }
                groupUI.assertShowProblemHasBeenCalled();
                groupUI.assertProblem(groupValidationProblemString); // the group UI will not be updated automatically because vl1 is suspended. SO it'll be in an inconsistent state


            ///////////////////////////////////////////////////
            g.runWithValidationSuspended(new Runnable() {
                public void run() {
                    g.runWithValidationSuspended(new Runnable() {
                        public void run() {
                            vl2.simulateProblem("fatal 2");
                            vl1.assertNotValidated();
                            vl2.assertNotValidated();
                            gval.assertNotValidated();
                            vl1UI.assertShowProblemHasNotBeenCalled();
                            vl2UI.assertShowProblemHasNotBeenCalled();
                            groupUI.assertShowProblemHasNotBeenCalled();
                    }});
                    vl1.assertNotValidated();
                    vl2.assertNotValidated();
                    gval.assertNotValidated();
                    vl1UI.assertShowProblemHasNotBeenCalled();
                    vl2UI.assertShowProblemHasNotBeenCalled();
                    groupUI.assertShowProblemHasNotBeenCalled();
                }});
            vl1.assertValidated();
            vl2.assertValidated();
            gval.assertNotValidated(); // vl2 has a fatal problem so it's no point revalidating the group validator
            if(showGroupProblemInChildrenUIs){
                vl1UI.assertShowProblemHasBeenCalled();
            } else {
                vl1UI.assertShowProblemHasNotBeenCalled();
            }
            vl1UI.assertProblem("info");
            vl2UI.assertShowProblemHasBeenCalled(); 
            vl2UI.assertProblem("fatal 2");
            groupUI.assertShowProblemHasBeenCalled();
            groupUI.assertProblem("fatal 2");

        }
      }
    }

    
    @Test public void testAddToSelfGeneratesException() {
        final String errorMessageToLookFor = "Ancestry to self";
        VL vl1 = VL.create(1);
        VL vl2 = VL.create(2);
        VL vl3 = VL.create(3);
        VL vl4 = VL.create(4);
        VL vl5 = VL.create(5);
        ValidationGroup a = new ValidationGroup(){};
        ValidationGroup b = new ValidationGroup(){};
        ValidationGroup c = new ValidationGroup(){};
        ValidationGroup d = new ValidationGroup(){};
        ValidationGroup e = new ValidationGroup(){};

        a.addItem(vl1, false);
        a.addItem(vl2, false);
        b.addItem(vl3, false);
        c.addItem(vl4, false);
        a.addItem(b, false);
        a.addItem(c, false);
        c.addItem(vl5, false);

        try {
            a.addItem(a, false);
            fail();
        } catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), errorMessageToLookFor);
        }
        try {
            b.addItem(a, false);
            fail();
        } catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), errorMessageToLookFor);
        }
        c.addItem(d, false);
        d.addItem(e, false);
        try {
            d.addItem(a, false);
            fail();
        } catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), errorMessageToLookFor);
        }
        try {
            e.addItem(a, false);
            fail();
        } catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), errorMessageToLookFor);
        }

        /*

         Final test. Start with this:

          c
          |
          b
           and
          a
          |
          c

         and the add a to b, so that we get

          c
          |
          b
          |
          a
          |
          c

         Now, this should throw an exception since we're clearly creating ancestry
         to self. But it's a tricky case, since neither a nor b, the ones involved
         in the add, is the one that is ancestor-to-self (because that is c.)

         */

        a = new ValidationGroup(){};
        b = new ValidationGroup(){};
        c = new ValidationGroup(){};
        a.addItem(c, false);
        c.addItem(b, false);
        try {
            b.addItem(a, false);
            fail();
        } catch (IllegalArgumentException ex){
            assertEquals(ex.getMessage(), errorMessageToLookFor);
        }


    }

    @Test public void testSanity() {
        UI ui = new UI(1);
        ValidationGroup g = new ValidationGroup(ui){};
        ui.assertShowProblemHasNotBeenCalled(); 
        ui.assertNotProblem();

        UI vlUI = new UI(2);
        vlUI.assertShowProblemHasNotBeenCalled();
        VL vl = VL.create(vlUI, 2);
        vlUI.assertShowProblemHasNotBeenCalled();
        vlUI.assertNotProblem();

        vl.simulateProblem ("pass");
        vl.assertValidated();
        vl.assertPassed();
        vlUI.assertShowProblemHasNotBeenCalled();// Nothing new, should not update UI
        vlUI.assertNotProblem();
        ui.assertShowProblemHasNotBeenCalled();// Nothing new, should not update UI

        g.addItem(vl, false);

        vl.assertNotValidated();
        vlUI.assertShowProblemHasNotBeenCalled();

        vl.simulateProblem ("fail");
        vl.assertValidated();
        vl.assertFatal();
        vlUI.assertShowProblemHasBeenCalled();
        vlUI.assertProblem("fail");
        ui.assertShowProblemHasBeenCalled();
        ui.assertProblem("fail");

        // Check that mocks have been properly cleared
        vl.assertNotValidated();
        vlUI.assertShowProblemHasNotBeenCalled();
        ui.assertShowProblemHasNotBeenCalled();

        vl.simulateProblem ("pass");
        vl.assertValidated();
        vl.assertPassed();
        vlUI.assertShowProblemHasBeenCalled();
        vlUI.assertNotProblem();
        ui.assertShowProblemHasBeenCalled();
        ui.assertNotProblem();

        // Check that mocks have been properly cleared again
        vl.assertNotValidated();
        vlUI.assertShowProblemHasNotBeenCalled();
        ui.assertShowProblemHasNotBeenCalled();

        // Create a second validation listener. Simulate a problem in it. Then add
        // it to the group as well. Check that the group updates its UI.

        UI vlU2 = new UI(22);
        vlU2.assertShowProblemHasNotBeenCalled();
        VL vl2 = VL.create(vlU2, 22);
        vlU2.assertShowProblemHasNotBeenCalled();
        vlU2.assertNotProblem();

        vl2.simulateProblem ("fail");
        vl2.assertValidated();
        vl2.assertFatal();
        vlU2.assertShowProblemHasBeenCalled();
        vlU2.assertProblem("fail");

        g.addItem(vl2, false);

        vl.assertNotValidated();
        vlUI.assertShowProblemHasNotBeenCalled();
        vlUI.assertNotProblem();
        ui.assertShowProblemHasBeenCalled();
        ui.assertProblem("fail");
        vl2.assertNotValidated();
        vlU2.assertShowProblemHasNotBeenCalled();
        vlU2.assertProblem("fail");
    }

    @Test public void testAddAndRemoveUIs() {
        UI ui1 = new UI(1);
        UI ui2 = new UI(2);
        UI ui3 = new UI(3);
        ValidationGroup g = new ValidationGroup(ui1){};
        g.addUI(ui2);

        ui1.assertShowProblemHasNotBeenCalled(); // ValidationUI passed in constructor is untouched.
        ui2.assertShowProblemHasBeenCalled();    // ValidationUI passed to addUI() is updated
        ui3.assertShowProblemHasNotBeenCalled(); // ui3: Has not been addded yet

        UI vlUI1 = new UI(11);
        VL vl1 = VL.create(vlUI1, 11);
        vl1.assertNotValidated();
        vlUI1.assertShowProblemHasNotBeenCalled(); // ValidationUI "connected" via constructor should not be touched
        vlUI1.assertNotProblem();
        g.addItem(vl1, false);
        vlUI1.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasNotBeenCalled();
        ui2.assertShowProblemHasNotBeenCalled();
        ui3.assertShowProblemHasNotBeenCalled(); // ui3: Has not been addded yet
        

        UI vlUI2 = new UI(12);
        VL vl2 = VL.create(vlUI2, 12);
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        vlUI2.assertNotProblem();
        g.addItem(vl2, false);
        vlUI2.assertShowProblemHasNotBeenCalled(); // still null Problem, no need to update UI
        vlUI1.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasNotBeenCalled();
        ui2.assertShowProblemHasNotBeenCalled();
        ui3.assertShowProblemHasNotBeenCalled(); // ui3: Has not been addded yet

        vl1.simulateProblem("fail");
        vl1.assertValidated();
        vl1.assertFatal();
        vlUI1.assertShowProblemHasBeenCalled();
        vlUI1.assertProblem("fail");
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasBeenCalled();
        ui1.assertProblem("fail");
        ui2.assertShowProblemHasBeenCalled();
        ui2.assertProblem("fail");
        ui3.assertShowProblemHasNotBeenCalled(); // ui3: Has not been addded yet

        g.addUI(ui3);
        vl1.assertNotValidated();
        vlUI1.assertShowProblemHasNotBeenCalled();
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasNotBeenCalled();
        ui2.assertShowProblemHasNotBeenCalled();
        ui3.assertShowProblemHasBeenCalled(); // When added, UI:s should be updated with whatever problem the group has
        ui3.assertProblem("fail");

        vl1.simulateProblem ("pass");
        vl1.assertValidated();
        vlUI1.assertShowProblemHasBeenCalled();
        vlUI1.assertNotProblem();
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasBeenCalled();
        ui1.assertNotProblem();
        ui2.assertShowProblemHasBeenCalled();
        ui2.assertNotProblem();
        ui3.assertShowProblemHasBeenCalled();
        ui3.assertNotProblem();

        vl1.simulateProblem ("pass"); // Pass again! But this time, UI:s shouldn't be updated
        vl1.assertValidated();
        vlUI1.assertShowProblemHasNotBeenCalled();
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasNotBeenCalled();
        ui2.assertShowProblemHasNotBeenCalled();
        ui3.assertShowProblemHasNotBeenCalled();

        g.removeUI(ui1);
        vl1.assertNotValidated();
        vlUI1.assertShowProblemHasNotBeenCalled();
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasBeenCalled();
        ui1.assertNotProblem();
        ui2.assertShowProblemHasNotBeenCalled();
        ui3.assertShowProblemHasNotBeenCalled();
        
        vl1.simulateProblem("fail");
        vl1.assertValidated();
        vlUI1.assertShowProblemHasBeenCalled();
        vlUI1.assertProblem("fail");
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasNotBeenCalled();
        ui2.assertShowProblemHasBeenCalled();
        ui2.assertProblem("fail");
        ui3.assertShowProblemHasBeenCalled();
        ui3.assertProblem("fail");

        g.addUI(ui1);
        vl1.assertNotValidated();
        vlUI1.assertShowProblemHasNotBeenCalled();
        vl2.assertNotValidated();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasBeenCalled();
        ui1.assertProblem("fail");
        ui2.assertShowProblemHasNotBeenCalled();
        ui3.assertShowProblemHasNotBeenCalled();

        g.removeUI(ui2);
        vlUI1.assertShowProblemHasNotBeenCalled();
        vlUI2.assertShowProblemHasNotBeenCalled();
        ui1.assertShowProblemHasNotBeenCalled();
        ui2.assertShowProblemHasBeenCalled();
        ui2.assertNotProblem();
        ui3.assertShowProblemHasNotBeenCalled();
    }


    @Test public void testAddAndRemoveGroups() {
       boolean testAddWithDisabledUI = false;
       do {
        UI uiVLC = new UI(33);
        
        VL vlA = VL.create(1);
        VL vlB = VL.create(2);
        VL vlC = VL.create(uiVLC, 33);
        VL vlD = VL.create(4);
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiVLC.assertShowProblemHasNotBeenCalled();

        UI uiA = new UI(1);
        UI uiB = new UI(2);
        UI uiC = new UI(3);
        UI uiD = new UI(4);
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();

        ValidationGroup a = new ValidationGroup(uiA){};
        ValidationGroup b = new ValidationGroup(uiB){};
        ValidationGroup c = new ValidationGroup(uiC){};
        ValidationGroup d = new ValidationGroup(uiD){};
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();


        a.addItem (vlA, false);
        b.addItem (vlB, false);
        if(testAddWithDisabledUI){
            c.addItem(vlC, true);
        }else{
            c.addItem (vlC, false);
        }
        d.addItem (vlD, false);
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertNotProblem();
        uiA.assertShowProblemHasNotBeenCalled();
        uiA.assertNotProblem();
        uiB.assertShowProblemHasNotBeenCalled();
        uiB.assertNotProblem();
        uiC.assertShowProblemHasNotBeenCalled();
        uiC.assertNotProblem();
        uiD.assertShowProblemHasNotBeenCalled();
        uiD.assertNotProblem();

        /* Create a little "tree" of ValidationGroups where d is "root", and c
         and a are leafs. Each groups has its own separate ValidationItem (that
         we use to trigger validation) as well as its own ValidationUI (that we
         check for correctness)
         
                d
               / \
              c    b
                    \
                     a
         */

        d.addItem(c, false);
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();

        if(testAddWithDisabledUI){
            d.addItem(b, true);
        }else{
            d.addItem(b, false);
        }
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();
        b.addItem(a, false);
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();
        

        ///////////////////////////////////////////////////////////////////////
        // A validation triggered at the root ("d") should not spread down the branches
        vlD.simulateProblem("fail");
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertValidated();
        vlD.assertFatal();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertProblem("fail");
        // Check that mocks are properly cleared
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

        vlD.simulateProblem("pass");
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertValidated();
        vlD.assertPassed();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertNotProblem();
        // Check that mocks are properly cleared
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

        ///////////////////////////////////////////////////////////////////////
        // A validation triggered in "c" should affect c-UI and up to d-UI, but not down in b-UI or a-UI.
        // (d-VL should not be affected, because d-Vl is a sibling to c (not a parent))
        vlC.simulateProblem("fail");
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertValidated();
        vlC.assertFatal();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasBeenCalled();
        uiC.assertProblem("fail");
        if(testAddWithDisabledUI){
            uiVLC.assertShowProblemHasNotBeenCalled();
            uiVLC.assertNotProblem();
        }else{
            uiVLC.assertShowProblemHasBeenCalled();
            uiVLC.assertProblem("fail");
        }
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertProblem("fail");
        // Check that mocks are properly cleared
        vlC.assertNotValidated();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

        vlC.simulateProblem("pass");
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertValidated();
        vlC.assertPassed();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasBeenCalled();
        uiC.assertNotProblem();
        if(testAddWithDisabledUI){
            uiVLC.assertShowProblemHasNotBeenCalled();
        }else{
            uiVLC.assertShowProblemHasBeenCalled();
        }
        uiVLC.assertNotProblem();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertNotProblem();
        // Check that mocks are properly cleared
        vlC.assertNotValidated();
        uiC.assertShowProblemHasNotBeenCalled();
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

        ///////////////////////////////////////////////////////////////////////
        // A validation triggered in "b" should affect b-UI and up to d-UI, but not down in c-UI or a-UI.
        // (d-VL should not be affected, because d-Vl is a sibling to b (not a parent))
        vlB.simulateProblem("fail");
        vlA.assertNotValidated();
        vlB.assertValidated();
        vlB.assertFatal();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        if(testAddWithDisabledUI){ 
            uiB.assertShowProblemHasNotBeenCalled();
        }else{
            uiB.assertShowProblemHasBeenCalled();
            uiB.assertProblem("fail");
        }
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertProblem("fail");
        // Check that mocks are properly cleared
        vlB.assertNotValidated();
        uiB.assertShowProblemHasNotBeenCalled();
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

        vlB.simulateProblem("pass");
        vlA.assertNotValidated();
        vlB.assertValidated();
        vlB.assertPassed();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        if(testAddWithDisabledUI){
            uiB.assertShowProblemHasNotBeenCalled();
        }else{
            uiB.assertShowProblemHasBeenCalled();
            uiB.assertNotProblem();
        }
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertNotProblem();
        // Check that mocks are properly cleared
        vlB.assertNotValidated();
        uiB.assertShowProblemHasNotBeenCalled();
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

        ///////////////////////////////////////////////////////////////////////
        // A validation triggered in "a" should affect a-UI, b-UI and d-UI, but not down in c-UI.
        // (Neither b-VL nor d-VL should not be affected, only a-VL, the one that triggered the validation)
        vlA.simulateProblem("fail");
        vlA.assertValidated();
        vlA.assertFatal();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasBeenCalled();
        uiA.assertProblem("fail");
        if(testAddWithDisabledUI){
            uiB.assertShowProblemHasNotBeenCalled();
        }else{
            uiB.assertShowProblemHasBeenCalled();
            uiB.assertProblem("fail");
        }
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertProblem("fail");
        // Check that mocks are properly cleared
        vlA.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        vlB.assertNotValidated();
        uiB.assertShowProblemHasNotBeenCalled();
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();

       
        vlA.simulateProblem("pass");
        vlA.assertValidated();
        vlA.assertPassed();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasBeenCalled();
        uiA.assertNotProblem();
        if(testAddWithDisabledUI){
            uiB.assertShowProblemHasNotBeenCalled();
        }else{
            uiB.assertShowProblemHasBeenCalled();
            uiB.assertNotProblem();
        }
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertNotProblem();
        // Check that mocks are properly cleared
        vlA.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        vlB.assertNotValidated();
        uiB.assertShowProblemHasNotBeenCalled();
        vlD.assertNotValidated();
        uiD.assertShowProblemHasNotBeenCalled();


        //////////////////////////////////////
        /* Start removing groups from eachother ...
         First, remove "c" from "d", check that d-UI is affected,
         trigger "c" and check that no-one but "c" is affected at all

                d
                 \
            c      b
                    \
                     a

         */
        d.remove(c);
        // remove(): from d's point of view it's as if a problem has been triggered
        // in c, so d could have been affected. Note though that
        // c did not have the lead problem, so the validationgroup will skip
        // updating its UI (as an optimization)
        uiD.assertShowProblemHasNotBeenCalled();
        uiD.assertNotProblem();

        vlC.simulateProblem("fail");
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertValidated();
        vlC.assertFatal();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasBeenCalled();
        if(testAddWithDisabledUI){
            uiVLC.assertShowProblemHasNotBeenCalled();
        }else{
            uiVLC.assertShowProblemHasBeenCalled();
            uiVLC.assertProblem("fail");
        }
        uiC.assertProblem("fail");
        uiD.assertShowProblemHasNotBeenCalled();

        vlC.simulateProblem("pass");
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertValidated();
        vlC.assertPassed();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasBeenCalled();
        uiC.assertNotProblem();
        if(testAddWithDisabledUI){
            uiVLC.assertShowProblemHasNotBeenCalled();
        }else{
            uiVLC.assertShowProblemHasBeenCalled();
            uiVLC.assertNotProblem();
        }
        uiD.assertShowProblemHasNotBeenCalled();

        /*
        Now, add "d" as a child of "c", trigger on "b" and check that "b"-UI,
        "c"-UI and "d"-UI get affected but not "a"-UI

              c
               \
                d
                 \
                   b
                    \
                     a
         
         */
        c.addItem(d, false);
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();
        uiD.assertNotProblem();
        vlB.simulateProblem("fail");
        vlA.assertNotValidated();
        vlB.assertValidated();
        vlB.assertFatal();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        if(testAddWithDisabledUI){
            uiB.assertShowProblemHasNotBeenCalled();
        }else{
            uiB.assertShowProblemHasBeenCalled();
            uiB.assertProblem("fail");
        }
        uiC.assertShowProblemHasBeenCalled();
        uiC.assertProblem("fail");
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertProblem("fail");

        vlB.simulateProblem("pass");
        vlA.assertNotValidated();
        vlB.assertValidated();
        vlB.assertPassed();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        if(testAddWithDisabledUI){
            uiB.assertShowProblemHasNotBeenCalled();
        }else{
            uiB.assertShowProblemHasBeenCalled();
            uiB.assertNotProblem();
        }
        uiC.assertShowProblemHasBeenCalled();
        uiC.assertNotProblem();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasBeenCalled();
        uiD.assertNotProblem();

        
        /*
        Lastly, remove "b" from "d"
         -- d-UI and c-UI should be affected
         .. and add "c" to "b"..
              c
               \
                d
                 \
                   b
                    \
                     a

          ==>
                   b
                 /  \
               c     a
              /
             d

         and trigger "a", and check that only a-UI and b-UI are affected.
         */
        d.remove(b);
        vlA.assertNotValidated();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();

        b.addItem(c, false);
        uiA.assertShowProblemHasNotBeenCalled();
        uiB.assertShowProblemHasNotBeenCalled();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();

        vlA.simulateProblem("fail");
        vlA.assertValidated();
        vlA.assertFatal();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasBeenCalled();
        uiA.assertProblem("fail");
        uiB.assertShowProblemHasBeenCalled();
        uiB.assertProblem("fail");
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();

        vlA.simulateProblem("pass");
        vlA.assertValidated();
        vlA.assertPassed();
        vlB.assertNotValidated();
        vlC.assertNotValidated();
        vlD.assertNotValidated();
        uiA.assertShowProblemHasBeenCalled();
        uiA.assertNotProblem();
        uiB.assertShowProblemHasBeenCalled();
        uiB.assertNotProblem();
        uiC.assertShowProblemHasNotBeenCalled();
        uiVLC.assertShowProblemHasNotBeenCalled();
        uiD.assertShowProblemHasNotBeenCalled();
      } while(testAddWithDisabledUI = !testAddWithDisabledUI);
       // } while (false);

    }

    @Test public void testAddAndRemoveGroupsContainingProblems(){
        {
            // Add and remove a ValidationItem that contains Problem (and sometimes not)
            // to a ValidationGroup (that does not contain a problem).
            VL vl1 = VL.create(1);
            vl1.assertNotValidated();
            UI uiA = new UI(1);
            uiA.assertShowProblemHasNotBeenCalled();
            uiA.assertNotProblem();
            ValidationGroup a = new ValidationGroup(uiA){};
            uiA.assertShowProblemHasNotBeenCalled();

            vl1.simulateProblem("pass");
            vl1.assertValidated();
            a.addItem(vl1, false);
            uiA.assertShowProblemHasNotBeenCalled(); 

            a.remove(vl1);

            uiA.assertShowProblemHasNotBeenCalled();

            vl1.simulateProblem("fail");
            vl1.assertValidated();
            vl1.assertFatal();
            a.addItem(vl1, false);
            vl1.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled();
            uiA.assertProblem("fail");

            a.remove(vl1);
            vl1.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled(); 
            uiA.assertNotProblem();

            a.addItem(vl1, false);
            vl1.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled(); 
            uiA.assertProblem("fail");
        }

        {
            // Add and remove a ValidationItem that contains a Problem (and sometimes not)
            // to a ValidationGroup that *does* contain another problem, a fatal one.
            UI uiA = new UI(1);
            uiA.assertShowProblemHasNotBeenCalled();
            uiA.assertNotProblem();
            ValidationGroup a = new ValidationGroup(uiA){};
            uiA.assertShowProblemHasNotBeenCalled();
            VL vlBad = VL.create(0);
            vlBad.assertNotValidated();
            vlBad.simulateProblem("fail");
            vlBad.assertValidated();

            a.addItem(vlBad, false);
            vlBad.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled();
            uiA.assertProblem("fail");

            VL vl1 = VL.create(1);
            vl1.assertNotValidated();
            vl1.simulateProblem("pass");
            vl1.assertValidated();
            vl1.assertPassed();
            a.addItem(vl1, false);
            vl1.assertNotValidated();
            vlBad.assertNotValidated();
            uiA.assertShowProblemHasNotBeenCalled();
            uiA.assertProblem("fail");

            a.remove(vl1);
            vl1.assertNotValidated();
            vlBad.assertNotValidated();
            uiA.assertShowProblemHasNotBeenCalled();

            // This is not super important perhaps, but ok: When a ValidationItem
            // containing a Problem is added to a ValidationGroup also containing a Problem
            // (with equal Severity), then the *newly* added Problem takes precedence --
            // it's as if the user has UI-interacted with (instead of programatically
            // added) the ValidationItem.
            vl1.simulateProblem("fail 2");
            vl1.assertValidated();
            vl1.assertFatal();
            a.addItem(vl1, false);
            vl1.assertNotValidated();
            vlBad.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled();
            uiA.assertProblem("fail 2");

            // And the problem is removed, and the previous one shown again:
            a.remove(vl1);
            vl1.assertNotValidated();
            vlBad.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled();
            uiA.assertProblem("fail");

            a.addItem(vl1, false);
            vl1.assertNotValidated();
            vlBad.assertNotValidated();
            uiA.assertShowProblemHasBeenCalled();
            uiA.assertProblem("fail 2");

        }
        
    }

    ///////////////////////////////////////////////
    /// Mock classes
    ///////////////////////////////////////////////
    private final static String groupValidationProblemString ="group validation failed";
    private static class MyGroupValidator extends GroupValidator {
        MyGroupValidator(boolean shallShowProblemInChildrenUIs){ super(shallShowProblemInChildrenUIs); }
        private Severity simulatedProblem = null;
        private boolean validated = false;
        @Override
        protected void performGroupValidation(Problems problems) {
            assertFalse("MyGroupValidator#performValidation called twice", validated);
            validated = true;
            if( simulatedProblem != null){
                problems.add(groupValidationProblemString, simulatedProblem);
            }
        }
        void assertValidated(){ assertTrue(validated); validated = false; }
        void assertNotValidated(){ assertFalse(validated);  }
    }

    private static class UI implements ValidationUI {
        private final int val;
        UI (int val) {
            this.val = val;
        }

        private Problem p;
        private boolean showProblemHasBeenCalled;

        public void assertShowProblemHasBeenCalled() {
            assertTrue(showProblemHasBeenCalled);
            showProblemHasBeenCalled = false;
        }

        public void assertShowProblemHasNotBeenCalled() {
            assertFalse(showProblemHasBeenCalled);
        }

        public void assertProblem(String msg) {
            assertNotNull(p);
            assertEquals(msg, p.getMessage());
        }
        public void assertNotProblem() {
            assertNull(p);
        }

        
        @Override
        public void showProblem(Problem p) {
            if( showProblemHasBeenCalled ) {
                assertFalse("showProblem called twice with two different problems?", showProblemHasBeenCalled);
            }
            this.p = p;
            showProblemHasBeenCalled = true;
        }

        @Override
        public String toString() {
            return "UI" + val;
        }

        public void clearProblem() {
            showProblem(null);
        }
    }

    private static class VL extends ValidationListener {
        private boolean validated;
        private Severity currentSeverity;
        final V v = new V();
        private String simulatedProblem = "pass";
        private int id;

        static VL create(){
            return create(0);
        }
        static VL create(int id){
            return new VL(ValidationUI.NO_OP, id);
        }

        static VL create(ValidationUI ui){
            return create(ui, 0);
        }
        static VL create(ValidationUI ui, int id){
            return new VL(ui, id);
        }
        
        private VL(ValidationUI cd, int val){
            super(Object.class, cd, null);
            this.id = val;
        }

        void assertValidated() {
            assertTrue(validated);
            validated = false;
        }

        void assertNotValidated() {
            assertFalse(validated);
        }

        void assertPassed() {
            assertNull(currentSeverity);
        }

        void assertInfo() {
            assertEquals(Severity.INFO, currentSeverity);
        }

        void assertWarning() {
            assertEquals(Severity.WARNING, currentSeverity);
        }

        void assertFatal() {
            assertEquals(Severity.FATAL, currentSeverity);
        }

        public void simulateProblem(String txt) {
            this.simulatedProblem = txt;
            performValidation();
        }

        @Override
        public void performValidation(Problems ps) {
            assertFalse("performValidation called twice?", validated);
            this.validated = true;
            v.validate(ps, null, simulatedProblem);
            Problem lead = ps.getLeadProblem();
            if(lead == null)
                currentSeverity = null;
            else{
                currentSeverity = lead.severity();
            }
        }

        @Override
        public String toString() {
            return "VL" + id;
        }

        private class V implements Validator<String> {
            @Override
            public void validate(Problems problems, String compName, String model) {
                if ("pass".equals(model)) {
		    // Nothing.
                } else if( "info".equals(model)){
                    problems.add(model, Severity.INFO);
                } else if( "warning".equals(model)){
                    problems.add(model, Severity.WARNING);
                } else {
                    problems.add(model, Severity.FATAL);
		}
                
            }

            public Class<String> modelType() {
                return String.class;
            }
        }
    }
}
