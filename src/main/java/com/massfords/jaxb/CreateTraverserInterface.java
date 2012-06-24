package com.massfords.jaxb;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import java.util.Set;

/**
 * Creates the traverser interface. A traverse method is added for each of the generated beans.
 * 
 * @author markford
 */
public class CreateTraverserInterface extends CodeCreator {
    
    private JDefinedClass visitor;

    public CreateTraverserInterface(JDefinedClass visitor, Outline outline, JPackage jpackage) {
        super(outline, jpackage);
        this.visitor = visitor;
    }

    @Override
    protected void run(Set<ClassOutline> classes) {
        setOutput(getOutline().getClassFactory().createInterface(getPackage(), "Traverser", null));
        for (ClassOutline classOutline : classes) {
            if (!classOutline.target.isAbstract()) {
                // add the bean to the traverser
                JMethod traverseMethod = getOutput().method(JMod.PUBLIC, void.class, "traverse");
                traverseMethod.param(classOutline.implClass, "aBean");
                traverseMethod.param(visitor, "aVisitor");
            }
        }
    }
}
