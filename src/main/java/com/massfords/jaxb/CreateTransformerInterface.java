package com.massfords.jaxb;

import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JTypeVar;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

import java.util.Set;

/**
 * Creates the visitor interface. After creating the interface, a visit method is added for each of the beans.
 * 
 * @author utard
 */
public class CreateTransformerInterface extends CodeCreator {
    
    public CreateTransformerInterface(Outline outline, JPackage jPackage) {
        super(outline, jPackage);
    }
    
    @Override
    protected void run(Set<ClassOutline> classes) {
        
        setOutput(outline.getClassFactory().createInterface(jpackage, "Transformer", null));
        JTypeVar genericType = getOutput().generify("T");
        
        for(ClassOutline classOutline : classes) {
            if (!classOutline.target.isAbstract()) {
                // add the bean to the visitor
                JMethod vizMethod = getOutput().method(JMod.PUBLIC, genericType, "transform");
                vizMethod.param(classOutline.implClass, "aBean");
            }
        }
    }
}
