package com.massfords.jaxb;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import java.util.Set;

/**
 * Creates a no-op implementation of the Visitor interface. After creating the class
 * a visit method is added for each of the beans that were generated.
 * 
 * @author markford
 */
public class CreateBaseVisitorClass extends CodeCreator {

    private JDefinedClass visitor;

    public CreateBaseVisitorClass(JDefinedClass visitor, Outline outline, JPackage jPackage) {
        super(outline, jPackage);
        this.visitor = visitor;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
        setOutput(getOutline().getClassFactory().createClass(getPackage(), "BaseVisitor", null));
        getOutput()._implements(visitor);
        for (ClassOutline classOutline : classes) {
            if (!classOutline.target.isAbstract()) {
                // add the method to the base vizzy
                getOutput().method(JMod.PUBLIC, void.class, "visit").param(classOutline.implClass, "aBean");
            }
        }
    }
}
